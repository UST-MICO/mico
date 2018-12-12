package io.github.ust.mico.core.mapping;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a interface, e.g., REST API, of a {@link MicoService}.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoServiceInterface {
	
	@Id
	@GeneratedValue
	private long id;
	
	
	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	// The id of the parent service.
	@ApiModelProperty(required = true)
	private long serviceId;
	
	// The list of ports.
	@ApiModelProperty(required = true)
	private List<MicoServicePort> ports;
	
	// ----------------------
	// -> Optional fields ---
	// ----------------------
	
	// Legacy from initial service model.
	private String publicDns;
	// Legacy from initial service model.
	private String description;
	// Legacy from initial service model.
	private String protocol;
	
}
