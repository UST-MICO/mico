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

import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO for {@link MicoServiceDeploymentInfo} intended to use with responses only.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoServiceDeploymentInfoResponseDTO extends MicoServiceDeploymentInfoRequestDTO {
	
    /**
     * Information about the actual Kubernetes resources created by a deployment.
     * Contains details about the used Kubernetes {@link Deployment} and {@link Service Services}.
     * Is read only.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Kubernetes Deployment Information"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Information about the actual Kubernetes resources " +
                "created by a deployment. Contains details about the used Kubernetes Deployment and Services.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private KubernetesDeploymentInfoResponseDTO kubernetesDeploymentInfo;

	
    // -------------------
    // -> Constructors ---
    // -------------------
	
	/**
     * Creates an instance of {@code MicoServiceDeploymentInfoResponseDTO} based on a
     * {@code MicoServiceDeploymentInfo}.
     * 
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}.
     */
	public MicoServiceDeploymentInfoResponseDTO(MicoServiceDeploymentInfo serviceDeploymentInfo) {
		super(serviceDeploymentInfo);
		
		// Labels need to be set explicitly to have a list of MicoLabelResponseDTOs
		// and not a list of MicoLabelRequestDTOs, since the list is declared
		// in MicoServiceDeploymentInfoRequestDTO and typed to MicoLabelRequestDTO.
		setLabels(serviceDeploymentInfo.getLabels().stream().map(MicoLabelResponseDTO::new).collect(Collectors.toList()));
		
		// Environment variables need to be set explicitly to have a list of MicoEnvironmentVariableResponseDTOs
		// and not a list of MicoEnvironmentVariableRequestDTOs, since the list is declared
		// in MicoServiceDeploymentInfoRequestDTO and typed to MicoEnvironmentVariableRequestDTO.
		setEnvironmentVariables(serviceDeploymentInfo.getEnvironmentVariables().stream().map(MicoEnvironmentVariableResponseDTO::new).collect(Collectors.toList()));
		
		// Kubernetes deployment info maybe null if not available
		if (serviceDeploymentInfo.getKubernetesDeploymentInfo() != null) {
			setKubernetesDeploymentInfo(new KubernetesDeploymentInfoResponseDTO(serviceDeploymentInfo.getKubernetesDeploymentInfo()));
		}
	}

}
