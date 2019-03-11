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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
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
@JsonInclude(Include.NON_NULL)
public class MicoServiceInterfaceRequestDTO {
	
    // ----------------------
    // -> Required fields ---
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
            @ExtensionProperty(name = "description", value = "The name of this MicoServiceInterface")
        }
    )})
    @NotNull
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
            @ExtensionProperty(name = "description", value = "The list of the interfaces ports.\n" +
                " Must not be empty.")
        }
    )})
    @NotEmpty
    @Valid
    private List<MicoServicePort> ports = new ArrayList<>();


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The public DNS.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Public DNS"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "The public DNS.")
        }
    )})
    private String publicDns;

    /**
     * Human readable description of this service interface,
     * e.g., the functionality provided.
     * {@code null} values are skipped.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "default", value = ""),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service interface.\n " +
                "Null values are skipped.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private String description = "";

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

    /**
     * The transport protocol, e.g., TCP.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Transport Protocol"),
            @ExtensionProperty(name = "x-order", value = "130"),
            @ExtensionProperty(name = "description", value = "The transport protocol of this interface.")
        }
    )})
    private String transportProtocol;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
     * Creates an instance of this DTO based on a
     * {@code MicoServiceInterface}.
     * 
     * @param serviceInterface the {@link MicoServiceInterface}.
     */
    public MicoServiceInterfaceRequestDTO(MicoServiceInterface serviceInterface) {
    	this.serviceInterfaceName = serviceInterface.getServiceInterfaceName();
    	this.ports = serviceInterface.getPorts();
    	this.publicDns = serviceInterface.getPublicDns();
    	this.description = serviceInterface.getDescription();
    	this.protocol = serviceInterface.getProtocol();
    	this.transportProtocol = serviceInterface.getTransportProtocol();
    	
    }

}
