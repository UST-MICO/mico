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

import lombok.Value;

/**
 * Represents a resource constraint specifying the CPU units
 * and memory. Can be used as a upper (limiting) and
 * lower (requesting) constraint.
 */
@Value
public class MicoResourceConstraint {

    /**
     * Measured in CPU units. One Kubernetes CPU (unit) is equivaletnt to:
     * - 1 AWS vCPU
     * - 1 GCP Core
     * - 1 Azure vCore
     * - 1 IBM vCPU
     * - 1 Hyperthread on a bare-metal Intel processor with Hyperthreading
     * Can also be specified as a fraction up to precision 0.001.
     */
    private final double cpuUnits;

    /**
     * Memory in bytes.
     */
    private final long memoryInBytes;

}
