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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoServiceInterface} intended to use with requests only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServiceInterfaceRequestDTO {
	
    // ----------------------
    // -> Required Fields ---
    // ----------------------
	
	/**
     * The name of this {@link MicoServiceInterface}.
     * Pattern is the same than the one for Kubernetes Service names.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Interface Name"),
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_NAMING_REGEX),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The name of this MicoServiceInterface"),
            @ExtensionProperty(name = "minLength", value = "3"),
            @ExtensionProperty(name = "maxLength", value = KubernetesNameNormalizer.MICO_NAME_MAX_SIZE +""),
        }
    )})
    @NotNull
    @Size(min = 3, max = KubernetesNameNormalizer.MICO_NAME_MAX_SIZE, message = "must have a length between 3 and " + KubernetesNameNormalizer.MICO_NAME_MAX_SIZE)
    @Pattern(regexp = Patterns.KUBERNETES_NAMING_REGEX, message = Patterns.KUBERNETES_NAMING_MESSAGE)
    private String serviceInterfaceName;

    /**
     * The list of ports.
     * Must not be empty.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Ports"),
            @ExtensionProperty(name = "x-order", value = "200"),
            @ExtensionProperty(name = "minItems", value = "1"),
            @ExtensionProperty(name = "description", value = "The list of ports of this interface.\n" +
                " Must not be empty.")
        }
    )})
    @NotEmpty
    @Valid
    private List<MicoServicePortRequestDTO> ports = new ArrayList<>();


    // ----------------------
    // -> Optional Fields ---
    // ----------------------

    /**
     * Human readable description of this service interface,
     * e.g., the functionality provided.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service interface.\n " +
                "Null values are skipped.")
        }
    )})
    private String description;

    /**
     * The protocol of this interface, e.g., HTTPS.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Protocol"),
            @ExtensionProperty(name = "x-order", value = "120"),
            @ExtensionProperty(name = "description", value = "The protocol of this interface.")
        }
    )})
    private String protocol;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
     * Creates an instance of {@code MicoServiceInterfaceRequestDTO} based on a
     * {@code MicoServiceInterface}.
     * 
     * @param serviceInterface the {@link MicoServiceInterface}.
     */
    public MicoServiceInterfaceRequestDTO(MicoServiceInterface serviceInterface) {
    	this.serviceInterfaceName = serviceInterface.getServiceInterfaceName();
    	this.ports = serviceInterface.getPorts().stream().map(MicoServicePortRequestDTO::new).collect(Collectors.toList());
    	this.description = serviceInterface.getDescription();
    	this.protocol = serviceInterface.getProtocol();
    	
    }

}
