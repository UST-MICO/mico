package io.github.ust.mico.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "kubernetes.prometheus")
public class PrometheusConfig {

    /**
     * The uri of the prometheus service
     */
    @NotBlank
    private String uri;
}
