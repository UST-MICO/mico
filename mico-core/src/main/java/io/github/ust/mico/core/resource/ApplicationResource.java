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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.dto.request.MicoApplicationRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.dto.response.MicoApplicationWithServicesResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfoQueryResult;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/" + ApplicationResource.PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationResource {

    public static final String PATH_APPLICATIONS = "applications";
    public static final String PATH_SERVICES = "services";
    public static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";
    public static final String PATH_PROMOTE = "promote";
    public static final String PATH_STATUS = "status";

    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "serviceShortName";
    private static final String PATH_VARIABLE_SERVICE_VERSION = "serviceVersion";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getAllApplications() {
        return ResponseEntity.ok(
            new Resources<>(getApplicationWithServicesResponseDTOResourceList(applicationRepository.findAll(3)),
                linkTo(methodOn(ApplicationResource.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        return ResponseEntity.ok(
            new Resources<>(getApplicationWithServicesResponseDTOResourceList(micoApplicationList),
                linkTo(methodOn(ApplicationResource.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        return ResponseEntity.ok(getApplicationWithServicesResponseDTOResourceWithDeploymentStatus(application));
    }

    @PostMapping
    public ResponseEntity<Resource<MicoApplicationResponseDTO>> createApplication(@Valid @RequestBody MicoApplicationRequestDTO applicationDto) {
        // Check whether application already exists (not allowed)
        Optional<MicoApplication> applicationOptional = applicationRepository.
            findByShortNameAndVersion(applicationDto.getShortName(), applicationDto.getVersion());
        if (applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Application '" + applicationDto.getShortName() + "' '" + applicationDto.getVersion() + "' already exists.");
        }

        MicoApplication savedApplication = applicationRepository.save(MicoApplication.valueOf(applicationDto));
		MicoApplicationResponseDTO savedApplicationResponseDto = new MicoApplicationResponseDTO(savedApplication)
		    .setDeploymentStatus(MicoApplicationDeploymentStatus.NOT_DEPLOYED);

        return ResponseEntity
            .created(linkTo(methodOn(ApplicationResource.class)
                .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
            .body(new Resource<>(savedApplicationResponseDto, getApplicationLinks(savedApplication)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationResponseDTO>> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                               @PathVariable(PATH_VARIABLE_VERSION) String version,
                                               @Valid @RequestBody MicoApplicationRequestDTO applicationRequestDto) {
        if (!applicationRequestDto.getShortName().equals(shortName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "ShortName of the provided application does not match the request parameter");
        }
        if (!applicationRequestDto.getVersion().equals(version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Version of the provided application does not match the request parameter");
        }
        MicoApplication existingApplication = getApplicationFromDatabase(shortName, version);
        MicoApplication updatedApplication = applicationRepository.save(MicoApplication.valueOf(applicationRequestDto).setId(existingApplication.getId()));

        return ResponseEntity.ok(getApplicationResponseDTOResourceWithDeploymentStatus(updatedApplication));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PROMOTE)
    public ResponseEntity<Resource<MicoApplicationResponseDTO>> promoteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                           @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                           @Valid @RequestBody MicoVersionRequestDTO newVersionDto) {
    	log.debug("Received request to promote MicoApplication '{}' '{}' to version '{}'", shortName, version, newVersionDto.getVersion());
    	
    	System.out.println(newVersionDto);
    	
    	// Application to promote (copy)
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        System.out.println(application);
        log.debug("Received following MicoApplication from database: {}", application);
        
        // Update the version and set id to null, otherwise the original application
        // would be updated but we want a new application instance to be created.
        application.setVersion(newVersionDto.getVersion()).setId(null);

        // Save the new (promoted) application in the database,
        // all edges (deployment information) will be copied, too.
        MicoApplication updatedApplication = applicationRepository.save(application);
        System.out.println(updatedApplication);
        log.debug("Saved following MicoApplication in database: {}", updatedApplication);

        return ResponseEntity.ok(getApplicationResponseDTOResourceWithDeploymentStatus(updatedApplication));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Void> deleteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                  @PathVariable(PATH_VARIABLE_VERSION) String version) throws KubernetesResourceException {
        MicoApplication application = getApplicationFromDatabase(shortName, version);

        // Check whether application is currently deployed, i.e., it cannot be deleted
        if (micoKubernetesClient.isApplicationDeployed(application)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is currently deployed!");
        }

        // Delete application in database
        applicationRepository.delete(application);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteAllVersionsOfAnApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) throws KubernetesResourceException {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        // Check whether there is any version of the application in the database at all
        if (micoApplicationList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // If at least one version of the application is currently deployed,
        // none of the versions shall be deleted
        for (MicoApplication application : micoApplicationList) {
            if (micoKubernetesClient.isApplicationDeployed(application)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application is currently deployed in version " + application.getVersion() + "!");
            }
        }

        // No version of the application is deployed -> delete all
        applicationRepository.deleteAll(micoApplicationList);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getServicesFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
    	return ResponseEntity.ok(
            new Resources<>(getServiceResponseDTOResourceList(shortName, version),
                linkTo(methodOn(ApplicationResource.class).getServicesFromApplication(shortName, version)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}/{" + PATH_VARIABLE_SERVICE_VERSION + "}")
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_VERSION) String serviceVersion) {
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        MicoService existingService = getServiceFromDatabase(serviceShortName, serviceVersion);
        
        // Check whether the application already contains the service
        if (application.getServices().contains(existingService)) {
        	// Application already contains the service -> not allowed
        	log.info("Application '{}' '{}' already contains service '{}' '{}'.",
        		shortName, version, serviceShortName, serviceVersion);
        } else {
        	log.info("Add service '{}' '{}' to application '{}' '{}'.",
        		serviceShortName, serviceVersion, shortName, version);
			// Create default service deployment information for new service
			MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
			    .setService(existingService);
			// Both the service list and the service deployment info list
			// of the application need to be updated
			application.getServices().add(existingService);
			application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
			// Save the application (also creates the new node for the service deployment information)
			applicationRepository.save(application);
		}
        
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteServiceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                             @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName) {
        // Retrieve application from database (checks whether it exists)
        MicoApplication application = getApplicationFromDatabase(serviceShortName, version);
        
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
        // Retrieve service deployment info from database if there is any
        Optional<MicoServiceDeploymentInfoQueryResult> serviceDeploymentInfoQueryResultOptional = serviceDeploymentInfoRepository.findByApplicationAndService(shortName, version, serviceShortName);
        if (!serviceDeploymentInfoQueryResultOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Service deployment information for service '" + serviceShortName + "' in application '" + shortName
                    + "' in version '" + version + "' could not be found.");
        }

        // Convert to service deployment info DTO and return it
        MicoServiceDeploymentInfo serviceDeploymentInfo = serviceDeploymentInfoQueryResultOptional.get().getServiceDeploymentInfo();
        return ResponseEntity.ok(new Resource<>(new MicoServiceDeploymentInfoResponseDTO(serviceDeploymentInfo),
            linkTo(methodOn(ApplicationResource.class)
                .getServiceDeploymentInformation(shortName, version, serviceShortName)).withSelfRel()));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_INFORMATION + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> updateServiceDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                                     @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                                                                     @Valid @RequestBody MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) {
        // Check whether the corresponding service to update the deployment information for
        // is available in the database (done via the deployment information relationship)
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        if (application.getServiceDeploymentInfos().stream().noneMatch(sdi -> sdi.getService().getShortName().equals(serviceShortName))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Service deployment information for service '" + serviceShortName + "' in application '" + shortName
                    + "' in version '" + version + "' could not be found.");
        }

        // Search the corresponding deployment information ...
        MicoServiceDeploymentInfoResponseDTO serviceDeploymentInfoResponseDTO = null;
        for (MicoServiceDeploymentInfo serviceDeploymentInfo : application.getServiceDeploymentInfos()) {
            if (serviceDeploymentInfo.getService().getShortName().equals(serviceShortName)) {
                // ... and update it with the values from the deployment information from the DTO
                serviceDeploymentInfo.applyValuesFrom(serviceDeploymentInfoDTO);
                serviceDeploymentInfoResponseDTO = new MicoServiceDeploymentInfoResponseDTO(serviceDeploymentInfo);
                log.info("Service deployment information for service '{}' in application '{}' in version '{}' has been updated.",
                    serviceShortName, shortName, version);
                break;
            }
        }

        applicationRepository.save(application);

        // TODO: Update actual Kubernetes deployment (see issue mico#416).

		return ResponseEntity.ok(new Resource<>(serviceDeploymentInfoResponseDTO,
			linkTo(methodOn(ApplicationResource.class).getServiceDeploymentInformation(shortName, version, serviceShortName))
		        .withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_STATUS)
    public ResponseEntity<Resource<MicoApplicationStatusResponseDTO>> getStatusOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication micoApplication = getApplicationFromDatabase(shortName, version);
        MicoApplicationStatusResponseDTO applicationStatus = micoStatusService.getApplicationStatus(micoApplication);
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

    private Resource<MicoApplicationResponseDTO> getApplicationResponseDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationResponseDTO dto = new MicoApplicationResponseDTO(application, getApplicationDeploymentStatus(application));
        return new Resource<>(dto, getApplicationLinks(application));
    }

    private MicoApplicationDeploymentStatus getApplicationDeploymentStatus(MicoApplication application) {
        try {
        	if (micoKubernetesClient.isApplicationDeployed(application)) {
        		return MicoApplicationDeploymentStatus.DEPLOYED;
        	}
        } catch (KubernetesResourceException e) {
            log.debug(e.getMessage(), e);
        }
        return MicoApplicationDeploymentStatus.NOT_DEPLOYED;
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
