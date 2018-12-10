package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.DependsOn;
import io.github.ust.mico.core.Service;
import io.github.ust.mico.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceController {

    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";
    private static final String PATH_VARIABLE_ID = "id";

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<Service>>> getServiceList() {
        List<Service> services = serviceRepository.findAll();
        List<Resource<Service>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceController.class).getServiceList()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    //TODO Add validation to path variables
    public ResponseEntity<Resource<Service>> getServiceByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        return serviceOpt.map(service -> new Resource<>(service, getServiceLinks(service)))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/")
    public ResponseEntity<Resources<Resource<Service>>> getVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<Service> services = serviceRepository.findByShortName(shortName);
        List<Resource<Service>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceController.class).getVersionsOfService(shortName)).withSelfRel()));
    }

    //TODO: Ambiguous endpoint with /services/shortName
    //@GetMapping("/{" + PATH_VARIABLE_ID + "}/")
    public ResponseEntity<Resource<Service>> getServiceById(@PathVariable(PATH_VARIABLE_ID) Long id) {
        Optional<Service> serviceOpt = serviceRepository.findById(id);

        return serviceOpt.map(service -> new Resource<>(service, getServiceLinks(service)))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Resource<Service>> createService(@RequestBody Service newService) {
        if (newService.getDependsOn() == null) {
            Service savedService = serviceRepository.save(newService);

            return ResponseEntity
                    .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                    .body(new Resource<>(newService, getServiceLinks(newService)));
        } else {
            List<DependsOn> dependees = newService.getDependsOn();
            LinkedList<Service> services = getDependentServices(dependees);
            List<DependsOn> newDependees = new LinkedList<>();

            services.forEach(service -> {
                Optional<Service> serviceOpt = serviceRepository.findById(service.getId());
                serviceOpt.isPresent();
                DependsOn dependsOnService = new DependsOn(serviceOpt.get());
                newDependees.add(dependsOnService);
            });

            newService.setDependsOn(newDependees);

            Service savedService = serviceRepository.save(newService);

            return ResponseEntity
                    .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                    .body(new Resource<>(newService, getServiceLinks(newService)));
        }
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resources<Resource<Service>>> getDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);

        Service service = serviceOpt.get();

        List<DependsOn> dependees = service.getDependsOn();
        LinkedList<Service> services = getDependentServices(dependees);

        List<Resource<Service>> resourceList = getServiceResourcesList(services);

        return ResponseEntity.ok(
                new Resources<>(resourceList,
                        linkTo(methodOn(ServiceController.class).getDependees(shortName, version)).withSelfRel()));
    }

    private LinkedList<Service> getDependentServices(List<DependsOn> dependees) {
        LinkedList<Service> services = new LinkedList<>();

        dependees.forEach(dependee -> {
            String shortName = dependee.getService().getShortName();
            String version = dependee.getService().getVersion();

            Optional<Service> dependeeServiceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
            Service dependeeService = dependeeServiceOpt.get();
            services.add(dependeeService);
        });

        return services;
    }

    private List<Resource<Service>> getServiceResourcesList(List<Service> services) {
        return services.stream().map(service -> new Resource<>(service, getServiceLinks(service)))
                .collect(Collectors.toList());
    }

    private Iterable<Link> getServiceLinks(Service service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getServiceList()).withRel("services"));
        return links;
    }

}
