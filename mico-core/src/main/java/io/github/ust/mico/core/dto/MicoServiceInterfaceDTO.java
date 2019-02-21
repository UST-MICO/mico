package io.github.ust.mico.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a service in Kubernetes for a {@link io.github.ust.mico.core.model.MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoServiceInterfaceDTO {

    /**
     * Name of the {@link io.github.ust.mico.core.model.MicoServiceInterface}.
     */
    private String name;
    
}
