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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.ust.mico.core.model.MicoMessage.Type;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Wraps the deployment status of a {@link MicoApplication}
 * and some messages (optional) with more detailed information.
 * <p>
 * Note that this class is only used for business logic purposes
 * and is not persisted.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
	 * Some messages (optional) that can be passed along with
	 * the status to provide more detailed information.
	 */
	private List<MicoMessage> messages = new ArrayList<>();


    // ----------------------
    // -> Static Creators ---
    // ----------------------
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#DEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages one or messages.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus deployed(String... messages) {
		return new MicoApplicationDeploymentStatus(Value.DEPLOYED).setMessages(
			Arrays.asList(messages).stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#DEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages the {@link List} of messages as {@code String}.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus deployed(List<String> messages) {
		return new MicoApplicationDeploymentStatus(Value.DEPLOYED).setMessages(
			messages.stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
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
		return new MicoApplicationDeploymentStatus(Value.DEPLOYED).setMessages(
			CollectionUtils.listOf(new MicoMessage(message, messageType)));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNDEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages one or messages.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus undeployed(String... messages) {
		return new MicoApplicationDeploymentStatus(Value.UNDEPLOYED).setMessages(
			Arrays.asList(messages).stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNDEPLOYED} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages the {@link List} of messages as {@code String}.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus undeployed(List<String> messages) {
		return new MicoApplicationDeploymentStatus(Value.UNDEPLOYED).setMessages(
			messages.stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
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
		return new MicoApplicationDeploymentStatus(Value.UNDEPLOYED).setMessages(
			CollectionUtils.listOf(new MicoMessage(message, messageType)));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#PENDING} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages one or messages.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus pending(String... messages) {
		return new MicoApplicationDeploymentStatus(Value.PENDING).setMessages(
			Arrays.asList(messages).stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#PENDING} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages the {@link List} of messages as {@code String}.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus pending(List<String> messages) {
		return new MicoApplicationDeploymentStatus(Value.PENDING).setMessages(
			messages.stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
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
		return new MicoApplicationDeploymentStatus(Value.PENDING).setMessages(
			CollectionUtils.listOf(new MicoMessage(message, messageType)));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#INCOMPLETE} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#ERROR Error}.
	 * 
	 * @param messages one or messages.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus incomplete(String... messages) {
		return new MicoApplicationDeploymentStatus(Value.INCOMPLETE).setMessages(
			Arrays.asList(messages).stream().map(m -> new MicoMessage(m, Type.ERROR))
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#INCOMPLETE} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#ERROR Error}.
	 * 
	 * @param messages the {@link List} of messages as {@code String}.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus incomplete(List<String> messages) {
		return new MicoApplicationDeploymentStatus(Value.INCOMPLETE).setMessages(
			messages.stream().map(m -> new MicoMessage(m, Type.ERROR))
			.collect(Collectors.toList()));
	}
	
	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#INCOMPLETE} as well as a {@code MicoMessage}
	 * with the given message content and type.
	 * 
	 * @param message the content of the message.
	 * @param messageType the {@link Type} of the message.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus incomplete(String message, Type messageType) {
		return new MicoApplicationDeploymentStatus(Value.INCOMPLETE).setMessages(
			CollectionUtils.listOf(new MicoMessage(message, messageType)));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNKNOWN} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages one or messages.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus unknown(String... messages) {
		return new MicoApplicationDeploymentStatus(Value.UNKNOWN).setMessages(
			Arrays.asList(messages).stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a new {@code MicoApplicationDeploymentStatus} instance
	 * with the value {@link Value#UNKNOWN} as well as a {@code MicoMessage}
	 * with the given message content and type {@link Type#INFO Info}.
	 * 
	 * @param messages the {@link List} of messages as {@code String}.
	 * @return a {@link MicoApplicationDeploymentStatus}.
	 */
	public static final MicoApplicationDeploymentStatus unknown(List<String> messages) {
		return new MicoApplicationDeploymentStatus(Value.UNKNOWN).setMessages(
			messages.stream().map(m -> new MicoMessage(m, Type.INFO))
			.collect(Collectors.toList()));
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
		return new MicoApplicationDeploymentStatus(Value.UNKNOWN).setMessages(
			CollectionUtils.listOf(new MicoMessage(message, messageType)));
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
	    @JsonProperty("Incomplete")
	    INCOMPLETE("Incomplete"),
	    
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
