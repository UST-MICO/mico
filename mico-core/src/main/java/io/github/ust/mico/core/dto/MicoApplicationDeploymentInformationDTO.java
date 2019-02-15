package io.github.ust.mico.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO: Class comment.
public class MicoApplicationDeploymentInformationDTO {

    // TODO: Add comments for fields.
    private List<MicoServiceDeploymentInformationDTO> serviceDeploymentInformation = new ArrayList<MicoServiceDeploymentInformationDTO>();

}
