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

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.ust.mico.core.dto.request.MicoEnvironmentVariableRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * An environment variable represented as a simple key-value pair.
 * Necessary since Neo4j does not allow to persist
 * properties of composite types.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoEnvironmentVariable {
	
	@Id
	@GeneratedValue
	private Long id;

    /**
     * Name of the environment variable.
     */
    private String name;

    /**
     * Value of the environment variable.
     */
    private String value;


    // ----------------------
    // -> Static Creators ---
    // ----------------------
    
    /**
     * Creates a new {@code MicoEnvironmentVariable} based on a {@code MicoEnvironmentVariableRequestDTO}.
     * Note that the id will be set to {@code null}.
     * 
     * @param environmentVariableDto the {@link MicoEnvironmentVariableRequestDTO}.
     * @return a {@link MicoEnvironmentVariable}.
     */
    public static MicoEnvironmentVariable valueOf(MicoEnvironmentVariableRequestDTO environmentVariableDto) {
        return new MicoEnvironmentVariable()
                .setName(environmentVariableDto.getName())
                .setValue(environmentVariableDto.getValue());
    }

}
