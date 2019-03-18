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
package io.github.ust.mico.core.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;
import io.github.ust.mico.core.model.MicoServiceBackgroundTask;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for a {@link MicoApplicationJobStatus} intended to use with responses only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationJobStatusResponseDTO {

	/**
	 * The aggregated status of jobs for a {@link MicoApplication} (read-only).
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Status"), @ExtensionProperty(name = "x-order", value = "10"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The aggregated status of jobs for an application.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private MicoServiceBackgroundTask.Status status;

	/**
	 * The list of jobs for a {@link MicoApplication} (read-only).
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "List of Jobs"),
	        @ExtensionProperty(name = "x-order", value = "20"), @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The list of jobs for an application.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private List<MicoServiceBackgroundTaskResponseDTO> jobs = new ArrayList<>();
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
   
	/**
	 * Creates a {@code MicoApplicationJobStatusDTO} based on a
	 * {@link MicoApplicationJobStatus}.
	 *
	 * @param applicationJobStatus the {@link MicoApplicationJobStatus}.
	 */
	public MicoApplicationJobStatusResponseDTO(MicoApplicationJobStatus applicationJobStatus) {
		this.status = applicationJobStatus.getStatus();
		this.jobs = applicationJobStatus.getJobs().stream().map(MicoServiceBackgroundTaskResponseDTO::new).collect(Collectors.toList());
	}
}
