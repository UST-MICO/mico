package io.github.ust.mico.core.service.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * ArgumentSpec defines the actual values to use to populate a template's parameters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ArgumentSpec {

    /**
     * Name is the name of the argument
     */
    private String name;

    /**
     * Value is the value of the argument
     */
    private String value;
}
