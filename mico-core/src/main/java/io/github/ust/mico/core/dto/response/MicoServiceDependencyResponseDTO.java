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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.request.MicoServiceDependencyRequestDTO;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoServiceDependency} intended to use with responses only.
 * Note that only the depended service is included compared to a
 * {@link MicoServiceDependency}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoServiceDependencyResponseDTO extends MicoServiceDependencyRequestDTO {
	
	// Note: as soon as someone adds fields to this class, please add
	// @AllArgsConstructor to this class in order
	// to conform to the other DTOs.
	
	
	// ----------------------
    // -> Required fields ---
    // ----------------------
	
	/**
     * The id of this service dependency.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "ID"),
            @ExtensionProperty(name = "x-order", value = "5"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "description", value = "The ID of this service dependency.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
     * Creates an instance of {@code MicoServiceDependencyResponseDTO} based on a
     * {@code MicoServiceDependency}.
     * 
     * @param serviceDependency the {@link MicoServiceDependency}.
     */
    public MicoServiceDependencyResponseDTO(MicoServiceDependency serviceDependency) {
    	super(serviceDependency);
    }

}
