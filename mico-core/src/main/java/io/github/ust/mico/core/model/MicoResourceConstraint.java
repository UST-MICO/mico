package io.github.ust.mico.core.model;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a resource constraint specifying the CPU units
 * and memory. Can be used as a upper (limiting) and
 * lower (requesting) constraint.
 */
@Value
@Builder
public class MicoResourceConstraint {

    // Measured in CPU units. One Kubernetes CPU (unit) is equivalent to:
    //  - 1 AWS vCPU
    //  - 1 GCP Core
    //  - 1 Azure vCore
    //  - 1 IBM vCPU
    //  - 1 Hyperthread on a bare-metal Intel processor with Hyperthreading
    // Can also be specified as a fraction up to precision 0.001.
    private final double cpuUnits;

    // Memory in bytes.
    private final long memoryInBytes;

}
