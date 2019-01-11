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
import lombok.RequiredArgsConstructor;
import lombok.Singular;

/**
 * Represents a service in the context of MICO.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoService {

    /**
     * The id of this service.
     */
    @Id
    @GeneratedValue
    private final long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * A brief name for the service intended
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
     * The version of this service.
     */
    @ApiModelProperty(required = true)
    private final MicoVersion version;

    /**
     * Human readable description of this service.
     */
    @ApiModelProperty(required = true)
    private final String description;

    /**
     * The list of interfaces this service provides.
     */
    @ApiModelProperty(required = true)
    @Relationship // TODO: @Jan -> check please.
    @Singular
    private final List<MicoServiceInterface> serviceInterfaces;

    /**
     * The URL to the root directory of, e.g., the
     * corresponding GitHub repository.
     */
    @ApiModelProperty(required = true)
    private final String vcsRoot;

    /**
     * The relative (to vcsRoot) path to the Dockerfile.
     */
    @ApiModelProperty(required = true)
    private final String dockerfilePath;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The list of services that this service requires
     * in order to run normally.
     */
    @Relationship // TODO: @Jan -> check please.
    @Singular
    private List<MicoServiceDependency> dependencies;

    /**
     * Same MicoService with previous version.
     */
    @Relationship(type = "PREDECESSOR", direction = Relationship.OUTGOING)
    private MicoService predecessor;

    /**
     * Human readable contact information for support purposes.
     */
    private String contact;

    /**
     * Human readable information for the service owner
     * who is responsible for this service.
     */
    private String owner;

}
