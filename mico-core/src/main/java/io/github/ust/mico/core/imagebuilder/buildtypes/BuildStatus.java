package io.github.ust.mico.core.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * BuildStatus is the status for a Build resource
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BuildStatus {

    /**
     * Optional. Cluster provides additional information if the builder is Cluster.
     */
    ClusterSpec cluster;
    // GoogleSpec google;
    // Time startTime;
    // Time completionTime;
    // List<ContainerState> stepStatus;

    /**
     * Optional. StepsCompleted lists the name of build steps completed.
     */
    List<String> stepsCompleted;

    // BuildProvider builder;

    // GoogleSpec google;

    // time startTime;

    // Time completionTime;

    // StepStates stepStates;

    // Conditions conditions;
}
