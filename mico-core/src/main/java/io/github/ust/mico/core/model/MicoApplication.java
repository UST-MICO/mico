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
import org.neo4j.ogm.annotation.Relationship;

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

    // TODO: @Jakob -> Do we want to link the DB object instead of the ID?
    // The ids of the services this application is composed of.
    @ApiModelProperty(required = true)
    @Singular
    @Relationship //TODO: @Jan -> add more info / annotation needed?
    private final List<Long> services;

    // The information necessary for deploying this application.
    @ApiModelProperty(required = true)
    @Relationship  //TODO: @Jan -> add more info / annotation needed?
    private final MicoApplicationDeploymentInfo deploymentInfo;

}
