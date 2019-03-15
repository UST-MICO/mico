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

import org.neo4j.ogm.annotation.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Represents the information necessary for deploying a {@link MicoApplication}.
 * DTO is {@link MicoServiceDeploymentInfoResponseDTO}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@RelationshipEntity(type = "INCLUDES_SERVICE")
public class MicoServiceDeploymentInfo {

    /**
     * The id of this service deployment info.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The {@link MicoApplication} that uses a {@link MicoService}
     * this deployment information refers to.
     */
    @JsonBackReference
    @StartNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MicoApplication application;

    /**
     * The {@link MicoService} this deployment information refers to.
     */
    @EndNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MicoService service;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * Number of desired instances. Defaults to 1.
     */
    private int replicas = 1;

    /**
     * Those labels are key-value pairs that are attached to the deployment
     * of this service. Intended to be used to specify identifying attributes
     * that are meaningful and relevant to users, but do not directly imply
     * semantics to the core system. Labels can be used to organize and to select
     * subsets of objects. Labels can be attached to objects at creation time and
     * subsequently added and modified at any time.
     * Each key must be unique for a given object.
     */
    private List<MicoLabel> labels = new ArrayList<>();

    /**
     * Indicates whether and when to pull the image.
     * Default image pull policy is {@link ImagePullPolicy#ALWAYS}.
     */
    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.ALWAYS;

    /**
     * Restart policy for all containers.
     * Default restart policy is {@link RestartPolicy#ALWAYS}.
     */
    private RestartPolicy restartPolicy = RestartPolicy.ALWAYS;



    /**
     * Applies the values of all properties of a
     * {@code MicoServiceDeploymentInfoRequestDTO} to this
     * {@code MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoRequestDTO}.
     * @return this {@link MicoServiceDeploymentInfo} with the values
     * of the properties of the given {@link MicoServiceDeploymentInfoRequestDTO}.
     */
    public MicoServiceDeploymentInfo applyValuesFrom(MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) {
        return setReplicas(serviceDeploymentInfoDTO.getReplicas())
            .setLabels(serviceDeploymentInfoDTO.getLabels())
            .setImagePullPolicy(serviceDeploymentInfoDTO.getImagePullPolicy())
            .setRestartPolicy(serviceDeploymentInfoDTO.getRestartPolicy());
    }

    /**
     * Applies the values of all properties of a
     * {@code MicoServiceDeploymentInfoResponseDTO} to this
     * {@code MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoResponseDTO}.
     * @return this {@link MicoServiceDeploymentInfo} with the values
     * of the properties of the given {@link MicoServiceDeploymentInfoResponseDTO}.
     */
    public MicoServiceDeploymentInfo applyValuesFrom(MicoServiceDeploymentInfoResponseDTO serviceDeploymentInfoDTO) {
        return applyValuesFrom((MicoServiceDeploymentInfoRequestDTO) serviceDeploymentInfoDTO);
    }


    /**
     * Enumeration for the different policies specifying
     * when to pull an image.
     */
    public enum ImagePullPolicy {

        ALWAYS,
        NEVER,
        IF_NOT_PRESENT
    }


    /**
     * Enumeration for all supported restart policies.
     */
    public enum RestartPolicy {

        ALWAYS,
        ON_FAILURE,
        NEVER
    }

}
