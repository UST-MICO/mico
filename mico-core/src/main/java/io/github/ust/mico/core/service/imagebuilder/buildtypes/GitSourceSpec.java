package io.github.ust.mico.core.service.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

/**
 * GitSourceSpec describes a Git repo source input to the Build.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "url",
    "revision"
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class GitSourceSpec {

    /**
     * URL of the Git repository to clone from.
     */
    private String url;

    /**
     * Git revision (branch, tag, commit SHA or ref) to clone.
     * See https://git-scm.com/docs/gitrevisions#_specifying_revisions for more information.
     */
    private String revision;
}
