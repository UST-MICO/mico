package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.configuration.KafkaFaasConnectorConfig;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationDeploymentStatusResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.KFConnectorDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.resource.ApplicationResource;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.UIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@Service
public class MicoApplicationBroker {

    @Autowired
    private KafkaFaasConnectorConfig kafkaFaasConnectorConfig;

    @Autowired
    private MicoServiceBroker micoServiceBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private KFConnectorDeploymentInfoRepository kfConnectorDeploymentInfoRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @Autowired
    private MicoServiceDeploymentInfoBroker serviceDeploymentInfoBroker;

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
        // This deletes also the service deployment information for the included KafkaFaasConnectors.
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
        // This deletes also the service deployment information for the included KafkaFaasConnectors.
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
        // Set the information that are not part of the request DTO based on the existing application:
        // ID, included services, service deployment information (both for normal services and the KafkaFaasConnectors).
        micoApplication.setId(existingMicoApplication.getId())
            .setServices(existingMicoApplication.getServices())
            .setServiceDeploymentInfos(serviceDeploymentInfoRepository.findMicoServiceSDIsByApplication(shortName, version))
            .setKafkaFaasConnectorDeploymentInfos(getKfConnectorDeploymentInfos(existingMicoApplication));
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
        prepareServiceDeploymentInfosForCopying(micoApplication.getServiceDeploymentInfos());
        prepareServiceDeploymentInfosForCopying(micoApplication.getKafkaFaasConnectorDeploymentInfos().stream()
            .map(KFConnectorDeploymentInfo::getServiceDeploymentInfo).collect(Collectors.toList()));

