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

package io.github.ust.mico.core.service.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * A single application container that you want to run within a pod.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BuildStep {

    /**
     * Name of the container specified as a DNS_LABEL.
     * Each container in a pod must have a unique name (DNS_LABEL).
     * Cannot be updated.
     */
    private String name;

    /**
     * Optional. Docker image name.
     * More info: https://kubernetes.io/docs/concepts/containers/images
     * This field is optional to allow higher level config management to default or override
     * container images in workload controllers like Deployments and StatefulSets.
     */
    private String image;

    /**
     * Optional. Entrypoint array. Not executed within a shell.
     * The docker image's ENTRYPOINT is used if this is not provided.
     * Variable references $(VAR_NAME) are expanded using the container's environment. If a variable
     * cannot be resolved, the reference in the input string will be unchanged. The $(VAR_NAME) syntax
     * can be escaped with a double $$, ie: $$(VAR_NAME). Escaped references will never be expanded,
     * regardless of whether the variable exists or not.
     * Cannot be updated.
     * More info: https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell
     */
    private List<String> command = new ArrayList<>();

    /**
     * Optional. Arguments to the entrypoint.
     * The docker image's CMD is used if this is not provided.
     * Variable references $(VAR_NAME) are expanded using the container's environment. If a variable
     * cannot be resolved, the reference in the input string will be unchanged. The $(VAR_NAME) syntax
     * can be escaped with a double $$, ie: $$(VAR_NAME). Escaped references will never be expanded,
     * regardless of whether the variable exists or not.
     * Cannot be updated.
     * More info: https://kubernetes.io/docs/tasks/inject-data-application/define-command-argument-container/#running-a-command-in-a-shell
     */
    private List<String> args = new ArrayList<>();

    /**
     * Optional. Container's working directory.
     * If not specified, the container runtime's default will be used, which
     * might be configured in the container image.
     * Cannot be updated.
     */
    private String workingDir;

    // private List<Port> ports;

    // private EnvForm envForm;

    /**
     * Optional. List of environment variables to set in the container.
     * Cannot be updated.
     */
    private List<EnvVar> env = new ArrayList<>();

    // private ResourceRequirements resources;

    // private List<VolumeMount> volumeMounts;

    // private List<VolumeDevice> volumeDevices;

    // private PullPolicy imagePullPolicy;

    // private SecurityContext securityContext;
}
