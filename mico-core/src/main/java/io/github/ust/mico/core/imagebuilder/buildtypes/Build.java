package io.github.ust.mico.core.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.fabric8.kubernetes.client.CustomResource;
import lombok.*;

/**
 * Build represents a build of a container image.
 * A Build is made up of a source, and a set of steps. Steps can mount volumes to share data between themselves.
 * A build may be created by instantiating a BuildTemplate.
 * Implementation of the Build types:
 * https://github.com/knative/build/blob/9127bb7ec158b60da08dda6aa9081af98951f3bb/pkg/apis/build/v1alpha1/build_types.go#L107
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec"
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Build extends CustomResource {

    private static final String BUILD_API_VERSION = "build.knative.dev/v1alpha1";
    private static final String BUILD_KIND_NAME = "Build";

    /**
     * BuildSpec is the spec for a Build resource
     */
    private BuildSpec spec;

    /**
     * BuildStatus is the status for a Build resource
     */
    private BuildStatus status;

    @Builder
    public Build(BuildSpec spec) {
        super.setKind(BUILD_KIND_NAME);
        super.setApiVersion(BUILD_API_VERSION);
        this.spec = spec;
    }
}
