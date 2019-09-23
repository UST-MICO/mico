package io.github.ust.mico.core.broker;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
    private MicoServiceDeploymentInfoBroker micoServiceDeploymentInfoBroker;

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

    /**
     * Deploys an application with all its included services and KafkaFaasConnector instances.
     *
     * @param shortName the short name of the {@link MicoApplication}
     * @param version   the version of the {@link MicoApplication}
     * @return the {@link MicoApplicationJobStatus}
     * @throws MicoApplicationNotFoundException      if the {@link MicoApplication} does not exist
     * @throws MicoServiceInterfaceNotFoundException if the {@link MicoServiceInterface} does not exist
     * @throws DeploymentException                   if there is an error during the deployment to Kubernetes
     */
    public MicoApplicationJobStatus deployApplication(String shortName, String version)
        throws MicoApplicationNotFoundException, MicoServiceInterfaceNotFoundException, DeploymentException {

        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(shortName, version);

        checkIfMicoApplicationIsDeployable(micoApplication);

        log.info("Deploy MicoApplication '{}' in version '{}' with {} included MicoService(s) and {} KafkaFaasConnector instance(s).",
            shortName, version, micoApplication.getServices().size(), micoApplication.getKafkaFaasConnectorDeploymentInfos().size());

        // Add the included MicoServices and KafkaFaasConnector instances (distinct by version)
        // to a list of services that should be build.
        List<MicoService> servicesToBuild = new ArrayList<>();
        servicesToBuild.addAll(micoApplication.getServices());
        servicesToBuild.addAll(micoApplication.getKafkaFaasConnectorDeploymentInfos().stream()
            .map(MicoServiceDeploymentInfo::getService)
            .filter(CollectionUtils.distinctByKey(MicoService::getVersion))
            .collect(Collectors.toList()));

        // Create the build jobs for each MicoService and start them immediately.
        List<CompletableFuture<MicoService>> buildJobs = new ArrayList<>();
        for (MicoService micoService : servicesToBuild) {
            createBuildJobForMicoService(micoApplication, micoService, buildJobs);
        }

        // When all build jobs are finished, create the Kubernetes resources for the deployment of a MicoService
        CompletableFuture<List<MicoService>> allBuildJobs = FutureUtils.all(buildJobs);
        allBuildJobs.whenComplete((servicesWithNullValues, throwable) -> {
            // All failed builds lead to a null in the service deployment list.
            long failedJobs = servicesWithNullValues.stream().filter(Objects::isNull).count();
            if (failedJobs > 0) {
                log.warn("{} build job(s) failed. Skip creating / updating of Kubernetes resources.", failedJobs);
                return;
            }

            List<MicoService> servicesWithSuccessfulBuild = servicesWithNullValues.stream()
                .filter(Objects::nonNull).collect(toList());

            List<MicoServiceDeploymentInfo> serviceDeploymentInfos = new ArrayList<>();
            serviceDeploymentInfos.addAll(micoApplication.getKafkaFaasConnectorDeploymentInfos());
            serviceDeploymentInfos.addAll(micoApplication.getServiceDeploymentInfos());

            log.info("All {} build job(s) for the deployment of MicoApplication '{}' '{}' finished successfully. " +
                    "Start creating or updating Kubernetes resources ({} Kubernetes Deployments are affected).", servicesWithSuccessfulBuild.size(),
                micoApplication.getShortName(), micoApplication.getVersion(), serviceDeploymentInfos.size());

            // Create the Kubernetes resources based on all service deployment information
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
     * Creates the build job that have to be executed
     * for the deployment of the provided {@link MicoService}
     * and adds it to the provided list of build jobs.
     * The build jobs are started immediately
     * and will be stored into the database.
     *
     * @param micoApplication the {@link MicoApplication}
     * @param micoService     the {@link MicoService}
     * @param buildJobs       the list of build jobs.
     */
    private void createBuildJobForMicoService(MicoApplication micoApplication, MicoService micoService,
                                              List<CompletableFuture<MicoService>> buildJobs) {

        log.debug("Creating build job for service '{}' '{}' ...", micoService.getShortName(), micoService.getVersion());
        // Check if a build for this MicoService is already running.
        // If yes no build is required, lock changes to running jobs.
        // If the current job status is done, error or cancel delete it and create a new job to get a new id.
        Optional<MicoServiceBackgroundJob> jobOptional = backgroundJobBroker.getJobByMicoService(
            micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD);
        if (jobOptional.isPresent()) {
            if (jobOptional.get().getStatus() != MicoServiceBackgroundJob.Status.RUNNING) {
                backgroundJobBroker.deleteJob(jobOptional.get().getId());
            } else {
                log.info("Build job for service '{}' '{}' is already running.",
                    micoService.getShortName(), micoService.getVersion());
                return;
            }
        }
        MicoServiceBackgroundJob job = new MicoServiceBackgroundJob()
            .setServiceShortName(micoService.getShortName())
            .setServiceVersion(micoService.getVersion())
            .setType(MicoServiceBackgroundJob.Type.BUILD)
            .setStatus(MicoServiceBackgroundJob.Status.RUNNING);
        backgroundJobBroker.saveJob(job);

        log.info("Start build of service '{}' '{}'.", micoService.getShortName(), micoService.getVersion());
        CompletableFuture<MicoService> buildJob = CompletableFuture.supplyAsync(() -> buildMicoService(micoService))
            .exceptionally(ex -> {
                // Build failed
                backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                    MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, ExceptionUtils.getRootCauseMessage(ex));
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
        backgroundJobBroker.saveFutureOfJob(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD, buildJob);
    }

    private void checkIfMicoApplicationIsDeployable(MicoApplication micoApplication) throws MicoServiceInterfaceNotFoundException, DeploymentException {
        for (MicoService micoService : micoApplication.getServices()) {
            // If the service is not Kafka-enabled, there must be at least one interface.
            if (!micoService.isKafkaEnabled() &&
                (micoService.getServiceInterfaces() == null || micoService.getServiceInterfaces().isEmpty())) {
                throw new MicoServiceInterfaceNotFoundException(micoService.getShortName(), micoService.getVersion());
            }

            if (!micoService.getDependencies().isEmpty()) {
                // TODO: Check if dependencies are valid. Covered by mico#583
                throw new DeploymentException("The deployment of service dependencies is currently not implemented. " +
                    "See https://github.com/UST-MICO/mico/issues/583");
            }
        }

        for (MicoServiceDeploymentInfo serviceDeploymentInfo : micoApplication.getServiceDeploymentInfos()) {
            // If the service is Kafka-enabled check if the required properties are valid
            if (serviceDeploymentInfo.getService().isKafkaEnabled() && !checkIfKafkaEnabledServiceIsDeployable(serviceDeploymentInfo)) {
                throw new DeploymentException("The topics of the kafka enabled service '" +
                    serviceDeploymentInfo.getService().getShortName() + "' with instance ID '" + serviceDeploymentInfo.getInstanceId() +
                    "' are not set correctly");
            }
        }
    }

    /**
     * Checks if the properties of the {@link MicoServiceDeploymentInfo} are valid
     * so the corresponding {@link MicoService} is considered deployable.
     *
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return {@code true} if the the Kafka-enabled service is considered deployable
     */
    private boolean checkIfKafkaEnabledServiceIsDeployable(MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
        Optional<MicoEnvironmentVariable> inputTopic = findEnvironmentVariable(micoServiceDeploymentInfo.getEnvironmentVariables(), MicoEnvironmentVariable.DefaultNames.KAFKA_TOPIC_INPUT.name());
        Optional<MicoEnvironmentVariable> outputTopic = findEnvironmentVariable(micoServiceDeploymentInfo.getEnvironmentVariables(), MicoEnvironmentVariable.DefaultNames.KAFKA_TOPIC_OUTPUT.name());
        Optional<MicoEnvironmentVariable> openfaasFunctionName = findEnvironmentVariable(micoServiceDeploymentInfo.getEnvironmentVariables(), MicoEnvironmentVariable.DefaultNames.OPENFAAS_FUNCTION_NAME.name());
        if (!inputTopic.isPresent() || inputTopic.get().getValue() == null) {
            return false;
        }
        if (openfaasFunctionName.isPresent() &&
            (!outputTopic.isPresent() || outputTopic.get().getValue() == null)) {
            return openfaasFunctionName.get().getValue() != null;
        }
        return true;
    }

    private Optional<MicoEnvironmentVariable> findEnvironmentVariable(List<MicoEnvironmentVariable> micoEnvironmentVariables, String name) {
        Optional<MicoEnvironmentVariable> optionalMicoEnvironmentVariable = micoEnvironmentVariables.stream()
            .filter(micoEnvironmentVariable -> micoEnvironmentVariable.getName().equals(name)).findFirst();
        return optionalMicoEnvironmentVariable;
    }

    private MicoService buildMicoService(MicoService micoService) {
        try {
            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<String> buildFuture = imageBuilder.build(micoService);
            if (buildFuture.get() != null) {
                String dockerImageUri = buildFuture.get();
                log.info("Build of service '{}' in version '{}' finished with image '{}'.",
                    micoService.getShortName(), micoService.getVersion(), dockerImageUri);
                micoService.setDockerImageUri(dockerImageUri);
                // Save the MicoService with a depth of 0 to the database.
                // Only the properties of this MicoService entity will be stored to the database.
                serviceRepository.save(micoService, 0);
            } else {
                String errorMessage = "Build of service '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' didn't return a Docker image URI.";
                throw new CompletionException(new RuntimeException(errorMessage));
            }
        } catch (InterruptedException | ExecutionException | NotInitializedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return micoService;
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
            deployment = micoKubernetesClient.createMicoService(serviceDeploymentInfo);
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
            io.fabric8.kubernetes.api.model.Service createdService = micoKubernetesClient.createMicoServiceInterface(serviceInterface, micoService);
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
