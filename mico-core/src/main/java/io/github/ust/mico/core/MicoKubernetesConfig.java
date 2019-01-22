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
@ConfigurationProperties(prefix = "kubernetes")
public class MicoKubernetesConfig {

    /**
     * The name of the namespace MICO itself is running in.
     */
    @NotBlank
    private String namespaceMicoSystem;

    /**
     * The name of the namespace all services deployed
     * by MICO are running in.
     */
    @NotBlank
    private String namespaceMicoWorkspace;

    /**
     * The default image registry used by MICO.
     */
    @NotBlank
    private String imageRepositoryUrl;

}
