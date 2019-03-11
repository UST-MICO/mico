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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoApplication;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoApplication} intended to use with responses only.
 * Additionally includes all of services of the application.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationWithServicesResponseDTO extends MicoApplicationResponseDTO {

    /**
     * All services of the application as {@link MicoServiceResponseDTO}.
     */
    @ApiModelProperty(extensions = {@Extension(
            name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
            properties = {
                @ExtensionProperty(name = "title", value = "Services"),
                @ExtensionProperty(name = "x-order", value = "100"),
                @ExtensionProperty(name = "description", value = "All services of the application.")
            }
        )})
    private List<MicoServiceResponseDTO> services = new ArrayList<>();
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
     * Creates an instance of {@code MicoApplicationWithServicesResponseDTO} based on a
     * {@code MicoApplication}. Note that the
     * deployment status is not set since it cannot be
     * inferred from the {@code MicoApplication} itself
     *  
     * @param application the {@link MicoApplication}.
     */
	public MicoApplicationWithServicesResponseDTO(MicoApplication application) {
		super(application);
		services = application.getServiceDeploymentInfos().stream()
        	.map(sdi -> sdi.getService())
        	.map(service -> new MicoServiceResponseDTO(service))
        	.collect(Collectors.toList());
	}
    
	/**
     * Creates an instance of {@code MicoApplicationWithServicesResponseDTO} based on a
     * {@code MicoApplication} and a {@code MicoApplicationDeploymentStatus}.
     *  
     * @param application the {@link MicoApplication}.
     * @param deploymentStatus the {@link MicoApplicationDeploymentStatus}. 
     */
	public MicoApplicationWithServicesResponseDTO(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus) {
		this(application);
		setDeploymentStatus(deploymentStatus);
	}

}
