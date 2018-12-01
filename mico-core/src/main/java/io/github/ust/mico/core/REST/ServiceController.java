package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.Service;
import io.github.ust.mico.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/services", produces = "application/hal+json")
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;


}
