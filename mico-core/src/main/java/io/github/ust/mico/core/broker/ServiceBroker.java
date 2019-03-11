package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ServiceBroker {

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    public List<MicoService> getAllServicesAsList() {
        return serviceRepository.findAll(2);
    }

    public MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        log.debug("Got following serviceOptional: {}", serviceOptional);
        if (!serviceOptional.isPresent()) {
            //TODO
            log.debug("Service in ServiceOptional is not present.");
        }
        MicoService existingService = serviceOptional.get();
        log.debug("Got following service from serviceOptional: {} ", existingService);
        return existingService;
    }

    public MicoService updateExistingService(String shortName, String version, MicoService service) {
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

    public MicoService getServiceById(Long id) {
        Optional<MicoService> serviceOptional = serviceRepository.findById(id);
        log.debug("Got following serviceOptional: {}", serviceOptional);
        if (!serviceOptional.isPresent()) {
            //TODO
            log.debug("Service in ServiceOptional is not present.");
        }
        MicoService existingService = serviceOptional.get();
        log.debug("Got following service from serviceOptional: {} ", existingService);
        return existingService;
    }

    public List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) throws ResponseStatusException {
        List<MicoService> micoServiceList = serviceRepository.findByShortName(shortName);
        log.debug("Retrieve service list from database: {}", micoServiceList);
        if (micoServiceList.isEmpty()) {
            //TODO
        }
        return micoServiceList;
    }

    public void deleteService(String shortName, String version) throws KubernetesResourceException {
        MicoService service = getServiceFromDatabase(shortName, version);
        throwConflictIfServiceIsDeployed(service);
        if (!getDependers(service).isEmpty()) {
            //TODO
        }
        serviceRepository.deleteServiceByShortNameAndVersion(shortName, version);
    }

    public void deleteAllVersionsOfService(String shortName) {
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
            //TODO
        }
    }

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

}
