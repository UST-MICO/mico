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
import io.github.ust.mico.core.model.MicoInterfaceConnection;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for the information needed to connect a {@link MicoService} to an {@link MicoServiceInterface}
 * of another {@link MicoService} intended to use with requests only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoInterfaceConnectionRequestDTO {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * Name of the environment variable that is used to set the fully qualified name of an interface.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Environment Variable Name"),
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_ENV_VAR_NAME_REGEX),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Name of the environment variable " +
                "that is used to set the fully qualified name of an interface.")
        }
    )})
    @NotEmpty
    @Pattern(regexp = Patterns.KUBERNETES_ENV_VAR_NAME_REGEX, message = Patterns.KUBERNETES_ENV_VAR_NAME_MESSAGE)
    private String environmentVariableName;

    /**
     * Name of the {@link MicoServiceInterface} of another {@link MicoService}.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Interface Name"),
            @ExtensionProperty(name = "minLength", value = "1"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Name of the MicoServiceInterface of another MicoService.")
        }
    )})
    @NotEmpty
    private String micoServiceInterfaceName;

    /**
     * Name of the {@link MicoService}.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Short Name"),
            @ExtensionProperty(name = "minLength", value = "1"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Name of the MicoService.")
        }
    )})
    @NotEmpty
    private String micoServiceShortName;



    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoInterfaceConnectionRequestDTO} based on a
     * {@code MicoInterfaceConnection}.
     *
     * @param interfaceConnection the {@link MicoInterfaceConnection}.
     */
    public MicoInterfaceConnectionRequestDTO(MicoInterfaceConnection interfaceConnection) {
        this.environmentVariableName = interfaceConnection.getEnvironmentVariableName();
        this.micoServiceInterfaceName = interfaceConnection.getMicoServiceInterfaceName();
        this.micoServiceShortName = interfaceConnection.getMicoServiceShortName();
    }
}
