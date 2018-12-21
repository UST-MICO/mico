package io.github.ust.mico.core.imagebuilder;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "image-builder")
@Getter
@Setter
public class ImageBuilderConfig {

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

    /**
     * the url to the image destination
     */
    @NotBlank
    private String imageRepositoryUrl;

    /**
     * the namespace in which the build will be executed
     */
    @NotBlank
    private String buildExecutionNamespace;
}
