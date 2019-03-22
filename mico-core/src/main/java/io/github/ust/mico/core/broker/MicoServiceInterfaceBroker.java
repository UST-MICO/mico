package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.exception.MicoServiceInterfaceNotFoundException;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MicoServiceInterfaceBroker {

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    public List<MicoServiceInterface> getInterfacesOfService(String shortName, String version) {
        List<MicoServiceInterface> serviceInterfaces = serviceInterfaceRepository.findByService(shortName, version);

        return serviceInterfaces;
    }

    public MicoServiceInterface getInterfaceOfServiceByName(String shortName, String version, String interfaceName) throws MicoServiceInterfaceNotFoundException {
        Optional<MicoServiceInterface> micoServiceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(shortName, version, interfaceName);

        if(!micoServiceInterfaceOptional.isPresent()){
            throw new MicoServiceInterfaceNotFoundException(shortName, version, interfaceName);
        }

        return micoServiceInterfaceOptional.get();
    }

}
