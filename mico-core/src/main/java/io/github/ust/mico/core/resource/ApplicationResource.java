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

import io.github.ust.mico.core.broker.MicoApplicationBroker;
import io.github.ust.mico.core.dto.request.MicoApplicationRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.KFConnectorDeploymentInfoResponseDTO;
import io.github.ust.mico.core.dto.response.MicoApplicationWithServicesResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationDeploymentStatusResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = ApplicationResource.PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationResource {

    public static final String PATH_APPLICATIONS = "/applications";

    private static final String PATH_SERVICES = "services";
    private static final String PATH_PROMOTE = "promote";
    private static final String PATH_DEPLOYMENT_STATUS = "deploymentStatus";
    private static final String PATH_STATUS = "status";
    private static final String PATH_KAFKA_FAAS_CONNECTOR = "kafka-faas-connector";

    static final String PATH_VARIABLE_SHORT_NAME = "micoApplicationShortName";
    static final String PATH_VARIABLE_VERSION = "micoApplicationVersion";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "micoServiceShortName";
    private static final String PATH_VARIABLE_SERVICE_VERSION = "micoServiceVersion";
    private static final String PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION = "kafkaFaasConnectorVersion";
    private static final String PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID = "kafkaFaasConnectorInstanceId";
    private static final String PATH_VARIABLE_INSTANCE_ID = "instanceId";

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
    public ResponseEntity<Void> deleteAllVersionsOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        try {
            broker.deleteMicoApplicationsByShortName(shortName);
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getServicesOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<MicoService> micoServices;
        try {
            micoServices = broker.getMicoServicesOfMicoApplicationByShortNameAndVersion(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return ResponseEntity.ok(
            new Resources<>(ServiceResource.getServiceResponseDTOResourcesList(micoServices),
                linkTo(methodOn(ApplicationResource.class).getServicesOfApplication(shortName, version)).withSelfRel()));
    }

    @ApiOperation(value = "Adds or updates an association between a MicoApplication and a MicoService. An existing and" +
        " already associated MicoService with an equal short name will be replaced with its new version." +
        " Only one MicoService in one specific version is allowed per MicoApplication.")
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}/{" + PATH_VARIABLE_SERVICE_VERSION + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                                                                  @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                                                                  @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                                                                  @PathVariable(PATH_VARIABLE_SERVICE_VERSION) String serviceVersion) {
        MicoServiceDeploymentInfo serviceDeploymentInfo;
        try {
            serviceDeploymentInfo = broker.addMicoServiceToMicoApplicationByShortNameAndVersion(
                applicationShortName, applicationVersion, serviceShortName, serviceVersion, Optional.empty());
        } catch (MicoApplicationNotFoundException | MicoServiceNotFoundException | MicoServiceDeploymentInformationNotFoundException | KubernetesResourceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoServiceAddedMoreThanOnceToMicoApplicationException | MicoApplicationIsNotUndeployedException | MicoApplicationDoesNotIncludeMicoServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (MicoTopicRoleUsedMultipleTimesException | KafkaFaasConnectorNotAllowedHereException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        return ResponseEntity.ok(new Resource<>(new MicoServiceDeploymentInfoResponseDTO(serviceDeploymentInfo)));
    }

    @ApiOperation(value = "Reuses an existing service deployment information instance and adds it to the application.")
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}/{" + PATH_VARIABLE_SERVICE_VERSION + "}/{" + PATH_VARIABLE_INSTANCE_ID + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                                                                  @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                                                                  @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                                                                  @PathVariable(PATH_VARIABLE_SERVICE_VERSION) String serviceVersion,
                                                                                                  @PathVariable(PATH_VARIABLE_INSTANCE_ID) String instanceId) {
        MicoServiceDeploymentInfo serviceDeploymentInfo;
        try {
            serviceDeploymentInfo = broker.addMicoServiceToMicoApplicationByShortNameAndVersion(
                applicationShortName, applicationVersion, serviceShortName, serviceVersion, Optional.ofNullable(instanceId));
        } catch (MicoApplicationNotFoundException | MicoServiceNotFoundException | MicoServiceDeploymentInformationNotFoundException | KubernetesResourceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoServiceAddedMoreThanOnceToMicoApplicationException | MicoApplicationIsNotUndeployedException | MicoApplicationDoesNotIncludeMicoServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (MicoTopicRoleUsedMultipleTimesException | KafkaFaasConnectorNotAllowedHereException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }

        return ResponseEntity.ok(new Resource<>(new MicoServiceDeploymentInfoResponseDTO(serviceDeploymentInfo)));
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

    @ApiOperation(value = "Adds a new association between a MicoApplication and a KafkaFaasConnector (MicoService). " +
        "Multiple instances of a KafkaFaasConnector are allowed per MicoApplication.")
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR + "/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "}")
    public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> addKafkaFaasConnectorInstanceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                                                                                     @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION) String kfConnectorVersion) {
        MicoServiceDeploymentInfo kafkaFaasConnectorSDI;
        try {
            kafkaFaasConnectorSDI = broker.addKafkaFaasConnectorInstanceToMicoApplicationByVersion(
                applicationShortName, applicationVersion, kfConnectorVersion);
        } catch (MicoApplicationNotFoundException | KafkaFaasConnectorVersionNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.ok(new Resource<>(new KFConnectorDeploymentInfoResponseDTO(kafkaFaasConnectorSDI)));
    }

    @ApiOperation(value = "Updates an existing KafkaFaasConnector deployment information instance with a new version.")
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR + "/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "}/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID + "}")
    public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> updateKafkaFaasConnectorInstanceOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                                                                                        @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION) String kfConnectorVersion,
                                                                                                                        @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID) String instanceId) {
        MicoServiceDeploymentInfo kafkaFaasConnectorSDI;
        try {
            kafkaFaasConnectorSDI = broker.updateKafkaFaasConnectorInstanceOfMicoApplicationByVersionAndInstanceId(
                applicationShortName, applicationVersion, kfConnectorVersion, instanceId);
        } catch (MicoApplicationNotFoundException | KafkaFaasConnectorVersionNotFoundException | KafkaFaasConnectorInstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.ok(new Resource<>(new KFConnectorDeploymentInfoResponseDTO(kafkaFaasConnectorSDI)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR)
    public ResponseEntity<Void> deleteAllKafkaFaasConnectorInstancesFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                    @PathVariable(PATH_VARIABLE_VERSION) String version) {
        try {
            broker.removeAllKafkaFaasConnectorInstancesFromMicoApplication(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR + "/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "}")
    public ResponseEntity<Void> deleteKafkaFaasConnectorInstancesFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                 @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION) String kfConnectorVersion) {
        try {
            broker.removeKafkaFaasConnectorInstancesFromMicoApplicationByVersion(shortName, version, kfConnectorVersion);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsNotUndeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR + "/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "}/{" + PATH_VARIABLE_INSTANCE_ID + "}")
    public ResponseEntity<Void> deleteKafkaFaasConnectorInstanceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION) String kfConnectorVersion,
                                                                                @PathVariable(PATH_VARIABLE_INSTANCE_ID) String instanceId) {
        try {
            broker.removeKafkaFaasConnectorInstanceFromMicoApplicationByVersionAndInstanceId(shortName, version, kfConnectorVersion, instanceId);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationDoesNotIncludeKFConnectorInstanceException | MicoApplicationIsNotUndeployedException | KafkaFaasConnectorInstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_STATUS)
    public ResponseEntity<Resource<MicoApplicationDeploymentStatusResponseDTO>> getApplicationDeploymentStatus(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                               @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplicationDeploymentStatus applicationDeploymentStatus;
        try {
            applicationDeploymentStatus = broker.getApplicationDeploymentStatus(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return ResponseEntity.ok(new Resource<>(new MicoApplicationDeploymentStatusResponseDTO(applicationDeploymentStatus)));
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
        try {
            dto.setDeploymentStatus(new MicoApplicationDeploymentStatusResponseDTO(
                broker.getApplicationDeploymentStatus(application.getShortName(), application.getVersion())));
        } catch (MicoApplicationNotFoundException e) {
            // Application was already checked -> it's safe to not throw an exception.
            // Don't throw an exception because it would make the code way more complex.
            log.error(e.getMessage());
        }
        return new Resource<>(dto, broker.getLinksOfMicoApplication(application));
    }
}
