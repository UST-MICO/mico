package io.github.ust.mico.core.build;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BuildStatus {
    // BuildProvider builder;

    /**
     * Cluster provides additional information if the builder is Cluster.
     * +optional
     */
    ClusterSpec cluster;
    // GoogleSpec google;
    // Time startTime;
    // Time completionTime;
    // List<ContainerState> stepStatus;

    /**
     * StepsCompleted lists the name of build steps completed.
     * +optional
     */
    List<String> stepsCompleted;
    // Conditions conditions;
}
