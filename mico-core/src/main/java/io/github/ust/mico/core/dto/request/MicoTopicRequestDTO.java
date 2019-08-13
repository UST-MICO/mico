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

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoTopicRequestDTO {

    /**
     * Role of the topic. Default is {@code MicoTopicRole.Role#INPUT}
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Role"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "Role of the topic.")
        }
    )})
    @NotNull
    private MicoTopicRole.Role role;

    /**
     * Name of the topic.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Name"),
            @ExtensionProperty(name = "pattern", value = Patterns.KAFKA_TOPIC_NAME_REGEX),
            @ExtensionProperty(name = "minLength", value = "1"),
            @ExtensionProperty(name = "maxLength", value = "249"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Name of the topic.")
        }
    )})
    @Size(min = 1, max = 249, message = "must have a length between 1 and 249")
    @Pattern(regexp = Patterns.KAFKA_TOPIC_NAME_REGEX, message = Patterns.KAFKA_TOPIC_NAME_MESSAGE)
    private String name;


    // -------------------
    // -> Constructors ---
    // -------------------

    /**
     * Creates an instance of {@code MicoTopicRequestDTO} based on a
     * {@code MicoTopicRole} that includes the {@code MicoTopic} and a role.
     *
     * @param micoTopicRole the {@link MicoTopicRole}.
     */
    public MicoTopicRequestDTO(MicoTopicRole micoTopicRole) {
        this.role = micoTopicRole.getRole();
        this.name = micoTopicRole.getTopic().getName();
    }
}
