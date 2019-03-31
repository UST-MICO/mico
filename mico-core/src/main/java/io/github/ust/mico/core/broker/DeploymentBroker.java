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
import java.util.stream.Collectors;

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
            log.debug("Checking MicoService '{}' '{}' ...", micoService.getShortName(), micoService.getVersion());
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

            log.info("Start build of MicoService '{}' '{}'.", micoService.getShortName(), micoService.getVersion());
            CompletableFuture<MicoServiceDeploymentInfo> buildJob = CompletableFuture.supplyAsync(() -> buildMicoService(serviceDeploymentInfo))
                .exceptionally(ex -> {
                    log.error(ex.getMessage(), ex);
                    backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                        MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, ex.getMessage());
                    List<CompletableFuture<MicoServiceDeploymentInfo>> runningBuildJobs = buildJobs.stream()
                        .filter(j -> !j.isDone() && !j.isCancelled() && !j.isCompletedExceptionally()).collect(Collectors.toList());
                    log.warn("There are still {} other jobs running for the deployment of MicoApplication '{}' '{}'",
                        runningBuildJobs.size(), micoApplication.getShortName(), micoApplication.getVersion());

                    for (CompletableFuture<MicoServiceDeploymentInfo> runningBuildJob : runningBuildJobs) {
                        try {
                            MicoServiceDeploymentInfo sdi = runningBuildJob.get();
                            log.debug("Cancel running build job for MicoService '{}' '{}'.", sdi.getService().getShortName(), sdi.getService().getVersion());
                            runningBuildJob.cancel(true);
                            // TODO: Ensure job status will be set to 'CANCELLED' or similar)
                        } catch (InterruptedException | ExecutionException e) {
                            log.warn("Failed to cancel build job. Caused by: " + e.getMessage());
                        }
                    }
                    return null;
                });
            log.debug("Started build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            buildJobs.add(buildJob);
            backgroundJobBroker.saveFutureOfJob(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD, buildJob);
        }

        // When all build jobs are finished, create the Kubernetes resources for the deployment of a MicoService
        CompletableFuture<List<MicoServiceDeploymentInfo>> allBuildJobs = FutureUtils.all(buildJobs);
        allBuildJobs.whenComplete((serviceDeploymentInfosWithNullValues, throwable) -> {
            // All failed builds lead to a null in the service deployment list.
            long failedJobs = serviceDeploymentInfosWithNullValues.stream().filter(Objects::isNull).count();
            if (failedJobs > 0) {
                log.warn("{} build jobs failed. Skip creating / updating of Kubernetes resources.", failedJobs);
                return;
            }

            List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfosWithNullValues.stream()
                .filter(Objects::nonNull).collect(toList());
            log.info("All {} build jobs for the deployment of MicoApplication '{}' '{}' finished successfully. " +
                    "Start creating or updating Kubernetes resources.", serviceDeploymentInfos.size(),
                micoApplication.getShortName(), micoApplication.getVersion());

            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                MicoService micoService = serviceDeploymentInfo.getService();
                try {
                    KubernetesDeploymentInfo kubernetesDeploymentInfo = createOrUpdateKubernetesResources(serviceDeploymentInfo);
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
                // Save the ServiceDeploymentInfo entity with a depth of 1 to the database.
                // A new node for the KubernetesDeploymentInfo
                // and a relation to the existing ServiceDeploymentInfo node will be created.
                MicoServiceDeploymentInfo savedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(serviceDeploymentInfo, 1);
                log.debug("Saved new Kubernetes deployment information of MicoService '{}' '{}' for MicoApplication '{}' '{} to database: {}",
                    savedServiceDeploymentInfo.getService().getShortName(),
                    savedServiceDeploymentInfo.getService().getVersion(),
                    micoApplication.getShortName(), micoApplication.getVersion(),
                    savedServiceDeploymentInfo.getKubernetesDeploymentInfo());
            }

            // At last set up the connections between the deployed MicoServices
            micoKubernetesClient.createOrUpdateInterfaceConnections(micoApplication);

            log.info("Finished creating or updating Kubernetes resources for the deployment of MicoApplication '{}' '{}'.",
                micoApplication.getShortName(), micoApplication.getVersion());
        });

        return backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version);
    }

    public void undeployApplication(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsDeployingException {

        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(shortName, version);

        log.info("Undeploy MicoApplication '{}' in version '{}' with {} included MicoService(s).",
            shortName, version, micoApplication.getServices().size());

        MicoApplicationDeploymentStatus applicationDeploymentStatus = micoKubernetesClient.getApplicationDeploymentStatus(micoApplication);
        switch (applicationDeploymentStatus.getValue()) {
            case DEPLOYED:
            case INCOMPLETE:
            case UNKNOWN:
                // The application should be undeployed if the current state is either 'deployed', 'incomplete' or unknown'.
                micoKubernetesClient.undeployApplication(micoApplication);
                break;
            case PENDING:
                throw new MicoApplicationIsDeployingException(micoApplication.getShortName(), micoApplication.getVersion());
            case UNDEPLOYED:
                log.info("MicoApplication '{}' in version '{}' is considered to be undeployed. No undeployment required.",
                    micoApplication.getShortName(), micoApplication.getVersion());
                break;
            default:
                throw new IllegalArgumentException("Unknown application deployment status: " + applicationDeploymentStatus.getValue());
        }
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
            if (buildFuture.get() != null) {
                String dockerImageUri = buildFuture.get();
                log.info("Build of MicoService '{}' in version '{}' finished with image '{}'.",
                    micoService.getShortName(), micoService.getVersion(), dockerImageUri);
                micoService.setDockerImageUri(dockerImageUri);
                // Save the MicoService with a depth of 0 to the database.
                // Only the properties of this MicoService entity will be stored to the database.
                serviceRepository.save(micoService, 0);
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
     * Creates or updates the Kubernetes resources based on the {@code MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the {@link KubernetesDeploymentInfo}
     * @throws KubernetesResourceException if there is an error during the creation of Kubernetes resources
     */
    private KubernetesDeploymentInfo createOrUpdateKubernetesResources(MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException {
        MicoService micoService = serviceDeploymentInfo.getService();
        log.info("Creating / updating Kubernetes resources for MicoService '{}' in version '{}'.",
            micoService.getShortName(), micoService.getVersion());
        log.debug("Using deployment information for MicoService '{}' in version '{}': {}",
            micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.toString());

        // If the Kubernetes deployment already exists and is deployed, scale out,
        // otherwise create the Kubernetes deployment
        boolean micoServiceIsDeployed = micoKubernetesClient.isMicoServiceDeployed(micoService);
        if (micoServiceIsDeployed && serviceDeploymentInfo.getKubernetesDeploymentInfo() != null) {
            log.info("MicoService '{}' '{}' is already deployed by this MicoApplication. Do nothing.",
                micoService.getShortName(), micoService.getVersion());
            return serviceDeploymentInfo.getKubernetesDeploymentInfo();
        }

        Deployment deployment;
        if (!micoServiceIsDeployed) {
            log.info("MicoService '{}' '{}' is not deployed yet. Create the required Kubernetes resources.",
                micoService.getShortName(), micoService.getVersion());
            deployment = micoKubernetesClient.createMicoService(serviceDeploymentInfo);
        } else {
            // MICO service was deployed by another MICO application.
            // Get information about the actual deployment to be able to perform the scaling.
            log.info("MicoService '{}' '{}' was already deployed by another MicoApplication. Scale out by increasing the replicas by {}.",
                micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.getReplicas());

            Optional<Deployment> deploymentOptional = micoKubernetesClient.getDeploymentOfMicoService(micoService);
            if (deploymentOptional.isPresent()) {
                deployment = deploymentOptional.get();
            } else {
                throw new KubernetesResourceException(
                    "Deployment for MicoService '" + micoService.getShortName() + "' in version '"
                        + micoService.getVersion() + "' is not available.");
            }
            KubernetesDeploymentInfo temporaryKubernetesDeploymentInfo = new KubernetesDeploymentInfo()
                .setNamespace(deployment.getMetadata().getNamespace())
                .setDeploymentName(deployment.getMetadata().getName())
                .setServiceNames(new ArrayList<>());
            log.debug("MicoService '{}' '{}' is already deployed. Use the Kubernetes deployment information for scaling out: {}",
                micoService.getShortName(), micoService.getVersion(), temporaryKubernetesDeploymentInfo);
            serviceDeploymentInfo.setKubernetesDeploymentInfo(temporaryKubernetesDeploymentInfo);

            deploymentOptional = micoKubernetesClient.scaleOut(serviceDeploymentInfo, serviceDeploymentInfo.getReplicas());
            if (deploymentOptional.isPresent()) {
                deployment = deploymentOptional.get();
            } else {
                throw new KubernetesResourceException(
                    "Deployment for MicoService '" + micoService.getShortName() + "' in version '"
                        + micoService.getVersion() + "' is not available.");
            }
        }

        // Create / update the Kubernetes services that corresponds to the interfaces of the MICO services.
        List<io.fabric8.kubernetes.api.model.Service> createdServices = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            io.fabric8.kubernetes.api.model.Service createdService = micoKubernetesClient.createMicoServiceInterface(serviceInterface, micoService);
            createdServices.add(createdService);
        }

        log.info("Successfully created / updated Kubernetes resources for MicoService '{}' in version '{}'",
            micoService.getShortName(), micoService.getVersion());

        // Create or update the Kubernetes deployment information, that will be stored in the database later
        KubernetesDeploymentInfo kubernetesDeploymentInfo = new KubernetesDeploymentInfo();
        if (serviceDeploymentInfo.getKubernetesDeploymentInfo() != null) {
            // If the ID is set, the Kubernetes deployment information will be updated in the database.
            // Otherwise a new node will be created in the database.
            kubernetesDeploymentInfo.setId(serviceDeploymentInfo.getKubernetesDeploymentInfo().getId());
        }
        kubernetesDeploymentInfo.setNamespace(deployment.getMetadata().getNamespace())
            .setDeploymentName(deployment.getMetadata().getName())
            .setServiceNames(createdServices.stream().map(service -> service.getMetadata().getName()).collect(toList()));

        return kubernetesDeploymentInfo;
    }
}
