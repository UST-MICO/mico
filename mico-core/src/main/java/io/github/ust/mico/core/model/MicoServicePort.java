package io.github.ust.mico.core.model;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoServicePort {

    /**
     * The id of this service port.
     */
    @JsonIgnore
    @Id
    @GeneratedValue
    private Long id;

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The port number of the externally exposed port.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Exposed Port Number"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The port number of the externally exposed port.")
        })})
    private int number;

    /**
     * The type (protocol) of the port
     * (Pivio -> transport_protocol).
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Type"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "The type (protocol) of the port. TCP or UDP.")
        })})
    private MicoPortType type = MicoPortType.DEFAULT;

    /**
     * The port inside the container.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Target Port Number"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The port inside the container.")
        })})
    private int targetPort;

}
