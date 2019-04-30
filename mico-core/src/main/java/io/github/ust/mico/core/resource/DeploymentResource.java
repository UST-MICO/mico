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

import io.github.ust.mico.core.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.broker.DeploymentBroker;
import io.github.ust.mico.core.dto.response.MicoApplicationJobStatusResponseDTO;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;

@RestController
@RequestMapping(value = "/applications/{shortName}/{version}", produces = MediaTypes.HAL_JSON_VALUE)
public class DeploymentResource {
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private DeploymentBroker deploymentBroker;

    @PostMapping("/deploy")
    public ResponseEntity<Resource<MicoApplicationJobStatusResponseDTO>> deploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplicationJobStatus micoApplicationJobStatus;
        try {
            micoApplicationJobStatus = deploymentBroker.deployApplication(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoServiceInterfaceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (MicoApplicationDoesNotIncludeMicoServiceException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (DeploymentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
        }

        return ResponseEntity.accepted()
            .body(new Resource<>(new MicoApplicationJobStatusResponseDTO(micoApplicationJobStatus)));
    }

    @PostMapping("/undeploy")
    public ResponseEntity<Void> undeploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {

        try {
            deploymentBroker.undeployApplication(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsDeployingException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }
}
