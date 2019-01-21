package io.github.ust.mico.core.REST;

import javax.xml.ws.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ust.mico.core.ApplicationRepository;
import io.github.ust.mico.core.NotInitializedException;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/applications/{shortName}/{version}/deploy", produces = MediaTypes.HAL_JSON_VALUE)
@Slf4j
public class DeploymentController {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private ImageBuilder imageBuilder;

    @PostMapping
    public ResponseEntity<Void> deploy() {
        try {
            imageBuilder.init();
        } catch (NotInitializedException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        return ResponseEntity.ok().build();
    }

}
