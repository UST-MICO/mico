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

package io.github.ust.mico.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DTO for a using application that holds name, shortName, and version of a {@link io.github.ust.mico.core.model.MicoApplication}.
 * Used when the using applications of shared services are requested.
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicoUsingApplicationDTO {

    /**
     * Name of the {@link io.github.ust.mico.core.model.MicoApplication}.
     */
    private String name;

    /**
     * Short name of the {@link io.github.ust.mico.core.model.MicoApplication}.
     */
    private String shortName;

    /**
     * Version of the {@link io.github.ust.mico.core.model.MicoApplication}.
     */
    private String version;

}
