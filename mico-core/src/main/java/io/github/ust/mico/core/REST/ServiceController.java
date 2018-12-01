package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.Service;
import io.github.ust.mico.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<Service>>> getServiceList() {
        List<Service> services = serviceRepository.findAll();
        List<Resource<Service>> serviceResources = services.stream().map(service -> new Resource<>(service,
                linkTo(methodOn(ServiceController.class).getServiceList()).withRel("services")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceController.class).getServiceList()).withSelfRel()));
    }


}
