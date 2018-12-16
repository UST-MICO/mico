package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.DependsOn;
import io.github.ust.mico.core.Service;
import io.github.ust.mico.core.ServiceInterface;
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


    public static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    public static final String PATH_VARIABLE_VERSION = "version";
    public static final String PATH_VARIABLE_ID = "id";
    private static final String PATH_VARIABLE_SERVICE_INTERFACE_NAME = "serviceInterfaceName";
    private static final String PATH_PART_INTERFACES = "/interfaces";

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

    //GET| /services/{shortName}/{version}/interface
    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES)
    public ResponseEntity<Resources<Resource<ServiceInterface>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<ServiceInterface> serviceInterfaces = serviceRepository.findInterfacesOfService(shortName, version);
        List<Resource<ServiceInterface>> serviceInterfaceResources = serviceInterfaces.stream().map(
                serviceInterface -> new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))
        ).collect(Collectors.toList());
        return ResponseEntity.ok(new Resources<>(serviceInterfaceResources, linkTo(methodOn(ServiceController.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<ServiceInterface>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                         @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<ServiceInterface> serviceInterfaceOptional = serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
        return serviceInterfaceOptional.map(serviceInterface ->
                new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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
        //Check if shortName and version combination already exists
        Optional<Service> serviceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion());
        Service serviceToCheck = serviceOptional.orElse(null);

        if (serviceToCheck != null) {
            return ResponseEntity.badRequest()
                    .header("Bad Request: Service already exists.")
                    .body(new Resource<>(newService, getServiceLinks(newService)));
        } else {
            if (newService.getDependsOn() == null) {
                Service savedService = serviceRepository.save(newService);

                return ResponseEntity
                        .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                        .body(new Resource<>(newService, getServiceLinks(newService)));
            } else {
                Service serviceWithDependees = setServiceDependees(newService);

                Service savedService = serviceRepository.save(serviceWithDependees);

                return ResponseEntity
                        .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                        .body(new Resource<>(newService, getServiceLinks(newService)));
            }
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

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<Service>> createNewDependee(@RequestBody Service newServiceDependee,
                                                               @PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                               @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        Service service = serviceOpt.get();
        List<DependsOn> dependees = service.getDependsOn();

        if (newServiceDependee.getDependsOn() == null) {
            Service savedDependeeService = serviceRepository.save(newServiceDependee);
            dependees.add(new DependsOn(service, savedDependeeService));
            service.setDependsOn(dependees);
            Service savedService = serviceRepository.save(service);

            return ResponseEntity
                    .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                    .body(new Resource<>(savedService, getServiceLinks(savedService)));
        } else {

        }
        return null;
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<Service>> deleteAllDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        Service service = serviceOpt.get();
        List<DependsOn> dependees = new LinkedList<>();
        service.setDependsOn(dependees);

        Service savedService = serviceRepository.save(service);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    //TODO: Doublecheck if wanted /services/{shortName}/{version}/dependees/{shortName}/{version}
    //@DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public void deleteDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName, @PathVariable(PATH_VARIABLE_VERSION) String version) {

    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependers")
    public ResponseEntity<Resources<Resource<Service>>> getDependers(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<Service> serviceList = serviceRepository.findAll();
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        Service serviceToLookFor = serviceOpt.get();

        List<Service> dependers = new LinkedList<>();

        serviceList.forEach(service -> {
            List<DependsOn> dependees = service.getDependsOn();
            if (dependees != null) {
                dependees.forEach(dependee -> {
                    if (dependee.getService().equals(serviceToLookFor)) {
                        dependers.add(dependee.getService());
                    }
                });
            }
        });

        List<Resource<Service>> resourceList = getServiceResourcesList(dependers);
        return ResponseEntity.ok(
                new Resources<>(resourceList,
                        linkTo(methodOn(ServiceController.class).getDependers(shortName, version)).withSelfRel()));

    }

    //Get the dependees of a service, check if they exists, if true get the ids and set the dependees
    public Service setServiceDependees(Service newService) {
        List<DependsOn> dependees = newService.getDependsOn();
        LinkedList<Service> services = getDependentServices(dependees);

        List<DependsOn> newDependees = new LinkedList<>();

        services.forEach(service -> {
            DependsOn dependsOnService = new DependsOn(newService, service);
            newDependees.add(dependsOnService);
        });

        newService.setDependsOn(newDependees);

        return newService;
    }

    private LinkedList<Service> getDependentServices(List<DependsOn> dependees) {
        LinkedList<Service> services = new LinkedList<>();

        dependees.forEach(dependee -> {
            String shortName = dependee.getServiceDependee().getShortName();
            String version = dependee.getServiceDependee().getVersion();

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

    private Iterable<Link> getServiceInterfaceLinks(ServiceInterface serviceInterface, String shortName, String version) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        return links;
    }

    private Iterable<Link> getServiceLinks(Service service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getServiceList()).withRel("services"));
        return links;
    }

}
