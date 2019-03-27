package io.github.ust.mico.core.broker;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.resource.ApplicationResource;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;

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
    private MicoEnvironmentVariableRepository micoEnvironmentVariableRepository;

    @Autowired
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
    private MicoInterfaceConnectionRepository micoInterfaceConnectionRepository;

    public MicoApplication getMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!micoApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(shortName, version);
        }
        return micoApplicationOptional.get();
    }

    public List<MicoApplication> getMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);
        if (micoApplicationList.isEmpty()) {
            throw new MicoApplicationNotFoundException(shortName);
        }
        return micoApplicationList;
    }

    public List<MicoApplication> getMicoApplications() {
        return applicationRepository.findAll(3);
    }

    public void deleteMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsDeployedException {
        // Retrieve application to delete from the database (checks whether it exists)
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);

        // Check whether application is currently deployed, i.e., it cannot be deleted
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            throw new MicoApplicationIsDeployedException(shortName, version);
        }

        // Any service deployment information this application provides must be deleted
        // before (!) the actual application is deleted, otherwise the query for
        // deleting the service deployment information would not work.
        serviceDeploymentInfoRepository.deleteAllByApplication(shortName, version);

        // Delete actual application
        applicationRepository.delete(micoApplication);
    }

    public void deleteMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException, MicoApplicationIsDeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplicationsByShortName(shortName);

        // If at least one version of the application is currently deployed,
        // none of the versions shall be deleted
        for (MicoApplication micoApplication : micoApplicationList) {
            if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
                throw new MicoApplicationIsDeployedException(micoApplication.getShortName(), micoApplication.getVersion());
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

    public MicoApplication updateMicoApplication(String shortName, String version, MicoApplication micoApplication) throws MicoApplicationNotFoundException, ShortNameOfMicoApplicationDoesNotMatchException, VersionOfMicoApplicationDoesNotMatchException {
        if (!micoApplication.getShortName().equals(shortName)) {
            throw new ShortNameOfMicoApplicationDoesNotMatchException();
        }
        if (!micoApplication.getVersion().equals(version)) {
            throw new VersionOfMicoApplicationDoesNotMatchException();
        }
        MicoApplication existingMicoApplication = getMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
        micoApplication.setId(existingMicoApplication.getId())
            .setServices(existingMicoApplication.getServices())
            .setServiceDeploymentInfos(existingMicoApplication.getServiceDeploymentInfos());
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

    public MicoApplication addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException, MicoServiceAddedMoreThanOnceToMicoApplicationException {
        // Retrieve application and service from database (checks whether they exist)
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        MicoService micoService = micoServiceBroker.getServiceFromDatabase(serviceShortName, serviceVersion);

        // Find all services with identical short name within this application
        List<MicoService> micoServices = micoApplication.getServices().stream().filter(s -> s.getShortName().equals(serviceShortName)).collect(Collectors.toList());

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
            return applicationRepository.save(micoApplication);
        } else {
            // Service already included, replace it with its newer version, ...
            MicoService existingMicoService = micoServices.get(0);

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

    public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        // Retrieve application from database (checks whether it exists)
        MicoApplication micoApplication = checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

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
        MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException {

        MicoServiceDeploymentInfo storedServiceDeploymentInfo = getMicoServiceDeploymentInformation(applicationShortName, applicationVersion, serviceShortName);

        // Update existing service deployment information and save it in the database.
        storedServiceDeploymentInfo.applyValuesFrom(serviceDeploymentInfoDTO);
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(storedServiceDeploymentInfo);

        // In case addition properties (stored as separate node entity) such as labels, environment variables
        // have been removed from this service deployment information,
        // the standard save() function of the service deployment information repository will not delete those
        // "tangling" (without relationships) labels (nodes), hence the manual clean up.
        micoLabelRepository.cleanUp();
        micoEnvironmentVariableRepository.cleanUp();
        kubernetesDeploymentInfoRepository.cleanUp();
        micoInterfaceConnectionRepository.cleanUp();

        // FIXME: Currently we only supported scale in / scale out.
        // 		  Every information except the replicas is ignored!
        int replicasDiff = serviceDeploymentInfoDTO.getReplicas() - storedServiceDeploymentInfo.getReplicas();
        if (replicasDiff > 0) {
        	micoKubernetesClient.scaleOut(updatedServiceDeploymentInfo, replicasDiff);
        } else if (replicasDiff < 0) {
        	micoKubernetesClient.scaleIn(updatedServiceDeploymentInfo, Math.abs(replicasDiff));
        } else {
        	// TODO: If no scale operation is required, maybe some other
        	// 		 information still needs to be updated.
        }

        return updatedServiceDeploymentInfo;
    }

    //TODO: Change return value to not use a DTO (see issue mico#630)
    public MicoApplicationStatusResponseDTO getMicoApplicationStatusOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return micoStatusService.getApplicationStatus(micoApplication);
    }

    public MicoApplicationDeploymentStatus getApplicationDeploymentStatus(String shortName, String version) {
        return micoKubernetesClient.getApplicationDeploymentStatus(shortName, version);
    }

    //TODO: Move to Resource or keep in Broker? (see issue mico#632)
    public Iterable<Link> getLinksOfMicoApplication(MicoApplication application) {
        ArrayList<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ApplicationResource.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationResource.class).getAllApplications()).withRel("applications"));
        return links;
    }

}
