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

package io.github.ust.mico.core.dto.response.status;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.Pod;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO;
import io.github.ust.mico.core.model.MicoService;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for the status information of a {@link MicoService} intended to use with responses only..
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServiceStatusResponseDTO {

    /**
     * ShortName of the {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Short Name"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Short name of the MicoService.")
        }
    )})
    private String shortName;

    /**
     * Version of the {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Version of the MicoService.")
        }
    )})
    private String version;

    /**
     * Name of the {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Name"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Name of the MicoService.")
        }
    )})
    private String name;

    /**
     * Counter for the number of replicas the corresponding that should be available.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Requested Replicas"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Counter for the number of replicas of " +
                "the corresponding MicoService that should be available.")
        }
    )})
    private int requestedReplicas;

    /**
     * Counter for the number of replicas of the corresponding that are actually available.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Available Replicas"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "Counter for the number of replicas of " +
                "the corresponding MicoService that are actually available.")
        }
    )})
    private int availableReplicas;

    /**
     * Contains information about the Kubernetes services deployed for each MicoServiceInterface of the corresponding
     * MicoService.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Interfaces Information"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Contains information about the Kubernetes services deployed "
                + "for each MicoServiceInterface of the corresponding MicoService.")
        }
    )})
    private List<MicoServiceInterfaceStatusResponseDTO> interfacesInformation = new ArrayList<>();

    /**
     * List of {@link MicoApplicationResponseDTO MicoApplicationResponseDTOs} representing all applications that share
     * the MicoService.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Applications Using This Service"),
            @ExtensionProperty(name = "x-order", value = "70"),
            @ExtensionProperty(name = "description", value = "List of MicoApplicationDTOs " +
                "representing all applications that share the MicoService.")
        }
    )})
    private List<MicoApplicationResponseDTO> applicationsUsingThisService = new ArrayList<>();

    /**
     * List of all {@link Pod Pods} of all replicas of a deployment of the {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Pods Information"),
            @ExtensionProperty(name = "x-order", value = "80"),
            @ExtensionProperty(name = "description", value = "List of all pods of all replicas" +
                " of a deployment of the MicoService.")
        }
    )})
    private List<KubernetesPodInformationResponseDTO> podsInformation = new ArrayList<>();

    /**
     * List of {@link KubernetesNodeMetricsResponseDTO KubernetesNodeMetricsDTOs} with metrics for each node used by the
     * {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Node Metrics"),
            @ExtensionProperty(name = "x-order", value = "90"),
            @ExtensionProperty(name = "description", value = "List of KubernetesNodeMetricsDTO with metrics for each node used by this MicoService.")
        }
    )})
    private List<KubernetesNodeMetricsResponseDTO> nodeMetrics = new ArrayList<>();

    /**
     * Contains error messages for Kubernetes services that are not deployed or not available due to other reasons.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Error Messages"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Contains error messages for Kubernetes services that "
                + "are not deployed or not available due to other reasons.")
        }
    )})
    private List<String> errorMessages = new ArrayList<>();
}
