package io.github.ust.mico.core.imagebuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "serviceAccountName",
    "template",
    "source",
    "sources",
    "steps"
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BuildSpec {

    /**
     * Workaround.
     * https://github.com/kubernetes/kubernetes/issues/58778
     */
    private double generation;

    /**
     * Steps are the steps of the build; each step is run sequentially with the
     * source mounted into /workspace.
     */
    @Singular
    private List<BuildStep> steps;

    /**
     * Optional. Template, if specified, references a BuildTemplate resource to use to
     * populate fields in the build, and optional Arguments to pass to the
     * template. The default Kind of template is BuildTemplate
     */
    private Template template;

    /**
     * Optional. Source specifies the inputs to the build.
     */
    private Source source;

    /**
     * Optional. Sources specifies the inputs to the build.
     */
    private List<Source> sources;

    /**
     * Optional. The name of the service account as which to run this build.
     */
    private String serviceAccountName;

    /**
     * Optional. Volumes is a collection of volumes that are available to mount into the
     * steps of the build.
     */
    @Singular
    private List<Volume> volumes;

    /**
     * Optional. Time after which the build times out. Defaults to 10 minutes.
     * Specified build timeout should be less than 24h.
     * Refer Go's ParseDuration documentation for expected format: https://golang.org/pkg/time/#ParseDuration
     */
    private String timeout;
}
