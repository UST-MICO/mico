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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents a dependency of a {@link MicoService}.
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
    // -> Required fields ---
    // ----------------------

    /**
     * This is the {@link MicoService} that requires (depends on)
     * the {@link MicoServiceDependency#dependedService}.
     */
    @JsonIgnore
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id", scope=MicoService.class)
    @StartNode
    @EqualsAndHashCode.Exclude
    private MicoService service;

    /**
     * This is the {@link MicoService} depended by
     * {@link MicoServiceDependency#service}.
     */
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id", scope=MicoService.class)
    @ApiModelProperty(required = true)
    @EndNode
    @EqualsAndHashCode.Exclude
    private MicoService dependedService;

    /**
     * The minimum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private String minVersion;

    /**
     * The maximum version of the depended service
     * that is supported.
     */
    @ApiModelProperty(required = true)
    private String maxVersion;

    @JsonIgnore
    public MicoVersion getMinMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.minVersion);
        return micoVersion;
    }

    @JsonIgnore
    public MicoVersion getMaxMicoVersion() throws VersionNotSupportedException {
        MicoVersion micoVersion = MicoVersion.valueOf(this.maxVersion);
        return micoVersion;
    }

}
