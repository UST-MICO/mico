package io.github.ust.mico.core.mapping;

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
	
	@Id
	@GeneratedValue
	private long id;
	

	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	@ApiModelProperty(required = true)
	private long serviceId;
	@ApiModelProperty(required = true)
	private String minVersion;
	@ApiModelProperty(required = true)
	private String maxVersion;

}
