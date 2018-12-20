package io.github.ust.mico.core.build;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.client.CustomResource;
import lombok.*;

import javax.annotation.Generated;

/**
 * Build represents a build of a container image. A Build is made up of a
 * source, and a set of steps. Steps can mount volumes to share data between
 * themselves. A build may be created by instantiating a BuildTemplate.
 *
 * Implemenation of the Build types:
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
@ToString(callSuper=true)
public class Build extends CustomResource {

    private static final String BUILD_API_VERSION = "build.knative.dev/v1alpha1";
    private static final String BUILD_KIND_NAME = "Build";

    private BuildSpec spec;
    private BuildStatus status;

    @Builder
    public Build(BuildSpec spec) {
        super.setKind(BUILD_KIND_NAME);
        super.setApiVersion(BUILD_API_VERSION);
        this.spec = spec;
    }

//    @Override
//    public String toString() {
//        return "Build{" +
//                "apiVersion='" + getApiVersion() + '\'' +
//                ", metadata=" + getMetadata() +
//                ", spec=" + spec +
//                '}';
//    }
}
