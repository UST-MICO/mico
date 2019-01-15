package io.github.ust.mico.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.neo4j.ogm.annotation.*;

/**
 * Represents a dependency of a {@link MicoService}.
 */
@Data
@AllArgsConstructor
@Builder
@RelationshipEntity(type = "DEPENDS_ON")
public class MicoServiceDependency {

    /**
     * The id of this service dependency.
     */
    @Id
    @GeneratedValue
    private final Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * 
     */
    @JsonIgnore
    @StartNode
    private MicoService service;

    /**
     *
     */
    @ApiModelProperty(required = true)
    @JsonIgnore
    @EndNode
    private MicoService dependedService;

    /**
     * The minimum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private final MicoVersion minVersion;

    /**
     * The maximum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private final MicoVersion maxVersion;

}
