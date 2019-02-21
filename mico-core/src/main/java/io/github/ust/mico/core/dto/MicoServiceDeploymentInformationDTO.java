package io.github.ust.mico.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for deployment information of {@link io.github.ust.mico.core.model.MicoService}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoServiceDeploymentInformationDTO {

    // TODO: Add comments for fields.
    private String name;
    private String shortName;
    private String version;
    private int requestedReplicas;
    private int availableReplicas;
    private List<MicoServiceInterfaceDTO> interfacesInformation = new ArrayList<>();
    private List<KubernetesPodInfoDTO> podInfo = new ArrayList<>();

}
