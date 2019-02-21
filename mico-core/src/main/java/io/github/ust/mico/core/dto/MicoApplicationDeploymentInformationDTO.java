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
 * DTO for deployment information of a {@link io.github.ust.mico.core.model.MicoApplication}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoApplicationDeploymentInformationDTO {

    /**
     * List of {@link MicoServiceDeploymentInformationDTO}, each object contains information of deployed {@link MicoService}
     */
    private List<MicoServiceDeploymentInformationDTO> serviceDeploymentInformation = new ArrayList<>();

}
