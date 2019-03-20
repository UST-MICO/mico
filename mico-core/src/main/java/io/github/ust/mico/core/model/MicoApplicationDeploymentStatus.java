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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;

/**
 * Enumeration for all possible states a deployment of
 * a {@link MicoApplication} can be in.
 */
@AllArgsConstructor
public enum MicoApplicationDeploymentStatus {

	@JsonProperty("Deployed")
    DEPLOYED("Deployed"),
    @JsonProperty("Pending")
    PENDING("Pending"),
    @JsonProperty("Incompleted")
    INCOMPLETED("Incompleted"),
    @JsonProperty("NotDeployed")
    NOT_DEPLOYED("NotDeployed"),
    @JsonProperty("Unknown")
    UNKNOWN("Unknown");
	
	private final String value;
	
    @Override
    public String toString() {
        return value;
    }

}