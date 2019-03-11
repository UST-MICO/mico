package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        if (!serviceOptional.isPresent()) {
            //TODO
        }
        MicoService existingService = serviceOptional.get();
        return existingService;
    }

    public MicoService updateExistingService(String shortName, String version, MicoService service) {
        MicoService existingService = getServiceFromDatabase(shortName, version);
        service.setId(existingService.getId());
        service.setServiceInterfaces(existingService.getServiceInterfaces());
        MicoService updatedService = serviceRepository.save(service);

        return updatedService;
    }

    public Optional<MicoService> getServiceById(Long id) {
        Optional<MicoService> serviceOpt = serviceRepository.findById(id);
        return serviceOpt;
    }

    //TODO: Fix logging
    public List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) throws ResponseStatusException {
        List<MicoService> micoServiceList = serviceRepository.findByShortName(shortName);
        //log.debug("Retrieve service list from database: {}", micoServiceList);
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

    /**
     * Checks if a service is deployed and throws a ResponseStatusException with the http status CONFLICT (409) if
     * the service is deployed.
     *
     * @param service Checks if this service is deployed
     * @throws KubernetesResourceException if the service is deployed. It uses the http status CONFLICT
     */
    //TODO: Fix logging
    private void throwConflictIfServiceIsDeployed(MicoService service) throws KubernetesResourceException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            //TODO
        }
    }

    public List<MicoService> getDependers(MicoService serviceToLookFor) {
        List<MicoService> serviceList = serviceRepository.findAll(2);

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

        return dependers;
    }

}
