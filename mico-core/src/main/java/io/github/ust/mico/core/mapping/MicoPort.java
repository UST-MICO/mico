package io.github.ust.mico.core.mapping;

import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Value
@Builder
@NodeEntity
public class MicoPort {

	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	// The port number.
	@ApiModelProperty(required = true)
	private int number;
	
	// The type (protocol) of the port.
	@ApiModelProperty(required = true)
	@Default
	private MicoPortType type = MicoPortType.DEFAULT;
	
}