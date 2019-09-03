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

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.response.status.MicoApplicationDeploymentStatusResponseDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for a {@link MicoApplication} intended to use with responses only. Additionally includes all of services of the
 * application.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

    /**
     * All KafkaFaasConnector deployment information
     * used by the application as {@link KFConnectorDeploymentInfoResponseDTO}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "KafkaFaasConnectors"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "All KafkaFaasConnector deployment information used by the application.")
        }
    )})
    private List<KFConnectorDeploymentInfoResponseDTO> kfConnectorDeploymentInfos = new ArrayList<>();

    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoApplicationWithServicesResponseDTO} based on a {@code MicoApplication}. Note
     * that the deployment status is not set since it cannot be inferred from the {@code MicoApplication} itself
     *
     * @param application the {@link MicoApplication}.
     */
    public MicoApplicationWithServicesResponseDTO(MicoApplication application) {
        super(application);
        services = application.getServices().stream()
            .map(MicoServiceResponseDTO::new)
            .collect(Collectors.toList());
        kfConnectorDeploymentInfos = application.getKafkaFaasConnectorDeploymentInfos().stream()
            .map(KFConnectorDeploymentInfoResponseDTO::new)
            .collect(Collectors.toList());
    }

    /**
     * Creates an instance of {@code MicoApplicationWithServicesResponseDTO}
     * based on a {@code MicoApplication} and a {@code MicoApplicationDeploymentStatus}.
     *
     * @param application      the {@link MicoApplication}.
     * @param deploymentStatus the {@link MicoApplicationDeploymentStatus}.
     */
    public MicoApplicationWithServicesResponseDTO(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus) {
        this(application);
        setDeploymentStatus(new MicoApplicationDeploymentStatusResponseDTO(deploymentStatus));
    }
}
