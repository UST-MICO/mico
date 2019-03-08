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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO for a {@link MicoApplication} without services
 * and their deployment information. Contains the current
 * deployment status of this application (may be unknown).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationResponseDTO {
	
	// TODO: Consider inheriting from MicoApplicationRequestDTO

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * A brief name for the application intended
     * for use as a unique identifier.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Short Name"),
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_NAMING_REGEX),
            @ExtensionProperty(name = "minLength", value = "3"),
            @ExtensionProperty(name = "maxLength", value = "253"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Unique short name of the application.")
        }
    )})
    @Size(min = 3, max = 253, message = "must have a length between 3 and 253")
    @Pattern(regexp = Patterns.KUBERNETES_NAMING_REGEX, message = Patterns.KUBERNETES_NAMING_MESSAGE)
    private String shortName;

    /**
     * The version of this application.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "pattern", value = Patterns.SEMANTIC_VERSION_WITH_PREFIX_REGEX),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The version of this application.")
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
            @ExtensionProperty(name = "description", value = "Human readable name of the application.")
        }
    )})
    @NotNull
    private String name;

    /**
     * Human readable description of this application.
     * Is allowed to be empty (default). {@code null} values are skipped.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "default", value = ""),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Human readable description of this application.\n " +
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
            @ExtensionProperty(name = "title", value = "Contact"),
            @ExtensionProperty(name = "x-order", value = "210"),
            @ExtensionProperty(name = "description", value = "Human readable contact information for support purposes.")
        }
    )})
    private String contact;

    /**
     * Human readable information for the application owner
     * who is responsible for this application.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Owner"),
            @ExtensionProperty(name = "x-order", value = "200"),
            @ExtensionProperty(name = "description", value = "Human readable information for the application owner, " +
                "who is responsible for this application.")
        }
    )})
    private String owner;

    /**
     * Indicates whether the {@link MicoApplication} is currently deployed.
     * Default is {@link MicoApplicationDeploymentStatus#UNKNOWN}.
     * Is read only and will be updated by the backend at every request.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Deployment Status"),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "220"),
            @ExtensionProperty(name = "description", value = "Holds the current deployment status of this application.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MicoApplicationDeploymentStatus deploymentStatus = MicoApplicationDeploymentStatus.NOT_DEPLOYED;
    
    
    // ----------------------
    // -> Static Creators ---
    // ----------------------

    /**
     * Creates a {@code MicoApplicationDTO} based on a
     * {@code MicoApplication}. Note that the deployment status
     * needs to be set explicitly since it cannot be inferred
     * from the given {@link MicoApplication} itself.
     *
     * @param application the {@link MicoApplication}.
     * @return a {@link MicoApplicationResponseDTO} with all the values
     * 		   of the given {@code MicoApplication}.
     */
    public static MicoApplicationResponseDTO valueOf(MicoApplication application) {
        return new MicoApplicationResponseDTO()
            .setShortName(application.getShortName())
            .setName(application.getName())
            .setVersion(application.getVersion())
            .setDescription(application.getDescription())
            .setContact(application.getContact())
            .setOwner(application.getOwner());
    }


    /**
     * Enumeration for all possible states a deployment of
     * a {@link MicoApplication} can be in.
     */
    public enum MicoApplicationDeploymentStatus {

        DEPLOYED,
        NOT_DEPLOYED;
    	
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

    }

}
