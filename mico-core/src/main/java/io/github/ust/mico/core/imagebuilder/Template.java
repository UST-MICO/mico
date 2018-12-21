package io.github.ust.mico.core.imagebuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Template {

    /**
     * Optional. The Kind of the template to be used, possible values are BuildTemplate
     * or ClusterBuildTemplate. If nothing is specified, the default if is BuildTemplate
     */
    private String name;

    /**
     * Optional. The Kind of the template to be used, possible values are BuildTemplate
     * or ClusterBuildTemplate. If nothing is specified, the default if is BuildTemplate
     */
    private String kind;

    /**
     * Optional. Arguments, if specified, lists values that should be applied to the
     * parameters specified by the template.
     */
    private ArgumentSpec arguments;

    /**
     * Optional. Env, if specified will provide variables to all build template steps.
     * This will override any of the template's steps environment variables.
     */
    private EnvVar env;

}
