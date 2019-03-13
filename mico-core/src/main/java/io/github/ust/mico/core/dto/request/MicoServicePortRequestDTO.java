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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoPortType;
import io.github.ust.mico.core.model.MicoServicePort;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoServicePort} intended to use with requests only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServicePortRequestDTO {
	
    // ----------------------
    // -> Required fields ---
    // ----------------------
	
	/**
     * The port number of the externally exposed port.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Exposed Port Number"),
            @ExtensionProperty(name = "minimum", value = "1"),
            @ExtensionProperty(name = "maximum", value = "65535"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The port number of the externally exposed port.")
        }
    )})
    @Min(value = 1, message = "must be at least 1")
    @Max(value = 65535, message = "must be at most 65535")
    private int port;

    /**
     * The type (protocol) of the port (Pivio -> transport_protocol).
     * Default port type is {@link MicoPortType#TCP}.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Type"),
            @ExtensionProperty(name = "default", value = "TCP"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "The type (protocol) of the port. TCP or UDP.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private MicoPortType type = MicoPortType.TCP;

    /**
     * The port inside the container.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Target Port Number"),
            @ExtensionProperty(name = "minimum", value = "1"),
            @ExtensionProperty(name = "maximum", value = "65535"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The port inside the container.")
        }
    )})
    @Min(value = 1, message = "must be at least 1")
    @Max(value = 65535, message = "must be at most 65535")
    private int targetPort;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
     * Creates an instance of {@code MicoServicePortRequestDTO} based on a
     * {@code MicoServicePort}.
     * 
     * @param servicePort the {@link MicoServicePort}.
     */
    public MicoServicePortRequestDTO(MicoServicePort servicePort) {
    	this.port = servicePort.getPort();
    	this.type = servicePort.getType();
    	this.targetPort = servicePort.getTargetPort();
    }

}
