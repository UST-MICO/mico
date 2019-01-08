package io.github.ust.mico.core.model;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Represents an application as a set of {@link MicoService}s
 * in the context of MICO.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoApplication {

    @Id
    @GeneratedValue
    private final long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The ids of the services this application is composed of.
     */
    // TODO: @Jan -> @Relationship?
    @ApiModelProperty(required = true)
    @Singular
    private final List<Long> services;

    /**
     * The information necessary for deploying this application.
     */
    // TODO: @Jan -> @Relationship?
    @ApiModelProperty(required = true)
    private final MicoApplicationDeploymentInfo deploymentInfo;

}
