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

    @NotBlank
    private String namespaceName;

    @NotBlank
    private String imageName;
}
