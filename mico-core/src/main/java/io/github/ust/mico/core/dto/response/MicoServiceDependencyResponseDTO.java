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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoServiceDependency}. Note that
 * only the depended service is included compared to
 * {@link MicoServiceDependency}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoServiceDependencyResponseDTO {
	
    // ----------------------
    // -> Required fields ---
    // ----------------------

	/**
     * The depended service {@link MicoServiceResponseDTO}.
     */
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    private MicoServiceResponseDTO dependedService;

    /**
     * The minimum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private String minVersion;

    /**
     * The maximum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private String maxVersion;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
     * Creates an instance of this DTO based on a
     * {@code MicoServiceDeploymentInfo}.
     * 
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}.
     */
    public MicoServiceDependencyResponseDTO(MicoServiceDependency serviceDependency) {
    	this.dependedService = new MicoServiceResponseDTO(serviceDependency.getDependedService());
    	this.minVersion = serviceDependency.getMinVersion();
    	this.maxVersion = serviceDependency.getMaxVersion();
    }

}
