package io.github.ust.mico.core.mapping;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Value;

/**
 * Represents a service in the context of MICO.
 */
@Value
@Builder
@NodeEntity
public class MicoService {
	
	@Id
	@GeneratedValue
	private long id;

	
	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	// The name of the artifact. Intended for humans. 
	@ApiModelProperty(required = true)
	private String name;
	
	// A brief name for the service intended
	// for use as a unique identifier.
	@ApiModelProperty(required = true)
	private String shortName;
	
	// The version of this service.
	@ApiModelProperty(required = true)
	private String version;
	@ApiModelProperty(required = true)
	
	// Human readable description of this service.
	private String description;
	
	// The list of services that this service requires
	// in order to run normally.
	@Relationship
	private List<MicoServiceDependency> dependencies;
	
	// The list of interfaces this service provides.
	@Relationship
	@ApiModelProperty(required = true)
	private List<MicoServiceInterface> interfaces;
	
	// The URL to the root directory of, e.g., the
	// corresponding GitHub repository.
	@ApiModelProperty(required = true)
	private String vcsRoot;
	
	// The relative (to vcsRoot) path to the Dockerfile.
	@ApiModelProperty(required = true)
	private String dockerfilePath;

	
	// ----------------------
	// -> Optional fields ---
	// ----------------------
	
	private String contact;
	private String owner;

}
