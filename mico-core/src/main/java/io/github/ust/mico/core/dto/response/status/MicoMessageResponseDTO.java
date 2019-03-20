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

package io.github.ust.mico.core.dto.response.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoMessage;
import io.github.ust.mico.core.model.MicoMessage.Type;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoMessage} intended to use with responses only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoMessageResponseDTO {
	
    // ----------------------
    // -> Required Fields ---
    // ----------------------
	
    /**
     * The actual message content.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Content."),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The actual message content.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String content;
    
    /**
     * The {@link Type} of the message.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Type."),
            @ExtensionProperty(name = "readOnly", value = "true"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The type of the message.")
        }
    )})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Type type;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
   
    /**
     * Creates an instance of {@code MicoMessageResponseDTO} based on a
     * {@code MicoMessage}.
     *  
     * @param message the {@link MicoMessage message}.
     */
	public MicoMessageResponseDTO(MicoMessage message) {
		this.content = message.getContent();
		this.type = message.getType();
	}
    
}
