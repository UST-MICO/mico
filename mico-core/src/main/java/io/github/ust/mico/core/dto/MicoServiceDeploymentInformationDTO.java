package io.github.ust.mico.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for deployment information of {@link io.github.ust.mico.core.model.MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoServiceDeploymentInformationDTO {

    /**
     * Name of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private String name;

    /**
     * shortName of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private String shortName;

    /**
     * Version of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private String version;

    /**
     * Counter for number of replicas that should be available.
     */
    private int requestedReplicas;

    /**
     * Counter for that are actually available.
      */
    private int availableReplicas;

    /**
     * Each item in this list represents a Kubernetes Service.
     */
    private List<MicoServiceInterfaceDTO> interfacesInformation = new ArrayList<>();

    /**
     * List of {@link io.fabric8.kubernetes.api.model.Pod}s of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private List<KubernetesPodInfoDTO> podInfo = new ArrayList<>();

}
