package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.GitHubCrawler;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

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
    public static final String PATH_DELETE_SHORT_NAME = "shortNameToDelete";
    public static final String PATH_DELETE_VERSION = "versionToDelete";
    public static final String PATH_VARIABLE_IMPORT = "import";
    public static final String PATH_VARIABLE_GITHUB = "github";

    @Autowired
    private MicoServiceRepository serviceRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoService>>> getServiceList() {
        List<MicoService> services = serviceRepository.findAll();
        List<Resource<MicoService>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
            new Resources<>(serviceResources,
                linkTo(methodOn(ServiceController.class).getServiceList()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    //TODO Add validation to path variables
    public ResponseEntity<Resource<MicoService>> getServiceByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        return serviceOpt.map(service -> new Resource<>(service, getServiceLinks(service)))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoService>> updateService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                               @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                               @RequestBody MicoService service) {
        if (!service.getShortName().equals(shortName) || !service.getVersion().equals(version)) {
            return ResponseEntity.badRequest().build();
        } else {
            Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
            if (!serviceOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            } else {
                service.setId(serviceOpt.get().getId());
                MicoService updatedService = serviceRepository.save(service);

                return ResponseEntity.ok(new Resource<>(updatedService,
                    linkTo(methodOn(ServiceController.class).updateService(shortName, version, service)).withSelfRel()));
            }
        }
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Void> deleteService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MicoService service = serviceOpt.get();

        if (getDependers(service).isEmpty()) {
            serviceRepository.deleteServiceByShortNameAndVersion(shortName, version);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/")
    public ResponseEntity<Resources<Resource<MicoService>>> getVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoService> services = serviceRepository.findByShortName(shortName);
        List<Resource<MicoService>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
            new Resources<>(serviceResources,
                linkTo(methodOn(ServiceController.class).getVersionsOfService(shortName)).withSelfRel()));
    }

    //TODO: Ambiguous endpoint with /services/shortName
    //@GetMapping("/{" + PATH_VARIABLE_ID + "}/")
    public ResponseEntity<Resource<MicoService>> getServiceById(@PathVariable(PATH_VARIABLE_ID) Long id) {
        Optional<MicoService> serviceOpt = serviceRepository.findById(id);

        return serviceOpt.map(service -> new Resource<>(service, getServiceLinks(service)))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Resource<MicoService>> createService(@RequestBody MicoService newService) {
        //Check if shortName and version combination already exists
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion().toString());
        MicoService serviceToCheck = serviceOptional.orElse(null);

        if (serviceToCheck != null) {
            return ResponseEntity.badRequest().build();
        } else {
            MicoService savedService = serviceRepository.save(newService);

            return ResponseEntity
                .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(newService, getServiceLinks(newService)));
        }
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resources<Resource<MicoService>>> getDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        MicoService service = serviceOpt.get();

        List<MicoServiceDependency> dependees = service.getDependencies();
        if (dependees == null) {
            return ResponseEntity.notFound().build();
        }

        LinkedList<MicoService> services = getDependentServices(dependees);

        List<Resource<MicoService>> resourceList = getServiceResourcesList(services);

        return ResponseEntity.ok(
            new Resources<>(resourceList,
                linkTo(methodOn(ServiceController.class).getDependees(shortName, version)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<MicoService>> createNewDependee(@RequestBody MicoServiceDependency newServiceDependee,
                                                                   @PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                   @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MicoService service = serviceOpt.get();


        Optional<MicoService> serviceDependeeOpt = serviceRepository.findByShortNameAndVersion(newServiceDependee.getDependedService().getShortName(),
            newServiceDependee.getDependedService().getVersion().toString());

        if (!serviceDependeeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        newServiceDependee.setService(service);
        newServiceDependee.setDependedService(serviceDependeeOpt.get());

        List<MicoServiceDependency> dependees = service.getDependencies();
        if (dependees == null) {
            dependees = new LinkedList<>();
        }
        dependees.add(newServiceDependee);
        service.setDependencies(dependees);

        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
            .body(new Resource<>(service, getServiceLinks(service)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<MicoService>> deleteAllDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                    @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MicoService service = serviceOpt.get();
        List<MicoServiceDependency> dependees = new LinkedList<>();
        service.setDependencies(dependees);

        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
            .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees"
        + "/{" + PATH_DELETE_SHORT_NAME + "}/{" + PATH_DELETE_VERSION + "}")
    public ResponseEntity<Resource<MicoService>> deleteDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                @PathVariable(PATH_DELETE_SHORT_NAME) String shortNameToDelete,
                                                                @PathVariable(PATH_DELETE_VERSION) String versionToDelete) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MicoService service = serviceOpt.get();

        Optional<MicoService> serviceOptToDelete = serviceRepository.findByShortNameAndVersion(shortNameToDelete, versionToDelete);
        if (!serviceOptToDelete.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MicoService serviceToDelete = serviceOptToDelete.get();

        List<MicoServiceDependency> newDependees = new LinkedList<>();
        List<MicoServiceDependency> dependees = service.getDependencies();

        if (dependees != null) {
            dependees.forEach(dependsOn -> {
                if (dependsOn.getDependedService().getId() != serviceToDelete.getId()) {
                    newDependees.add(dependsOn);
                }
            });
        }

        service.setDependencies(newDependees);
        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
            .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependers")
    public ResponseEntity<Resources<Resource<MicoService>>> getDependers(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        MicoService serviceToLookFor = serviceOpt.get();

        List<MicoService> dependers = getDependers(serviceToLookFor);

        List<Resource<MicoService>> resourceList = getServiceResourcesList(dependers);
        return ResponseEntity.ok(
            new Resources<>(resourceList,
                linkTo(methodOn(ServiceController.class).getDependers(shortName, version)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_IMPORT + "}/{" + PATH_VARIABLE_GITHUB + "}")
    public ResponseEntity<Resource<MicoService>> importMicoServiceFromGitHub(@RequestBody String url) {
        // Crawl information from GitHub and create new MicoService
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();
        GitHubCrawler crawler = new GitHubCrawler(restTemplate);
        MicoService newService = crawler.crawlGitHubRepoLatestRelease(url);

        //Check if shortName and version combination already exists
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion().toString());
        MicoService serviceToCheck = serviceOptional.orElse(null);

        if (serviceToCheck != null) {
            return ResponseEntity.badRequest().build();
        } else {
            MicoService savedService = serviceRepository.save(newService);

            return ResponseEntity
                .created(linkTo(methodOn(ServiceController.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(newService, getServiceLinks(newService)));
        }
    }

    public List<MicoService> getDependers(MicoService serviceToLookFor) {
        List<MicoService> serviceList = serviceRepository.findAll();

        List<MicoService> dependers = new LinkedList<>();

        serviceList.forEach(service -> {
            List<MicoServiceDependency> dependees = service.getDependencies();
            if (dependees != null) {
                dependees.forEach(dependee -> {
                    if (dependee.getDependedService().equals(serviceToLookFor)) {
                        dependers.add(dependee.getService());
                    }
                });
            }
        });

        return dependers;
    }

    public MicoService getService(MicoService newService) {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion().toString());
        MicoService serviceToCheck = serviceOptional.orElse(null);
        if (serviceToCheck == null) {
            return null;
        } else {
            return serviceToCheck;
        }
    }

    //Get the dependees of a service, check if they exists, if true get the ids and set the dependees
    public MicoService setServiceDependees(MicoService newService) {
        MicoService serviceToGetId = getService(newService);
        if (serviceToGetId == null) {
            MicoService savedService = serviceRepository.save(MicoService.builder().shortName(newService.getShortName()).version(newService.getVersion()).build());

            List<MicoServiceDependency> dependees = savedService.getDependencies();
            LinkedList<MicoService> services = getDependentServices(dependees);

            List<MicoServiceDependency> newDependees = new LinkedList<>();

            if (services != null) {
                services.forEach(service -> newDependees.add(MicoServiceDependency.builder().service(savedService).dependedService(service).build()));
            }

            savedService.setDependencies(newDependees);

            return savedService;
        } else {
            newService.setId(serviceToGetId.getId());
            List<MicoServiceDependency> dependees = newService.getDependencies();
            LinkedList<MicoService> services = getDependentServices(dependees);

            List<MicoServiceDependency> newDependees = new LinkedList<>();

            services.forEach(service -> newDependees.add(MicoServiceDependency.builder().service(newService).dependedService(service).build()));

            newService.setDependencies(newDependees);

            return newService;
        }
    }

    private LinkedList<MicoService> getDependentServices(List<MicoServiceDependency> dependees) {
        if (dependees == null) {
            return null;
        }

        LinkedList<MicoService> services = new LinkedList<>();

        dependees.forEach(dependee -> {
            String shortName = dependee.getDependedService().getShortName();
            String version = dependee.getDependedService().getVersion();

            Optional<MicoService> dependeeServiceOpt = serviceRepository.findByShortNameAndVersion(shortName, version.toString());
            MicoService dependeeService = dependeeServiceOpt.orElse(null);
            if (dependeeService == null) {
                MicoService newService = serviceRepository.save(MicoService.builder().shortName(shortName).version(version).build());
                services.add(newService);
            } else {
                services.add(dependeeService);
            }
        });

        return services;
    }

    private List<Resource<MicoService>> getServiceResourcesList(List<MicoService> services) {
        return services.stream().map(service -> new Resource<>(service, getServiceLinks(service)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getServiceLinks(MicoService service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion().toString())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getServiceList()).withRel("services"));
        return links;
    }

}
