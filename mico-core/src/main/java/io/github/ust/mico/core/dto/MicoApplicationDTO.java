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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoApplication;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoApplication} without services
 * and their deployment information. Contains the current
 * deployment status of this application (may be unknown).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationDTO {

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
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Unique short name of the application.")
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
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Human readable name of the application.")
        }
    )})
    private String name;

    /**
     * The version of this application.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Version number of the application.")
        }
    )})
    private String version;

    /**
     * Human readable description of this application.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Human readable description of this application.")
        }
    )})
    private String description;


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
            @ExtensionProperty(name = "x-order", value = "110"),
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
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Human readable information for the application owner, " +
                "who is responsible for this application.")
        }
    )})
    private String owner;
    
    /**
     * Indicates whether the {@link MicoApplication} is currently deployed.
     */
    @ApiModelProperty(extensions = {@Extension(
            name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
            properties = {
                @ExtensionProperty(name = "title", value = "Deployment Status"),
                @ExtensionProperty(name = "x-order", value = "120"),
                @ExtensionProperty(name = "description", value = "Holds the current deployment status of this application.")
            }
        )})
    private MicoApplicationDeploymentStatus deploymentStatus;
    
    
    /**
     * Creates a {@code MicoApplicationDTO} based on a
     * {@link MicoApplication}.
     * 
     * @param application the {@link MicoApplication}.
     * @return a {@link MicoApplicationDTO} with all the values
     *         of the given {@code MicoApplication}.
     */
    public static MicoApplicationDTO valueOf(MicoApplication application) {
        return new MicoApplicationDTO()
                .setShortName(application.getShortName())
                .setName(application.getName())
                .setVersion(application.getVersion())
                .setDescription(application.getDescription())
                .setContact(application.getContact())
                .setOwner(application.getOwner());
    }
    
    
    public enum MicoApplicationDeploymentStatus {
        
        DEPLOYED,
        NOT_DEPLOYED,
        UNKNOWN;
        
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
        
    }

}
