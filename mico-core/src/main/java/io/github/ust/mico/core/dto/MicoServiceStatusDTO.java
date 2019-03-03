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

package io.github.ust.mico.core.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.api.model.Pod;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
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
 * DTO for status information of a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoServiceStatusDTO {

    /**
     * Name of a {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Name"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Name of a MicoService.")
        }
    )})
    private String name;

    /**
     * ShortName of a {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Short Name"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Short name of a MicoService.")
        }
    )})
    private String shortName;

    /**
     * Version of a {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Version of a MicoService.")
        }
    )})
    private String version;

    /**
     * Counter for the number of replicas that should be available.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Requested Replicas"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Counter for number of replicas " +
                "of a MicoService that should be available.")
        }
    )})
    private int requestedReplicas;

    /**
     * Counter for the number of replicas that are actually available.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Available Replicas"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "Counter for replicas of " +
                "a MicoService that are actually available.")
        }
    )})
    private int availableReplicas;

    /**
     * Each {@link MicoServiceInterface} in this list is deployed as a Kubernetes Service for this {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Interfaces Information"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Each link MicoServiceInterface in this list is " +
                "deployed as a Kubernetes Service for this MicoService.")
        }
    )})
    private List<MicoServiceInterfaceStatusDTO> interfacesInformation = new ArrayList<>();

    /**
     * List of {@link MicoApplicationDTO MicoApplicationDTOs}, representing all applications that are using one service together.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Applications Using This Service"),
            @ExtensionProperty(name = "x-order", value = "70"),
            @ExtensionProperty(name = "description", value = "List of MicoApplicationDTOs, " +
                "representing all applications that are using one service together.")
        }
    )})
    private List<MicoApplicationDTO> applicationsUsingThisService  = new ArrayList<>();

    /**
     * List of all {@link Pod Pods} of all replicas of a deployment of a {@link MicoService}.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Pods Information"),
            @ExtensionProperty(name = "x-order", value = "80"),
            @ExtensionProperty(name = "description", value = "List of all pods of all replicas" +
                " of a deployment of a MicoService.")
        }
    )})
    private List<KubernetesPodInformationDTO> podsInformation = new ArrayList<>();

    /**
     * Each entry in this map represents a node with its average CPU load. The average CPU load is computed from all
     * pods of the deployment of a {@link MicoService}, which are running on this node.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Average CPU Load Per Node"),
            @ExtensionProperty(name = "x-order", value = "92"),
            @ExtensionProperty(name = "description", value = "Each entry in this map represents " +
                "a node with its average CPU load. \n" +
                "The average CPU load is computed from all pods of the deployment " +
                "of a MicoService running on this node.")
        }
    )})
    private Map<String, Integer> averageCpuLoadPerNode;

    /**
     * Each entry in this map represents a node with its average memory usage. The average memory usage is computed from
     * all pods of the deployment of a {@link MicoService}, which are running on this node.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Average Memory Usage Per Node"),
            @ExtensionProperty(name = "x-order", value = "93"),
            @ExtensionProperty(name = "description", value = "Each entry in this map represents a " +
                "node with its average memory usage.\n " +
                "The average memory usage is computed from all pods of the deployment" +
                "of a MicoService, which are running on this node.")
        }
    )})
    private Map<String, Integer> averageMemoryUsagePerNode;

    /**
     * Contains error messages for services that are not deployed or not available due to other reasons.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Error Messages"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Contains error messages for services that are not deployed or not available due to other reasons.")
        }
    )})
    private List<String> errorMessages = new ArrayList<>();
}
