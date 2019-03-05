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

import io.github.ust.mico.core.model.MicoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoServiceDependencyGraphEdgeDTO {

    private String sourceShortName;
    private String sourceVersion;
    private String targetShortName;
    private String targetVersion;

    public MicoServiceDependencyGraphEdgeDTO(MicoService source, MicoService target){
        this.sourceShortName = source.getShortName();
        this.sourceVersion = source.getVersion();
        this.targetShortName = target.getShortName();
        this.targetVersion = target.getVersion();
    }

}
