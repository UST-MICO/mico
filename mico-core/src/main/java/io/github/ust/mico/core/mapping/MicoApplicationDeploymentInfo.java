package io.github.ust.mico.core.mapping;

import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the information necessary for deploying
 * a {@link MicoApplication}.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoApplicationDeploymentInfo {
	
	@Id
	@GeneratedValue
	private long id;

	
	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	@ApiModelProperty(required = true)
	private Map<Long, MicoServiceDeploymentInfo> serviceDeploymentInfos; // service id -> service deployment info

}
