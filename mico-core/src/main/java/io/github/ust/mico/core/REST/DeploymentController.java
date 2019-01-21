package io.github.ust.mico.core.REST;

import java.util.Optional;

import javax.xml.ws.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ust.mico.core.Application;
import io.github.ust.mico.core.ApplicationRepository;
import io.github.ust.mico.core.NotInitializedException;
import io.github.ust.mico.core.ServiceRepository;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.model.MicoService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/applications/{shortName}/{version}/deploy", produces = MediaTypes.HAL_JSON_VALUE)
@Slf4j
public class DeploymentController {
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ImageBuilder imageBuilder;

    @PostMapping
    public ResponseEntity<Void> deploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
            @PathVariable(PATH_VARIABLE_VERSION) String version) {
        try {
            imageBuilder.init();
        } catch (NotInitializedException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Optional<Application> application = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!application.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        application.
        
        MicoService micoService = serviceRepository.findByShortNameAndVersion(shortName, version);
        Build build = imageBuilder.build(micoService);
        // TODO Use build resource (e.g. for response)
        
        return ResponseEntity.ok().build();
    }

}
