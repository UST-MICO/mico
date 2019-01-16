package io.github.ust.mico.core.model;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

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

    /**
     * The id of this service.
     */
    @Id
    @GeneratedValue
    private Long id;

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * A brief name for the application intended
     * for use as a unique identifier.
     */
    @ApiModelProperty(required = true)
    private final String shortName;

    /**
     * The name of the artifact. Intended for humans.
     */
    @ApiModelProperty(required = true)
    private final String name;

    /**
     * The version of this application.
     */
    @ApiModelProperty(required = true)
    private final MicoVersion version;

    /**
     * Human readable description of this application.
     */
    @ApiModelProperty(required = true)
    private final String description;

    /**
     * The services this application is composed of.
     */
    @ApiModelProperty(required = true)
    @Singular
    @Relationship(type = "INCLUDES")
    private final List<MicoService> services;

    /**
     * The information necessary for deploying this application.
     */
    @ApiModelProperty(required = true)
    private final MicoApplicationDeploymentInfo deploymentInfo;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * Human readable contact information for support purposes.
     */
    private String contact;

    /**
     * Human readable information for the application owner
     * who is responsible for this application.
     */
    private String owner;

}
