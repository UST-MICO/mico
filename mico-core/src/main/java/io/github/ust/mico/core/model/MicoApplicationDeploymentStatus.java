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

	/** 
	 * Indicates that a {@link MicoApplication} with all its {@link MicoService}
	 * has been deployed successfully.
	 */
	@JsonProperty("Deployed")
    DEPLOYED("Deployed"),
    
    /** 
	 * Indicates that a {@link MicoApplication} with all its {@link MicoService}
	 * has been undeployed successfully.
	 */
    @JsonProperty("Undeployed")
	UNDEPLOYED("Undeployed"),
	
	/**
	 * Indicates that a {@link MicoApplication} is currently being
	 * deployed / undeployed.
	 */
    @JsonProperty("Pending")
    PENDING("Pending"),
    
    /**
	 * Indicates that the deployment / undeployment of a {@link MicoApplication}
	 * did not complete due to at least one {@link MicoService} of the {@code MicoApplication}
	 * that couldn't be deployed / undeployed successfully.
	 */
    @JsonProperty("Incompleted")
    INCOMPLETED("Incompleted"),
    
    /**
	 * Indicates that the current deployment status of a {@link MicoApplication}
	 * is not known.
	 */
    @JsonProperty("Unknown")
    UNKNOWN("Unknown");
	
	private final String value;
	
    @Override
    public String toString() {
        return value;
    }

}