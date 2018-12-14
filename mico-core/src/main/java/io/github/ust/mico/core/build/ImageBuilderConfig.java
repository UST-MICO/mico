package io.github.ust.mico.core.build;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "image-builder")
@Getter
@Setter
public class ImageBuilderConfig {

    @NotBlank
    private String serviceAccountName;

    @NotBlank
    private String buildStepName;

    @NotBlank
    private String kanikoExecutorImageUrl;

    @NotBlank
    private String imageRepositoryUrl;

    // TODO Currently namespace `default` is used. Appropriate?
    @NotBlank
    @Setter
    private String buildExecutionNamespace = "default";
}
