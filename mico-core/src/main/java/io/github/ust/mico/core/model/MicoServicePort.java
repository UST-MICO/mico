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

import io.github.ust.mico.core.dto.request.MicoServicePortRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a basic port with a port number and port type (protocol).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoServicePort {

    /**
     * The id of this service port.
     */
    @Id
    @GeneratedValue
    private Long id;

    
    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The port number of the externally exposed port.
     */
    private int port;

    /**
     * The type (protocol) of the port (Pivio -> transport_protocol).
     * Default port type is {@link MicoPortType#TCP}.
     */
    private MicoPortType type = MicoPortType.TCP;

    /**
     * The port inside the container.
     */
    private int targetPort;
 
    
    // ----------------------
    // -> Static Creators ---
    // ----------------------
    
    /**
     * Creates a new {@code MicoServicePort} based on a {@code MicoServicePortRequestDTO}.
     * Note that the id will be set to {@code null}.
     * 
     * @param servicePortDto the {@link MicoServicePortRequestDTO}.
     * @return a {@link MicoServicePort}.
     */
    public static MicoServicePort valueOf(MicoServicePortRequestDTO servicePortDto) {
        return new MicoServicePort()
        	.setPort(servicePortDto.getPort())
        	.setType(servicePortDto.getType())
        	.setTargetPort(servicePortDto.getTargetPort());
    }
    
}
