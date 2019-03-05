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

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.RestartPolicy;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for {@link MicoServiceDeploymentInfo}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServiceDeploymentInfoResponseDTO {
	
	// TODO: Consider inheriting from MicoServiceDeploymentInfoRequestDTO.

    /**
     * Number of desired instances. Defaults to 1.
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
    private int replicas;

    /**
     * Minimum number of seconds for which this service should be ready
     * without any of its containers crashing, for it to be considered available.
     * Defaults to 0 (considered available as soon as it is ready).
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Time To Verify Ready State"),
            @ExtensionProperty(name = "minimum", value = "0"),
            @ExtensionProperty(name = "default", value = "0"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Minimum number of seconds for which this service should be ready " +
                "without any of its containers crashing, for it to be considered available. " +
                "0 is considered available as soon as it is ready.")
        }
    )})
    @PositiveOrZero(message = "must not be negative")
    private int minReadySecondsBeforeMarkedAvailable;

    /**
     * Those labels are key-value pairs that are attached to the deployment
     * of this service. Intended to be used to specify identifying attributes
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
            @ExtensionProperty(name = "x-order", value = "50"),
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
    private List<MicoLabel> labels = new ArrayList<>();

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
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Indicates whether and when to pull the image.\n " +
                "Null is ignored.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private ImagePullPolicy imagePullPolicy;

    /**
     * Restart policy for all containers.
     * Default restart policy is {@link RestartPolicy#ALWAYS}.
     * {@code null} is ignored.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Restart Policy"),
            @ExtensionProperty(name = "default", value = "ALWAYS"),
            @ExtensionProperty(name = "x-order", value = "70"),
            @ExtensionProperty(name = "description", value = "Restart policy for all containers.\n " +
                "Null is ignored.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private RestartPolicy restartPolicy = RestartPolicy.ALWAYS;


    /**
     * Creates a {@code MicoServiceDeploymentInfoDTO} based on a
     * {@link MicoServiceDeploymentInfo}.
     *
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo} to use.
     * @return a {@link MicoServiceDeploymentInfoResponseDTO} with all the values
     * of the given {@code MicoServiceDeploymentInfo}.
     */
    public static MicoServiceDeploymentInfoResponseDTO valueOf(MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
        return new MicoServiceDeploymentInfoResponseDTO()
            .setReplicas(micoServiceDeploymentInfo.getReplicas())
            .setMinReadySecondsBeforeMarkedAvailable(micoServiceDeploymentInfo.getMinReadySecondsBeforeMarkedAvailable())
            .setLabels(micoServiceDeploymentInfo.getLabels())
            .setImagePullPolicy(micoServiceDeploymentInfo.getImagePullPolicy())
            .setRestartPolicy(micoServiceDeploymentInfo.getRestartPolicy());
    }

}
