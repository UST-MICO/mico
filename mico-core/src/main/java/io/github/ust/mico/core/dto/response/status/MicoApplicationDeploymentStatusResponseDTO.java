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

package io.github.ust.mico.core.dto.response.status;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus.Value;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoApplicationDeploymentStatus} intended to use with responses only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationDeploymentStatusResponseDTO {
	
    // ----------------------
    // -> Required Fields ---
    // ----------------------
	
    /**
     * The actual status {@link Value}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Value"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The actual status value.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Value value;
	
    
    // ----------------------
    // -> Optional Fields ---
    // ----------------------
    
    /**
     * Messages with more detailed information about the status.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Messages"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Messages with more detailed information about the status.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<MicoMessageResponseDTO> messages = new ArrayList<>();
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
   
    /**
     * Creates an instance of {@code MicoApplicationDeploymentStatusResponseDTO} based on a
     * {@code MicoApplicationDeploymentStatus}.
     *  
     * @param applicationDeploymentStatus the {@link MicoApplicationDeploymentStatus applicationDeploymentStatus}.
     */
	public MicoApplicationDeploymentStatusResponseDTO(MicoApplicationDeploymentStatus applicationDeploymentStatus) {
		this.value = applicationDeploymentStatus.getValue();
		this.messages = applicationDeploymentStatus.getMessages()
			.stream().map(MicoMessageResponseDTO::new)
		    .collect(Collectors.toList());
	}
    
}
