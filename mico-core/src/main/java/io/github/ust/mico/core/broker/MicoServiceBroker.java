package io.github.ust.mico.core.broker;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphEdgeResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoServiceStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MicoServiceBroker {

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    public List<MicoService> getAllServicesAsList() {
        return serviceRepository.findAll(2);
    }

    public MicoService getServiceFromDatabase(String shortName, String version) throws MicoServiceNotFoundException {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        log.debug("Got following serviceOptional: {}", serviceOptional);
        if (!serviceOptional.isPresent()) {
            log.debug("Service not found.");
            throw new MicoServiceNotFoundException(shortName, version);
        }
        MicoService existingService = serviceOptional.get();
        log.debug("Got following service from serviceOptional: {} ", existingService);
        return existingService;
    }

    public MicoService updateExistingService(MicoService service) {
        log.debug("Updated service, before saving to database: {}", service);
        MicoService updatedService = serviceRepository.save(service);
        log.debug("Updated service, after saving to database: {}", updatedService);
        return updatedService;
    }

    public MicoService getServiceById(Long id) throws MicoServiceNotFoundException {
        Optional<MicoService> serviceOptional = serviceRepository.findById(id);
        log.debug("Got following serviceOptional: {}", serviceOptional);
        if (!serviceOptional.isPresent()) {
            log.debug("Service not found.");
            throw new MicoServiceNotFoundException(id);
        }
        MicoService existingService = serviceOptional.get();
        log.debug("Got following service from serviceOptional: {} ", existingService);
        return existingService;
    }

    public List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) throws ResponseStatusException, MicoServiceNotFoundException {
        List<MicoService> micoServiceList = serviceRepository.findByShortName(shortName);
        log.debug("Retrieve service list from database: {}", micoServiceList);
        if (micoServiceList.isEmpty()) {
            log.debug("Service not found.");
            throw new MicoServiceNotFoundException(shortName);
        }
        return micoServiceList;
    }

    public void deleteService(MicoService service) throws KubernetesResourceException, MicoServiceHasDependersException, MicoServiceIsDeployedException {
        throwConflictIfServiceIsDeployed(service);
        if (!getDependers(service).isEmpty()) {
            throw new MicoServiceHasDependersException(service.getShortName(), service.getVersion());
        }
        serviceRepository.deleteServiceByShortNameAndVersion(service.getShortName(), service.getVersion());
    }

    public void deleteAllVersionsOfService(String shortName) throws MicoServiceNotFoundException, KubernetesResourceException, MicoServiceIsDeployedException {
        List<MicoService> micoServiceList = getAllVersionsOfServiceFromDatabase(shortName);
        log.debug("Got following services from database: {}", micoServiceList);
        for (MicoService micoService : micoServiceList) {
            throwConflictIfServiceIsDeployed(micoService);
        }
        micoServiceList.forEach(service -> serviceRepository.delete(service));
    }

    /**
     * Checks if a service is deployed and throws a ResponseStatusException with the http status CONFLICT (409) if
     * the service is deployed.
     *
     * @param service Checks if this service is deployed
     * @throws KubernetesResourceException if the service is deployed. It uses the http status CONFLICT
     */
    private void throwConflictIfServiceIsDeployed(MicoService service) throws KubernetesResourceException, MicoServiceIsDeployedException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            log.info("Micoservice '{}' in version '{}' is deployed. It is not possible to delete a deployed service.", service.getShortName(), service.getVersion());
            throw new MicoServiceIsDeployedException(service.getShortName(), service.getVersion());
        }
    }

    //TODO: Same functionality as findDependers?
    public List<MicoService> getDependers(MicoService serviceToLookFor) {
        List<MicoService> serviceList = serviceRepository.findAll(2);
        log.debug("Got following services as list from the database: {}", serviceList);
        List<MicoService> dependers = new LinkedList<>();

        serviceList.forEach(service -> {
            List<MicoServiceDependency> dependees = service.getDependencies();
            if (dependees != null) {
                dependees.forEach(dependee -> {
                    if (dependee.getDependedService().equals(serviceToLookFor)) {
                        dependers.add(dependee.getService());
                    }
                });
            }
        });
        log.debug("Found following dependers: {}", dependers);
        return dependers;
    }

    //TODO: Same functionality as getDependers?
    public List<MicoService> findDependers(MicoService service) {
        return serviceRepository.findDependers(service.getShortName(), service.getVersion());
    }

    //TODO: Update return from micoStatusRepository from DTO to model object
    public MicoServiceStatusResponseDTO getStatusOfService(String shortName, String version) throws MicoServiceNotFoundException {
        MicoServiceStatusResponseDTO serviceStatus;
        MicoService micoService = getServiceFromDatabase(shortName, version);
        serviceStatus = micoStatusService.getServiceStatus(micoService);
        return serviceStatus;
    }

    public MicoService persistService(MicoService newService) throws MicoServiceAlreadyExistsException {
        Optional<MicoService> micoServiceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion());
        if (micoServiceOptional.isPresent()) {
            throw new MicoServiceAlreadyExistsException(newService.getShortName(), newService.getVersion());
        }
        for (MicoServiceInterface serviceInterface : newService.getServiceInterfaces()) {
        	serviceInterface.getDescription(); // Just to suppress the "unused" warning for serviceInterface ...
            //TODO: Verfiy how to put this into method into ServiceBroker
            //validateProvidedInterface(newService.getShortName(), newService.getVersion(), serviceInterface);
        }
        MicoService savedService = serviceRepository.save(newService);
        return savedService;
    }

    public List<MicoService> getDependeesByMicoService(MicoService service) {
        return serviceRepository.findDependees(service.getShortName(), service.getVersion());
    }

    public boolean checkIfDependencyAlreadyExists(MicoService service, MicoService serviceDependee) {
        boolean dependencyAlreadyExists = (service.getDependencies() != null) && service.getDependencies().stream().anyMatch(
            dependency -> dependency.getDependedService().getShortName().equals(serviceDependee.getShortName())
                && dependency.getDependedService().getVersion().equals(serviceDependee.getVersion()));

        log.debug("Check if the dependency already exists is '{}", dependencyAlreadyExists);

        return dependencyAlreadyExists;
    }

    public MicoService persistNewDependencyBetweenServices(MicoService service, MicoService serviceDependee) {
        MicoServiceDependency processedServiceDependee = new MicoServiceDependency()
            .setDependedService(serviceDependee)
            .setService(service);

        log.info("New dependency for MicoService '{}' '{}' -[:DEPENDS_ON]-> '{}' '{}'",
            service.getShortName(),
            service.getVersion(),
            processedServiceDependee.getDependedService().getShortName(),
            processedServiceDependee.getDependedService().getVersion());

        service.getDependencies().add(processedServiceDependee);
        serviceRepository.save(service);

        return service;
    }

    public MicoService deleteDependencyBetweenServices(MicoService service, MicoService serviceToDelete) {
        service.getDependencies().removeIf(dependency -> dependency.getDependedService().getId().equals(serviceToDelete.getId()));
        MicoService updatedService = serviceRepository.save(service);
        return updatedService;
    }

    public MicoService deleteAllDependees(MicoService service) {
        service.getDependencies().clear();
        MicoService resultingService = serviceRepository.save(service);

        log.debug("Service after deleting all dependencies: {}", resultingService);

        return resultingService;
    }

    public MicoService promoteService(MicoService service, String newVersion) throws MicoServiceAlreadyExistsException {
        // In order to copy the service along with all service interfaces nodes
        // and all port nodes of the service interface we need to set the id of
        // service interfaces and ports to null.
        // That way, Neo4j will create new entities instead of updating the existing ones.
        service.setVersion(newVersion).setId(null);
        service.getServiceInterfaces().forEach(serviceInterface -> serviceInterface.setId(null));
        service.getServiceInterfaces().forEach(serviceInterface -> serviceInterface.getPorts().forEach(port -> port.setId(null)));

        log.debug("Set new version in service: {}", service);

        MicoService updatedService = persistService(service);

        log.debug("Updated service: {}", updatedService);

        return updatedService;
    }

    //TODO: Create test
    //TODO: We shoud not use DTOs here, improve
    public MicoServiceDependencyGraphResponseDTO getDependencyGraph(MicoService micoServiceRoot) throws MicoServiceNotFoundException {
        List<MicoService> micoServices = serviceRepository.findDependeesIncludeDepender(micoServiceRoot.getShortName(),
            micoServiceRoot.getVersion());

        List<MicoServiceResponseDTO> micoServiceDTOS = micoServices.stream().map(MicoServiceResponseDTO::new).collect(Collectors.toList());
        MicoServiceDependencyGraphResponseDTO micoServiceDependencyGraph = new MicoServiceDependencyGraphResponseDTO().setMicoServices(micoServiceDTOS);
        LinkedList<MicoServiceDependencyGraphEdgeResponseDTO> micoServiceDependencyGraphEdgeList = new LinkedList<>();
        for (MicoService micoService : micoServices) {
            //Request each mico service again from the db, because the dependencies are not included
            //in the result of the custom query. TODO improve query to also include the dependencies (Depth parameter)
            MicoService micoServiceFromDB = getServiceFromDatabase(micoService.getShortName(), micoService.getVersion());
            micoServiceFromDB.getDependencies().forEach(micoServiceDependency -> {
                MicoServiceDependencyGraphEdgeResponseDTO edge = new MicoServiceDependencyGraphEdgeResponseDTO(micoService, micoServiceDependency.getDependedService());
                micoServiceDependencyGraphEdgeList.add(edge);
            });
        }
        micoServiceDependencyGraph.setMicoServiceDependencyGraphEdgeList(micoServiceDependencyGraphEdgeList);

        return micoServiceDependencyGraph;
    }

    /**
     * Return yaml for a {@link MicoService} for the give shortName and version.
     *
     * @param shortName the short name of the {@link MicoService}.
     * @param version   version the version of the {@link MicoService}.
     * @return the kubernetes YAML for the {@link MicoService}.
     */
    public String getServiceYamlByShortNameAndVersion(String shortName, String version) throws MicoServiceNotFoundException, JsonProcessingException, KubernetesResourceException {
        return micoKubernetesClient.getYaml(getServiceFromDatabase(shortName, version));
    }
}
