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

import org.neo4j.ogm.annotation.NodeEntity;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.ust.mico.core.model.MicoMessage.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Wraps the deployment status of a {@link MicoApplication}
 * and an optional message with more detailed information.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class MicoApplicationDeploymentStatus {

    // ----------------------
    // -> Required Fields ---
    // ----------------------
	
	/**
	 * The actual status value.
	 */
	private final Value value;


    // ----------------------
    // -> Optional Fields ---
    // ----------------------
	
	/**
	 * An optional message than can be passed along with
	 * status to provide more detailed information.
	 */
	private MicoMessage message;


    // ----------------------
    // -> Static Creators ---
    // ----------------------
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#DEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO}.
	 * 
	 * @param message the content of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus deployed(String message) {
		return new MicoApplicationDeploymentStatus(Value.DEPLOYED).setMessage(new MicoMessage(message, Type.INFO));
	}
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#DEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type.
	 * 
	 * @param message the content of the message.
	 * @param messageType the {@link Type} of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus deployed(String message, Type messageType) {
		return new MicoApplicationDeploymentStatus(Value.DEPLOYED).setMessage(new MicoMessage(message, messageType));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNDEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO}.
	 * 
	 * @param message the content of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus undeployed(String message) {
		return new MicoApplicationDeploymentStatus(Value.UNDEPLOYED).setMessage(new MicoMessage(message, Type.INFO));
	}
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNDEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type.
	 * 
	 * @param message the content of the message.
	 * @param messageType the {@link Type} of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus undeployed(String message, Type messageType) {
		return new MicoApplicationDeploymentStatus(Value.UNDEPLOYED).setMessage(new MicoMessage(message, messageType));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#PENDING} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO}.
	 * 
	 * @param message the content of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus pending(String message) {
		return new MicoApplicationDeploymentStatus(Value.PENDING).setMessage(new MicoMessage(message, Type.INFO));
	}
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#PENDING} as well as a {@code MicoMessage}
	 * with the given message content and type.
	 * 
	 * @param message the content of the message.
	 * @param messageType the {@link Type} of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus pending(String message, Type messageType) {
		return new MicoApplicationDeploymentStatus(Value.PENDING).setMessage(new MicoMessage(message, messageType));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#INCOMPLETED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#ERROR}.
	 * 
	 * @param message the content of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus incompleted(String message) {
		return new MicoApplicationDeploymentStatus(Value.INCOMPLETED).setMessage(new MicoMessage(message, Type.ERROR));
	}
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#INCOMPLETED} as well as a {@code MicoMessage}
	 * with the given message content and type.
	 * 
	 * @param message the content of the message.
	 * @param messageType the {@link Type} of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus incompleted(String message, Type messageType) {
		return new MicoApplicationDeploymentStatus(Value.INCOMPLETED).setMessage(new MicoMessage(message, messageType));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNKNOWN} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO}.
	 * 
	 * @param message the content of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus unknown(String message) {
		return new MicoApplicationDeploymentStatus(Value.UNKNOWN).setMessage(new MicoMessage(message, Type.INFO));
	}
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNKNOWN} as well as a {@code MicoMessage}
	 * with the given message content and type.
	 * 
	 * @param message the content of the message.
	 * @param messageType the {@link Type} of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus unknown(String message, Type messageType) {
		return new MicoApplicationDeploymentStatus(Value.UNKNOWN).setMessage(new MicoMessage(message, messageType));
	}
	

	/**
	 * Enumeration for the different values of a
	 * {@link MicoApplicationDeploymentStatus}.
	 */
	@AllArgsConstructor
	public enum Value {
		
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

}