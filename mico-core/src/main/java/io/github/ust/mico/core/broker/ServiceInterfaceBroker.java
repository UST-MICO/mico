package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ServiceInterfaceBroker {

    @Autowired
    private MicoServiceRepository serviceRepository;

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

}
