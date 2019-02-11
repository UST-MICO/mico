package io.github.ust.mico.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiApplicationDeploymentInformation {

    public List<UiServiceDeploymentInformation> serviceDeploymentInformationList = new ArrayList<>();

}
