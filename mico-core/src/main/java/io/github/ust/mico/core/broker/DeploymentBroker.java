package io.github.ust.mico.core.broker;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class DeploymentBroker {

    @Autowired
    private MicoApplicationBroker micoApplicationBroker;

    @Autowired
    private BackgroundJobBroker backgroundJobBroker;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoServiceRepository serviceRepository;

    public MicoApplicationJobStatus deployApplication(String shortName, String version) throws MicoApplicationNotFoundException, MicoServiceInterfaceNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, DeploymentException {

        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(shortName, version);

        checkIfMicoApplicationIsDeployable(micoApplication);

        log.info("Deploy MicoApplication '{}' in version '{}' with {} included MicoService(s).",
                shortName, version, micoApplication.getServices().size());
        List<CompletableFuture<MicoServiceDeploymentInfo>> buildJobs = new ArrayList<>();
        for (MicoService micoService : micoApplication.getServices()) {
            // Check if a build for this MicoService is already running.
            // If yes no build is required, lock changes to running jobs.
            // If the current job status is done, error or cancel delete it and create a new job to get a new id.
            Optional<MicoServiceBackgroundJob> jobOptional = backgroundJobBroker.getJobByMicoService(
                    micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD);
            if (jobOptional.isPresent()) {
                if (jobOptional.get().getStatus() != MicoServiceBackgroundJob.Status.RUNNING) {
                    backgroundJobBroker.deleteJob(jobOptional.get().getId());
                } else {
                    log.info("Build job of MicoService '{}' '{}' is already running.",
                            micoService.getShortName(), micoService.getVersion());
                    continue;
                }
            }
            MicoServiceBackgroundJob job = new MicoServiceBackgroundJob()
                    .setServiceShortName(micoService.getShortName())
                    .setServiceVersion(micoService.getVersion())
                    .setType(MicoServiceBackgroundJob.Type.BUILD)
                    .setStatus(MicoServiceBackgroundJob.Status.RUNNING);
            backgroundJobBroker.saveJob(job);

            Optional<MicoServiceDeploymentInfo> serviceDeploymentInfoOptional = serviceDeploymentInfoRepository
                    .findByApplicationAndService(micoApplication.getShortName(), micoApplication.getVersion(),
                            micoService.getShortName(), micoService.getVersion());
            MicoServiceDeploymentInfo serviceDeploymentInfo = serviceDeploymentInfoOptional.orElseThrow(() ->
                    new IllegalStateException("Service deployment information for service '" + micoService.getShortName()
                            + "' in application '" + micoApplication.getShortName() + "' '" + micoApplication.getVersion()
                            + "' could not be found."));

            log.info("Start build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            CompletableFuture<MicoServiceDeploymentInfo> buildJob = CompletableFuture.supplyAsync(() -> buildMicoService(serviceDeploymentInfo))
                    .exceptionally(ex -> {
                        log.error(ex.getMessage(), ex);
                        backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                                MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, ex.getMessage());
                        return null;
                    });
            log.debug("Started build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            buildJobs.add(buildJob);
            backgroundJobBroker.saveFutureOfJob(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD, buildJob);
        }

        // When all build jobs are finished, create the Kubernetes resources for the deployment of a MicoService
        CompletableFuture<List<MicoServiceDeploymentInfo>> allBuildJobs = FutureUtils.all(buildJobs);
        allBuildJobs.whenComplete((serviceDeploymentInfosWithNullValues, throwable) -> {
            log.info("All build jobs for deployment of MicoApplication '{}' '{}' are finished. Start creating or updating Kubernetes resources.",
                    micoApplication.getShortName(), micoApplication.getVersion());
            // All failed builds lead to a null in the service deployment list.
            List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfosWithNullValues.stream().filter(Objects::nonNull).collect(toList());
            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                MicoService micoService = serviceDeploymentInfo.getService();
                try {
                    KubernetesDeploymentInfo kubernetesDeploymentInfo = createKubernetesResources(serviceDeploymentInfo);
                    serviceDeploymentInfo.setKubernetesDeploymentInfo(kubernetesDeploymentInfo);
                    backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                            MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.DONE);
                } catch (Exception e) {
                    backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                            MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                }
            }

            // After the Kubernetes deployments are created, save the actual deployment information to the database.
            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                log.debug("Saved new Kubernetes deployment information of '{}' '{}' to database: {}",
                        serviceDeploymentInfo.getService().getShortName(), serviceDeploymentInfo.getService().getVersion(),
                        serviceDeploymentInfo.getKubernetesDeploymentInfo());
                serviceDeploymentInfoRepository.save(serviceDeploymentInfo);
            }
            log.info("Finished creating or updating Kubernetes resources for deployment of MicoApplication '{}' '{}'. " +
                    "Start creating or updating interface connections.", micoApplication.getShortName(), micoApplication.getVersion());

            // At last set up the connections between the deployed MicoServices
            micoKubernetesClient.createOrUpdateInterfaceConnections(micoApplication);
        });

        MicoApplicationJobStatus micoApplicationJobStatus = backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version);
        return micoApplicationJobStatus;
    }

    public void undeployApplication(String shortName, String version) throws MicoApplicationNotFoundException {

        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(shortName, version);

        log.info("Undeploy MicoApplication '{}' in version '{}' with {} included MicoService(s).",
                shortName, version, micoApplication.getServices().size());

        if (!micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            // Currently we undeploy all MicoServices regardless whether the application is considered
            // to be deployed or not.
            // The reason is that there are possible some MicoServices deployed successfully and some not.
            // This undeployment should delete/scale the actually existing deployments.
            log.info("MicoApplication '{}' in version '{}' is considered to be not deployed. " +
                            "Nevertheless check if there are any MicoServices that should be undeployed.",
                    micoApplication.getShortName(), micoApplication.getVersion());
        }
        // TODO: Undeploy only if application is deployed or it is in a conflicted state. Covered by mico#535
        micoKubernetesClient.undeployApplication(micoApplication);
    }

    private void checkIfMicoApplicationIsDeployable(MicoApplication micoApplication) throws MicoApplicationDoesNotIncludeMicoServiceException, MicoServiceInterfaceNotFoundException, DeploymentException {
        if (micoApplication.getServices() == null || micoApplication.getServices().isEmpty()) {
            throw new MicoApplicationDoesNotIncludeMicoServiceException(micoApplication.getShortName(), micoApplication.getVersion());
        }
        for (MicoService micoService : micoApplication.getServices()) {
            if (micoService.getServiceInterfaces() == null || micoService.getServiceInterfaces().isEmpty()) {
                throw new MicoServiceInterfaceNotFoundException(micoService.getShortName(), micoService.getVersion());
            }
            if (!micoService.getDependencies().isEmpty()) {
                // TODO: Check if dependencies are valid. Covered by mico#583
                throw new DeploymentException("The deployment of service dependencies is currently not implemented. " +
                        "See https://github.com/UST-MICO/mico/issues/583");
            }
        }
    }

    private MicoServiceDeploymentInfo buildMicoService(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        MicoService micoService = serviceDeploymentInfo.getService();
        try {
            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<String> buildFuture = imageBuilder.build(micoService);
            log.debug("Build of MicoService '{}' in version '{}' finished.", micoService.getShortName(), micoService.getVersion());

            if (buildFuture.get() != null) {
                String dockerImageUri = buildFuture.get();
                log.info("Build of MicoService '{}' in version '{}' finished with image '{}'.",
                        micoService.getShortName(), micoService.getVersion(), dockerImageUri);
                micoService.setDockerImageUri(dockerImageUri);
                serviceRepository.save(micoService);
            } else {
                String errorMessage = "Build of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' didn't return a Docker image URI.";
                throw new CompletionException(new RuntimeException(errorMessage));
            }
        } catch (InterruptedException | ExecutionException | NotInitializedException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return serviceDeploymentInfo;
    }

    /**
     * Creates the Kubernetes resources based on the {@code MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the {@link KubernetesDeploymentInfo}
     * @throws KubernetesResourceException if there is an error during the creation of Kubernetes resources
     */
    private KubernetesDeploymentInfo createKubernetesResources(MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException {
        MicoService micoService = serviceDeploymentInfo.getService();
        log.info("Creating Kubernetes resources for MicoService '{}' in version '{}'", micoService.getShortName(), micoService.getVersion());
        log.debug("Using deployment information for MicoService '{}' in version '{}': {}",
                micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.toString());

        // TODO: Scale in/out existing Kubernetes resources instead of replacing existing resources (issue mico#416)
        Deployment createdDeployment = micoKubernetesClient.createMicoService(serviceDeploymentInfo);

        List<io.fabric8.kubernetes.api.model.Service> createdServices = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            io.fabric8.kubernetes.api.model.Service createdService = micoKubernetesClient.createMicoServiceInterface(serviceInterface, micoService);
            createdServices.add(createdService);
        }
        log.info("Successfully created Kubernetes resources for MicoService '{}' in version '{}'",
                micoService.getShortName(), micoService.getVersion());

        // Store the names of the created Kubernetes resources in the database
        return new KubernetesDeploymentInfo()
                .setNamespace(createdDeployment.getMetadata().getNamespace())
                .setDeploymentName(createdDeployment.getMetadata().getName())
                .setServiceNames(createdServices.stream().map(service -> service.getMetadata().getName()).collect(toList()));
    }
}
