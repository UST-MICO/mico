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
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.ImageBuildException;
import io.github.ust.mico.core.NotInitializedException;
import io.github.ust.mico.core.concurrency.MicoCoreBackgroundTaskFactory;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.mapping.KubernetesResourceException;
import io.github.ust.mico.core.mapping.MicoKubernetesClient;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        MicoApplication micoApplication = micoApplicationOptional.get();

        List<MicoService> micoServices = micoApplication.getServices();
        micoServices.forEach(micoService -> {
            // TODO Check if build is already running -> no build required
            // TODO Check if image for the requested version is already in docker registry -> no build required
            backgroundTaskFactory.runAsync(() -> buildImageAndWait(micoService), dockerImageUri -> {
                log.info("Build of image for service '{}' in version '{}' finished: {}",
                    micoService.getShortName(), micoService.getVersion(), dockerImageUri);

                micoService.setDockerImageUri(dockerImageUri);

                serviceRepository.save(micoService);
                try {
                    createKubernetesResources(micoApplication, micoService);
                } catch (KubernetesResourceException kre) {
                    log.error(kre.getMessage(), kre);
                    exceptionHandler(kre);
                }
            }, this::exceptionHandler);
        });

        return ResponseEntity.ok().build();
    }

    private String buildImageAndWait(MicoService micoService) {
        try {
            Build build = imageBuilder.build(micoService);
            String buildName = build.getMetadata().getName();

            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<Boolean> booleanCompletableFuture = imageBuilder.waitUntilBuildIsFinished(buildName);
            if (booleanCompletableFuture.get()) {
                return imageBuilder.createImageName(micoService.getShortName(), micoService.getVersion());
            } else {
                booleanCompletableFuture.cancel(true);
                throw new ImageBuildException("Build for service " + micoService.getShortName() + " failed");
            }
        } catch (NotInitializedException | InterruptedException | ExecutionException | ImageBuildException | TimeoutException e) {
            log.error(e.getMessage(), e);
            // TODO Handle NotInitializedException in async task properly
            return null;
        }
    }

    private void createKubernetesResources(MicoApplication micoApplication, MicoService micoService) throws KubernetesResourceException {
        log.debug("Start creating Kubernetes resources for MICO service '{}' in version '{}'", micoService.getShortName(), micoService.getVersion());

        // Kubernetes Deployment
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo();
        if (micoApplication.getDeploymentInfo() != null &&
            micoApplication.getDeploymentInfo().getServiceDeploymentInfos() != null) {
            micoServiceDeploymentInfo = micoApplication.getDeploymentInfo().getServiceDeploymentInfos().get(micoService.getId());
        }
        log.debug("Creating Kubernetes deployment for MICO service '{}' in version '{}'",
            micoService.getShortName(), micoService.getVersion());
        micoKubernetesClient.createMicoService(micoService, micoServiceDeploymentInfo);

        log.debug("Creating {} Kubernetes service(s) for MICO service '{}' in version '{}'",
            micoService.getServiceInterfaces().size(), micoService.getShortName(), micoService.getVersion());
        // Kubernetes Service(s)
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            micoKubernetesClient.createMicoServiceInterface(serviceInterface, micoService);
        }

        log.info("Created Kubernetes resources for MICO service '{}' in version '{}'",
            micoService.getShortName(), micoService.getVersion());
    }

    private Void exceptionHandler(Throwable e) {

        // TODO: Handle exceptions in async task properly, e.g., via message queue (RabbitMQ).
        // TODO: Also handle KubernetesResourceExceptions.

        log.error(e.getMessage(), e);
        return null;
    }

}
