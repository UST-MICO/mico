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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoApplication} including all of
 * its associated {@link MicoService MicoServices}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationWithServicesResponseDTO extends MicoApplicationResponseDTO {

    /**
     * All {@link MicoService MicoServices} of the {@link MicoApplication}.
     */
    @ApiModelProperty(extensions = {@Extension(
            name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
            properties = {
                @ExtensionProperty(name = "title", value = "Services"),
                @ExtensionProperty(name = "x-order", value = "100"),
                @ExtensionProperty(name = "description", value = "All services of the application.")
            }
        )})
    private List<MicoService> services = new ArrayList<>();
    
    
    // ----------------------
    // -> Static Creators ---
    // ----------------------
    
    /**
     * Creates a {@code MicoApplicationWithServicesDTO} based on a
     * {@link MicoApplication}. Note that the deployment status of the application
     * needs to be set explicitly since it cannot be inferred
     * from the given {@link MicoApplication} itself.
     * 
     * @param application the {@link MicoApplication}.
     * @return a {@link MicoApplicationWithServicesResponseDTO} with all the values
     *         of the given {@code MicoApplication}. 
     */
    public static MicoApplicationWithServicesResponseDTO valueOf(MicoApplication application) {
        return createBase(application)
                .setServices(application.getServiceDeploymentInfos().stream().map(sdi -> sdi.getService())
                        .collect(Collectors.toList()));
    }
    
    /**
     * Creates a {@code MicoApplicationWithServicesDTO} based on a
     * {@code MicoApplication}.
     * 
     * @param application the {@link MicoApplication}.
     * @param deploymentStatus indicates the current {@link MicoApplicationDeploymentStatus}.
     * @return a {@link MicoApplicationWithServicesResponseDTO} with all the values
     *         of the given {@code MicoApplication}. 
     */
    public static MicoApplicationWithServicesResponseDTO valueOf(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus) {
        return ((MicoApplicationWithServicesResponseDTO) createBase(application)
                .setDeploymentStatus(deploymentStatus))
                .setServices(application.getServiceDeploymentInfos().stream().map(sdi -> sdi.getService())
                        .collect(Collectors.toList()));
    }
    
    // Sets all direct application fields.
    private static MicoApplicationWithServicesResponseDTO createBase(MicoApplication application) {
        return (MicoApplicationWithServicesResponseDTO) new MicoApplicationWithServicesResponseDTO()
                .setShortName(application.getShortName())
                .setName(application.getName())
                .setVersion(application.getVersion())
                .setDescription(application.getDescription())
                .setContact(application.getContact())
                .setOwner(application.getOwner());
    }

}
