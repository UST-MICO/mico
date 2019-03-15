package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.MicoServiceAlreadyExistsException;
import io.github.ust.mico.core.exception.MicoServiceHasDependersException;
import io.github.ust.mico.core.exception.MicoServiceNotFoundException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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

    public MicoService updateExistingService(String shortName, String version, MicoService service) throws MicoServiceNotFoundException {
        MicoService existingService = getServiceFromDatabase(shortName, version);
        log.debug("Got following service from the database: {}", existingService);
        log.debug("Got following service from the request body: {}", service);
        service.setId(existingService.getId());
        service.setServiceInterfaces(existingService.getServiceInterfaces());
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

    public void deleteService(MicoService service) throws KubernetesResourceException, MicoServiceHasDependersException {
        throwConflictIfServiceIsDeployed(service);
        if (!getDependers(service).isEmpty()) {
            throw new MicoServiceHasDependersException(service.getShortName(), service.getVersion());
        }
        serviceRepository.deleteServiceByShortNameAndVersion(service.getShortName(), service.getVersion());
    }

    public void deleteAllVersionsOfService(String shortName) throws MicoServiceNotFoundException, KubernetesResourceException {
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
    private void throwConflictIfServiceIsDeployed(MicoService service) throws KubernetesResourceException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            log.info("Micoservice '{}' in version '{}' is deployed. It is not possible to delete a deployed service.", service.getShortName(), service.getVersion());
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
    public List<MicoService> findDependers(String shortName, String version) {
        return serviceRepository.findDependers(shortName, version);
    }

//    public MicoServiceStatusDTO getStatusOfService(String shortName, String version) throws MicoServiceNotFoundException {
//        MicoServiceStatusDTO serviceStatus = new MicoServiceStatusDTO();
//        MicoService micoService = getServiceFromDatabase(shortName, version);
//        serviceStatus = micoStatusService.getServiceStatus(micoService);
//        return serviceStatus;
//    }

    public MicoService persistService(MicoService newService) throws MicoServiceNotFoundException, MicoServiceAlreadyExistsException {
        MicoService micoService = getServiceFromDatabase(newService.getShortName(), newService.getVersion());
        if (micoService != null) {
            throw new MicoServiceAlreadyExistsException(newService.getShortName(), newService.getVersion());
        }
        for (MicoServiceInterface serviceInterface : newService.getServiceInterfaces()) {
            //TODO: Verfiy how to put this into method into ServiceBroker
            //validateProvidedInterface(newService.getShortName(), newService.getVersion(), serviceInterface);
        }
        MicoService savedService = serviceRepository.save(newService);
        return savedService;
    }

    public LinkedList<MicoService> getDependentServices(List<MicoServiceDependency> dependees) {
        if (dependees == null) {
            return null;
        }

        LinkedList<MicoService> services = new LinkedList<>();

        dependees.forEach(dependee -> {
            String shortName = dependee.getDependedService().getShortName();
            String version = dependee.getDependedService().getVersion();

            Optional<MicoService> dependeeServiceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
            MicoService dependeeService = dependeeServiceOpt.orElse(null);
            if (dependeeService == null) {
                // TODO: MicoService name is mandatory! Will be covered by issue mico#490
                MicoService newService = serviceRepository.save(new MicoService().setShortName(shortName).setVersion(version));
                services.add(newService);
            } else {
                services.add(dependeeService);
            }
        });

        return services;
    }

}
