package io.github.ust.mico.core.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Value
@Builder
@NodeEntity
public class MicoServicePort {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    // The port number of the externally exposed port.
    @ApiModelProperty(required = true)
    private final int number;

    // The type (protocol) of the port
    // (Pivio -> transport_protocol).
    @ApiModelProperty(required = true)
    @Default
    private final MicoPortType type = MicoPortType.DEFAULT;

    // The port of the container.
    @ApiModelProperty(required = true)
    private final int targetPort;

}
