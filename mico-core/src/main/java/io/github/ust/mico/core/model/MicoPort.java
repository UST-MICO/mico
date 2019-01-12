package io.github.ust.mico.core.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Value
@Builder
public class MicoPort {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The port number.
     */
    @ApiModelProperty(required = true)
    private final int number;

    /**
     * The type (protocol) of the port.
     */
    @ApiModelProperty(required = true)
    @Default
    private final MicoPortType type = MicoPortType.DEFAULT;

}