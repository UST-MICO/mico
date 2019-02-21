package io.github.ust.mico.core.model;

import java.util.ArrayList;
import java.util.List;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a container running in a Kubernetes Pod.
 * Multiple containers can run in one pod.
 * For each container you can specify resource contraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoImageContainer {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The name of the docker image.
     * Defaults to Service#shortName.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Image Name"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The name of the docker image.")
        }
    )})
    private String image;

    /**
     * The list of ports for this service.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Ports"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The list of ports for this service.")
        }
    )})
    private List<MicoPort> ports = new ArrayList<>();


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The name of the container (in the Kubernetes Pod).
     * Defaults to Service#shortName.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Container Name"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "The name of the container (in the Kubernetes Pod).")
        }
    )})
    private String name;

    /**
     * Limit describing the minimum amount of compute
     * resources allowed. If omitted it defaults to the
     * upper limit if that is explicitly specified.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Resource Limit (minimum)"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Limit describing the minimum amount of compute " +
                "resources allowed. If omitted it defaults to the upper limit if that is explicitly specified.")
        }
    )})
    private MicoResourceConstraint resourceLowerLimit;

    /**
     * Limit describing the maximum amount of compute
     * resources allowed.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Resource Limit (maximum)"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "Limit describing the maximum amount of compute " +
                "resources allowed.")
        }
    )})
    private MicoResourceConstraint resourceUpperLimit;

    /**
     * Indicates whether this container should have
     * a read-only root file system. Defaults to false.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Read Only Root File System"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Indicates whether this container should have a " +
                "read-only root file system. Defaults to false.")
        }
    )})
    private boolean readOnlyRootFileSystem = false;

    /**
     * Indicates whether the service must run as a non-root user.
     * If somehow not run as non-root user (not UID 0) it will
     * fail to start. Default to false.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Run As Non Root"),
            @ExtensionProperty(name = "x-order", value = "70"),
            @ExtensionProperty(name = "description", value = "Indicates whether the service must run as a non-root " +
                "user.\n" +
                "If somehow not run as non-root user (not UID 0) it will fail to start. Default to false.")
        }
    )})
    private boolean runAsNonRoot = false;

}
