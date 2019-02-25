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

package io.github.ust.mico.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for deployment information of {@link io.github.ust.mico.core.model.MicoService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoServiceStatusDTO {

    /**
     * Name of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private String name;

    /**
     * shortName of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private String shortName;

    /**
     * Version of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private String version;

    /**
     * Counter for number of replicas that should be available.
     */
    private int requestedReplicas;

    /**
     * Counter for replicas that are actually available.
      */
    private int availableReplicas;

    /**
     * Each item in this list represents a Kubernetes Service.
     */
    private List<MicoServiceInterfaceDTO> interfacesInformation = new ArrayList<>();

    /**
     * List of {@link io.fabric8.kubernetes.api.model.Pod}s of a {@link io.github.ust.mico.core.model.MicoService}.
     */
    private List<KubernetesPodInfoDTO> podInfo = new ArrayList<>();

    /**
     * Average cpu load in all pods of a {@link io.github.ust.mico.core.model.MicoService}
     */
    private int averageCpuLoad;

    /**
     * Average memory usage in all pods of a {@link io.github.ust.mico.core.model.MicoService}
     */
    private int averageMemoryUsage;

}
