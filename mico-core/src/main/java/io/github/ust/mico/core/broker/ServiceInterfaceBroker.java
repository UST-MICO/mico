package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.exception.MicoServiceInterfaceAlreadyExistsException;
import io.github.ust.mico.core.exception.MicoServiceNotFoundException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
public class ServiceInterfaceBroker {

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ServiceBroker serviceBroker;

    public Optional<List<MicoServiceInterface>> getAllInterfacesOfService(String shortName, String version) {
        Optional<List<MicoServiceInterface>> optionalMicoServiceInterfaceList = serviceRepository.findByShortNameAndVersion(shortName, version)
                        .map(MicoService::getServiceInterfaces)
                        .map(ArrayList::new);

        log.debug("Found following serviceInterfaces as list: {}", optionalMicoServiceInterfaceList.get());

        return optionalMicoServiceInterfaceList;
    }

    public Optional<MicoServiceInterface> getServiceInterfaceByServiceInterfaceName(String shortName, String version, String serviceInterfaceName) {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceRepository.findByShortNameAndVersion(shortName, version).flatMap(service -> {
            // Use service to get the fully mapped interface objects from the ogm
            if (service.getServiceInterfaces() == null) {
                return Optional.empty();
            }
            return service.getServiceInterfaces().stream().filter(serviceInterface ->
                    serviceInterface.getServiceInterfaceName().equals(serviceInterfaceName
                    )).findFirst();
        });

        log.debug("Found following serviceInterface: {}", serviceInterfaceOptional.get());

        return serviceInterfaceOptional;
    }

    public void deleteServiceInterfaceByServiceInterfaceName(String shortName, String version, String serviceInterfaceName) {
        serviceRepository.deleteInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
    }

    public MicoServiceInterface createServiceInterface (String shortName, String version, MicoServiceInterface micoServiceInterface) throws MicoServiceNotFoundException, MicoServiceInterfaceAlreadyExistsException {
        MicoService service = serviceBroker.getServiceFromDatabase(shortName, version);
        if (serviceInterfaceExists(micoServiceInterface, service)) {
            throw new MicoServiceInterfaceAlreadyExistsException(shortName, version, micoServiceInterface.getServiceInterfaceName());
        }

        service.getServiceInterfaces().add(micoServiceInterface);

        log.debug("Added following serviceInterface to micoService: {}", micoServiceInterface);
        log.debug("Adapted service: {}", service);

        serviceRepository.save(service);

        MicoServiceInterface micoServiceInterfaceToReturn = getServiceInterfaceByServiceInterfaceName(shortName, version, micoServiceInterface.getServiceInterfaceName()).get();
        log.debug("Will return following micoServiceInterface: {}", micoServiceInterfaceToReturn);

        return micoServiceInterfaceToReturn;
    }

    /**
     * Checks if a micoServiceInterface exists for a given micoService. The matching is based on the interface name.
     *
     * @param serviceInterface
     * @param service
     * @return
     */
    private boolean serviceInterfaceExists(MicoServiceInterface serviceInterface, MicoService service) {
        if (service.getServiceInterfaces() == null) {
            return false;
        }
        return service.getServiceInterfaces().stream().anyMatch(getMicoServiceInterfaceNameMatchingPredicate(serviceInterface.getServiceInterfaceName()));
    }

    /**
     * Generates a predicate which matches the given micoServiceInterfaceName.
     *
     * @param micoServiceInterfaceName
     * @return
     */
    private Predicate<MicoServiceInterface> getMicoServiceInterfaceNameMatchingPredicate(String micoServiceInterfaceName) {
        return existingServiceInterface -> existingServiceInterface.getServiceInterfaceName().equals(micoServiceInterfaceName);
    }

}
