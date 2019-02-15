package io.github.ust.mico.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiServiceDeploymentInformation {

    int requestedReplicas;
    int availableReplicas;
    List<UiExternalMicoInterfaceInformation> interfacesInformation;
    List<UiPodInfo> podInfo;

}
