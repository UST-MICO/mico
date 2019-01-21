package io.github.ust.mico.core.mapping;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for {@link MicoKubeClient}.
 */
@Component
@ConfigurationProperties(prefix = "kubernetes")
public class KubernetesMappingConfig {

    /**
     * The name of the namespace MICO itself is running in.
     */
    @Getter
    @Setter
    @NotBlank
    private String namespaceMicoSystem;
    
    /**
     * The name of the namespace all services deployed
     * by MICO are running in.
     */
    @Getter
    @Setter
    @NotBlank
    private String namespaceMicoWorkspace;
    
    /**
     * The default image registry used by MICO.
     */
    @Getter
    @Setter
    @NotBlank
    private String defaultImageRegistry;

}
