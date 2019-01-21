package io.github.ust.mico.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
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
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * This is the {@link MicoService} that requires (depends on)
     * the {@link MicoServiceDependency#dependedService}.
     */
    @JsonIgnore
    @StartNode
    private MicoService service;

    /**
     * This is the {@link MicoService} dependend by
     * {@link MicoServiceDependency#service}.
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

    @JsonProperty("serviceDependee")
    private MicoService getDependee() {
        MicoService dependee = this.dependedService;
        dependee.setDependencies(null);
        return dependee;
    }

}