        return createMicoApplication(micoApplication);
    }

    /**
     * Retrieves the list of {@code KFConnectorDeploymentInfos} that are part of the {@code MicoApplication}.
     * They are used for the deployment of KafkaFaasConnector instances.
     *
     * @param existingMicoApplication the {@link MicoApplication}
     * @return the list of {@link KFConnectorDeploymentInfo KFConnectorDeploymentInfos}
     */
    private List<KFConnectorDeploymentInfo> getKfConnectorDeploymentInfos(MicoApplication existingMicoApplication) {
        List<String> instanceIds = existingMicoApplication.getKafkaFaasConnectorDeploymentInfos().stream()
            .map(KFConnectorDeploymentInfo::getInstanceId).collect(Collectors.toList());
        List<KFConnectorDeploymentInfo> existingKFCDIs = new ArrayList<>();
        for (String instanceId : instanceIds) {
            Optional<KFConnectorDeploymentInfo> kfCDIOpt = kfConnectorDeploymentInfoRepository.findByInstanceId(instanceId);
            kfCDIOpt.ifPresent(existingKFCDIs::add);
        }
        return existingKFCDIs;
    }

    /**
     * Sets all IDs of the included entities of the service deployment infos to null,
     * so it's possible to make a copy.
     * The included {@link KubernetesDeploymentInfo} is deleted completely,
     * so the new application is considered to be not deployed.
     *
     * @param serviceDeploymentInfos the {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos}
     */
    private void prepareServiceDeploymentInfosForCopying(List<MicoServiceDeploymentInfo> serviceDeploymentInfos) {
        for (MicoServiceDeploymentInfo sdi : serviceDeploymentInfos) {
            sdi.setId(null);
            sdi.getLabels().forEach(label -> label.setId(null));
            sdi.getEnvironmentVariables().forEach(envVar -> envVar.setId(null));
            sdi.getTopics().forEach(topic -> topic.setId(null));
            sdi.getInterfaceConnections().forEach(ic -> ic.setId(null));
            // The actual Kubernetes deployment information must not be copied, because the new application
            // is considered to be not deployed yet.
            sdi.setKubernetesDeploymentInfo(null);
        }
    }

    public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
    }

    public List<MicoApplication> getMicoApplicationsUsingMicoService(String serviceShortName, String serviceVersion) {
        return applicationRepository.findAllByUsedService(serviceShortName, serviceVersion);
    }

    public void addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion)
        throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException, MicoServiceAddedMoreThanOnceToMicoApplicationException,
        MicoApplicationIsNotUndeployedException, MicoTopicRoleUsedMultipleTimesException, MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException, MicoApplicationDoesNotIncludeMicoServiceException, KafkaFaasConnectorNotAllowedHereException {

        // KafkaFaasConnector is not allowed here, because it should be handled differently.
        // See method `addKafkaFaasConnectorInstanceToMicoApplicationByVersion`
        if (serviceShortName.equals(kafkaFaasConnectorConfig.getServiceName())) {
            throw new KafkaFaasConnectorNotAllowedHereException();
        }

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

            // Both the service list and the service deployment info list of the application need to be updated ...
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);

            // ... before the application can be saved.
            applicationRepository.save(micoApplication);

            // Set default deployment information (environment variables, topics)
            serviceDeploymentInfoBroker.setDefaultDeploymentInformationForKafkaEnabledService(micoServiceDeploymentInfo);
            serviceDeploymentInfoBroker.updateMicoServiceDeploymentInformation(applicationShortName, applicationVersion, serviceShortName,
                new MicoServiceDeploymentInfoRequestDTO(micoServiceDeploymentInfo));
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
                applicationRepository.save(micoApplication);
            }
        }
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
            // 1. Delete the corresponding service deployment information
            serviceDeploymentInfoRepository.deleteByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
            // 2. Remove the service from the application
            micoApplication.getServices().removeIf(s -> s.getShortName().equals(serviceShortName));
            return applicationRepository.save(micoApplication);
        }

        // TODO: Update Kubernetes deployment (see issue mico#627)
    }

    public KFConnectorDeploymentInfo addKafkaFaasConnectorInstanceToMicoApplicationByVersion(
        String applicationShortName, String applicationVersion, String kfConnectorVersion)
        throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException, KafkaFaasConnectorVersionNotFoundException {

        // Retrieve application and service from database (checks whether they exist)
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);

        // Check whether application is currently undeployed, if not it is not allowed to add a KafkaFaasConnector instance to the application
        if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
            throw new MicoApplicationIsNotUndeployedException(applicationShortName, applicationShortName);
        }

        MicoService kfConnector;
        try {
            kfConnector = micoServiceBroker.getServiceFromDatabase(kafkaFaasConnectorConfig.getServiceName(), kfConnectorVersion);
        } catch (MicoServiceNotFoundException e) {
            throw new KafkaFaasConnectorVersionNotFoundException(kfConnectorVersion);
        }

        String instanceId = UIDUtils.uidFor(kfConnector);
        MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo().setService(kfConnector);
        KFConnectorDeploymentInfo kfConnectorDeploymentInfo = new KFConnectorDeploymentInfo()
            .setServiceDeploymentInfo(sdi)
            .setApplication(micoApplication)
            .setInstanceId(instanceId);
        micoApplication.getKafkaFaasConnectorDeploymentInfos().add(kfConnectorDeploymentInfo);
        applicationRepository.save(micoApplication);

        log.debug("Added KafkaFaasConnector in version '{}' with instance ID '{}' to MicoApplication '{}' '{}'." +
                "This application now uses {} KF connector instances.",
            kfConnectorVersion, instanceId, applicationShortName, applicationVersion,
            micoApplication.getKafkaFaasConnectorDeploymentInfos().size());

        // TODO: Set default deployment information (covered in epic mico#750)
        // Set default deployment information (environment variables, topics)
        //serviceDeploymentInfoBroker.setDefaultDeploymentInformationForKafkaEnabledService(sdi);
        //serviceDeploymentInfoBroker.updateKafkaFaasConnectorDeploymentInformation(applicationShortName, applicationVersion, instanceId,
        //    new KFConnectorDeploymentInfoRequestDTO(sdi));
        return kfConnectorDeploymentInfo;
    }

    public void removeKafkaFaasConnectorInstanceFromMicoApplicationByInstanceId(
        String applicationShortName, String applicationVersion, String instanceId)
        throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException, KafkaFaasConnectorInstanceNotFoundException, MicoApplicationDoesNotIncludeKFConnectorInstanceException {

        // Retrieve application from database (checks whether it exists)
        MicoApplication micoApplication = checkForKFConnectorInMicoApplication(applicationShortName, applicationVersion, instanceId);

        // Check whether application is currently undeployed, if not it is not allowed to remove a KafkaFaasConnector instance from the application
        if (!micoKubernetesClient.isApplicationUndeployed(micoApplication)) {
            throw new MicoApplicationIsNotUndeployedException(applicationShortName, applicationVersion);
        }

        Optional<KFConnectorDeploymentInfo> kfConnectorDeploymentInfoOpt = kfConnectorDeploymentInfoRepository.findByInstanceId(instanceId);
        if (!kfConnectorDeploymentInfoOpt.isPresent()) {
            throw new KafkaFaasConnectorInstanceNotFoundException(instanceId);
        }

        // It's only required to delete the corresponding service deployment information
        KFConnectorDeploymentInfo kfConnectorDeploymentInfo = kfConnectorDeploymentInfoOpt.get();
        MicoServiceDeploymentInfo sdi = kfConnectorDeploymentInfo.getServiceDeploymentInfo();
        serviceDeploymentInfoRepository.delete(sdi);

        // TODO: Expect that the KafkaFaasConnectorDeploymentInfo is removed (covered in epic mico#750)
        MicoApplication updatedMicoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        log.debug("Updated MicoApplication: {}", updatedMicoApplication);

        // TODO: Update Kubernetes deployment (see issue mico#627)
    }

    MicoApplication checkForMicoServiceInMicoApplication(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);

        if (micoApplication.getServices().stream().noneMatch(service -> service.getShortName().equals(serviceShortName))) {
            throw new MicoApplicationDoesNotIncludeMicoServiceException(applicationShortName, applicationVersion, serviceShortName);
        }
        return micoApplication;
    }

    private MicoApplication checkForKFConnectorInMicoApplication(String applicationShortName, String applicationVersion, String instanceId) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeKFConnectorInstanceException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);

        if (micoApplication.getKafkaFaasConnectorDeploymentInfos().stream().noneMatch(kf_cdi -> kf_cdi.getInstanceId().equals(instanceId))) {
            throw new MicoApplicationDoesNotIncludeKFConnectorInstanceException(applicationShortName, applicationVersion, instanceId);
        }
        return micoApplication;
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
