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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents basic information for a Pod in Kubernetes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KubernetesPodInfoDTO {

    /**
     * Name of the {@link io.fabric8.kubernetes.api.model.Pod}.
     */
    private String podName;

    /**
     * States the lifecycle of a pod:
     * Pending, Running, Succeeded, Failed, Unknown, Completed, CrashLoopBackOff
     */
    private String phase;

    /**
     * IP address of the host to which the pod is assigned.
     */
    private String hostIp;

    /**
     * Name of the node the pod is running on.
     */
    private String nodeName;

    /**
     * Counter for restarts of all container of this pod.
     */
    private int restarts;

    /**
     * Contains the start time of the pod.
     */
    private String age;

    /**
     * Information about used hardware resources (CPU/RAM)
     */
    private KuberenetesPodMetricsDTO metrics;
    
}
