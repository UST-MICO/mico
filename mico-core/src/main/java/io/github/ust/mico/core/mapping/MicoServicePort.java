package io.github.ust.mico.core.mapping;

import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoServicePort {

	// ----------------------
	// -> Required fields ---
	// ----------------------

	// The port number of the externally exposed port.
	@ApiModelProperty(required = true)
	private int number;

	// The type (protocol) of the port
	// (Pivio -> transport_protocol).
	@ApiModelProperty(required = true)
	@Default
	private MicoPortType type = MicoPortType.DEFAULT;

	// The port of the container.
	@ApiModelProperty(required = true)
	private int targetPort;

}