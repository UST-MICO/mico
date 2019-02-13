package io.github.ust.mico.core.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

/**
 * Represents an application as a set of {@link MicoService}s
 * in the context of MICO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoApplication {

    /**
     * The id of this application.
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
    private String shortName;

    /**
     * The name of the artifact. Intended for humans.
     */
    @ApiModelProperty(required = true)
    private String name;

    /**
     * The version of this application.
     */
    @ApiModelProperty(required = true)
    private String version;

    /**
     * Human readable description of this application.
     */
    @ApiModelProperty(required = true)
    private String description;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The services this application is composed of.
     */
    @Singular
    @Relationship(type = "INCLUDES")
    private List<MicoService> services = new ArrayList<>();
    
    /**
     * The information necessary for deploying this application.
     */
    private MicoApplicationDeploymentInfo deploymentInfo = new MicoApplicationDeploymentInfo();

    /**
     * Human readable contact information for support purposes.
     */
    private String contact;

    /**
     * Human readable information for the application owner
     * who is responsible for this application.
     */
    private String owner;


    @JsonIgnore
    public MicoVersion getMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.version);
        return micoVersion;
    }

}
