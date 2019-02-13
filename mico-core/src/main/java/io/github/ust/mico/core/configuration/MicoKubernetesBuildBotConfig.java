package io.github.ust.mico.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

/**
 * Configuration of the build bot ({@link io.github.ust.mico.core.service.imagebuilder.ImageBuilder})
 */
@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "kubernetes.build-bot")
public class MicoKubernetesBuildBotConfig {

    /**
     * The name of the namespace in which the {@link io.github.ust.mico.core.service.imagebuilder.ImageBuilder}
     * builds the images.
     */
    @NotBlank
    private String namespaceBuildExecution;

    /**
     * The Docker image repository that is used by MICO to store the images
     * that are build by {@link io.github.ust.mico.core.service.imagebuilder.ImageBuilder}.
     */
    @NotBlank
    private String dockerImageRepositoryUrl;

    /**
     * The service account name to have write access to the specified docker image repository.
     */
    @NotBlank
    private String dockerRegistryServiceAccountName;

    /**
     * The url to the kaniko executor image that is used by
     * {@link io.github.ust.mico.core.service.imagebuilder.ImageBuilder}
     */
    @NotBlank
    private String kanikoExecutorImageUrl;

    /**
     * The timeout in seconds after which the build is stopped.
     */
    private int buildTimeout;
}
