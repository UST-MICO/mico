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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import io.github.ust.mico.core.dto.request.MicoServiceInterfaceRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a interface, e.g., REST API, of a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoServiceInterface {

    /**
     * The id of this service interface.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The name of this {@link MicoServiceInterface}.
     * Pattern is the same than the one for Kubernetes Service names.
     */
    private String serviceInterfaceName;

    /**
     * The list of ports.
     * Must not be empty.
     */
    @Relationship(type = "PROVIDES", direction = Relationship.UNDIRECTED)
    private List<MicoServicePort> ports = new ArrayList<>();


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The public DNS.
     */
    private String publicDns;

    /**
     * Human readable description of this service interface,
     * e.g., the functionality provided.
     * {@code null} values are skipped.
     */
    private String description;

    /**
     * The protocol of this interface, e.g., HTTPS.
     */
    private String protocol;

    /**
     * The transport protocol, e.g., TCP.
     */
    private String transportProtocol;


    // ----------------------
    // -> Static Creators ---
    // ----------------------
    
    /**
     * Creates a new {@code MicoServiceInterface} based on a {@code MicoServiceInterfaceRequestDTO}.
     * Note that the id will be set to {@code null}.
     * 
     * @param serviceInterfaceDto the {@link MicoServiceInterfaceRequestDTO}.
     * @return a {@link MicoServiceInterface}.
     */
    public static MicoServiceInterface valueOf(MicoServiceInterfaceRequestDTO serviceInterfaceDto) {
        return new MicoServiceInterface()
        	.setServiceInterfaceName(serviceInterfaceDto.getServiceInterfaceName())
        	.setPorts(serviceInterfaceDto.getPorts().stream().map(MicoServicePort::valueOf).collect(Collectors.toList()))
        	.setPublicDns(serviceInterfaceDto.getPublicDns())
        	.setDescription(serviceInterfaceDto.getDescription())
        	.setProtocol(serviceInterfaceDto.getProtocol())
        	.setTransportProtocol(serviceInterfaceDto.getTransportProtocol());
    }

}
