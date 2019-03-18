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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.KubernetesDeploymentInfo;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for {@link KubernetesDeploymentInfo} intended to use with responses only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class KubernetesDeploymentInfoResponseDTO {
	
	/**
     * The namespace in which the Kubernetes {@link Deployment} is created.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Namespace"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The namespace in which the Kubernetes Deployment is created.")
        }
    )})
    private String namespace;

    /**
     * The name of the Kubernetes {@link Deployment} created by a {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Deployment Name"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The name of the Kubernetes Deployment " +
                "created by a MicoService.")
        }
    )})
    private String deploymentName;

    /**
     * The names of the Kubernetes {@link Service Services}
     * created by {@link MicoServiceInterface MicoServiceInterfaces}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Names"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "The names of the Kubernetes Services " +
                "created by MicoServiceInterfaces.")
        }
    )})
    private List<String> serviceNames = new ArrayList<>();

    
    // -------------------
    // -> Constructors ---
    // -------------------

    /**
	 * Creates an instance of {@code KubernetesDeploymentInfoResponseDTO} based on a
	 * {@code KubernetesDeploymentInfo}.
	 * 
	 * @param kubernetesDeploymentInfo the {@link KubernetesDeploymentInfo}.
	 */
	public KubernetesDeploymentInfoResponseDTO(KubernetesDeploymentInfo kubernetesDeploymentInfo) {
		this.namespace = kubernetesDeploymentInfo.getNamespace();
		this.deploymentName = kubernetesDeploymentInfo.getDeploymentName();
		this.serviceNames = kubernetesDeploymentInfo.getServiceNames();
	}

}
