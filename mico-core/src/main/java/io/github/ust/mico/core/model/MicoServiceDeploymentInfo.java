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

import java.util.ArrayList;
import java.util.List;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

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
    @JsonIgnore
    // TODO: @JsonIdentityInfo really necessary?
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = MicoApplication.class)
    @StartNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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
    @JsonIgnore
    // TODO: @JsonIdentityInfo really necessary?
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = MicoService.class)
    @EndNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Number of desired instances. Defaults to 1.")
        }
    )})
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
            @ExtensionProperty(name = "description", value = "Minimum number of seconds for which this service should be ready " +
                "without any of its containers crashing, for it to be considered available. " +
                "Defaults to 0 (considered available as soon as it is ready).")
        }
    )})
    private int minReadySecondsBeforeMarkedAvailable = 0;

    /**
     * Those labels are key-value pairs that are attached to the deployment
     * of this service. Intended to be used to specify identifying attributes
     * that are meaningful and relevant to users, but do not directly imply
     * semantics to the core system. Labels can be used to organize and to select
     * subsets of objects. Labels can be attached to objects at creation time and
     * subsequently added and modified at any time.
     * Each key must be unique for a given object.
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
                " Each key must be unique for a given object.")
        }
    )})
    private List<MicoLabel<String, String>> labels = new ArrayList<>();

    /**
     * Indicates whether and when to pull the image.
     * Defaults to ImagePullPolicy#DEFAULT.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Image Pull Policy"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Indicates whether and when to pull the image.\n" +
                "Defaults to DEFAULT.")
        }
    )})
    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.DEFAULT;

    /**
     * Restart policy for all containers.
     * Defaults to RestartPolicy#ALWAYS.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Restart Policy"),
            @ExtensionProperty(name = "x-order", value = "70"),
            @ExtensionProperty(name = "description", value = "Restart policy for all containers.\n" +
                " Defaults to ALWAYS.")
        }
    )})
    private RestartPolicy restartPolicy = RestartPolicy.DEFAULT;


    /**
     * Enumeration for the different policies specifying
     * when to pull an image.
     */
    public enum ImagePullPolicy {

        ALWAYS,
        NEVER,
        IF_NOT_PRESENT;

        /**
         * Default image pull policy is {@link ImagePullPolicy#ALWAYS}.
         */
        public static ImagePullPolicy DEFAULT = ImagePullPolicy.ALWAYS;

    }


    /**
     * Enumeration for all supported restart policies.
     */
    public enum RestartPolicy {

        ALWAYS,
        ON_FAILURE,
        NEVER;

        /**
         * Default restart policy is {@link RestartPolicy#ALWAYS}.
         */
        public static RestartPolicy DEFAULT = RestartPolicy.ALWAYS;

    }

}
