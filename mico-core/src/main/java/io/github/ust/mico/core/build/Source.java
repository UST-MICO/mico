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
public class Source {

    /**
     * Git represents source in a Git repository.
     */
    private GitSourceSpec git;

    // TODO GCSSourceSpec, Custom, SubPath, TargetPath
}
