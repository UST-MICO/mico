package io.github.ust.mico.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

/**
 * Configuration that includes information about the MICO Kubernetes cluster
 */
@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "kubernetes.build-bot")
public class MicoKubernetesBuildBotConfig {

    /**
     * The name of the namespace in which the {@link io.github.ust.mico.core.imagebuilder.ImageBuilder}
     * builds the images.
     */
    @NotBlank
    private String namespaceBuildExecution;

    /**
     * the service account name to access a docker registry
     */
    @NotBlank
    private String serviceAccountName;

    /**
     * the url to the kaniko executor image
     */
    @NotBlank
    private String kanikoExecutorImageUrl;

}
