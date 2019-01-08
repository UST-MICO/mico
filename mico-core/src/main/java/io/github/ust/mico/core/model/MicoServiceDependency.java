package io.github.ust.mico.core.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a dependency of a {@link MicoService}.
 */
@Data
@AllArgsConstructor
@Builder
@RelationshipEntity
public class MicoServiceDependency {

    /**
     * The id of this service dependency.
     */
    @Id
    @GeneratedValue
    private final long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The id of the depended service.
     */
    // TODO: serviceId needed? MicoService is linked via RelationshipEntity.
    // The id of the depended service.
    @ApiModelProperty(required = true)
    private final long serviceId;

    /**
     * The minimum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private final String minVersion;

    /**
     * The maximum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private final String maxVersion;

}
