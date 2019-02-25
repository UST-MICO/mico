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

import javax.validation.constraints.Pattern;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a service in the context of MICO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class MicoService {

    /**
     * The id of this service.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * A brief name for the service.
     * Pattern is the same as the one for Kubernetes Service names.
     */
    @Pattern(regexp="^[a-z]([-a-z0-9]*[a-z0-9])?$")
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Short Name"),
            @ExtensionProperty(name = "pattern", value = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "A unique name of the MicoService.")
        }
    )})
    private String shortName;

    /**
     * The name of the artifact. Intended for humans.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Name"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "A human readable name of the MicoService.")
        }
    )})
    private String name;

    /**
     * The version of this service. Refers to GitHub release tag.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "pattern", value = "^(\\w+)?(\\d+)\\.(\\d+)\\.(\\d+)(-(\\w+\\.)*\\w+)?$"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "The version of this service. Refers to GitHub release " +
                "tag.")
        }
    )})
    private String version;

    /**
     * Human readable description of this service.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service.")
        }
    )})
    private String description;

    /**
     * The list of interfaces this service provides.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Interfaces"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "The list of interfaces this service provides.")
        }
    )})
    @Relationship(type = "PROVIDES_INTERFACES", direction = Relationship.UNDIRECTED)
    private List<MicoServiceInterface> serviceInterfaces = new ArrayList<>();

    /**
     * Indicates where this service originates from, e.g.,
     * GitHub (downloaded and built by MICO) or DockerHub
     * (ready-to-use image).
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Crawling Origin"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Indicates where this service originates from.")
        }
    )})
    private MicoServiceCrawlingOrigin serviceCrawlingOrigin;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The list of services that this service requires
     * in order to run normally.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Dependencies"),
            @ExtensionProperty(name = "x-order", value = "90"),
            @ExtensionProperty(name = "description", value = "The list of services that this service requires in " +
                "order to run normally.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Relationship(type = "DEPENDS_ON")
    private List<MicoServiceDependency> dependencies = new ArrayList<>();

    /**
     * Same MicoService with previous version.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Predecessor"),
            @ExtensionProperty(name = "x-order", value = "80"),
            @ExtensionProperty(name = "description", value = "Previous version of this service.")
        }
    )})
    @Relationship(type = "PREDECESSOR")
    private MicoService predecessor;

    /**
     * Human readable contact information for support purposes.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Contact Information"),
            @ExtensionProperty(name = "x-order", value = "70"),
            @ExtensionProperty(name = "description", value = "Human readable contact information for support purposes.")
        }
    )})
    private String contact;

    /**
     * Human readable information for the service owner
     * who is responsible for this service.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Owner"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Human readable information for the services owner, who " +
                "is responsible for this service.")
        }
    )})
    private String owner;

    /**
     * The URL that could be used for a git clone,
     * to clone the current master branch.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "\"git clone\" Url"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "The URL that could be used for a git clone, to clone " +
                "the current master branch.")
        }
    )})
    private String gitCloneUrl;

    /**
     * The URL to the get the information about a specific git release.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Git Release Info Url"),
            @ExtensionProperty(name = "x-order", value = "120"),
            @ExtensionProperty(name = "description", value = "The URL to the get the information about a specific " +
                "git release.")
        }
    )})
    private String gitReleaseInfoUrl;

    /**
     * The relative path to the Dockerfile.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Path to Dockerfile"),
            @ExtensionProperty(name = "pattern", value = "^(?!/.*$).*"),
            @ExtensionProperty(name = "x-order", value = "130"),
            @ExtensionProperty(name = "description", value = "The relative path to the Dockerfile.")
        }
    )})
    @Pattern(regexp = "^(?!/.*$).*", message = "Path must be relative to the Git clone url")
    private String dockerfilePath;

    /**
     * The fully qualified URI to the image on DockerHub.
     * Either set after the image has been built by MICO
     * (if the service originates from GitHub) or set by the
     * user directly.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Docker Image Url"),
            @ExtensionProperty(name = "x-order", value = "140"),
            @ExtensionProperty(name = "description", value = "The fully qualified URI to the image on DockerHub.")
        }
    )})
    private String dockerImageUri;

    @JsonIgnore
    public MicoVersion getMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.version);
        return micoVersion;
    }

}
