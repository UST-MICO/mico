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
import lombok.*;

import java.util.List;

/**
 * BuildStatus is the status for a Build resource
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BuildStatus {

    /**
     * Optional. Cluster provides additional information if the builder is Cluster.
     */
    private ClusterSpec cluster;
    // private GoogleSpec google;
    // private Time startTime;
    // private Time completionTime;
    // private List<ContainerState> stepStatus;

    /**
     * Optional. StepsCompleted lists the name of build steps completed.
     */
    private List<String> stepsCompleted;

    // private BuildProvider builder;

    // private GoogleSpec google;

    // private Time startTime;

    // private Time completionTime;

    // private StepStates stepStates;

    // private Conditions conditions;
}
