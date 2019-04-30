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

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.ust.mico.core.dto.request.MicoInterfaceConnectionRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * An interface connection contains the the information needed to connect a {@link MicoService}
 * to an {@link MicoServiceInterface} of another {@link MicoService}.
 * <p>
 * Instances of this class are persisted as nodes in the Neo4j database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@NodeEntity
public class MicoInterfaceConnection {

    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * Name of the environment variable that is used to set the fully qualified name of an interface.
     */
    private String environmentVariableName;

    /**
     * Name of the {@link MicoServiceInterface} of another {@link MicoService}.
     */
    private String micoServiceInterfaceName;

    /**
     * Name of the {@link MicoService}.
     */
    private String micoServiceShortName;


    // ----------------------
    // -> Static Creators ---
    // ----------------------

    /**
     * Creates a new {@code MicoInterfaceConnection} based on a {@code MicoInterfaceConnectionRequestDTO}.
     *
     * @param interfaceConnectionDTO the {@link MicoInterfaceConnectionRequestDTO}.
     * @return a {@link MicoInterfaceConnection}.
     */
    public static MicoInterfaceConnection valueOf(MicoInterfaceConnectionRequestDTO interfaceConnectionDTO) {
        return new MicoInterfaceConnection()
            .setEnvironmentVariableName(interfaceConnectionDTO.getEnvironmentVariableName())
            .setMicoServiceInterfaceName(interfaceConnectionDTO.getMicoServiceInterfaceName())
            .setMicoServiceShortName(interfaceConnectionDTO.getMicoServiceShortName());
    }

}
