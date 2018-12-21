package io.github.ust.mico.core.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Represents the source of a volume to mount.
 * Only one of its members may be specified.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
@NoArgsConstructor
//@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VolumeSource {

    // Not implemented yet.
    // See https://github.com/knative/build/blob/9127bb7ec158b60da08dda6aa9081af98951f3bb/vendor/k8s.io/api/core/v1/types.go
}
