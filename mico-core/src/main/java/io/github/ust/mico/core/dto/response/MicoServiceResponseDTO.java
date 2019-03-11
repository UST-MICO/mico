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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.request.MicoServiceRequestDTO;
import io.github.ust.mico.core.model.*;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoService} intended for use with responses only.
 * Note that the {@link MicoServiceDependency MicoServiceDependencies}
 * and {@link MicoServiceInterface MicoServiceInterfaces}
 * are not included.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoServiceResponseDTO extends MicoServiceRequestDTO {
	
    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * Indicates where this service originates from, e.g.,
     * GitHub (downloaded and built by MICO) or DockerHub
     * (ready-to-use image).
     * {@code null} is ignored.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Crawling Origin"),
            @ExtensionProperty(name = "default", value = "NOT_DEFINED"),
            @ExtensionProperty(name = "x-order", value = "100"),
            @ExtensionProperty(name = "description", value = "Indicates where this service originates from.")
        }
    )})
    @JsonSetter(nulls = Nulls.SKIP)
    private MicoServiceCrawlingOrigin serviceCrawlingOrigin = MicoServiceCrawlingOrigin.NOT_DEFINED;
    
    
    // -------------------
    // -> Constructors ---
    // -------------------
    
    /**
   	 * Creates an instance of this DTO based on a
   	 * {@code MicoService}.
   	 * 
   	 * @param service the {@link MicoService}.
   	 */
	public MicoServiceResponseDTO(MicoService service) {
		super(service);
		this.serviceCrawlingOrigin = service.getServiceCrawlingOrigin();
	}

}
