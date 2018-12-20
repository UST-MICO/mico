package io.github.ust.mico.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Component
@PropertySource("classpath:test.properties")
@ConfigurationProperties(prefix = "integration-tests")
@Getter
@Setter
public class IntegrationTestsConfig {

    /**
     * Kubernetes namespace name that should be used for the integration tests
     */
    @NotBlank
    private String kubernetesNamespaceName;

    /**
     * DockerHub username Base64 encoded
     */
    @NotBlank
    private String dockerHubUsernameBase64;

    /**
     * DockerHub password Base64 encoded
     */
    @NotBlank
    private String dockerHubPasswordBase64;
}
