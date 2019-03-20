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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.dto.response.KubernetesDeploymentInfoResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Information about the Kubernetes resources
 * that are created through an actual deployment of a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class KubernetesDeploymentInfo {

    /**
     * The id of this Kubernetes deployment info.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The namespace in which the Kubernetes {@link Deployment} is created.
     */
    private String namespace;

    /**
     * The name of the Kubernetes {@link Deployment} created by a {@link MicoService}
     */
    private String deploymentName;

    /**
     * The names of the Kubernetes {@link Service Services}
     * created by {@link MicoServiceInterface MicoServiceInterfaces}
     */
    private List<String> serviceNames = new ArrayList<>();

    /**
     * Applies the values of all properties of a
     * {@code KubernetesDeploymentInfoResponseDTO} to this
     * {@code KubernetesDeploymentInfo}. Note that the id
     * will not be affected.
     *
     * @param kubernetesDeploymentInfoDto the {@link KubernetesDeploymentInfoResponseDTO}.
     * @return this {@link KubernetesDeploymentInfo} with the values
     * of the properties of the given {@link KubernetesDeploymentInfoResponseDTO}.
     */
    public KubernetesDeploymentInfo applyValuesFrom(KubernetesDeploymentInfoResponseDTO kubernetesDeploymentInfoDto) {
        return setNamespace(kubernetesDeploymentInfoDto.getNamespace())
            .setDeploymentName(kubernetesDeploymentInfoDto.getDeploymentName())
            .setServiceNames(kubernetesDeploymentInfoDto.getServiceNames());
    }


    // ----------------------
    // -> Static creators ---
    // ----------------------

    /**
     * Creates a new {@code KubernetesDeploymentInfo} based on a {@code KubernetesDeploymentInfoResponseDTO}.
     * Note that the id will be set to {@code null}.
     *
     * @param kubernetesDeploymentInfoDto the {@link KubernetesDeploymentInfoResponseDTO}.
     * @return a {@link KubernetesDeploymentInfo}.
     */
    public static KubernetesDeploymentInfo valueOf(KubernetesDeploymentInfoResponseDTO kubernetesDeploymentInfoDto) {
        return new KubernetesDeploymentInfo()
            .setNamespace(kubernetesDeploymentInfoDto.getNamespace())
            .setDeploymentName(kubernetesDeploymentInfoDto.getDeploymentName())
            .setServiceNames(kubernetesDeploymentInfoDto.getServiceNames());
    }

}
