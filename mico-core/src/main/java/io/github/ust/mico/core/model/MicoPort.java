package io.github.ust.mico.core.model;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.Value;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Value
public class MicoPort {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The port number.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Port Number"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The port number.")
        }
    )})
    private final int number;

    /**
     * The type (protocol) of the port.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Port Type"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The type (protocol) of the port.")
        }
    )})
    private final MicoPortType type = MicoPortType.DEFAULT;

}
