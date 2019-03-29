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

import org.neo4j.ogm.annotation.*;

import com.fasterxml.jackson.annotation.*;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * Represents a dependency of a {@link MicoService}.
 * <p>
 * Instances of this class are persisted as relationships between nodes
 * of the type {@link MicoService} in the Neo4j database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@RelationshipEntity(type = "DEPENDS_ON")
public class MicoServiceDependency {

    /**
     * The id of this service dependency.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required Fields ---
    // ----------------------

    /**
     * This is the {@link MicoService} that requires (depends on)
     * the {@link MicoServiceDependency#dependedService}.
     */
    @JsonBackReference
    @StartNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MicoService service;

    /**
     * This is the {@link MicoService} depended by
     * {@link MicoServiceDependency#service}.
     */
    @ApiModelProperty(required = true)
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id", scope=MicoService.class)
    @EndNode
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private MicoService dependedService;

}
