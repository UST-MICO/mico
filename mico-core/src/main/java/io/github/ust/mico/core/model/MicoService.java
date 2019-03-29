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

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import io.github.ust.mico.core.dto.request.MicoServiceRequestDTO;
import io.github.ust.mico.core.exception.VersionNotSupportedException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a service in the context of MICO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoService {

    /**
     * The id of this service.
     * MUST be readable and writable from the perspective of the Jackson mapper
     * to handle recursive service dependencies.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required Fields ---
    // ----------------------

    /**
     * A brief name for the service.
     * In conjunction with the version it must be unique.
     * Pattern is the same as the one for Kubernetes Service names.
     */
    private String shortName;

    /**
     * The name of the artifact. Intended for humans.
     * Required only for the usage in the UI.
     */
    private String name;

    /**
     * The version of this service.
     * E.g. the GitHub release tag.
     */
    private String version;

    /**
     * Human readable description of this service.
     * Is allowed to be empty (default). {@code null} values are skipped.
     */
    private String description;

    /**
     * Indicates where this service originates from, e.g.,
     * GitHub (downloaded and built by MICO) or DockerHub
     * (ready-to-use image).
     * {@code null} is ignored.
     */
    private MicoServiceCrawlingOrigin serviceCrawlingOrigin;


    // ----------------------
    // -> Optional Fields ---
    // ----------------------

    /**
     * The list of interfaces this service provides.
     * Is read only. Use special API for updating.
     */
    @Relationship(type = "PROVIDES", direction = Relationship.UNDIRECTED)
    private List<MicoServiceInterface> serviceInterfaces = new ArrayList<>();

    /**
     * The list of services that this service requires
     * in order to run normally.
     * Is read only. Use special API for updating.
     */
    @Relationship(type = "DEPENDS_ON")
    private List<MicoServiceDependency> dependencies = new ArrayList<>();

    /**
     * Human readable contact information for support purposes.
     */
    private String contact;

    /**
     * Human readable information for the service owner
     * who is responsible for this service.
     */
    private String owner;

    /**
     * The URL that could be used for a git clone,
     * to clone the current master branch.
     */
    private String gitCloneUrl;

    /**
     * The path to the Dockerfile must be relative to the root folder of the git repository.
     */

    private String dockerfilePath;

    /**
     * The fully qualified URI to the image on DockerHub.
     * Either set after the image has been built by MICO
     * (if the service originates from GitHub) or set by the
     * user directly.
     */
    private String dockerImageUri;

    public MicoVersion getMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.version);
        return micoVersion;
    }


    // ----------------------
    // -> Static Creators ---
    // ----------------------
    
    /**
     * Creates a new {@code MicoService} based on a {@code MicoServiceRequestDTO}.
     * Note that the id will be set to {@code null}.
     * 
     * @param serviceDto the {@link MicoServiceRequestDTO}.
     * @return a {@link MicoService}.
     */
    public static MicoService valueOf(MicoServiceRequestDTO serviceDto) {
        return new MicoService()
                .setShortName(serviceDto.getShortName())
                .setName(serviceDto.getName())
                .setVersion(serviceDto.getVersion())
                .setDescription(serviceDto.getDescription())
                .setContact(serviceDto.getContact())
                .setOwner(serviceDto.getOwner())
                .setGitCloneUrl(serviceDto.getGitCloneUrl())
                .setDockerfilePath(serviceDto.getDockerfilePath())
                .setDockerImageUri(serviceDto.getDockerImageUri());
    }

}
