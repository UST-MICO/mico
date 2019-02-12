package io.github.ust.mico.core.model;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a dependency of a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id", scope=MicoService.class)
    @StartNode
    @EqualsAndHashCode.Exclude
    private MicoService service;

    /**
     * This is the {@link MicoService} depended by
     * {@link MicoServiceDependency#service}.
     */
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id", scope=MicoService.class)
    @ApiModelProperty(required = true)
    @EndNode
    @EqualsAndHashCode.Exclude
    private MicoService dependedService;

    /**
     * The minimum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private String minVersion;

    /**
     * The maximum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private String maxVersion;

    @JsonIgnore
    public MicoVersion getMinMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.minVersion);
        return micoVersion;
    }

    @JsonIgnore
    public MicoVersion getMaxMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.maxVersion);
        return micoVersion;
    }

}
