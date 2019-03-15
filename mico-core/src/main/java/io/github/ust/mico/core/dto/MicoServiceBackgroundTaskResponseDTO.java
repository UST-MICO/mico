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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoServiceBackgroundTask;
import io.github.ust.mico.core.model.MicoServiceBackgroundTask.Status;
import io.github.ust.mico.core.model.MicoService;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoServiceBackgroundTask} intended to use with responses only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MicoServiceBackgroundTaskResponseDTO {

	/**
	 * The generated job id.
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Job Id"),
	        @ExtensionProperty(name = "x-order", value = "10"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The generated job id.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String id;

	/**
	 * The short name of the corresponding {@link MicoService}.
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Service Short Name"),
	        @ExtensionProperty(name = "x-order", value = "20"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The name of the corresponding service.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String serviceShortName;

	/**
	 * The version to the corresponding {@link MicoService}.
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Service Version"),
	        @ExtensionProperty(name = "x-order", value = "30"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The version of the corresponding service.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String serviceVersion;

	/**
	 * The type of job.
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Job Type"),
	        @ExtensionProperty(name = "x-order", value = "40"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The type of job.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private MicoServiceBackgroundTask.Type type;

	/**
	 * The current {@link Status} of the job.
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Job Status"),
	        @ExtensionProperty(name = "x-order", value = "50"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "The current status of the job.")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private MicoServiceBackgroundTask.Status status;

	/**
	 * An error message in case the has failed.
	 */
	@ApiModelProperty(extensions = {
	    @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
	        @ExtensionProperty(name = "title", value = "Error Message"),
	        @ExtensionProperty(name = "x-order", value = "60"),
	        @ExtensionProperty(name = "readOnly", value = "true"),
	        @ExtensionProperty(name = "description", value = "An error message in case the has failed")
	    })
	})
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String errorMessage;

	
	// -------------------
	// -> Constructors ---
	// -------------------

	/**
	 * Creates a {@code MicoBackgroundTaskResponseDTO} based on a
	 * {@code MicoServiceBackgroundTask}.
	 *
	 * @param task the {@link MicoServiceBackgroundTask}.
	 * @return a {@link MicoServiceBackgroundTaskResponseDTO} with all the values of the
	 *         given {@code MicoServiceBackgroundTask}.
	 */
	public MicoServiceBackgroundTaskResponseDTO(MicoServiceBackgroundTask serviceBackgroundTask) {
		this.id = serviceBackgroundTask.getId();
		this.serviceShortName = serviceBackgroundTask.getServiceShortName();
		this.serviceVersion = serviceBackgroundTask.getServiceVersion();
		this.type = serviceBackgroundTask.getType();
		this.status = serviceBackgroundTask.getStatus();
		this.errorMessage = serviceBackgroundTask.getErrorMessage();
	}

}
