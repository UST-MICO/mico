package io.github.ust.mico.core.service.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.*;

import java.util.List;

/**
 * A single application container that you want to run within a pod.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "name",
    "image",
    "args",
    "env",
    "command"
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BuildStep {

    /**
     * Name of the container specified as a DNS_LABEL.
     * Each container in a pod must have a unique name (DNS_LABEL).
     * Cannot be updated.
     */
    private String name;

    /**
     * Optional. Docker image name.
     * More info: https://kubernetes.io/docs/concepts/containers/images
     * This field is optional to allow higher level config management to default or override
     * container images in workload controllers like Deployments and StatefulSets.
     */
    private String image;

    /**
     * Optional. Entrypoint array. Not executed within a shell.
     * The docker image's ENTRYPOINT is used if this is not provided.
     * Variable references $(VAR_NAME) are expanded using the container's environment. If a variable
     * cannot be resolved, the reference in the input string will be unchanged. The $(VAR_NAME) syntax
     * can be escaped with a double $$, ie: $$(VAR_NAME). Escaped references will never be expanded,
     * regardless of whether the variable exists or not.
     * Cannot be updated.
     * More info: https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell
     */
    @Singular("command")
    private List<String> command;

    /**
     * Optional. Arguments to the entrypoint.
     * The docker image's CMD is used if this is not provided.
     * Variable references $(VAR_NAME) are expanded using the container's environment. If a variable
     * cannot be resolved, the reference in the input string will be unchanged. The $(VAR_NAME) syntax
     * can be escaped with a double $$, ie: $$(VAR_NAME). Escaped references will never be expanded,
     * regardless of whether the variable exists or not.
     * Cannot be updated.
     * More info: https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell
     */
    @Singular
    private List<String> args;

    /**
     * Optional. Container's working directory.
     * If not specified, the container runtime's default will be used, which
     * might be configured in the container image.
     * Cannot be updated.
     */
    private String workingDir;

    // private List<Port> ports;

    // private EnvForm envForm;

    /**
     * Optional. List of environment variables to set in the container.
     * Cannot be updated.
     */
    @Singular("env")
    private List<EnvVar> env;

    // private ResourceRequirements resources;

    // private List<VolumeMount> volumeMounts;

    // private List<VolumeDevice> volumeDevices;

    // private PullPolicy imagePullPolicy;

    // private SecurityContext securityContext;
}
