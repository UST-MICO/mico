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
import java.util.List;

import io.github.ust.mico.core.model.MicoServiceBackgroundJob.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents the job status for a {@link MicoApplication}.
 * Contains a list of jobs.
 * <p>
 * Note that this class is only used for business logic purposes
 * and instances are not persisted.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MicoApplicationJobStatus {
	
    /**
     * The short name of the {@link MicoApplication}.
     */
    private String applicationShortName;
    
    /**
     * The version of the {@link MicoApplication}.
     */
    private String applicationVersion;
    
    /**
     * The aggregated status of jobs for the {@link MicoApplication}.
     */
    private MicoServiceBackgroundJob.Status status = Status.UNDEFINED;
    
    /**
     * The list of jobs for the {@link MicoApplication}.
     */
    private List<MicoServiceBackgroundJob> jobs = new ArrayList<>();
    
}
