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

import io.github.ust.mico.core.dto.request.MicoApplicationRequestDTO;
import io.github.ust.mico.core.exception.VersionNotSupportedException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an application as a set of {@link MicoService}s
 * in the context of MICO.
 * <p>
 * Instances of this class are persisted as nodes in the Neo4j database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoApplication {

    /**
     * The id of this application.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required Fields ---
    // ----------------------

    /**
     * A brief name for the application intended
     * for use as a unique identifier.
     * To be consistent we want to be compatible with Kubernetes resource names,
     * therefore it must match the Kubernetes naming pattern.
     */
    private String shortName;

    /**
     * The name of the artifact. Intended for humans.
     * Required only for the usage in the UI.
     */
    private String name;

    /**
     * The version of this application.
     */
    private String version;

    /**
     * Human readable description of this application.
     * Is allowed to be empty (default). {@code null} values are skipped.
     */
    private String description;


    // ----------------------
    // -> Optional Fields ---
    // ----------------------

    /**
     * The list of included {@link MicoService MicoServices}.
     */
    @Relationship(type = "INCLUDES")
    private List<MicoService> services = new ArrayList<>();

    /**
     * The list of service deployment information this application uses
     * for the deployment of the included services.
     */
    @Relationship(type = "PROVIDES")
    private List<MicoServiceDeploymentInfo> serviceDeploymentInfos = new ArrayList<>();

    /**
     * The list of service deployment information this application uses
     * for the deployment of the KafkaFaasConnector multiple times.
     */
    @Relationship(type = "PROVIDES_KF_CONNECTOR")
    private List<MicoServiceDeploymentInfo> kafkaFaasConnectorDeploymentInfos = new ArrayList<>();

    /**
     * Human readable contact information for support purposes.
     */
    private String contact;

    /**
     * Human readable information for the application owner
     * who is responsible for this application.
     */
    private String owner;

    public MicoVersion getMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.version);
        return micoVersion;
    }


    // ----------------------
    // -> Static Creators ---
    // ----------------------

    /**
     * Creates a new {@code MicoApplication} based on a {@code MicoApplicationRequestDTO}.
     * Note that the id will be set to {@code null}.
     *
     * @param applicationDto the {@link MicoApplicationRequestDTO}.
     * @return a {@link MicoApplication}.
     */
    public static MicoApplication valueOf(MicoApplicationRequestDTO applicationDto) {
        return new MicoApplication()
            .setShortName(applicationDto.getShortName())
            .setName(applicationDto.getName())
            .setVersion(applicationDto.getVersion())
            .setDescription(applicationDto.getDescription())
            .setContact(applicationDto.getContact())
            .setOwner(applicationDto.getOwner());
    }

}
