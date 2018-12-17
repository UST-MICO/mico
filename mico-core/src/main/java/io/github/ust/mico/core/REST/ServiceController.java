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
    private static final String PATH_DELETE_SHORT_NAME = "shortNameToDelete";
    private static final String PATH_DELETE_VERSION = "versionToDelete";

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
        if(!serviceOpt.isPresent())
            return ResponseEntity.notFound().build();
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
        if(!serviceOpt.isPresent())
            return ResponseEntity.notFound().build();
        Service service = serviceOpt.get();
        Service newService = getService(newServiceDependee);
        List<DependsOn> dependees = service.getDependsOn();

        if (newService == null) {
            newService = serviceRepository.save(newServiceDependee);
            dependees.add(new DependsOn(service, newService));
            Service savedService = serviceRepository.save(service);

            return ResponseEntity
                    .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                    .body(new Resource<>(savedService, getServiceLinks(savedService)));
        } else {
            if (newServiceDependee.getDependsOn() == null) {
                dependees.add(new DependsOn(service, newService));
                service.setDependsOn(dependees);
                Service savedService = serviceRepository.save(service);

                return ResponseEntity
                        .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                        .body(new Resource<>(savedService, getServiceLinks(savedService)));
            } else {
                Service serviceDependee = setServiceDependees(newServiceDependee);
                DependsOn serviceDependsOn = new DependsOn(service,serviceDependee);
                List<DependsOn> allServiceDependees = service.getDependsOn();
                if(allServiceDependees == null){
                    allServiceDependees = new LinkedList<>();
                }
                allServiceDependees.add(serviceDependsOn);
                service.setDependsOn(allServiceDependees);

                Service savedService = serviceRepository.save(service);

                return ResponseEntity
                        .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                        .body(new Resource<>(service, getServiceLinks(service)));
            }
        }
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<Service>> deleteAllDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if(!serviceOpt.isPresent())
            return ResponseEntity.notFound().build();
        Service service = serviceOpt.get();
        List<DependsOn> dependees = new LinkedList<>();
        service.setDependsOn(dependees);

        Service savedService = serviceRepository.save(service);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees"
            + "/{" + PATH_DELETE_SHORT_NAME + "}/{" + PATH_DELETE_VERSION + "}")
    public ResponseEntity<Resource<Service>> deleteDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                            @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                            @PathVariable(PATH_DELETE_SHORT_NAME) String shortNameToDelete,
                                                            @PathVariable(PATH_DELETE_VERSION) String versionToDelete) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if(!serviceOpt.isPresent())
            return ResponseEntity.notFound().build();
        Service service = serviceOpt.get();

        Optional<Service> serviceOptToDelete = serviceRepository.findByShortNameAndVersion(shortNameToDelete, versionToDelete);
        if(!serviceOptToDelete.isPresent())
            return ResponseEntity.notFound().build();
        Service serviceToDelete = serviceOptToDelete.get();

        List<DependsOn> newDependees = new LinkedList<>();
        List<DependsOn> dependees = service.getDependsOn();

        if(dependees != null) {
            dependees.forEach(dependsOn -> {
                if (dependsOn.getServiceDependee().getId() != serviceToDelete.getId()) {
                    newDependees.add(dependsOn);
                }
            });
        }

        service.setDependsOn(newDependees);
        Service savedService = serviceRepository.save(service);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependers")
    public ResponseEntity<Resources<Resource<Service>>> getDependers(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<Service> serviceList = serviceRepository.findAll();
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if(!serviceOpt.isPresent())
            return ResponseEntity.notFound().build();
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

    public Service getService(Service newService) {
        Optional<Service> serviceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion());
        Service serviceToCheck = serviceOptional.orElse(null);
        if (serviceToCheck == null) {
            return null;
        } else {
            return serviceToCheck;
        }
    }

    //Get the dependees of a service, check if they exists, if true get the ids and set the dependees
    public Service setServiceDependees(Service newService) {
        Service serviceToGetId = getService(newService);
        if(serviceToGetId == null){
            Service savedService = serviceRepository.save(new Service(newService.getShortName(),newService.getVersion()));

            List<DependsOn> dependees = savedService.getDependsOn();
            LinkedList<Service> services = getDependentServices(dependees);

            List<DependsOn> newDependees = new LinkedList<>();

            if(services != null){
                services.forEach(service -> {
                    DependsOn dependsOnService = new DependsOn(savedService, service);
                    newDependees.add(dependsOnService);
                });
            }

            savedService.setDependsOn(newDependees);

            return savedService;
        }else {
            newService.setId(serviceToGetId.getId());
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

    }

    private LinkedList<Service> getDependentServices(List<DependsOn> dependees) {
        if(dependees == null){
            return null;
        }

        LinkedList<Service> services = new LinkedList<>();

        dependees.forEach(dependee -> {
            String shortName = dependee.getServiceDependee().getShortName();
            String version = dependee.getServiceDependee().getVersion();

            Optional<Service> dependeeServiceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
            Service dependeeService = dependeeServiceOpt.orElse(null);
            if(dependeeService == null){
                Service newService = serviceRepository.save(new Service(shortName,version));
                services.add(newService);
            }else {
                services.add(dependeeService);
            }
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
