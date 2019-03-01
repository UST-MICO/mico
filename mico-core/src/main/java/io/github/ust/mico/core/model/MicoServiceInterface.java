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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.util.Patterns;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.data.neo4j.annotation.QueryResult;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a interface, e.g., REST API, of a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
@QueryResult
public class MicoServiceInterface {

    /**
     * The id of this service interface.
     */
    @Id
    @GeneratedValue
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;


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
            @ExtensionProperty(name = "pattern", value = Patterns.Constants.KUBERNETES_NAMING_REGEX),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The name of this MicoServiceInterface")
        }
    )})
    @Pattern(regexp = Patterns.Constants.KUBERNETES_NAMING_REGEX, message = Patterns.Constants.KUBERNETES_NAMING_MESSAGE)
    private String serviceInterfaceName;

    /**
     * The list of ports.
     * Must not be empty.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Ports"),
            @ExtensionProperty(name = "pattern", value = Patterns.Constants.NOT_EMPTY_REGEX),
            @ExtensionProperty(name = "x-order", value = "200"),
            @ExtensionProperty(name = "description", value = "The list of the interfaces ports.\n" +
                " Must not be empty.")
        }
    )})
    @Relationship(type = "PROVIDES_PORTS", direction = Relationship.UNDIRECTED)
    @NotEmpty
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
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Description"),
            @ExtensionProperty(name = "x-order", value = "110"),
            @ExtensionProperty(name = "description", value = "Human readable description of this service interface.")
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

}
