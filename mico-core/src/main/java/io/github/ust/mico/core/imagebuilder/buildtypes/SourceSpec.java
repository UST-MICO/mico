package io.github.ust.mico.core.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * SourceSpec defines the input to the Build
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SourceSpec {

    /**
     * Optional. Git represents source in a Git repository.
     */
    private GitSourceSpec git;
}
