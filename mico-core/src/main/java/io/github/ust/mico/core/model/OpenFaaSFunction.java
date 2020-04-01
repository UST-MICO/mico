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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.ust.mico.core.dto.request.MicoEnvironmentVariableRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Represents an OpenFaaS function as used by the KafkaFaaSConnector
 * <p>
 * Instances of this class are persisted as nodes in the Neo4j database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@NodeEntity
public class OpenFaaSFunction {

    public OpenFaaSFunction(Long id, String name){
        this.id = id;
        this.name = name;
    }

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Name of the OpenFaaS Function
     */
    private String name;

    /**
     * Configuration of the OpenFaaS Function as JSON string ,e.g., to configure filter critera for a function that
     * implements a Message Filter
     */
    private String configuration;

}
