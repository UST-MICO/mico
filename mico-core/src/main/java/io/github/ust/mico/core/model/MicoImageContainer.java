package io.github.ust.mico.core.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(required = true)
    private String image;

    /**
     * The list of ports for this service.
     */
    @ApiModelProperty(required = true)
    private List<MicoPort> ports = new ArrayList<>();


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The name of the container (in the Kubernetes Pod).
     * Defaults to Service#shortName.
     */
    @ApiModelProperty(required = true)
    private String name;

    /**
     * Limit describing the minimum amount of compute
     * resources allowed. If omitted it defaults to the
     * upper limit if that is explicitly specified.
     */
    private MicoResourceConstraint resourceLowerLimit;

    /**
     * Limit describing the maximum amount of compute
     * resources allowed.
     */
    private MicoResourceConstraint resourceUpperLimit;

    /**
     * Indicates whether this container should have
     * a read-only root file system. Defaults to false.
     */
    private boolean readOnlyRootFileSystem = false;

    /**
     * Indicates whether the service must run as a non-root user.
     * If somehow not run as non-root user (not UID 0) it will
     * fail to start. Default to false.
     */
    private boolean runAsNonRoot = false;

}
