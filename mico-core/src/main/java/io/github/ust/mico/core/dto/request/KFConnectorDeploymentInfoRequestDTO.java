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

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.model.OpenFaaSFunction;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Optional;

/**
 * DTO for {@link MicoServiceDeploymentInfo} specialised for a KafkaFaasConnector intended to use with requests only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KFConnectorDeploymentInfoRequestDTO {

    /**
     * Instance ID.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "InstanceId"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "ID of the instance.")
        }
    )})
    @NotNull
    private String instanceId;

    /**
     * Name of the input topic.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "InputTopicName"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Name of the input topic.")
        }
    )})
    private String inputTopicName;

    /**
     * Name of the output topic.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "OutputTopicName"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Name of the output topic.")
        }
    )})
    private String outputTopicName;

    /**
     * Name of the OpenFaaS function.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "OpenFaaSFunctionName"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "pattern", value = Patterns.OPEN_FAAS_FUNCTION_NAME_REGEX),
            @ExtensionProperty(name = "minLength", value = "0"),
            @ExtensionProperty(name = "maxLength", value = "63"),
            @ExtensionProperty(name = "description", value = "Name of the OpenFaaS function.")
        }
    )})
    @Size(max = 63, message = "must be 63 characters or less")
    @Pattern(regexp = Patterns.OPEN_FAAS_FUNCTION_NAME_REGEX, message = Patterns.OPEN_FAAS_FUNCTION_NAME_MESSAGE)
    private String openFaaSFunctionName;


    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code KFConnectorDeploymentInfoRequestDTO} based on a
     * {@code MicoServiceDeploymentInfo}.
     *
     * @param kfConnectorDeploymentInfo the {@link MicoServiceDeploymentInfo}.
     */
    public KFConnectorDeploymentInfoRequestDTO(MicoServiceDeploymentInfo kfConnectorDeploymentInfo) {
        this.instanceId = kfConnectorDeploymentInfo.getInstanceId();
        Optional<MicoTopicRole> inputTopicRoleOpt = kfConnectorDeploymentInfo.getTopics().stream()
            .filter(t -> t.getRole().equals(MicoTopicRole.Role.INPUT)).findFirst();
        inputTopicRoleOpt.ifPresent(micoTopicRole -> this.inputTopicName = micoTopicRole.getTopic().getName());
        Optional<MicoTopicRole> outputTopicRoleOpt = kfConnectorDeploymentInfo.getTopics().stream()
            .filter(t -> t.getRole().equals(MicoTopicRole.Role.OUTPUT)).findFirst();
        outputTopicRoleOpt.ifPresent(micoTopicRole -> this.outputTopicName = micoTopicRole.getTopic().getName());
        OpenFaaSFunction openFaaSFunction = kfConnectorDeploymentInfo.getOpenFaaSFunction();
        if (openFaaSFunction != null) this.openFaaSFunctionName = openFaaSFunction.getName();
    }
}
