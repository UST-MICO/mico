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

import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO for {@link MicoServiceDeploymentInfo} intended to use only with responses.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class MicoServiceDeploymentInfoResponseDTO extends MicoServiceDeploymentInfoRequestDTO {
	
	// Note: as soon as someone adds fields to this class, please add
	// @AllArgsConstructor to this class in order
	// to conform to the other DTOs.

	
    // -------------------
    // -> Constructors ---
    // -------------------
	
	/**
	 * TODO: Constructor comment.
	 * 
	 * @param application
	 */
	public MicoServiceDeploymentInfoResponseDTO(MicoServiceDeploymentInfo serviceDeploymentInfo) {
		super(serviceDeploymentInfo);
	}

//    /**
//     * Creates a {@code MicoServiceDeploymentInfoDTO} based on a
//     * {@code MicoServiceDeploymentInfo}.
//     *
//     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo} to use.
//     * @return a {@link MicoServiceDeploymentInfoResponseDTO} with all the values
//     * of the given {@code MicoServiceDeploymentInfo}.
//     */
//    public static MicoServiceDeploymentInfoResponseDTO valueOf(MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
//        return (MicoServiceDeploymentInfoResponseDTO) MicoServiceDeploymentInfoRequestDTO.valueOf(micoServiceDeploymentInfo);
//    }

}
