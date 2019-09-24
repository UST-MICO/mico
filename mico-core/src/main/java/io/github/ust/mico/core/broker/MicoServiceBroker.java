package io.github.ust.mico.core.broker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import io.github.ust.mico.core.configuration.KafkaFaasConnectorConfig;
import io.github.ust.mico.core.model.MicoApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphEdgeResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoServiceStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MicoServiceBroker {

    @Autowired
    private MicoApplicationBroker micoApplicationBroker;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @Autowired
    private KafkaFaasConnectorConfig kafkaFaasConnectorConfig;

    public List<MicoService> getAllServicesAsList() {
        return serviceRepository.findAll(2);
    }

    public MicoService getServiceFromDatabase(String shortName, String version) throws MicoServiceNotFoundException {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOptional.isPresent()) {
            throw new MicoServiceNotFoundException(shortName, version);
        }
        return serviceOptional.get();
    }

    public MicoService updateExistingService(MicoService service) throws MicoServiceIsDeployedException {
        throwConflictIfServiceIsDeployed(service);
        MicoService updatedService = serviceRepository.save(service);
        log.debug("Updated service: {}", updatedService);
        return updatedService;
    }

    public MicoService getServiceById(Long id) throws MicoServiceNotFoundException {
        Optional<MicoService> serviceOptional = serviceRepository.findById(id);
        if (!serviceOptional.isPresent()) {
            throw new MicoServiceNotFoundException(id);
        }
        return serviceOptional.get();
    }

    public List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) {
        return serviceRepository.findByShortName(shortName);
    }

    public void deleteService(MicoService service) throws MicoServiceHasDependersException, MicoServiceIsDeployedException, MicoServiceIsUsedByMicoApplicationsException {
        throwConflictIfServiceIsDeployed(service);
        throwConflictIfServiceIsIncludedInApplications(service);
        if (!getDependers(service).isEmpty()) {
            throw new MicoServiceHasDependersException(service.getShortName(), service.getVersion());
        }
        serviceRepository.deleteServiceByShortNameAndVersion(service.getShortName(), service.getVersion());
        log.debug("Deleted MicoService '{}' '{}'.", service.getShortName(), service.getVersion());
    }

    public void deleteAllVersionsOfService(String shortName) throws MicoServiceIsDeployedException, MicoServiceIsUsedByMicoApplicationsException {
        List<MicoService> serviceList = getAllVersionsOfServiceFromDatabase(shortName);
        for (MicoService service : serviceList) {
            throwConflictIfServiceIsDeployed(service);
            throwConflictIfServiceIsIncludedInApplications(service);
        }
        for (MicoService service : serviceList) {
            serviceRepository.delete(service);
            log.debug("Deleted MicoService '{}' '{}'.", service.getShortName(), service.getVersion());
        }
    }

    /**
     * Checks if a service is deployed. If yes the method throws a {@code MicoServiceIsDeployedException}.
     *
     * @param service the {@link MicoService}
     * @throws MicoServiceIsDeployedException if the service is deployed
     */
    private void throwConflictIfServiceIsDeployed(MicoService service) throws MicoServiceIsDeployedException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            log.info("MicoService '{}' in version '{}' is deployed. Undeployment is required.",
                service.getShortName(), service.getVersion());
            throw new MicoServiceIsDeployedException(service.getShortName(), service.getVersion());
        }
    }

    /**
     * Checks if a service is used by other applications. If yes the method throws a {@code MicoServiceIsUsedByMicoApplicationsException}.
     *
     * @param service the {@link MicoService}
     * @throws MicoServiceIsUsedByMicoApplicationsException if the service is used by applications
     */
    private void throwConflictIfServiceIsIncludedInApplications(MicoService service) throws MicoServiceIsUsedByMicoApplicationsException {
        List<MicoApplication> applications = micoApplicationBroker.getMicoApplicationsUsingMicoService(service.getShortName(), service.getVersion());
        if (!applications.isEmpty()) {
            throw new MicoServiceIsUsedByMicoApplicationsException(service.getShortName(), service.getVersion(), applications);
        }
    }

    //TODO: Same functionality as findDependers?
    public List<MicoService> getDependers(MicoService serviceToLookFor) {
        List<MicoService> serviceList = serviceRepository.findAll(2);
        log.debug("Got following services as list from the database: {}", serviceList);
        List<MicoService> dependers = new ArrayList<>();

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
        return serviceRepository.save(newService);
    }

    public List<MicoService> getDependeesByMicoService(MicoService service) {
        return serviceRepository.findDependees(service.getShortName(), service.getVersion());
    }

    public boolean checkIfDependencyAlreadyExists(MicoService service, MicoService serviceDependee) {
        boolean dependencyAlreadyExists = (service.getDependencies() != null) && service.getDependencies().stream().anyMatch(
            dependency -> dependency.getDependedService().getShortName().equals(serviceDependee.getShortName())
                && dependency.getDependedService().getVersion().equals(serviceDependee.getVersion()));

        log.debug("Check if the dependency between '{}' '{}' and '{}' '{}' already exists: {}",
            service.getShortName(), service.getVersion(), serviceDependee.getShortName(), serviceDependee.getVersion(),
            dependencyAlreadyExists);

        return dependencyAlreadyExists;
    }

    public MicoService persistNewDependencyBetweenServices(MicoService service, MicoService serviceDependee) throws MicoServiceIsDeployedException {
        // Ensure that the service and the new dependency are not deployed.
        throwConflictIfServiceIsDeployed(service);
        throwConflictIfServiceIsDeployed(serviceDependee);

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

    public MicoService deleteDependencyBetweenServices(MicoService service, MicoService serviceToDelete) throws MicoServiceIsDeployedException {
        throwConflictIfServiceIsDeployed(service);
        service.getDependencies().removeIf(dependency -> dependency.getDependedService().getId().equals(serviceToDelete.getId()));
        return serviceRepository.save(service);
    }

    public MicoService deleteAllDependees(MicoService service) throws MicoServiceIsDeployedException {
        throwConflictIfServiceIsDeployed(service);

        service.getDependencies().clear();
        MicoService resultingService = serviceRepository.save(service);

        log.debug("Service after deleting all dependencies: {}", resultingService);

        return resultingService;
    }

    public MicoService promoteService(MicoService service, String newVersion) throws MicoServiceAlreadyExistsException {

        // Update the version of the application
        service.setVersion(newVersion);

        // Remove docker image URI
        service.setDockerImageUri(null);

        // In order to copy the service along with all service interfaces nodes
        // and all port nodes of the service interface we need to set the id of
        // service interfaces and ports to null.
        // That way, Neo4j will create new entities instead of updating the existing ones.
        service.setId(null);
        service.getServiceInterfaces().forEach(serviceInterface -> serviceInterface.setId(null));
        service.getServiceInterfaces().forEach(serviceInterface -> serviceInterface.getPorts().forEach(port -> port.setId(null)));

        return persistService(service);
    }

    //TODO: Create test
    //TODO: We shoud not use DTOs here, improve
    public MicoServiceDependencyGraphResponseDTO getDependencyGraph(MicoService micoServiceRoot) throws MicoServiceNotFoundException {
        List<MicoService> micoServices = serviceRepository.findDependeesIncludeDepender(micoServiceRoot.getShortName(),
            micoServiceRoot.getVersion());

        List<MicoServiceResponseDTO> micoServiceDTOS = micoServices.stream().map(MicoServiceResponseDTO::new).collect(Collectors.toList());
        MicoServiceDependencyGraphResponseDTO micoServiceDependencyGraph = new MicoServiceDependencyGraphResponseDTO().setMicoServices(micoServiceDTOS);
        ArrayList<MicoServiceDependencyGraphEdgeResponseDTO> micoServiceDependencyGraphEdgeList = new ArrayList<>();
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
    public String getServiceYamlByShortNameAndVersion(String shortName, String version) throws MicoServiceNotFoundException, JsonProcessingException {
        return micoKubernetesClient.getYaml(getServiceFromDatabase(shortName, version));
    }

    public String getLatestKFConnectorVersion() throws KafkaFaasConnectorLatestVersionNotFound {
        List<String> kfConnectorVersions = serviceRepository.findByShortName(kafkaFaasConnectorConfig.getServiceName()).stream()
                .map(kfConnector -> kfConnector.getVersion()).sorted().collect(Collectors.toList());
        if(kfConnectorVersions.isEmpty()) {
            throw new KafkaFaasConnectorLatestVersionNotFound();
        }
        return Iterables.getLast(kfConnectorVersions);
    }
}
