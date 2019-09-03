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

package io.github.ust.mico.core.resource;

import io.github.ust.mico.core.broker.MicoServiceDeploymentInfoBroker;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceDeploymentInfoResource {

    private static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";

    private static final String PATH_VARIABLE_SHORT_NAME = "micoApplicationShortName";
    private static final String PATH_VARIABLE_VERSION = "micoApplicationVersion";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "micoServiceShortName";

    @Autowired
    private MicoServiceDeploymentInfoBroker broker;

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_INFORMATION + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> getServiceDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                          @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                                          @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName) {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo;
        try {
            micoServiceDeploymentInfo = broker.getMicoServiceDeploymentInformation(shortName, version, serviceShortName);
        } catch (MicoServiceDeploymentInformationNotFoundException | MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationDoesNotIncludeMicoServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        // Convert to service deployment info DTO and return it
        MicoServiceDeploymentInfoResponseDTO serviceDeploymentInfoResponseDto = new MicoServiceDeploymentInfoResponseDTO(micoServiceDeploymentInfo);
        return ResponseEntity.ok(new Resource<>(serviceDeploymentInfoResponseDto,
            linkTo(methodOn(ServiceDeploymentInfoResource.class)
                .getServiceDeploymentInformation(shortName, version, serviceShortName)).withSelfRel()));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_INFORMATION + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> updateServiceDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                                             @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                                                                             @Valid @RequestBody MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoRequestDto) {
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo;
        try {
            updatedServiceDeploymentInfo = broker.updateMicoServiceDeploymentInformation(
                shortName, version, serviceShortName, serviceDeploymentInfoRequestDto);
        } catch (MicoApplicationNotFoundException | MicoServiceDeploymentInformationNotFoundException
            | KubernetesResourceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationDoesNotIncludeMicoServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (MicoTopicRoleUsedMultipleTimesException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        // Convert to service deployment info DTO and return it
        MicoServiceDeploymentInfoResponseDTO updatedServiceDeploymentInfoDto = new MicoServiceDeploymentInfoResponseDTO(updatedServiceDeploymentInfo);
        return ResponseEntity.ok(new Resource<>(updatedServiceDeploymentInfoDto,
            linkTo(methodOn(ServiceDeploymentInfoResource.class).getServiceDeploymentInformation(shortName, version, serviceShortName))
                .withSelfRel()));
    }

}
