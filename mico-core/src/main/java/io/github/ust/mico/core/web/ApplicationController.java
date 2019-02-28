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

package io.github.ust.mico.core.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.github.ust.mico.core.dto.MicoApplicationDTO;
import io.github.ust.mico.core.dto.MicoApplicationDTO.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.dto.MicoApplicationWithServicesDTO;
import io.github.ust.mico.core.dto.MicoServiceDeploymentInfoDTO;
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
@RequestMapping(value = "/" + ApplicationController.PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationController {

    private static final String SERVICE_SHORT_NAME = "serviceShortName";

    public static final String PATH_APPLICATIONS = "applications";
    public static final String PATH_SERVICES = "services";
    public static final String PATH_PROMOTE = "promote";

    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "serviceShortName";

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
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesDTO>>> getAllApplications() {
        return ResponseEntity.ok(
                new Resources<>(getApplicationWithServicesDTOResourceList(applicationRepository.findAll(3)),
                        linkTo(methodOn(ApplicationController.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesDTO>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        return ResponseEntity.ok(
                new Resources<>(getApplicationWithServicesDTOResourceList(micoApplicationList),
                        linkTo(methodOn(ApplicationController.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationWithServicesDTO>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        return ResponseEntity.ok(getApplicationWithServicesDTOResourceWithDeploymentStatus(application));
    }

    @PostMapping
    public ResponseEntity<Resource<MicoApplicationDTO>> createApplication(@Valid @RequestBody MicoApplicationDTO newApplicationDto) {
        // Check whether application already exists (not allowed)
        Optional<MicoApplication> applicationOptional = applicationRepository.
                findByShortNameAndVersion(newApplicationDto.getShortName(), newApplicationDto.getVersion());
        if (applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application '" + newApplicationDto.getShortName() + "' '" + newApplicationDto.getVersion() + "' already exists.");
        }
        
        MicoApplication savedApplication = applicationRepository.save(MicoApplication.valueOf(newApplicationDto));

        return ResponseEntity
                .created(linkTo(methodOn(ApplicationController.class)
                        .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
                .body(getApplicationDTOResourceWithDeploymentStatus(savedApplication));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationDTO>> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                       @Valid @RequestBody MicoApplicationDTO applicationDto) {
        if (!applicationDto.getShortName().equals(shortName) || !applicationDto.getVersion().equals(version)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Application shortName or version does not match request body.");
        }

        MicoApplication existingApplication = getApplicationFromDatabase(shortName, version);
        MicoApplication updatedApplication = applicationRepository.save(MicoApplication.valueOf(applicationDto).setId(existingApplication.getId()));
        
        return ResponseEntity.ok(getApplicationDTOResourceWithDeploymentStatus(updatedApplication));
    }
    
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PROMOTE)
    public ResponseEntity<Resource<MicoApplicationDTO>> promoteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                       @NotEmpty @RequestBody String newVersion) {
        // Application to promote (copy)
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        
        // Update the version and set id to null, otherwise the original application
        // would be updated but we want a new application instance to be created.
        application.setVersion(newVersion).setId(null);
        
        // Save the new (promoted) application in the database,
        // all edges (deployment information) will be copied, too.
        MicoApplication updatedApplication = applicationRepository.save(application);
        
        return ResponseEntity.ok(getApplicationDTOResourceWithDeploymentStatus(updatedApplication));
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

    /**
     * Returns a list of services associated with the mico application specified by the parameters.
     *
     * @param shortName the name of the application
     * @param version   the version of the application
     * @return the list of mico services that are associated with the application
     */
    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Resources<Resource<MicoService>>> getServicesFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication micoApplication = getApplicationFromDatabase(shortName, version);
        List<MicoService> micoServices = serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
        List<Resource<MicoService>> micoServicesWithLinks = ServiceController.getServiceResourcesList(micoServices);
        return ResponseEntity.ok(
                new Resources<>(micoServicesWithLinks,
                        linkTo(methodOn(ApplicationController.class).getServicesFromApplication(shortName, version)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                        @RequestBody MicoService providedService) {
        MicoApplication application = getApplicationFromDatabase(applicationShortName, applicationVersion);
        MicoService existingService = validateProvidedService(providedService);
        
        // Each service can be added to one application only once
        if (application.getServiceDeploymentInfos().stream().noneMatch(sdi -> sdi.getService().equals(existingService))) {
            log.info("Add service '" + existingService.getShortName() + "' '" + existingService.getVersion()
                    + "' to application '" + applicationShortName + "' '" + applicationVersion + "'.");
            MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo();
            
            sdi.setApplication(application).setService(existingService);
            application.getServiceDeploymentInfos().add(sdi);
            applicationRepository.save(application);
        } else {
            log.info("Application '" + applicationShortName + "' '" + applicationVersion + "' already contains service '"
                            + existingService.getShortName() + "' '" + existingService.getVersion() + "'.");
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteServiceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                       @PathVariable(SERVICE_SHORT_NAME) String serviceShortName) {
        log.debug("Delete Mico service '{}' from Mico application '{}' in version '{}'", SERVICE_SHORT_NAME, shortName, version);

        // Retrieve the corresponding deployment info in order to remove it
        // from the list of deployment infos in the given application.
        // This will only remove the relationship (edge) between the given application
        // and the service to delete.
        Optional<MicoServiceDeploymentInfoQueryResult> sdiQueryResultOptional = serviceDeploymentInfoRepository
                .findByApplicationAndService(shortName, version, serviceShortName);
        if (sdiQueryResultOptional.isPresent()) {
            MicoServiceDeploymentInfoQueryResult sdiQueryResult = sdiQueryResultOptional.get();
            MicoApplication application = sdiQueryResult.getApplication();
            application.getServiceDeploymentInfos().remove(sdiQueryResult.getServiceDeploymentInfo());
            applicationRepository.save(application);
            
            List<MicoService> remainingServices = serviceRepository.findAllByApplication(shortName, version);
            log.debug("Service list of application '{}' '{}' has size: {}", shortName, version, remainingServices.size());
        }

        // TODO: Update Kubernetes deployment

        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/services/{" + SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoDTO>> getServiceDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
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
        return ResponseEntity.ok(new Resource<>(MicoServiceDeploymentInfoDTO.valueOf(serviceDeploymentInfo),
                linkTo(methodOn(ApplicationController.class)
                        .getServiceDeploymentInformation(shortName, version,serviceShortName)).withSelfRel()));
    }
    
    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/services/{" + SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Resource<MicoApplication>> updateServiceDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                     @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                                                     @Valid @RequestBody MicoServiceDeploymentInfoDTO serviceDeploymentInfoDTO) {
        // Check whether the corresponding service to update the deployment information for
        // is available in the database (done via the deployment information relationship)
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        if (application.getServiceDeploymentInfos().stream().noneMatch(sdi -> sdi.getService().getShortName().equals(serviceShortName))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Service deployment information for service '" + serviceShortName + "' in application '" + shortName
                            + "' in version '" + version + "' could not be found.");
        }
        
        // Search the corresponding deployment information ...
        for (MicoServiceDeploymentInfo serviceDeploymentInfo : application.getServiceDeploymentInfos()) {
            if (serviceDeploymentInfo.getService().getShortName().equals(serviceShortName)) {
                // ... and update it with the values from the deployment information from the DTO
                serviceDeploymentInfo.applyValuesFrom(serviceDeploymentInfoDTO);
                log.info("Service deployment information for service '{}' in application '{}' in version '{}' has been updated.",
                        serviceShortName, shortName, version);
                break;
            }
        }
        
        MicoApplication updatedApplication = applicationRepository.save(application);
        
        // TODO: Update actual Kubernetes deployment (see issue mico#416).
        
        return ResponseEntity.ok(new Resource<>(updatedApplication,
                linkTo(methodOn(ApplicationController.class).updateServiceDeploymentInformation(shortName, version,
                        serviceShortName, serviceDeploymentInfoDTO)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/status")
    public ResponseEntity<Resource<MicoApplicationStatusDTO>> getStatusOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication micoApplication = getApplicationFromDatabase(shortName, version);
        MicoApplicationStatusDTO applicationStatus = micoStatusService.getApplicationStatus(micoApplication);
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

    private List<Resource<MicoApplicationWithServicesDTO>> getApplicationWithServicesDTOResourceList(List<MicoApplication> applications) {
        return applications.stream().map(application -> getApplicationWithServicesDTOResourceWithDeploymentStatus(application)).collect(Collectors.toList());
    }
    
    private Resource<MicoApplicationWithServicesDTO> getApplicationWithServicesDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationWithServicesDTO dto = MicoApplicationWithServicesDTO.valueOf(application);
        dto.getApplication().setDeploymentStatus(getApplicationDeploymentStatus(application));
        return new Resource<MicoApplicationWithServicesDTO>(dto, getApplicationLinks(application));
    }
    
    private Resource<MicoApplicationDTO> getApplicationDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationDTO dto = MicoApplicationDTO.valueOf(application);
        dto.setDeploymentStatus(getApplicationDeploymentStatus(application));
        return new Resource<MicoApplicationDTO>(dto, getApplicationLinks(application));
    }
    
    private MicoApplicationDeploymentStatus getApplicationDeploymentStatus(MicoApplication application) {
        try {
            return micoKubernetesClient.isApplicationDeployed(application)
                    ? MicoApplicationDeploymentStatus.DEPLOYED
                    : MicoApplicationDeploymentStatus.NOT_DEPLOYED;
        } catch (KubernetesResourceException e) {
            log.debug(e.getMessage(), e);
            return MicoApplicationDeploymentStatus.UNKNOWN;
        }
    }

    private Iterable<Link> getApplicationLinks(MicoApplication application) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationController.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationController.class).getAllApplications()).withRel("applications"));
        return links;
    }

    /**
     * Validates the {@link MicoService} with the data that is stored in the database.
     * If the provided service is valid, return the existing service.
     *
     * @param providedService the {@link MicoService}
     * @return the already existing {@link MicoService}
     * @throws ResponseStatusException if a {@link MicoService} does not exist or there is a conflict
     */
    private MicoService validateProvidedService(MicoService providedService) throws ResponseStatusException {

        // Check if the provided service exists
        Optional<MicoService> existingServiceOptional = serviceRepository.findByShortNameAndVersion(providedService.getShortName(), providedService.getVersion());
        if (!existingServiceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() + "' does not exist!");
        }

        // If more than the short name and the version of the service are provided,
        // check if the data is consistent. If not throw a 409 conflict error.
        MicoService existingService = existingServiceOptional.get();
        if (providedService.getDockerImageUri() != null
                && !providedService.getDockerImageUri().equals(existingService.getDockerImageUri())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'dockerImageUri' with the existing service!");
        }
        if (providedService.getDockerfilePath() != null
                && !providedService.getDockerfilePath().equals(existingService.getDockerfilePath())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'dockerfilePath' with the existing service!");
        }
        if (providedService.getGitCloneUrl() != null
                && !providedService.getGitCloneUrl().equals(existingService.getGitCloneUrl())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'gitCloneUrl' with the existing service!");
        }
        if (providedService.getDescription() != null
                && !providedService.getDescription().equals(existingService.getDescription())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'description' with the existing service!");
        }
        if (providedService.getGitReleaseInfoUrl() != null
                && !providedService.getGitReleaseInfoUrl().equals(existingService.getGitReleaseInfoUrl())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'gitReleaseInfoUrl' with the existing service!");
        }
        if (providedService.getName() != null
                && !providedService.getName().equals(existingService.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'name' with the existing service!");
        }
        if (providedService.getContact() != null
                && !providedService.getContact().equals(existingService.getContact())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'contact' with the existing service!");
        }
        if (providedService.getOwner() != null
                && !providedService.getOwner().equals(existingService.getOwner())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided service '" + providedService.getShortName() + "' '" + providedService.getVersion() +
                            "' has a conflict in the property 'owner' with the existing service!");
        }
        return existingService;
    }
}
