package io.github.ust.mico.core.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * Represents an application as a set of {@link MicoService}s
 * in the context of MICO.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoApplication {

    //TODO: add additional attributes

    /**
     * The id of this service.
     */
    @Id
    @GeneratedValue
    private final Long id;

    // ----------------------
    // -> Required fields ---
    // ----------------------

    // The ids of the services this application is composed of.
    @ApiModelProperty(required = true)
    @Singular
    @Relationship(type = "INCLUDES")
    private final List<MicoService> services;

    // The information necessary for deploying this application.
    @ApiModelProperty(required = true)
    private final MicoApplicationDeploymentInfo deploymentInfo;

}
