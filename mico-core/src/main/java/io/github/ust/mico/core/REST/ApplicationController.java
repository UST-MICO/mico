package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.Application;
import io.github.ust.mico.core.ApplicationRepository;
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
@RequestMapping(value = "/applications", produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationController {
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private ApplicationRepository applicationRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<Application>>> getAllApplications() {
        List<Application> allApplications = applicationRepository.findAll();
        List<Resource<Application>> applicationResources = getApplicationResourceList(allApplications);

        return ResponseEntity.ok(
            new Resources<>(applicationResources,
                linkTo(methodOn(ApplicationController.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<Application>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Application> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return applicationOptional.map(application -> new Resource<>(application, getApplicationLinks(application)))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    private List<Resource<Application>> getApplicationResourceList(List<Application> applications) {
        return applications.stream().map(application -> new Resource<>(application, getApplicationLinks(application)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getApplicationLinks(Application application) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationController.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationController.class).getAllApplications()).withRel("applications"));
        return links;
    }

    @PostMapping
    public ResponseEntity<Resource<Application>> createApplication(@RequestBody Application newApplication) {
        Optional<Application> applicationOptional = applicationRepository.
            findByShortNameAndVersion(newApplication.getShortName(), newApplication.getVersion());
        if (applicationOptional.isPresent()) {
            return ResponseEntity.badRequest().build();
        } else {
            Application savedApplication = applicationRepository.save(newApplication);

            return ResponseEntity.created(linkTo(methodOn(ApplicationController.class)
                .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
                .body(new Resource<>(savedApplication, getApplicationLinks(savedApplication)));
        }

    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity deleteApplication (@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                             @PathVariable(PATH_VARIABLE_VERSION) String version){
        Optional<Application> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if(!applicationOptional.isPresent()){
            return ResponseEntity.notFound().build();
        } else {
            //only allow a deletion if the service has no dependees or dependers
            Application application = applicationOptional.get();
            //TODO: && getDependers(shortName,version) == null --> check for dependers missing
            if(application.getDependsOn() == null){
                applicationRepository.deleteApplicationByShortNameAndVersion(shortName,version);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
    }

}
