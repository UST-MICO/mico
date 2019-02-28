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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.dto.MicoApplicationDTO.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a {@link MicoApplication} including all of
 * its associated {@link MicoService MicoServices}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class MicoApplicationWithServicesDTO {

    /**
     * The {@link MicoApplication}.
     */
    @ApiModelProperty(extensions = {@Extension(
            name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
            properties = {
                @ExtensionProperty(name = "title", value = "Application"),
                @ExtensionProperty(name = "x-order", value = "10"),
                @ExtensionProperty(name = "description", value = "The application.")
            }
        )})
    private MicoApplicationDTO application;
    
    /**
     * All {@link MicoService MicoServices} of the {@link MicoApplication}.
     */
    @ApiModelProperty(extensions = {@Extension(
            name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
            properties = {
                @ExtensionProperty(name = "title", value = "Services"),
                @ExtensionProperty(name = "x-order", value = "20"),
                @ExtensionProperty(name = "description", value = "All services of the application.")
            }
        )})
    private List<MicoService> services = new ArrayList<>();
    
    
    /**
     * Creates a {@code MicoApplicationWithServicesDTO} based on a
     * {@link MicoApplication}. The field {@link MicoApplicationDTO#isDeployed}
     * will be unset.
     * 
     * @param application the {@link MicoApplication}.
     * @return a {@link MicoApplicationWithServicesDTO} with all the values
     *         of the given {@code MicoApplication}. 
     */
    public static MicoApplicationWithServicesDTO valueOf(MicoApplication application) {
        MicoApplicationWithServicesDTO result = new MicoApplicationWithServicesDTO()
                .setApplication(MicoApplicationDTO.valueOf(application))
                .setServices(application.getServiceDeploymentInfos().stream().map(sdi -> sdi.getService())
                        .collect(Collectors.toList()));
        
        return result;
    }
    
    /**
     * Creates a {@code MicoApplicationWithServicesDTO} based on a
     * {@link MicoApplication}.
     * 
     * @param application the {@link MicoApplication}.
     * @param deploymentStatus indicates the current {@link MicoApplicationDeploymentStatus}.
     * @return a {@link MicoApplicationWithServicesDTO} with all the values
     *         of the given {@code MicoApplication}. 
     */
    public static MicoApplicationWithServicesDTO valueOf(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus) {
        MicoApplicationWithServicesDTO result = new MicoApplicationWithServicesDTO()
                .setApplication(MicoApplicationDTO.valueOf(application).setDeploymentStatus(deploymentStatus))
                .setServices(application.getServiceDeploymentInfos().stream().map(sdi -> sdi.getService())
                        .collect(Collectors.toList()));
        
        return result;
    }
    
    /**
     * Creates a {@code MicoApplicationWithServicesDTO} based on a
     * {@link MicoApplicationDTO} and a {@link MicoService} list.
     * 
     * @param applicationDto the {@link MicoApplicationDTO}.
     * @param services the list of {@link MicoService MicoServices}.
     * @return a {@link MicoApplicationWithServicesDTO} with all the values
     *         of the given {@code MicoApplication} and {@link MicoService MicoServices}. 
     */
    public static MicoApplicationWithServicesDTO valueOf(MicoApplicationDTO applicationDto, List<MicoService> services) {
        MicoApplicationWithServicesDTO result = new MicoApplicationWithServicesDTO()
                .setApplication(applicationDto)
                .setServices(services);
        
        return result;
    }

}
