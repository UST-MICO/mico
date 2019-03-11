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

import io.github.ust.mico.core.dto.request.MicoServiceDependencyRequestDTO;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
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
    	super(serviceDependency);
    }

}
