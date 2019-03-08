package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceBroker {

    @Autowired
    private MicoServiceRepository serviceRepository;

    public List<MicoService> getAllServicesAsList (){
        return serviceRepository.findAll(2);
    }
}
