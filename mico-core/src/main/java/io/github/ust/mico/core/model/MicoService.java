package io.github.ust.mico.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ust.mico.core.VersionNotSupportedException;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * Represents a service in the context of MICO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class MicoService {

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
     * A brief name for the service intended
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
     * The version of this service. Refers to GitHub release tag.
     */
    @ApiModelProperty(required = true)
    private String version;

    /**
     * Human readable description of this service.
     */
    @ApiModelProperty(required = true)
    private String description;

    /**
     * The list of interfaces this service provides.
     */
    @ApiModelProperty(required = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Relationship(type = "PROVIDES", direction = Relationship.UNDIRECTED)
    @Singular
    private List<MicoServiceInterface> serviceInterfaces;

    /**
     * Indicates where this service originates from, e.g.,
     * GitHub (downloaded and built by MICO) or DockerHub
     * (ready-to-use image).
     */
    @ApiModelProperty(required = true)
    private MicoServiceCrawlingOrigin serviceCrawlingOrigin;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The list of services that this service requires
     * in order to run normally.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Relationship(type = "DEPENDS_ON")
    @Singular
    @Setter
    private List<MicoServiceDependency> dependencies;

    /**
     * Same MicoService with previous version.
     */
    @Relationship(type = "PREDECESSOR")
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

    /**
     * The URL to the root directory of, e.g., the
     * corresponding GitHub repository.
     */
    private String vcsRoot;

    /**
     * The relative (to vcsRoot) path to the Dockerfile.
     */
    @Pattern(regexp = "^(?!/.*$).*", message = "Path must be relative to vcsRoot")
    private String dockerfilePath;

    /**
     * The fully qualified URI to the image on DockerHub.
     * Either set after the image has been built by MICO
     * (if the service originates from GitHub) or set by the
     * user directly.
     */
    private String dockerImageUri;


    @JsonIgnore
    public MicoVersion getMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.version);
        return micoVersion;
    }

}
