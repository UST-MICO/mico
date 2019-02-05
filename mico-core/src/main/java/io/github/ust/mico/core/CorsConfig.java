package io.github.ust.mico.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "cors-policy")
public class CorsConfig {


    private List<String> allowedOrigins;
    private List<String> additionalAllowedMethods;

}
