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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * DTO for {@link MicoServiceDeploymentInfo} intended to use with responses only.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class KFConnectorDeploymentInfoResponseDTO extends KFConnectorDeploymentInfoRequestDTO {

    /**
     * The short name of the associated KafkaFaasConnector {@link MicoService}.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Short Name"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "The short name of the KafkaFaasConnector MicoService.")
        }
    )})
    private String shortName;

    /**
     * The version of the associated KafkaFaasConnector {@link MicoService}.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "The version of the associated KafkaFaasConnector MicoService. " +
                "Refers to GitHub release tag.")
        }
    )})
    private String version;

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
            @ExtensionProperty(name = "x-order", value = "200"),
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
     * Creates an instance of {@code KFConnectorDeploymentInfoResponseDTO} based on a {@code
     * MicoServiceDeploymentInfo}.
     *
     * @param kfConnectorDeploymentInfo the {@link MicoServiceDeploymentInfo}.
     */
    public KFConnectorDeploymentInfoResponseDTO(MicoServiceDeploymentInfo kfConnectorDeploymentInfo) {
        super(kfConnectorDeploymentInfo);

        if (kfConnectorDeploymentInfo.getService() != null) {
            setShortName(kfConnectorDeploymentInfo.getService().getShortName());
            setVersion(kfConnectorDeploymentInfo.getService().getVersion());
        }

        // Kubernetes deployment info could be null if not available
        if (kfConnectorDeploymentInfo.getKubernetesDeploymentInfo() != null) {
            setKubernetesDeploymentInfo(new KubernetesDeploymentInfoResponseDTO(
                kfConnectorDeploymentInfo.getKubernetesDeploymentInfo()));
        }
    }
}
