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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoService} intended to use with requests only.
 * Note that the {@link MicoServiceDependency MicoServiceDependencies}
 * and {@link MicoServiceInterface MicoServiceInterfaces}
 * are not included.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServiceRequestDTO {
	
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
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_NAMING_REGEX),
            @ExtensionProperty(name = "minLength", value = "3"),
            @ExtensionProperty(name = "maxLength", value = KubernetesNameNormalizer.MICO_NAME_MAX_SIZE +""),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "A unique name of the MicoService.")
        }
    )})
    @Size(min = 3, max = KubernetesNameNormalizer.MICO_NAME_MAX_SIZE, message = "must have a length between 3 and " + KubernetesNameNormalizer.MICO_NAME_MAX_SIZE)
    @Pattern(regexp = Patterns.KUBERNETES_NAMING_REGEX, message = Patterns.KUBERNETES_NAMING_MESSAGE)
    private String shortName;

    /**
     * The version of this service.
     * E.g. the GitHub release tag.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "pattern", value = Patterns.SEMANTIC_VERSION_WITH_PREFIX_REGEX),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The version of this service. Refers to GitHub release " +
                "tag.")
        }
    )})
    @NotEmpty
    @Pattern(regexp = Patterns.SEMANTIC_VERSION_WITH_PREFIX_REGEX, message = Patterns.SEMANTIC_VERSIONING_MESSAGE)
    private String version;

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
    @NotNull
    private String name;

    /**
     * Human readable description of this service.
     * Is allowed to be empty (default). {@code null} values are skipped.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "default", value = ""),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service.\n " +
                "Is allowed to be empty (default). Null values are skipped.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private String description = "";


    // ----------------------
    // -> Optional fields ---
    // ----------------------

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
     * The URL used for a git clone of a GitHub repository,
     * to clone the current master branch.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Git Clone URL"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "The URL used for a git clone of a GitHub repository, " +
                "to clone the current master branch.")
        }
    )})
    @URL(host = "github.com", message = "must be a valid GitHub URL")
    private String gitCloneUrl;

    /**
     * The path to the Dockerfile must be relative to the root folder of the git repository.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Path to Dockerfile"),
            @ExtensionProperty(name = "pattern", value = Patterns.RELATIVE_PATH_REGEX),
            @ExtensionProperty(name = "x-order", value = "130"),
            @ExtensionProperty(name = "description", value = "The path to the Dockerfile must be relative to the root folder of the git repository")
        }
    )})
    @Pattern(regexp = Patterns.RELATIVE_PATH_REGEX, message = "must be relative to the root folder of the git repository")
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
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
	/**
	 * Creates an instance of {@code MicoServiceRequestDTO} based on a
	 * {@code MicoService}.
	 * 
	 * @param service the {@link MicoService}.
	 */
	public MicoServiceRequestDTO(MicoService service) {
		this.shortName = service.getShortName();
		this.version = service.getVersion();
		this.name = service.getName();
		this.description = service.getDescription();
		this.contact = service.getContact();
		this.owner = service.getOwner();
		this.gitCloneUrl = service.getGitCloneUrl();
		this.dockerfilePath = service.getDockerfilePath();
		this.dockerImageUri = service.getDockerImageUri();
	}

}
