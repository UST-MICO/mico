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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

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
     * MUST be readable and writable from the perspective of the Jackson mapper
     * to handle recursive service dependencies.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * A brief name for the service.
     * In conjunction with the version it must be unique.
     * Pattern is the same as the one for Kubernetes Service names.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Short Name"),
            @ExtensionProperty(name = "pattern", value = Patterns.Constants.KUBERNETES_NAMING_REGEX),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "A unique name of the MicoService.")
        }
    )})
    @NotEmpty
    @Pattern(regexp = Patterns.Constants.KUBERNETES_NAMING_REGEX, message = Patterns.Constants.KUBERNETES_NAMING_MESSAGE)
    private String shortName;

    /**
     * The name of the artifact. Intended for humans.
     * Required only for the usage in the UI.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Name"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "A human readable name of the MicoService.")
        }
    )})
    private String name;

    /**
     * The version of this service.
     * E.g. the GitHub release tag.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "pattern", value = Patterns.Constants.SEMANTIC_VERSIONING_REGEX),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The version of this service. Refers to GitHub release " +
                "tag.")
        }
    )})
    @NotEmpty
    @Pattern(regexp = Patterns.Constants.SEMANTIC_VERSIONING_REGEX, message = Patterns.Constants.SEMANTIC_VERSIONING_MESSAGE)
    private String version;

    /**
     * Human readable description of this service.
     * Is allowed to be empty (default).
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "default", value = ""),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service.\n " +
                "Is allowed to be empty (default).")
        }
    )})
    private String description = "";

    /**
     * Indicates where this service originates from, e.g.,
     * GitHub (downloaded and built by MICO) or DockerHub
     * (ready-to-use image).
     * {@code null} is not allowed, if it is omitted the default value will be used.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Crawling Origin"),
            @ExtensionProperty(name = "default", value = "NOT_DEFINED"),
            @ExtensionProperty(name = "enum", value = "[\"GITHUB\", \"DOCKER\", \"NOT_DEFINED\"]"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Indicates where this service originates from.")
        }
    )})
    @NotNull
    private MicoServiceCrawlingOrigin serviceCrawlingOrigin = MicoServiceCrawlingOrigin.NOT_DEFINED;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The list of interfaces this service provides.
     * It can be omitted or be set to an empty list, but {@code null} is not allowed.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Interfaces"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "The list of interfaces this service provides.")
        }
    )})
    @Relationship(type = "PROVIDES_INTERFACES", direction = Relationship.UNDIRECTED)
    @NotNull
    private List<MicoServiceInterface> serviceInterfaces = new ArrayList<>();

    /**
     * The list of services that this service requires
     * in order to run normally.
     * Is read only. Use special API for updating.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Dependencies"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "90"),
            @ExtensionProperty(name = "description", value = "The list of services that this service requires in " +
                "order to run normally.")
        }
    )})
    @JsonManagedReference
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
            @ExtensionProperty(name = "title", value = "Git Clone URL"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "The URL that could be used for a git clone, to clone " +
                "the current master branch.")
        }
    )})
    @URL(host = "github.com", message = "must be a valid GitHub URL")
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
            @ExtensionProperty(name = "pattern", value = Patterns.Constants.RELATIVE_PATH_REGEX),
            @ExtensionProperty(name = "x-order", value = "130"),
            @ExtensionProperty(name = "description", value = "The relative path to the Dockerfile.")
        }
    )})
    @Pattern(regexp = Patterns.Constants.RELATIVE_PATH_REGEX, message = "must be relative to the Git clone url")
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
