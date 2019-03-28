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

package io.github.ust.mico.core.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.request.MicoApplicationRequestDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus.Value;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoApplication} intended to use with responses only. Note that neither the services nor their
 * deployment information is included. Contains the current deployment status of this application (may be unknown).
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoApplicationResponseDTO extends MicoApplicationRequestDTO {

    // ----------------------
    // -> Optional Fields ---
    // ----------------------

    /**
     * Indicates whether the {@link MicoApplication} is currently deployed. Default is {@link Value#UNDEPLOYED Undeployed}.
     * Is read only and will be updated by the backend at every request.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Deployment Status"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "220"),
            @ExtensionProperty(name = "description", value = "Holds the current deployment status of this application.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MicoApplicationDeploymentStatus deploymentStatus = new MicoApplicationDeploymentStatus(Value.UNDEPLOYED);


    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoApplicationResponseDTO} based on a {@code MicoApplication}. Note that the
     * deployment status is not set since it cannot be inferred from the {@code MicoApplication} itself
     *
     * @param application the {@link MicoApplication}.
     */
    public MicoApplicationResponseDTO(MicoApplication application) {
        super(application);
    }

    /**
     * Creates an instance of {@code MicoApplicationResponseDTO} based on a {@code MicoApplication} and a {@code
     * MicoApplicationDeploymentStatus}.
     *
     * @param application      the {@link MicoApplication}.
     * @param deploymentStatus the {@link MicoApplicationDeploymentStatus}.
     */
    public MicoApplicationResponseDTO(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus) {
        super(application);
        this.deploymentStatus = deploymentStatus;
    }
}
