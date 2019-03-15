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
        return serviceRepository.findByShortNameAndVersion(shortName, version)
                        .map(MicoService::getServiceInterfaces)
                        .map(ArrayList::new);
    }

}
