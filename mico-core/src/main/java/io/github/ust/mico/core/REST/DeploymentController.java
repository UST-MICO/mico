package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.*;
import io.github.ust.mico.core.concurrency.MicoCoreBackgroundTaskFactory;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.mapping.MicoKubeClient;
import io.github.ust.mico.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

    @Autowired
    private MicoCoreBackgroundTaskFactory factory;

    @Autowired
    private MicoKubeClient micoKubeClient;

    @PostMapping
    public ResponseEntity<Void> deploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                       @PathVariable(PATH_VARIABLE_VERSION) String version) throws VersionNotSupportedException {
        try {
            imageBuilder.init();
        } catch (NotInitializedException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // TODO
        // Optional<MicoApplication> application = applicationRepository.findByShortNameAndVersion(shortName, version);
        Long serviceId = 1337L;
        Optional<MicoApplication> micoApplication = Optional.of(MicoApplication.builder()
            .shortName("MicoApplication")
            .deploymentInfo(MicoApplicationDeploymentInfo.builder()
                .serviceDeploymentInfo(serviceId, MicoServiceDeploymentInfo.builder()
                    .replicas(1)
                    .build())
                .build())
            .service(MicoService.builder()
                .id(serviceId)
                .shortName("hello")
                .name("hello-mico-service")
                .version(MicoVersion.valueOf("v1.0.0"))
                .vcsRoot("https://github.com/UST-MICO/hello.git")
                .dockerfilePath("Dockerfile")
                .build())
            .build());

        if (!micoApplication.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<MicoService> micoServices = micoApplication.get().getServices();
        micoServices.forEach(micoService -> {
            // TODO Check if image was already built -> no build required
            factory.runAsync(() -> buildImage(micoService), result -> {
                log.info("Build of image for service '{}' finished", micoService.getShortName());

                // TODO Set docker image uri
                // micoService.setDockerImageUri();
                // TODO save it to database
                //serviceRepository.save(micoService);

                log.debug("Create Kubernetes resources for MICO service '{}'", micoService.getShortName());
                int replicas = micoApplication.get().getDeploymentInfo().getServiceDeploymentInfos().get(micoService.getId()).getReplicas();
                micoKubeClient.createMicoService(micoService, replicas);
                log.info("Created Kubernetes resources for MICO service '{}'", micoService.getShortName());

            }, e -> exceptionHandler(e));
        });

        return ResponseEntity.ok().build();
    }

    private MicoService buildImage(MicoService micoService) {
        try {
            Build build = imageBuilder.build(micoService);

            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<Boolean> booleanCompletableFuture = imageBuilder.waitUntilBuildIsFinished(build);
            if (booleanCompletableFuture.get()) {
                return micoService;
            } else {
                throw new ImageBuildException("Build for service " + micoService.getShortName() + " failed");
            }
        } catch (NotInitializedException | InterruptedException | ExecutionException | ImageBuildException | TimeoutException e) {
            log.error(e.getMessage(), e);
            // TODO Handle NotInitializedException in async task properly
            return null;
        }
    }

    private Void exceptionHandler(Throwable e) {

        // TODO Handle exceptions in async task properly. E.g. via message queue (RabbitMQ)

        log.error(e.getMessage(), e);
        return null;
    }

}
