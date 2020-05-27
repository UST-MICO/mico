package io.github.ust.mico.core.broker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.exception.DeploymentRequirementsOfKafkaFaasConnectorNotMetException;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.MicoApplicationIsDeployingException;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.exception.MicoServiceInterfaceNotFoundException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.KubernetesDeploymentInfo;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceBackgroundJob;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.TektonPipelinesController;
import io.github.ust.mico.core.util.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private TektonPipelinesController imageBuilder;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoServiceRepository serviceRepository;

    /**
     * Deploys an application with all its included services and KafkaFaasConnector instances.
     *
     * @param shortName the short name of the {@link MicoApplication}
     * @param version   the version of the {@link MicoApplication}
     * @return the {@link MicoApplicationJobStatus}
     * @throws MicoApplicationNotFoundException      if the {@link MicoApplication} does not exist
     * @throws MicoServiceInterfaceNotFoundException if the {@link MicoServiceInterface} does not exist
     */
    public MicoApplicationJobStatus deployApplication(String shortName, String version)
        throws MicoApplicationNotFoundException, MicoServiceInterfaceNotFoundException, DeploymentRequirementsOfKafkaFaasConnectorNotMetException {

        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(shortName, version);

        checkIfMicoApplicationIsDeployable(micoApplication);

        log.info("Deploy MicoApplication '{}' in version '{}' with {} included MicoService(s) and {} KafkaFaasConnector instance(s).",
            shortName, version, micoApplication.getServices().size(), micoApplication.getKafkaFaasConnectorDeploymentInfos().size());

        // Add the included MicoServices and KafkaFaasConnector instances (distinct by version)
        // to a list of services that should be build.
        List<MicoServiceDeploymentInfo> serviceInstancesToBuild = new ArrayList<>(micoApplication.getServiceDeploymentInfos());
        if (!micoApplication.getKafkaFaasConnectorDeploymentInfos().isEmpty()) {
            serviceInstancesToBuild.add(micoApplication.getKafkaFaasConnectorDeploymentInfos().get(0));
        }

        // TODO: avoid building images if the docker image is already available in the registry
        // TODO: decouple image building from MicoServices to avoid running duplicate image builds

        // Create the build jobs for each MicoService instance and start them immediately.
        List<CompletableFuture<MicoService>> buildJobs = new ArrayList<>();
        for (MicoServiceDeploymentInfo info : serviceInstancesToBuild) {
            if (Objects.isNull(info.getService().getDockerImageUri()) || info.getService().getDockerImageUri().isEmpty()) {
                createBuildJobForMicoServiceInstance(micoApplication, info, buildJobs);
            }
        }

        // When all build jobs are finished, create the Kubernetes resources for the deployment of a MicoService
        CompletableFuture<List<MicoService>> allBuildJobs = FutureUtils.all(buildJobs);
        log.info("Wait for completion of all build jobs...");
        allBuildJobs.whenComplete((servicesWithNullValues, throwable) -> {
            log.info("All build jobs are completed.");

            // All failed builds lead to a null in the service deployment list.
            long failedJobs = servicesWithNullValues.stream().filter(Objects::isNull).count();
            if (failedJobs > 0) {
                log.warn("{} build job(s) failed. Skip creating / updating of Kubernetes resources.", failedJobs);
                return;
            }

            List<MicoService> servicesWithSuccessfulBuild = servicesWithNullValues.stream()
                .filter(Objects::nonNull).collect(toList());

            // Save updated Docker Image URI to database.
            // TODO: Move save operation to main thread (avoid Neo4j threading problems) -> issue mico#842
            for (MicoService service : servicesWithSuccessfulBuild) {
                // Save the MicoService with a depth of 0 to the database.
                // Only the properties of this MicoService entity will be stored to the database.
                MicoService updatedService = serviceRepository.save(service, 0);
                log.debug("Saved docker image uri of MicoService '{}' '{}' to database successfully: {}",
                    updatedService.getShortName(), updatedService.getVersion(), updatedService.getDockerImageUri());

                // quick hack
                if ("kafka-faas-connector".equals(service.getName())) {
                    micoApplication.getServices().stream().filter(s -> s.getName().equals("kafka-faas-connector")).forEach(s -> {
                        s.setDockerImageUri(service.getDockerImageUri());
                        serviceRepository.save(s, 0);
                    });
                }
            }

            List<MicoServiceDeploymentInfo> serviceDeploymentInfos = new ArrayList<>();
            serviceDeploymentInfos.addAll(micoApplication.getKafkaFaasConnectorDeploymentInfos());
            serviceDeploymentInfos.addAll(micoApplication.getServiceDeploymentInfos());

            log.info("All {} build job(s) for the deployment of MicoApplication '{}' '{}' finished successfully. " +
                    "Start creating or updating Kubernetes resources ({} Kubernetes Deployments are affected).",
                servicesWithSuccessfulBuild.size(), micoApplication.getShortName(), micoApplication.getVersion(), serviceDeploymentInfos.size());

            // Create the Kubernetes resources based on all service deployment information
            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                MicoService micoService = serviceDeploymentInfo.getService();
                try {
                    KubernetesDeploymentInfo kubernetesDeploymentInfo = createOrUpdateKubernetesResources(serviceDeploymentInfo);
                    serviceDeploymentInfo.setKubernetesDeploymentInfo(kubernetesDeploymentInfo);
                    backgroundJobBroker.saveNewStatus(serviceDeploymentInfo,
                        MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.DONE);
                } catch (Exception e) {
                    backgroundJobBroker.saveNewStatus(serviceDeploymentInfo,
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

    /**
     * Undeploys an application with all its included services and KafkaFaasConnector instances.
     *
     * @param shortName the short name of the {@link MicoApplication}
     * @param version   the version of the {@link MicoApplication}
     * @throws MicoApplicationNotFoundException    if the {@link MicoApplication} does not exist
     * @throws MicoApplicationIsDeployingException if the {@link MicoApplication} is currently deploying
     */
    public void undeployApplication(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsDeployingException {

        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(shortName, version);

        log.info("Undeploy MicoApplication '{}' in version '{}' with {} included MicoService(s) and {} KafkaFaasConnector instance(s).",
            shortName, version, micoApplication.getServices().size(), micoApplication.getKafkaFaasConnectorDeploymentInfos().size());

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

    /**
     * Creates the build job that have to be executed for the deployment of the provided {@link MicoService} and adds it
     * to the provided list of build jobs. The build jobs are started immediately and will be stored into the database.
     *
     * @param micoApplication           the {@link MicoApplication}
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @param buildJobs                 the list of build jobs.
     */
    private void createBuildJobForMicoServiceInstance(MicoApplication micoApplication, MicoServiceDeploymentInfo micoServiceDeploymentInfo,
                                                      List<CompletableFuture<MicoService>> buildJobs) {

        MicoService micoService = micoServiceDeploymentInfo.getService();

        log.debug("Creating build job for service '{}' '{}' with instance ID '{}'...",
            micoService.getShortName(), micoService.getVersion(), micoServiceDeploymentInfo.getInstanceId());

        // Check if a build for this MicoService is already running.
        // If yes no build is required, lock changes to running jobs.
        // If the current job status is done, error or cancel delete it and create a new job to get a new id.
        Optional<MicoServiceBackgroundJob> jobOptional = backgroundJobBroker
            .getJobByMicoServiceInstanceId(micoServiceDeploymentInfo.getInstanceId(), MicoServiceBackgroundJob.Type.BUILD);
        if (jobOptional.isPresent()) {
            if (jobOptional.get().getStatus() != MicoServiceBackgroundJob.Status.RUNNING) {
                backgroundJobBroker.deleteJob(jobOptional.get().getId());
            } else {
                log.info("Build job for service '{}' '{}' with instance ID '{}' is already running.",
                    micoService.getShortName(), micoService.getVersion(), micoServiceDeploymentInfo.getInstanceId());
                return;
            }
        }
        MicoServiceBackgroundJob job = new MicoServiceBackgroundJob()
            .setServiceShortName(micoService.getShortName())
            .setServiceVersion(micoService.getVersion())
            .setInstanceId(micoServiceDeploymentInfo.getInstanceId())
            .setType(MicoServiceBackgroundJob.Type.BUILD)
            .setStatus(MicoServiceBackgroundJob.Status.RUNNING);
        backgroundJobBroker.saveJob(job);

        log.info("Start build of service '{}' '{}'.", micoService.getShortName(), micoService.getVersion());

        ExecutorService pool = Executors.newFixedThreadPool(3);
        CompletableFuture<MicoService> buildJob = CompletableFuture.supplyAsync(() -> buildMicoService(micoService), pool)
            .exceptionally(ex -> {
                // Build failed
                backgroundJobBroker.saveNewStatus(micoServiceDeploymentInfo, MicoServiceBackgroundJob.Type.BUILD,
                    MicoServiceBackgroundJob.Status.ERROR, ExceptionUtils.getRootCauseMessage(ex));
                List<CompletableFuture<MicoService>> runningBuildJobs = buildJobs.stream()
                    .filter(j -> !j.isDone() && !j.isCancelled() && !j.isCompletedExceptionally()).collect(Collectors.toList());
                if (runningBuildJobs.size() > 1) {
                    log.debug("There are still {} other job(s) running for the deployment of MicoApplication '{}' '{}'",
                        runningBuildJobs.size() - 1, micoApplication.getShortName(), micoApplication.getVersion());
                }
                return null;
            });
        log.debug("Started build of service '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
        buildJobs.add(buildJob);
        backgroundJobBroker.saveFutureOfJob(micoServiceDeploymentInfo, MicoServiceBackgroundJob.Type.BUILD, buildJob);
    }

    private void checkIfMicoApplicationIsDeployable(MicoApplication micoApplication) throws MicoServiceInterfaceNotFoundException, DeploymentRequirementsOfKafkaFaasConnectorNotMetException {
        for (MicoService micoService : micoApplication.getServices()) {
            // If the service is not Kafka-enabled, there must be at least one interface.
            if (!micoService.isKafkaEnabled() &&
                (micoService.getServiceInterfaces() == null || micoService.getServiceInterfaces().isEmpty())) {
                throw new MicoServiceInterfaceNotFoundException(micoService.getShortName(), micoService.getVersion());
            }

            if (!micoService.getDependencies().isEmpty()) {
                // TODO: Check if dependencies are valid. Covered by mico#583
                throw new NotImplementedException("The deployment of service dependencies is currently not implemented. " +
                    "See https://github.com/UST-MICO/mico/issues/583");
            }
        }

        // Check if the KafkaFaasConnector deployment information met the requirements
        for (MicoServiceDeploymentInfo kfConnectorDeploymentInfo : micoApplication.getKafkaFaasConnectorDeploymentInfos()) {
            checkIfKafkaFaasConnectorIsDeployable(kfConnectorDeploymentInfo);
        }
    }

    /**
     * Checks if the properties of the {@link MicoServiceDeploymentInfo} are valid so the corresponding
     * KafkaFaasConnector is considered deployable.
     *
     * @param kfConnectorDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @throws DeploymentRequirementsOfKafkaFaasConnectorNotMetException if the requirements are not met
     */
    public void checkIfKafkaFaasConnectorIsDeployable(MicoServiceDeploymentInfo kfConnectorDeploymentInfo) throws DeploymentRequirementsOfKafkaFaasConnectorNotMetException {
        // The input topic must be set for a KafkaFaasConnector
        if (kfConnectorDeploymentInfo.getTopics().stream().noneMatch(t -> t.getRole().equals(MicoTopicRole.Role.INPUT))) {
            throw new DeploymentRequirementsOfKafkaFaasConnectorNotMetException(kfConnectorDeploymentInfo,
                "The input topic is missing.");
        }
        // If there is no output topic, a OpenFaaS function name must be set for a KafkaFaasConnector
        if (kfConnectorDeploymentInfo.getTopics().stream().noneMatch(t -> t.getRole().equals(MicoTopicRole.Role.OUTPUT)) &&
            (kfConnectorDeploymentInfo.getOpenFaaSFunction() == null || kfConnectorDeploymentInfo.getOpenFaaSFunction().getName() == null)) {
            throw new DeploymentRequirementsOfKafkaFaasConnectorNotMetException(kfConnectorDeploymentInfo,
                "The requirements for the deployment of the KafkaFaasConnector are not met. " +
                    "Deployment information: " + kfConnectorDeploymentInfo);
        }
    }

    /**
     * Builds a {@link MicoService} and sets the resulting Docker image URI to the {@link MicoService} object. This
     * method does not save the changes to the database immediately, because it would lead to deadlock problems (Neo4j
     * multi-threading problems).
     *
     * @param micoService the {@link MicoService}
     * @return the {@link MicoService} with the updated Docker image URI
     */
    private MicoService buildMicoService(MicoService micoService) {
        try {
            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<String> buildFuture = imageBuilder.build(micoService);
            if (buildFuture.get() != null) {
                String dockerImageUri = buildFuture.get();
                log.info("Build of service '{}' in version '{}' finished with image '{}'.",
                    micoService.getShortName(), micoService.getVersion(), dockerImageUri);
                micoService.setDockerImageUri(dockerImageUri);
                return micoService;
            } else {
                String errorMessage = "Build of service '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' didn't return a Docker image URI.";
                throw new CompletionException(new RuntimeException(errorMessage));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | KubernetesResourceException | NotInitializedException e) {
            throw new RuntimeException(e);
        }
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
        String instanceId = serviceDeploymentInfo.getInstanceId();
        log.info("Creating / updating Kubernetes resources for service '{}' '{}' with instance ID '{}'.",
            micoService.getShortName(), micoService.getVersion(), instanceId);
        log.debug("Using deployment information for service '{}' '{}': {}",
            micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.toString());

        // If the Kubernetes deployment already exists and is deployed, scale out,
        // otherwise create the Kubernetes deployment
        boolean micoServiceInstanceIsDeployed = micoKubernetesClient.isMicoServiceInstanceDeployed(serviceDeploymentInfo);
        if (micoServiceInstanceIsDeployed && serviceDeploymentInfo.getKubernetesDeploymentInfo() != null) {
            log.info("MicoService '{}' '{}' in instance '{}' is already deployed by this MicoApplication. Nothing to do.",
                micoService.getShortName(), micoService.getVersion(), instanceId);
            return serviceDeploymentInfo.getKubernetesDeploymentInfo();
        }

        Deployment deployment;
        if (!micoServiceInstanceIsDeployed) {
            log.info("MicoService '{}' '{}' in instance '{}' is not deployed yet. Create the required Kubernetes resources.",
                micoService.getShortName(), micoService.getVersion(), instanceId);
            deployment = micoKubernetesClient.createMicoServiceInstance(serviceDeploymentInfo);
        } else {
            // MICO service was deployed by another MICO application.
            // Get information about the actual deployment to be able to perform the scaling.
            log.info("MicoService '{}' '{}' in instance '{}' was already deployed by another MicoApplication. Scale out by increasing the replicas by {}.",
                micoService.getShortName(), micoService.getVersion(), instanceId, serviceDeploymentInfo.getReplicas());

            Optional<Deployment> deploymentOptional = micoKubernetesClient.getDeploymentOfMicoServiceInstance(serviceDeploymentInfo);
            if (deploymentOptional.isPresent()) {
                deployment = deploymentOptional.get();
            } else {
                throw new KubernetesResourceException(
                    "Deployment for MicoService '" + micoService.getShortName() + "' in version '"
                        + micoService.getVersion() + "' in instance '" + instanceId + "' is not available.");
            }
            KubernetesDeploymentInfo temporaryKubernetesDeploymentInfo = new KubernetesDeploymentInfo()
                .setNamespace(deployment.getMetadata().getNamespace())
                .setDeploymentName(deployment.getMetadata().getName())
                .setServiceNames(new ArrayList<>());
            log.debug("MicoService '{}' '{}' in instance '{}' is already deployed. Use the Kubernetes deployment information for scaling out: {}",
                micoService.getShortName(), micoService.getVersion(), instanceId, temporaryKubernetesDeploymentInfo);
            serviceDeploymentInfo.setKubernetesDeploymentInfo(temporaryKubernetesDeploymentInfo);

            deploymentOptional = micoKubernetesClient.scaleOut(serviceDeploymentInfo, serviceDeploymentInfo.getReplicas());
            if (deploymentOptional.isPresent()) {
                deployment = deploymentOptional.get();
            } else {
                throw new KubernetesResourceException(
                    "Deployment for MicoService '" + micoService.getShortName() + "' in version '"
                        + micoService.getVersion() + "' in instance '" + instanceId + "' is not available.");
            }
        }

        // Create / update the Kubernetes services that corresponds to the interfaces of the MICO services.
        List<io.fabric8.kubernetes.api.model.Service> createdServices = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            io.fabric8.kubernetes.api.model.Service createdService = micoKubernetesClient
                .createMicoServiceInterface(serviceInterface, serviceDeploymentInfo);
            createdServices.add(createdService);
        }

        log.info("Successfully created / updated Kubernetes resources for MicoService '{}' in version '{}' in instance '{}'.",
            micoService.getShortName(), micoService.getVersion(), instanceId);

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
