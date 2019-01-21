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
    private ClusterSpec cluster;
    // private GoogleSpec google;
    // private Time startTime;
    // private Time completionTime;
    // private List<ContainerState> stepStatus;

    /**
     * Optional. StepsCompleted lists the name of build steps completed.
     */
    private List<String> stepsCompleted;

    // private BuildProvider builder;

    // private GoogleSpec google;

    // private Time startTime;

    // private Time completionTime;

    // private StepStates stepStates;

    // private Conditions conditions;
}
