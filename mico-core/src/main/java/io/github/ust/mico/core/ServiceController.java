package io.github.ust.mico.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;
    
}
