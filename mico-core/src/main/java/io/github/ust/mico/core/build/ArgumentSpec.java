package io.github.ust.mico.core.build;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ArgumentSpec {

    /**
     * Name is the name of the argument.
     */
    private String name;

    /**
     * Value is the value of the argument.
     */
    private String value;
}
