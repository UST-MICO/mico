package io.github.ust.mico.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.ust.mico.core.model.MicoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for deployment information of a {@link io.github.ust.mico.core.model.MicoApplication}.
 * A list contains all {@link MicoService} the {@link io.github.ust.mico.core.model.MicoApplication} consists of.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoApplicationDeploymentInformationDTO {

    /**
     * List of deployment information of {@link MicoService}s, which belong to a {@link io.github.ust.mico.core.model.MicoApplication}.
     */
    private List<MicoServiceDeploymentInformationDTO> serviceDeploymentInformation = new ArrayList<>();

}
