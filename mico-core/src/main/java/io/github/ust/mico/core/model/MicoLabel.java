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

import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a simple key-value pair label.
 * Necessary since Neo4j does not allow to persist
 * {@link Map} implementations.
 * Is used also as a DTO for requests and responses.
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoLabel {
	
	@Id
	@GeneratedValue
	private Long id;

    /**
     * Key of the label.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Key"),
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_LABEL_KEY_REGEX),
            @ExtensionProperty(name = "minLength", value = "1"),
            @ExtensionProperty(name = "maxLength", value = "253"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Key of the label.")
        }
    )})
    @Size(min = 1, max = 253, message = "must have a length between 1 and 63 without prefix or a length between 1 and 253 with prefix")
    @Pattern(regexp = Patterns.KUBERNETES_LABEL_KEY_REGEX, message = Patterns.KUBERNETES_LABEL_KEY_MESSAGE)
    private String key;

    /**
     * Value of the label.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Value"),
            @ExtensionProperty(name = "pattern", value = Patterns.KUBERNETES_LABEL_VALUE_REGEX),
            @ExtensionProperty(name = "minLength", value = "0"),
            @ExtensionProperty(name = "maxLength", value = "63"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Value of the label.")
        }
    )})
    @Size(max = 63, message = "must be 63 characters or less")
    @Pattern(regexp = Patterns.KUBERNETES_LABEL_VALUE_REGEX, message = Patterns.KUBERNETES_LABEL_VALUE_MESSAGE)
    private String value;

}
