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

import io.github.ust.mico.core.dto.request.MicoInterfaceConnectionRequestDTO;
import io.github.ust.mico.core.model.MicoInterfaceConnection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoInterfaceConnection} intended to use with responses only.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.ALWAYS)
public class MicoInterfaceConnectionResponseDTO extends MicoInterfaceConnectionRequestDTO {

	// Note: as soon as someone adds fields to this class, please check
	// whether Jackson requires this class to have a NoArgsConstructor,
	// if so, add the @NoArgsConstructor to this class.


    // -------------------
    // -> Constructors ---
    // -------------------

    /**
   	 * Creates an instance of {@code MicoInterfaceConnectionResponseDTO} based on a
   	 * {@code MicoInterfaceConnection}.
   	 *
   	 * @param interfaceConnection the {@link MicoInterfaceConnection}.
   	 */
	public MicoInterfaceConnectionResponseDTO(MicoInterfaceConnection interfaceConnection) {
		super(interfaceConnection);
	}

}
