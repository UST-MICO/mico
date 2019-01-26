package io.github.ust.mico.core.REST;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/applications", produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationController {

    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplication>>> getAllApplications() {
        List<MicoApplication> allApplications = applicationRepository.findAll();
        List<Resource<MicoApplication>> applicationResources = getApplicationResourceList(allApplications);

        return ResponseEntity.ok(
                new Resources<>(applicationResources,
                        linkTo(methodOn(ApplicationController.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return applicationOptional.map(application -> new Resource<>(application, getApplicationLinks(application)))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    private List<Resource<MicoApplication>> getApplicationResourceList(List<MicoApplication> applications) {
        return applications.stream().map(application -> new Resource<>(application, getApplicationLinks(application)))
                .collect(Collectors.toList());
    }

    private Iterable<Link> getApplicationLinks(MicoApplication application) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationController.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion().toString())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationController.class).getAllApplications()).withRel("applications"));
        return links;
    }

    @PostMapping
    public ResponseEntity<Resource<MicoApplication>> createApplication(@RequestBody MicoApplication newApplication) {
        Optional<MicoApplication> applicationOptional = applicationRepository.
                findByShortNameAndVersion(newApplication.getShortName(), newApplication.getVersion());
        if (applicationOptional.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        MicoApplication savedApplication = applicationRepository.save(newApplication);

        return ResponseEntity
                .created(linkTo(methodOn(ApplicationController.class)
                        .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
                .body(new Resource<>(savedApplication, getApplicationLinks(savedApplication)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                       @RequestBody MicoApplication application) {
        if (!application.getShortName().equals(shortName) || !application.getVersion().equals(version)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        application.setId(applicationOptional.get().getId());
        MicoApplication updatedApplication = applicationRepository.save(application);

        return ResponseEntity.ok(new Resource<>(updatedApplication, linkTo(methodOn(ApplicationController.class).updateApplication(shortName, version, application)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/")
    public ResponseEntity<Resources<Resource<MicoApplication>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        List<Resource<MicoApplication>> applicationResourceList = getApplicationResourceList(micoApplicationList);

        return ResponseEntity.ok(
            new Resources<>(applicationResourceList,
                linkTo(methodOn(ApplicationController.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/services")
    public ResponseEntity addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                  @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                  @RequestBody MicoService serviceFromBody) {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(serviceFromBody.getShortName(), serviceFromBody.getVersion());
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(applicationShortName, applicationVersion);
        if (serviceOptional.isPresent() && applicationOptional.isPresent()) {
            MicoService service = serviceOptional.get();
            MicoApplication application = applicationOptional.get();
            if (!application.getServices().contains(service)) {
                MicoApplication applicationWithService = application.toBuilder().service(service).build();
                applicationRepository.save(applicationWithService);
            }
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no such application/service");
        }
    }

}
