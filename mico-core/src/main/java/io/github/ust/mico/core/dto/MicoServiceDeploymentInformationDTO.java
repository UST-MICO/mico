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
public class MicoServiceDeploymentInformationDTO {

    // TODO: Add comments for fields.
    
    private int requestedReplicas;
    private int availableReplicas;
    private List<MicoServiceInterfaceDTO> interfacesInformation = new ArrayList<MicoServiceInterfaceDTO>();
    private List<KubernetesPodInfoDTO> podInfo = new ArrayList<KubernetesPodInfoDTO>();

}
