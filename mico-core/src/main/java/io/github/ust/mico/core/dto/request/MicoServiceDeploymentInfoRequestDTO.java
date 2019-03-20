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

package io.github.ust.mico.core.dto.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for {@link MicoServiceDeploymentInfo} intended to use with requests only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServiceDeploymentInfoRequestDTO {
	
    /**
     * Number of desired instances. Defaults to 1.
     * {@code null} is ignored.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Replicas"),
            @ExtensionProperty(name = "minimum", value = "1"),
            @ExtensionProperty(name = "default", value = "1"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Number of desired instances. " +
                "Defaults to 1.")
        }
    )})
    @Positive(message = "must be at least one replica")
    @JsonSetter(nulls = Nulls.SKIP)
    private int replicas = 1;

    /**
     * Those labels are key-value pairs that are attached to the deployment
     * of this {@link MicoService}. Intended to be used to specify identifying attributes
     * that are meaningful and relevant to users, but do not directly imply
     * semantics to the core system. Labels can be used to organize and to select
     * subsets of objects. Labels can be attached to objects at creation time and
     * subsequently added and modified at any time.
     * Each key must be unique for a given object.
     * {@code null} is ignored.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Labels"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Those labels are key-value pairs that are attached to the deployment" +
                " of this service. Intended to be used to specify identifying attributes" +
                " that are meaningful and relevant to users, but do not directly imply" +
                " semantics to the core system. Labels can be used to organize and to select" +
                " subsets of objects. Labels can be attached to objects at creation time and" +
                " subsequently added and modified at any time.\n" +
                " Each key must be unique for a given object.\n" +
                " Null is ignored.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    @Valid
    private List<MicoLabelRequestDTO> labels = new ArrayList<>();

    /**
     * Environment variables as key-value pairs that are attached to the deployment
     * of this {@link MicoService}. These environment values can be used by the deployed
     * {@link MicoService} during runtime. This could be useful to pass information to the
     * {@link MicoService} that is not known during design time or is likely to change.
     * Example could be an URL to another {@link MicoService} or an external service.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Environment Variables"),
            @ExtensionProperty(name = "x-order", value = "45"),
            @ExtensionProperty(name = "description", value = "Environment variables as key-value pairs that are attached to the deployment " +
                "of this MicoService. These environment values can be used by the deployed " +
                "MicoService during runtime. This could be useful to pass information to the " +
                "MicoService that is not known during design time or is likely to change. " +
                "Example could be an URL to another MicoService or an external service.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    @Valid
    private List<MicoEnvironmentVariableRequestDTO> environmentVariables = new ArrayList<>();

    /**
     * Indicates whether and when to pull the image.
     * Default image pull policy is {@link ImagePullPolicy#ALWAYS}.
     * {@code null} is ignored.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Image Pull Policy"),
            @ExtensionProperty(name = "default", value = "ALWAYS"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "Indicates whether and when to pull the image.\n " +
                "Null is ignored.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.ALWAYS;

    
    // -------------------
    // -> Constructors ---
    // -------------------
	
    /**
	 * Creates an instance of {@code MicoServiceDeploymentInfoRequestDTO} based on a
	 * {@code MicoServiceDeploymentInfo}.
	 * 
	 * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}.
	 */
	public MicoServiceDeploymentInfoRequestDTO(MicoServiceDeploymentInfo serviceDeploymentInfo) {
		this.replicas = serviceDeploymentInfo.getReplicas();
		this.labels = serviceDeploymentInfo.getLabels().stream().map(MicoLabelRequestDTO::new).collect(Collectors.toList());
		this.environmentVariables = serviceDeploymentInfo.getEnvironmentVariables().stream().map(MicoEnvironmentVariableRequestDTO::new).collect(Collectors.toList());
		this.imagePullPolicy = serviceDeploymentInfo.getImagePullPolicy();
	}

}
