/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.dto.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoEnvironmentVariable;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoEnvironmentVariable} intended to use with requests only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoEnvironmentVariableRequestDTO {

    /**
     * Name of the environment variable.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Name"),
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_ENV_VAR_NAME_REGEX),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Name of the environment variable.")
        }
    )})
    @NotEmpty
    @Pattern(regexp = Patterns.KUBERNETES_ENV_VAR_NAME_REGEX, message = Patterns.KUBERNETES_ENV_VAR_NAME_MESSAGE)
    private String name;

    /**
     * Value of the environment variable.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Value"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Value of the environment variable.")
        }
    )})
    private String value;


    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoEnvironmentVariableRequestDTO} based on a
     * {@code MicoEnvironmentVariable}.
     *
     * @param environmentVariable the {@link MicoEnvironmentVariable}.
     */
    public MicoEnvironmentVariableRequestDTO(MicoEnvironmentVariable environmentVariable) {
        this.name = environmentVariable.getName();
        this.value = environmentVariable.getValue();
    }

}
