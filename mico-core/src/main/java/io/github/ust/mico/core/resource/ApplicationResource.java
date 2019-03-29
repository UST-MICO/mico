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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.broker.MicoApplicationBroker;
import io.github.ust.mico.core.dto.request.MicoApplicationRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.MicoApplicationWithServicesResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationDeploymentStatusResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/applications", produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationResource {

    private static final String PATH_SERVICES = "services";
    private static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";
    private static final String PATH_PROMOTE = "promote";
    private static final String PATH_DEPLOYMENT_STATUS = "deploymentStatus";
    private static final String PATH_STATUS = "status";

    private static final String PATH_VARIABLE_SHORT_NAME = "micoApplicationShortName";
    private static final String PATH_VARIABLE_VERSION = "micoApplicationVersion";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "micoServiceShortName";
    private static final String PATH_VARIABLE_SERVICE_VERSION = "micoServiceVersion";

    @Autowired
    private MicoApplicationBroker broker;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getAllApplications() {
        List<MicoApplication> applications = broker.getMicoApplications();

        return ResponseEntity.ok(
                new Resources<>(getApplicationWithServicesResponseDTOResourceList(applications),
                        linkTo(methodOn(ApplicationResource.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> applications = broker.getMicoApplicationsByShortName(shortName);

        return ResponseEntity.ok(
                new Resources<>(getApplicationWithServicesResponseDTOResourceList(applications),
                        linkTo(methodOn(ApplicationResource.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication application;
        try {
            application = broker.getMicoApplicationByShortNameAndVersion(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        //TODO: HAL standard?
        return ResponseEntity.ok(getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(application));
    }

    @PostMapping
    public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> createApplication(@Valid @RequestBody MicoApplicationRequestDTO applicationDto) {
        MicoApplication application;
        try {
            application = broker.createMicoApplication(MicoApplication.valueOf(applicationDto));
        } catch (MicoApplicationAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        MicoApplicationWithServicesResponseDTO dto = new MicoApplicationWithServicesResponseDTO(application);

        return ResponseEntity
                .created(linkTo(methodOn(ApplicationResource.class)
                        .getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).toUri())
                .body(new Resource<>(dto, broker.getLinksOfMicoApplication(application)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                              @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                              @Valid @RequestBody MicoApplicationRequestDTO applicationRequestDto) {
        MicoApplication application;
        try {
            application = broker.updateMicoApplication(shortName, version, MicoApplication.valueOf(applicationRequestDto));
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ShortNameOfMicoApplicationDoesNotMatchException | VersionOfMicoApplicationDoesNotMatchException | MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        //TODO: HAL standard?
        return ResponseEntity.ok(getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(application));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PROMOTE)
    public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> promoteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                               @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                               @Valid @RequestBody MicoVersionRequestDTO newVersionDto) {
        MicoApplication application;
        try {
            application = broker.copyAndUpgradeMicoApplicationByShortNameAndVersion(shortName, version, newVersionDto.getVersion());
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.ok(getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(application));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Void> deleteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                  @PathVariable(PATH_VARIABLE_VERSION) String version) {
        try {
            broker.deleteMicoApplicationByShortNameAndVersion(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteAllVersionsOfAnApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        try {
            broker.deleteMicoApplicationsByShortName(shortName);
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getServicesFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                  @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<MicoService> micoServices;
        try {
            micoServices = broker.getMicoServicesOfMicoApplicationByShortNameAndVersion(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return ResponseEntity.ok(
                new Resources<>(ServiceResource.getServiceResponseDTOResourcesList(micoServices),
                        linkTo(methodOn(ApplicationResource.class).getServicesFromApplication(shortName, version)).withSelfRel()));
    }

    @ApiOperation(value = "Adds or updates an association between a MicoApplication and a MicoService. An existing and" +
            " already associated MicoService with an equal short name will be replaced with its new version." +
            " Only one MicoService in one specific version is allowed per MicoApplication.")
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}/{" + PATH_VARIABLE_SERVICE_VERSION + "}")
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_VERSION) String serviceVersion) {
        try {
            broker.addMicoServiceToMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion, serviceShortName, serviceVersion);
        } catch (MicoApplicationNotFoundException | MicoServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoServiceAlreadyAddedToMicoApplicationException | MicoServiceAddedMoreThanOnceToMicoApplicationException | MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteServiceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                             @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName) {
        try {
            broker.removeMicoServiceFromMicoApplicationByShortNameAndVersion(shortName, version, serviceShortName);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationDoesNotIncludeMicoServiceException | MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

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
                linkTo(methodOn(ApplicationResource.class)
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
        }

        // Convert to service deployment info DTO and return it
        MicoServiceDeploymentInfoResponseDTO updatedServiceDeploymentInfoDto = new MicoServiceDeploymentInfoResponseDTO(updatedServiceDeploymentInfo);
        return ResponseEntity.ok(new Resource<>(updatedServiceDeploymentInfoDto,
            linkTo(methodOn(ApplicationResource.class).getServiceDeploymentInformation(shortName, version, serviceShortName))
                .withSelfRel()));
    }
    
    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_STATUS)
    public ResponseEntity<Resource<MicoApplicationDeploymentStatusResponseDTO>> getApplicationDeploymentStatus(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     				@PathVariable(PATH_VARIABLE_VERSION) String version) {
		return ResponseEntity.ok(new Resource<>(new MicoApplicationDeploymentStatusResponseDTO(
		    broker.getApplicationDeploymentStatus(shortName, version))));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_STATUS)
    public ResponseEntity<Resource<MicoApplicationStatusResponseDTO>> getStatusOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplicationStatusResponseDTO applicationStatus;
        try {
            applicationStatus = broker.getApplicationStatus(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return ResponseEntity.ok(new Resource<>(applicationStatus));
    }

    private List<Resource<MicoApplicationWithServicesResponseDTO>> getApplicationWithServicesResponseDTOResourceList(List<MicoApplication> applications) {
        return applications.stream().map(this::getApplicationWithServicesResponseDTOResourceWithDeploymentStatus).collect(Collectors.toList());
    }

    private Resource<MicoApplicationWithServicesResponseDTO> getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationWithServicesResponseDTO dto = new MicoApplicationWithServicesResponseDTO(application);
		dto.setDeploymentStatus(new MicoApplicationDeploymentStatusResponseDTO(
		    broker.getApplicationDeploymentStatus(application.getShortName(), application.getVersion())));
        return new Resource<>(dto, broker.getLinksOfMicoApplication(application));
    }
}
