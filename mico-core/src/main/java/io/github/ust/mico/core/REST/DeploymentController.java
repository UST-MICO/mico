package io.github.ust.mico.core.REST;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ust.mico.core.ImageBuildException;
import io.github.ust.mico.core.NotInitializedException;
import io.github.ust.mico.core.concurrency.MicoCoreBackgroundTaskFactory;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.mapping.MicoKubernetesClient;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/applications/{shortName}/{version}/deploy", produces = MediaTypes.HAL_JSON_VALUE)
public class DeploymentController {
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private MicoCoreBackgroundTaskFactory backgroundTaskFactory;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @PostMapping
    public ResponseEntity<Void> deploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
        try {
            imageBuilder.init();
        } catch (NotInitializedException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);

        if (!micoApplicationOptional.isPresent()) {
            log.debug("MICO application with short name {} and version {} does not exist", shortName, version);
            return ResponseEntity.notFound().build();
        }
        MicoApplication micoApplication = micoApplicationOptional.get();

        List<MicoService> micoServices = micoApplication.getServices();
        micoServices.forEach(micoService -> {
            // TODO Check if image was already built -> no build required
            backgroundTaskFactory.runAsync(() -> buildImageAndWait(micoService), dockerImageUri -> {
                log.info("Build of image '{}' for service '{}' in version '{}' finished",
                    dockerImageUri, micoService.getShortName(), micoService.getVersion());

                MicoService micoServiceUpdatedWithImageName = micoService.toBuilder()
                    .dockerImageUri(dockerImageUri)
                    .build();

                serviceRepository.save(micoServiceUpdatedWithImageName);
                createKubernetesResources(micoApplication, micoServiceUpdatedWithImageName);
            }, this::exceptionHandler);
        });

        return ResponseEntity.ok().build();
    }

    private String buildImageAndWait(MicoService micoService) {
        try {
            Build build = imageBuilder.build(micoService);

            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<Boolean> booleanCompletableFuture = imageBuilder.waitUntilBuildIsFinished(build);
            if (booleanCompletableFuture.get()) {
                return imageBuilder.createImageName(micoService.getShortName(), micoService.getVersion());
            } else {
                throw new ImageBuildException("Build for service " + micoService.getShortName() + " failed");
            }
        } catch (NotInitializedException | InterruptedException | ExecutionException | ImageBuildException | TimeoutException e) {
            log.error(e.getMessage(), e);
            // TODO Handle NotInitializedException in async task properly
            return null;
        }
    }

    private void createKubernetesResources(MicoApplication micoApplication, MicoService micoService) {
        log.debug("Create Kubernetes resources for MICO service '{}'", micoService.getShortName());
        String applicationName = micoApplication.getShortName();
        
        // Kubernetes Deployment
        int replicas = micoApplication.getDeploymentInfo().getServiceDeploymentInfos().get(micoService.getId()).getReplicas();
        micoKubernetesClient.createMicoService(micoService, applicationName, replicas);
        
        // Kubernetes Service(s)
        micoService.getServiceInterfaces().forEach(serviceInterface -> {
            micoKubernetesClient.createMicoServiceInterface(serviceInterface, applicationName, micoService.getVersion());
        });
        
        log.info("Created Kubernetes resources for MICO service '{}'", micoService.getShortName());
    }

    private Void exceptionHandler(Throwable e) {

        // TODO Handle exceptions in async task properly. E.g. via message queue (RabbitMQ)

        log.error(e.getMessage(), e);
        return null;
    }

}
