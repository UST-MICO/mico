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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A simple message associated with a {@link Type}.
 * <p>
 * Note that this class is only used for business logic purposes
 * and instances are not persisted.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoMessage {

    // ----------------------
    // -> Required Fields ---
    // ----------------------
	
	/**
	 * The actual message content.
	 */
	private String content;
	
	/**
	 * The {@link Type} of this message.
	 */
	private Type type;
	
	/**
	 * Creates a new {@code MicoMessage} instance with the
	 * type {@link Type#INFO} and the given message content.
	 * 
	 * @param content the message content as {@code String}.
	 * @return a {@link MicoMessage}.
	 */
	public static final MicoMessage info(String content) {
		return new MicoMessage(content, Type.INFO);
	}
	
	/**
	 * Creates a new {@code MicoMessage} instance with the
	 * type {@link Type#WARNING} and the given message content.
	 * 
	 * @param content the message content as {@code String}.
	 * @return a {@link MicoMessage}.
	 */
	public static final MicoMessage warning(String content) {
		return new MicoMessage(content, Type.WARNING);
	}
	
	/**
	 * Creates a new {@code MicoMessage} instance with the
	 * type {@link Type#ERROR} and the given message content.
	 * 
	 * @param content the message content as {@code String}.
	 * @return a {@link MicoMessage}.
	 */
	public static final MicoMessage error(String content) {
		return new MicoMessage(content, Type.ERROR);
	}

	
	/**
	 * Enumeration for all types of a {@code MicoInfoMessage}.
	 */
	@AllArgsConstructor
	public enum Type {
		
		@JsonProperty("Info")
		INFO("Info"),
		
		@JsonProperty("Warning")
		WARNING("Warning"),
		
		@JsonProperty("Error")
		ERROR("Error");
		
		
		private final String value;
		
		@Override
		public String toString() {
			return value;
		}
		
	}

}
