package io.github.ust.mico.core.build;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ClusterSpec {

    /**
     * Namespace is the namespace in which the pod is running.
     */
    String namespace;

    /**
     * PodName is the name of the pod responsible for executing this build's steps.
     */
    String podName;
}
