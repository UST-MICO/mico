package io.github.ust.mico.core.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * Represents an application as a set of {@link MicoService}s
 * in the context of MICO.
 */
@Data
@Builder
//@RequiredArgsConstructor
//@AllArgsConstructor
@NodeEntity
public class MicoApplication extends MicoService {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    // The ids of the services this application is composed of.
    @ApiModelProperty(required = true)
    @Singular
    @Relationship
    private final List<MicoService> services;

    // The information necessary for deploying this application.
    @ApiModelProperty(required = true)
    private final MicoApplicationDeploymentInfo deploymentInfo;

}
