package io.github.ust.mico.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.neo4j.annotation.QueryResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a interface, e.g., REST API, of a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
@QueryResult
public class MicoServiceInterface {

    /**
     * The id of this service interface.
     */
    @JsonIgnore
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The name of this {@link MicoServiceInterface}.
     * Pattern is the same than the one for Kubernetes Service names.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Interface Name"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "pattern", value = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$"),
            @ExtensionProperty(name = "description", value = "The name of this MicoServiceInterface")
        })})
    @NotEmpty
    @Pattern(regexp="^[a-z]([-a-z0-9]*[a-z0-9])?$")
    private String serviceInterfaceName;

    /**
     * The list of ports.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Ports"),
            @ExtensionProperty(name = "x-order", value = "200"),
            @ExtensionProperty(name = "description", value = "The list of the interfaces ports")
        }
    )})
    @Relationship(type = "PROVIDES_PORTS", direction = Relationship.UNDIRECTED)
    private List<MicoServicePort> ports = new ArrayList<>();


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The public DNS.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "public DNS"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "The public DNS")
        }
    )})
    private String publicDns;

    /**
     * Human readable description of this service interface,
     * e.g., the functionality provided.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service interface")
        }
    )})
    private String description;

    /**
     * The protocol of this interface, e.g., HTTPS.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Protocol"),
            @ExtensionProperty(name = "x-order", value = "120"),
            @ExtensionProperty(name = "description", value = "The protocol of this interface")
        }
    )})
    private String protocol;

    /**
     * The transport protocol, e.g., TCP.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Transport Protocol"),
            @ExtensionProperty(name = "x-order", value = "130"),
            @ExtensionProperty(name = "description", value = "The transport protocol of this interface")
        }
    )})
    private String transportProtocol;

}
