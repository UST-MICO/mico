package io.github.ust.mico.core.mapping;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Value;

/**
 * Represents an application as a set of {@link MicoService}s
 * in the context of MICO.
 */
@Value
@Builder
@NodeEntity
public class MicoApplication {
	
	@Id
	@GeneratedValue
	private long id;

	
	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	@ApiModelProperty(required = true)
	private List<Long> services;
	@ApiModelProperty(required = true)
	private MicoApplicationDeploymentInfo deploymentInfo;

}
