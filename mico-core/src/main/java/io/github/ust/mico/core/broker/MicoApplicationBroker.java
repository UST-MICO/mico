package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.configuration.KafkaConfig;
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoTopicRequestDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationDeploymentStatusResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.resource.ApplicationResource;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@Service
public class MicoApplicationBroker {

    @Autowired
    private MicoServiceBroker micoServiceBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @Autowired
    private MicoLabelRepository micoLabelRepository;

    @Autowired
    private MicoTopicRepository micoTopicRepository;

    @Autowired
    private MicoEnvironmentVariableRepository micoEnvironmentVariableRepository;

    @Autowired
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
    private MicoInterfaceConnectionRepository micoInterfaceConnectionRepository;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private OpenFaaSConfig openFaaSConfig;

    public MicoApplication getMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!micoApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(shortName, version);
        }
        return micoApplicationOptional.get();
    }

    public List<MicoApplication> getMicoApplicationsByShortName(String shortName) {
        return applicationRepository.findByShortName(shortName);
    }

    public List<MicoApplication> getMicoApplications() {
        return applicationRepository.findAll(3);
    }

    public void deleteMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException {
        // Retrieve application to delete from the database (checks whether it exists)
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);

        // Check whether application is currently undeployed, if not it is not allowed to delete the application
        if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
            throw new MicoApplicationIsNotUndeployedException(shortName, version);
        }

        // Any service deployment information this application provides must be deleted
        // before (!) the actual application is deleted, otherwise the query for
        // deleting the service deployment information would not work.
        serviceDeploymentInfoRepository.deleteAllByApplication(shortName, version);

        // Delete actual application
        applicationRepository.delete(micoApplication);
    }

    public void deleteMicoApplicationsByShortName(String shortName) throws MicoApplicationIsNotUndeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplicationsByShortName(shortName);

        // If at least one version of the application is currently not undeployed,
        // none of the versions shall be deleted
        for (MicoApplication micoApplication : micoApplicationList) {
            if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
                throw new MicoApplicationIsNotUndeployedException(micoApplication.getShortName(), micoApplication.getVersion());
            }
        }

        // Any service deployment information one of the applications provides must be deleted
        // before (!) the actual application is deleted, otherwise the query for
        // deleting the service deployment information would not work.
        serviceDeploymentInfoRepository.deleteAllByApplication(shortName);

        // No version of the application is deployed -> delete all
        applicationRepository.deleteAll(micoApplicationList);
    }

    public MicoApplication createMicoApplication(MicoApplication micoApplication) throws MicoApplicationAlreadyExistsException {
        try {
            getMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
        } catch (MicoApplicationNotFoundException e) {
            return applicationRepository.save(micoApplication);
        }
        throw new MicoApplicationAlreadyExistsException(micoApplication.getShortName(), micoApplication.getVersion());
    }

    public MicoApplication updateMicoApplication(String shortName, String version, MicoApplication micoApplication) throws MicoApplicationNotFoundException, ShortNameOfMicoApplicationDoesNotMatchException, VersionOfMicoApplicationDoesNotMatchException, MicoApplicationIsNotUndeployedException {
        if (!micoApplication.getShortName().equals(shortName)) {
            throw new ShortNameOfMicoApplicationDoesNotMatchException();
        }
        if (!micoApplication.getVersion().equals(version)) {
            throw new VersionOfMicoApplicationDoesNotMatchException();
        }
        MicoApplication existingMicoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        // Check whether application is currently undeployed, if not it is not allowed to update the application
        if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
            throw new MicoApplicationIsNotUndeployedException(shortName, version);
        }
        // TODO: Ensure consistent strategy to update existing entities (covered by mico#690)
        micoApplication.setId(existingMicoApplication.getId())
            .setServices(existingMicoApplication.getServices())
            .setServiceDeploymentInfos(serviceDeploymentInfoRepository.findAllByApplication(shortName, version));
        return applicationRepository.save(micoApplication);
    }

    public MicoApplication copyAndUpgradeMicoApplicationByShortNameAndVersion(String shortName, String version, String newVersion) throws MicoApplicationNotFoundException, MicoApplicationAlreadyExistsException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);

        // Update the version of the application
        micoApplication.setVersion(newVersion);

        // In order to copy the application along with all service deployment information nodes
        // it provides and the nodes the service deployment information node(s) is/are connected to,
        // we need to set the id of all those nodes to null. That way, Neo4j will create new entities
        // instead of updating the existing ones.
        micoApplication.setId(null);
        micoApplication.getServiceDeploymentInfos().forEach(sdi -> sdi.setId(null));
        micoApplication.getServiceDeploymentInfos().forEach(sdi -> sdi.getLabels().forEach(label -> label.setId(null)));
        micoApplication.getServiceDeploymentInfos().forEach(sdi -> {
            sdi.getEnvironmentVariables().forEach(envVar -> envVar.setId(null));
            sdi.getInterfaceConnections().forEach(ic -> ic.setId(null));
        });
        // The actual Kubernetes deployment information must not be copied, because the new application
        // is considered to be not deployed yet.
        micoApplication.getServiceDeploymentInfos().forEach(sdi -> sdi.setKubernetesDeploymentInfo(null));

        return createMicoApplication(micoApplication);
    }

    public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
    }

    public List<MicoApplication> getMicoApplicationsUsingMicoService(String serviceShortName, String serviceVersion) {
        return applicationRepository.findAllByUsedService(serviceShortName, serviceVersion);
    }

    public MicoApplication addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException, MicoServiceAddedMoreThanOnceToMicoApplicationException, MicoApplicationIsNotUndeployedException {

        log.debug("Adding MicoService '{}' '{}' to MicoApplication '{}' '{}'.",
            serviceShortName, serviceVersion, applicationShortName, applicationVersion);
        // Retrieve application and service from database (checks whether they exist)
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);

        // Check whether application is currently undeployed, if not it is not allowed to add service to the application
        if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
            throw new MicoApplicationIsNotUndeployedException(applicationShortName, applicationShortName);
        }

        MicoService micoService = micoServiceBroker.getServiceFromDatabase(serviceShortName, serviceVersion);

        // Find all services with identical short name within this application
        List<MicoService> micoServices = micoApplication.getServices().stream().filter(s -> s.getShortName().equals(serviceShortName)).collect(Collectors.toList());
        log.debug("Found {} MicoService(s) with short name '{}' within application '{}' '{}'.",
            micoServices.size(), serviceShortName, applicationShortName, applicationVersion);

        if (micoServices.size() > 1) {
            // Illegal state, each service is allowed only once in every application
            throw new MicoServiceAddedMoreThanOnceToMicoApplicationException(micoApplication.getShortName(), micoApplication.getVersion());
        } else if (micoServices.size() == 0) {
            // Service not included yet, simply add it
            MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setService(micoService);
            setDefaultEnvironmentVariablesForKafkaEnabledService(micoServiceDeploymentInfo);

            // Both the service list and the service deployment info list of the application need to be updated ...
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);

            // ... before the application can be saved.
            return applicationRepository.save(micoApplication);
        } else {
            // Service already included, replace it with its newer version, ...
            MicoService existingMicoService = micoServices.get(0);
            log.debug("MicoService '{}' is already included in application '{}' '{}' in version '{}'. " +
                    "Replace it with the version '{}'.", serviceShortName, applicationShortName, applicationVersion,
                existingMicoService.getVersion(), serviceVersion);

            // ... but only replace if the new version is different from the current version
            if (existingMicoService.getVersion().equals(serviceVersion)) {
                throw new MicoServiceAlreadyAddedToMicoApplicationException(applicationShortName, applicationVersion, serviceShortName, serviceVersion);
            } else {
                // Replace service in list of services in application
                micoApplication.getServices().remove(existingMicoService);
                micoApplication.getServices().add(micoService);

                // Move the edge between the application and the service to the new version of the service
                // by updating the corresponding deployment info
                micoApplication.getServiceDeploymentInfos().stream()
                    .filter(sdi -> sdi.getService().getShortName().equals(serviceShortName))
                    .collect(Collectors.toList())
                    .forEach(sdi -> sdi.setService(micoService));

                // Save the application with the updated list of services
                // and service deployment infos in the database
                return applicationRepository.save(micoApplication);
            }
        }
    }

    /**
     * Sets the default environment variables for Kafka-enabled MicoServices. See {@link MicoEnvironmentVariable.DefaultEnvironmentVariableKafkaNames}
     * for a complete list.
     *
     * @param micoServiceDeploymentInfo The {@link MicoServiceDeploymentInfo} with an corresponding MicoService
     */
    private void setDefaultEnvironmentVariablesForKafkaEnabledService(MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
        MicoService micoService = micoServiceDeploymentInfo.getService();
        if (micoService == null) {
            throw new IllegalArgumentException("The MicoServiceDeploymentInfo needs a valid MicoService set to check if the service is Kafka enabled");
        }
        if (!micoService.isKafkaEnabled()) {
            log.debug("MicoService '{}' '{}' is not Kafka-enabled. Not necessary to adding specific env variables.",
                micoService.getShortName(), micoService.getVersion());
            return;
        }
        log.debug("Adding default environment variables to the Kafka-enabled MicoService '{}' '{}'.",
            micoService.getShortName(), micoService.getVersion());
        List<MicoEnvironmentVariable> micoEnvironmentVariables = micoServiceDeploymentInfo.getEnvironmentVariables();
        micoEnvironmentVariables.addAll(kafkaConfig.getDefaultEnvironmentVariablesForKafka());
        micoEnvironmentVariables.addAll(openFaaSConfig.getDefaultEnvironmentVariablesForOpenFaaS());
    }

    public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, MicoApplicationIsNotUndeployedException {
        // Retrieve application from database (checks whether it exists)
        MicoApplication micoApplication = checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

        // Check whether application is currently undeployed, if not it is not allowed to remove services from the application
        if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
            throw new MicoApplicationIsNotUndeployedException(applicationShortName, applicationVersion);
        }

        // Check whether the application contains the service
        if (micoApplication.getServices().stream().noneMatch(s -> s.getShortName().equals(serviceShortName))) {
            // Application does not include the service -> cannot not be deleted from it
            throw new MicoApplicationDoesNotIncludeMicoServiceException(applicationShortName, applicationVersion, serviceShortName);
        } else {
            // 1. Remove the service from the application
            micoApplication.getServices().removeIf(s -> s.getShortName().equals(serviceShortName));
            MicoApplication updatedMicoApplication = applicationRepository.save(micoApplication);
            // 2. Delete the corresponding service deployment information
            serviceDeploymentInfoRepository.deleteByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
            return updatedMicoApplication;
        }

        // TODO: Update Kubernetes deployment (see issue mico#627)
    }

    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException, MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

        Optional<MicoServiceDeploymentInfo> micoServiceDeploymentInfoOptional = serviceDeploymentInfoRepository.findByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
        if (micoServiceDeploymentInfoOptional.isPresent()) {
            return micoServiceDeploymentInfoOptional.get();
        } else {
            throw new MicoServiceDeploymentInformationNotFoundException(applicationShortName, applicationVersion, serviceShortName);
        }
    }

    private MicoApplication checkForMicoServiceInMicoApplication(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);

        if (micoApplication.getServices().stream().noneMatch(service -> service.getShortName().equals(serviceShortName))) {
            throw new MicoApplicationDoesNotIncludeMicoServiceException(applicationShortName, applicationVersion, serviceShortName);
        }
        return micoApplication;
    }

    public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion,
                                                                            String serviceShortName, MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws
        MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException,
        MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException, MicoTopicRoleUsedMultipleTimesException {

        validateTopics(serviceDeploymentInfoDTO);

        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        MicoServiceDeploymentInfo storedServiceDeploymentInfo = getMicoServiceDeploymentInformation(applicationShortName, applicationVersion, serviceShortName);

        int oldReplicas = storedServiceDeploymentInfo.getReplicas();

        // Update existing service deployment information and save it in the database.
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(
            storedServiceDeploymentInfo.applyValuesFrom(serviceDeploymentInfoDTO));

        // As a workaround it's necessary to save the same entity twice,
        // because otherwise it sometimes happens that the relationship entity `MicoTopicRole` has no properties.
        serviceDeploymentInfoRepository.save(updatedServiceDeploymentInfo);

        // In case addition properties (stored as separate node entity) such as labels, environment variables
        // have been removed from this service deployment information,
        // the standard save() function of the service deployment information repository will not delete those
        // "tangling" (without relationships) labels (nodes), hence the manual clean up.
        micoLabelRepository.cleanUp();
        micoTopicRepository.cleanUp();
        micoEnvironmentVariableRepository.cleanUp();
        kubernetesDeploymentInfoRepository.cleanUp();
        micoInterfaceConnectionRepository.cleanUp();

        // FIXME: Currently we only supported scale in / scale out.
        // 		  If the MICO service is already deployed, we only update the replicas.
        // 	      The other properties are ignored!
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            MicoService micoService = updatedServiceDeploymentInfo.getService();
            log.info("MicoApplication '{}' {}' is already deployed. Update the deployment of the included MicoService '{} '{}'.",
                micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName(), micoService.getVersion());

            // MICO service is already deployed. Update the replicas.
            int replicasDiff = serviceDeploymentInfoDTO.getReplicas() - oldReplicas;
            if (replicasDiff > 0) {
                log.debug("Increase replicas of MicoService '{}' '{}' by {}.", micoService.getShortName(), micoService.getVersion(), replicasDiff);
                micoKubernetesClient.scaleOut(updatedServiceDeploymentInfo, replicasDiff);
            } else if (replicasDiff < 0) {
                log.debug("Decrease replicas of MicoService '{}' '{}' by {}.", micoService.getShortName(), micoService.getVersion(), replicasDiff);
                micoKubernetesClient.scaleIn(updatedServiceDeploymentInfo, Math.abs(replicasDiff));
            } else {
                // TODO: If no scale operation is required, maybe some other
                // 		 information still needs to be updated.
            }
        }

        return updatedServiceDeploymentInfo;
    }

    /**
     * Validates the topics.
     * Throws an error if there are multiple topics with the same role.
     *
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoRequestDTO}
     * @throws MicoTopicRoleUsedMultipleTimesException if an {@code MicoTopicRole.Role} is not unique
     */
    private void validateTopics(MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws MicoTopicRoleUsedMultipleTimesException {
        List<MicoTopicRequestDTO> newTopics = serviceDeploymentInfoDTO.getTopics();
        Set<MicoTopicRole.Role> usedRoles = new HashSet<>();
        for (MicoTopicRequestDTO requestDTO : newTopics) {
            if (!usedRoles.add(requestDTO.getRole())) {
                // Role is used twice, however a role should be used only once
                throw new MicoTopicRoleUsedMultipleTimesException(requestDTO.getRole());
            }
        }
    }

    //TODO: Change return value to not use a DTO (see issue mico#630)
    public MicoApplicationStatusResponseDTO getApplicationStatus(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        MicoApplicationStatusResponseDTO applicationStatus = micoStatusService.getApplicationStatus(micoApplication);
        applicationStatus.setApplicationDeploymentStatusResponseDTO(new MicoApplicationDeploymentStatusResponseDTO(
            getApplicationDeploymentStatus(shortName, version)));
        return applicationStatus;
    }

    public MicoApplicationDeploymentStatus getApplicationDeploymentStatus(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return micoKubernetesClient.getApplicationDeploymentStatus(micoApplication);
    }

    //TODO: Move to Resource or keep in Broker? (see issue mico#632)
    public Iterable<Link> getLinksOfMicoApplication(MicoApplication application) {
        ArrayList<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ApplicationResource.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationResource.class).getAllApplications()).withRel("applications"));
        return links;
    }

}
