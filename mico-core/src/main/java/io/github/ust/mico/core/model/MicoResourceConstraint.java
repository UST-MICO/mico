package io.github.ust.mico.core.model;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.Value;

/**
 * Represents a resource constraint specifying the CPU units
 * and memory. Can be used as a upper (limiting) and
 * lower (requesting) constraint.
 */
@Value
public class MicoResourceConstraint {

    /**
     * Measured in CPU units. One Kubernetes CPU (unit) is equivaletnt to:
     * - 1 AWS vCPU
     * - 1 GCP Core
     * - 1 Azure vCore
     * - 1 IBM vCPU
     * - 1 Hyperthread on a bare-metal Intel processor with Hyperthreading
     * Can also be specified as a fraction up to precision 0.001.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Number Of CPU Units"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Measured in CPU units. One Kubernetes CPU (unit) is " +
                "equivalent to:\n- 1 AWS vCPU\n- 1 GCP Core\n- 1 Azure vCore\n- 1 IBM vCPU\n- 1 Hyperthread on a " +
                "bare-metal Intel processor with Hyperthreading\n Can also be specified as a fraction up to " +
                "precision 0.001.")
        }
    )})
    private final double cpuUnits;

    /**
     * Memory in bytes.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Memory"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Memory in bytes.")
        }
    )})
    private final long memoryInBytes;

}
