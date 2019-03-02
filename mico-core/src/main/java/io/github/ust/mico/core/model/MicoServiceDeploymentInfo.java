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

package io.github.ust.mico.core.model;

import com.fasterxml.jackson.annotation.*;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.MicoServiceDeploymentInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the information necessary for deploying
 * a {@link MicoApplication}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@RelationshipEntity(type = "INCLUDES_SERVICE")
public class MicoServiceDeploymentInfo {

    /**
     * The id of this service deployment info.
     */
    @Id
    @GeneratedValue
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The {@link MicoApplication} that uses a {@link MicoService}
     * this deployment information refers to.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Application"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The application that uses a service " +
                "this deployment information refers to.")
        }
    )})
    @JsonBackReference
    @StartNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @NotNull
    private MicoApplication application;

    /**
     * The {@link MicoService} this deployment information refers to.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The service this deployment information refers to.")
        }
    )})
    @EndNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @NotNull
    private MicoService service;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

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
            @ExtensionProperty(name = "description", value = "Number of desired instances. Defaults to 1.")
        }
    )})
    @Positive(message = "must be at least one replica")
    private int replicas = 1;

    /**
     * Minimum number of seconds for which this service should be ready
     * without any of its containers crashing, for it to be considered available.
     * Defaults to 0 (considered available as soon as it is ready).
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Time To Verify Ready State"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "default", value = "0"),
            @ExtensionProperty(name = "description", value = "Minimum number of seconds for which this service should be ready " +
                "without any of its containers crashing, for it to be considered available. " +
                "Defaults to 0 (considered available as soon as it is ready).")
        }
    )})
    @PositiveOrZero(message = "must not be negative")
    private int minReadySecondsBeforeMarkedAvailable = 0;

    /**
     * Those labels are key-value pairs that are attached to the deployment
     * of this service. Intended to be used to specify identifying attributes
     * that are meaningful and relevant to users, but do not directly imply
     * semantics to the core system. Labels can be used to organize and to select
     * subsets of objects. Labels can be attached to objects at creation time and
     * subsequently added and modified at any time.
     * Each key must be unique for a given object.
     * {@code null} values are skipped.
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
                " Null values are skipped.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private List<MicoLabel<String, String>> labels = new ArrayList<>();

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
            @ExtensionProperty(name = "description", value = "Indicates whether and when to pull the image.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.ALWAYS;

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
            @ExtensionProperty(name = "description", value = "Restart policy for all containers.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private RestartPolicy restartPolicy = RestartPolicy.ALWAYS;


    /**
     * Applies the values of all properties of a
     * {@link MicoServiceDeploymentInfoDTO} to this
     * {@code MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoDTO}.
     * @return this {@link MicoServiceDeploymentInfo} with the values
     * of the properties of the given {@link MicoServiceDeploymentInfoDTO}.
     */
    public MicoServiceDeploymentInfo applyValuesFrom(MicoServiceDeploymentInfoDTO serviceDeploymentInfoDTO) {
        return setReplicas(serviceDeploymentInfoDTO.getReplicas())
            .setMinReadySecondsBeforeMarkedAvailable(serviceDeploymentInfoDTO.getMinReadySecondsBeforeMarkedAvailable())
            .setLabels(serviceDeploymentInfoDTO.getLabels())
            .setImagePullPolicy(serviceDeploymentInfoDTO.getImagePullPolicy())
            .setRestartPolicy(serviceDeploymentInfoDTO.getRestartPolicy());
    }


    /**
     * Enumeration for the different policies specifying
     * when to pull an image.
     * Default image pull policy is {@link ImagePullPolicy#ALWAYS}.
     */
    public enum ImagePullPolicy {

        ALWAYS,
        NEVER,
        IF_NOT_PRESENT
    }


    /**
     * Enumeration for all supported restart policies.
     * Default restart policy is {@link RestartPolicy#ALWAYS}.
     */
    public enum RestartPolicy {

        ALWAYS,
        ON_FAILURE,
        NEVER
    }

}
