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
 * Represents a interface, e.g., REST API, of a {@link MicoService}.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity // TODO: @Jan -> maybe @RelationshipEntity?
public class MicoServiceInterface {

    /**
     * The id of this service interface.
     */
    @Id
    @GeneratedValue
    private final long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The id of the parent service.
     */
    // TODO: serviceId needed? MicoService is already linked via PROVIDES-Relationship.
    // The id of the parent service.
    @ApiModelProperty(required = true)
    private final long serviceId;

    /**
     * The list of ports.
     */
    @ApiModelProperty(required = true)
    @Singular
    @Relationship
    // TODO: @Jan -> maybe Relationship?
    //TODO: @Jan -> add more info / annotation needed?
    private final List<MicoServicePort> ports;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The public DNS.
     */
    private String publicDns;

    /**
     * Human readable description of this service interface,
     * e.g., the functionality provided.
     */
    private String description;

    /**
     * The protocol of this interface, e.g., HTTPS.
     */
    private String protocol;

    /**
     * The transport protocol, e.g., TCP.
     */
    private String transportProtocol;

}
