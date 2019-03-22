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
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.dto.response.MicoApplicationWithServicesResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = "/" + ApplicationResource.PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationResource {

    static final String PATH_APPLICATIONS = "applications";
    private static final String PATH_SERVICES = "services";
    private static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";
    private static final String PATH_PROMOTE = "promote";
    private static final String PATH_STATUS = "status";

    private static final String PATH_VARIABLE_SHORT_NAME = "micoApplicationShortName";
    private static final String PATH_VARIABLE_VERSION = "micoApplicationVersion";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "micoServiceShortName";
    private static final String PATH_VARIABLE_SERVICE_VERSION = "micoServiceVersion";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;
    
    @Autowired
    private MicoLabelRepository labelRepository;
    
    @Autowired
    private MicoEnvironmentVariableRepository environmentVariableRepository;

    @Autowired
    private MicoInterfaceConnectionRepository interfaceConnectionRepository;

    @Autowired
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @Autowired
    private MicoApplicationBroker broker;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getAllApplications() {
        List<MicoApplication> applications;
        try {
            applications = broker.getMicoApplications();
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return ResponseEntity.ok(
            new Resources<>(getApplicationWithServicesResponseDTOResourceList(applications),
                linkTo(methodOn(ApplicationResource.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> applications;
        try {
            applications = broker.getMicoApplicationsByShortName(shortName);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

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
        dto.setDeploymentStatus(MicoApplicationDeploymentStatus.NOT_DEPLOYED); //TODO: necessary?

        return ResponseEntity
            .created(linkTo(methodOn(ApplicationResource.class)
                .getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).toUri())
            .body(new Resource<>(dto, getApplicationLinks(application)));
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
        } catch (ShortNameOfMicoApplicationDoesNotMatchException | VersionOfMicoApplicationDoesNotMatchException e) {
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
        } catch (MicoApplicationIsDeployedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteAllVersionsOfAnApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        try {
            broker.deleteMicoApplicationsByShortName(shortName);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationIsDeployedException e) {
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

    //TODO: use broker
    @ApiOperation(value = "Adds or updates an association between a MicoApplication and a MicoService. An existing and" +
        " already associated MicoService with an equal short name will be replaced with its new version." +
        " Only one MicoService in one specific version is allowed per MicoApplication.")
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}/{" + PATH_VARIABLE_SERVICE_VERSION + "}")
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_VERSION) String serviceVersion) {
        // Retrieve application and service from database (checks whether they exist)
        MicoApplication application = getApplicationFromDatabase(applicationShortName, applicationVersion);
        MicoService service = getServiceFromDatabase(serviceShortName, serviceVersion);

        // Find all services with identical short name within this application
        List<MicoService> micoServicesWithIdenticalShortName = application.getServices().stream()
            .filter(s -> s.getShortName().equals(serviceShortName)).collect(Collectors.toList());

        if (micoServicesWithIdenticalShortName.size() > 1) {
            // Illegal state, each service is allowed only once in every application
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "There are multiple MicoServices with identical short names for MicoApplication '"
                    + applicationShortName + "' '" + applicationVersion + "'.");
        } else if (micoServicesWithIdenticalShortName.size() == 0) {
            // Service not included yet, simply add it
            log.info("Add service '{}' '{}' to application '{}' '{}'.",
                serviceShortName, serviceVersion, applicationShortName, applicationVersion);
            MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo().setService(service);
            // Both the service list and the service deployment info list
            // of the application need to be updated ...
            application.getServices().add(service);
            application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
            // ... before the application can be saved.
            applicationRepository.save(application);
        } else {
            // Service already included, replace it with its newer version, ...
            MicoService currentService = micoServicesWithIdenticalShortName.get(0);

            // ... but only replace if the new version is different from the current version
            if (currentService.getVersion().equals(serviceVersion)) {
                log.info("Application '{}' '{}' already contains service '{}' in version '{}'. Service will not be added to the application.",
                    applicationShortName, applicationVersion, serviceShortName, serviceVersion);
            } else {
                log.info("Replace service '{}' '{}' in application '{}' '{}' with new version '{}'.",
                    serviceShortName, currentService.getVersion(), applicationShortName, applicationVersion, serviceVersion);
                // Replace service in list of services in application
                application.getServices().remove(currentService);
                application.getServices().add(service);

                // Move the edge between the application and the service to the new version of the service
                // by updating the corresponding deployment info.
                application.getServiceDeploymentInfos().stream()
                    .filter(sdi -> sdi.getService().getShortName().equals(serviceShortName))
                    .collect(Collectors.toList())
                    .forEach(sdi -> sdi.setService(service));

                // Save the application with the updated list of services
                // and service deployment infos in the database
                applicationRepository.save(application);
            }
        }

        return ResponseEntity.noContent().build();
    }

    //TODO: use broker
    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteServiceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                             @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName) {
        // Retrieve application from database (checks whether it exists)
        MicoApplication application = getApplicationFromDatabase(shortName, version);

        // Check whether the application contains the service
        if (application.getServices().stream().noneMatch(service -> service.getShortName().equals(serviceShortName))) {
            // Application does not include the service -> cannot not be deleted from it
            log.debug("Application '{}' '{}' does not include service '{}', thus it cannot be deleted from it.",
                shortName, version, serviceShortName);
        } else {
            log.info("Delete service '{}' from application '{}' '{}'.",
                serviceShortName, shortName, version);
            // 1. Remove the service from the application
            application.getServices().removeIf(service -> service.getShortName().equals(serviceShortName));
            applicationRepository.save(application);
            // 2. Delete the corresponding service deployment information
            serviceDeploymentInfoRepository.deleteByApplicationAndService(shortName, version, serviceShortName);
        }

        // TODO: Update Kubernetes deployment

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
                                                                                                     @Valid @RequestBody MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo;
        try {
            micoServiceDeploymentInfo = broker.updateMicoServiceDeploymentInformation(shortName, version, serviceShortName, serviceDeploymentInfoDTO);
        } catch (MicoApplicationNotFoundException | MicoServiceDeploymentInformationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (MicoApplicationDoesNotIncludeMicoServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }

        return ResponseEntity.ok(new Resource<>(new MicoServiceDeploymentInfoResponseDTO(micoServiceDeploymentInfo),
			linkTo(methodOn(ApplicationResource.class).getServiceDeploymentInformation(shortName, version, serviceShortName))
		        .withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_STATUS)
    public ResponseEntity<Resource<MicoApplicationStatusResponseDTO>> getStatusOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplicationStatusResponseDTO applicationStatus;
        try {
            applicationStatus = broker.getMicoApplicationStatusOfMicoApplication(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return ResponseEntity.ok(new Resource<>(applicationStatus));
    }

    /**
     * Returns the existing {@link MicoApplication} object from the database for the given shortName and version.
     *
     * @param shortName the short name of a {@link MicoApplication}
     * @param version   the version of a {@link MicoApplication}
     * @return the existing {@link MicoApplication} from the database
     * @throws ResponseStatusException if a {@link MicoApplication} for the given shortName and version does not exist
     */
    private MicoApplication getApplicationFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoApplication> existingApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!existingApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        return existingApplicationOptional.get();
    }
    
    /**
     * //TODO This is a duplicate of {@link ServiceResource#getServiceFromDatabase(String, String)} this should be resolved
     * Returns the existing {@link MicoService} object from the database
     * for the given shortName and version.
     *
     * @param shortName the short name of the {@link MicoService}.
     * @param version the version of the {@link MicoService}.
     * @return the existing {@link MicoService} from the database if it exists.
     * @throws ResponseStatusException if no {@link MicoService} exists for the given shortName and version.
     */
    private MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> existingServciceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!existingServciceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' was not found!");
        }
        return existingServciceOptional.get();
    }

    private List<Resource<MicoApplicationWithServicesResponseDTO>> getApplicationWithServicesResponseDTOResourceList(List<MicoApplication> applications) {
        return applications.stream().map(application -> getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(application)).collect(Collectors.toList());
    }

    private Resource<MicoApplicationWithServicesResponseDTO> getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationWithServicesResponseDTO dto = new MicoApplicationWithServicesResponseDTO(application);
        dto.setDeploymentStatus(getApplicationDeploymentStatus(application));
        return new Resource<>(dto, getApplicationLinks(application));
    }

    private MicoApplicationDeploymentStatus getApplicationDeploymentStatus(MicoApplication application) {
        return micoKubernetesClient.isApplicationDeployed(application)
            ? MicoApplicationDeploymentStatus.DEPLOYED
            : MicoApplicationDeploymentStatus.NOT_DEPLOYED;
    }

    private Iterable<Link> getApplicationLinks(MicoApplication application) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationResource.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationResource.class).getAllApplications()).withRel("applications"));
        return links;
    }
    
    private List<Resource<MicoServiceResponseDTO>> getServiceResponseDTOResourceList(String applicationShortName, String applicationVersion) {
    	List<MicoService> services = serviceRepository.findAllByApplication(applicationShortName, applicationVersion);
    	return ServiceResource.getServiceResponseDTOResourcesList(services);
    }

}
