package io.github.ust.mico.core.build;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

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
     *
     * @param url New url of this Git repository.
     * @return The current url of this Git repository.
     */
    private String url;
    /**
     * Git revision (branch, tag, commit SHA or ref) to clone.  See
     * https://git-scm.com/docs/gitrevisions#_specifying_revisions for more
     * information.
     *
     * @param revision Target revision of this Git repository.
     * @return The Current revision of this Git repository.
     */
    private String revision;
}
