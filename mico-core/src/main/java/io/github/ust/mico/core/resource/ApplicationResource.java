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
import javax.validation.constraints.NotEmpty;
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

    public static final String PATH_APPLICATIONS = "applications";
    public static final String PATH_SERVICES = "services";
    public static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";
    public static final String PATH_PROMOTE = "promote";
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "serviceShortName";
    private static final String PATH_VARIABLE_SERVICE_VERSION = "serviceVersion";

    @Autowired
    private MicoApplicationBroker micoApplicationBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository; //TODO: remove?

    @Autowired
    private MicoServiceRepository serviceRepository; //TODO: remove?

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository; //TODO: remove?

    @Autowired
    private MicoKubernetesClient micoKubernetesClient; //TODO: remove?

    @Autowired
    private MicoStatusService micoStatusService; //TODO: remove?

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesDTO>>> getAllApplications() {
        return ResponseEntity.ok(
            new Resources<>(getApplicationWithServicesDTOResourceList(applicationRepository.findAll(3)),
                linkTo(methodOn(ApplicationResource.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplicationWithServicesDTO>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        return ResponseEntity.ok(
            new Resources<>(getApplicationWithServicesDTOResourceList(micoApplicationList),
                linkTo(methodOn(ApplicationResource.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplicationWithServicesDTO>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoApplication application = getApplicationFromDatabase(shortName, version);
        return ResponseEntity.ok(getApplicationWithServicesDTOResourceWithDeploymentStatus(application));
    }

    @PostMapping
    public ResponseEntity<?> createApplication(@Valid @RequestBody MicoApplicationDTO newApplicationDto) {
        // Check whether application already exists (not allowed)
        Optional<MicoApplication> applicationOptional = applicationRepository.
            findByShortNameAndVersion(newApplicationDto.getShortName(), newApplicationDto.getVersion());
        if (applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Application '" + newApplicationDto.getShortName() + "' '" + newApplicationDto.getVersion() + "' already exists.");
        }

        MicoApplication savedApplication = applicationRepository.save(MicoApplication.valueOf(newApplicationDto));

        return ResponseEntity
            .created(linkTo(methodOn(ApplicationResource.class)
                .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
            .body(new Resource<>(savedApplication, getApplicationLinks(savedApplication)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<?> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                               @PathVariable(PATH_VARIABLE_VERSION) String version,
                                               @Valid @RequestBody MicoApplicationDTO applicationDto) {
        if (!applicationDto.getShortName().equals(shortName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "ShortName of the provided application does not match the request parameter");
        }
        if (!applicationDto.getVersion().equals(version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Version of the provided application does not match the request parameter");
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

        return ResponseEntity
            .created(linkTo(methodOn(ApplicationResource.class)
                .getApplicationByShortNameAndVersion(updatedApplication.getShortName(), updatedApplication.getVersion())).toUri())
            .body(getApplicationDTOResourceWithDeploymentStatus(updatedApplication));
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
        List<Resource<MicoService>> micoServicesWithLinks = ServiceResource.getServiceResourcesList(micoServices);
        return ResponseEntity.ok(
            new Resources<>(micoServicesWithLinks,
                linkTo(methodOn(ApplicationResource.class).getServicesFromApplication(shortName, version)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}/{" + PATH_VARIABLE_SERVICE_VERSION + "}")
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName,
                                                        @PathVariable(PATH_VARIABLE_SERVICE_VERSION) String serviceVersion) {
        MicoApplication application = getApplicationFromDatabase(applicationShortName, applicationVersion);
        MicoService existingService = getServiceFromDatabase(serviceShortName, serviceVersion);

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

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteServiceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                             @PathVariable(PATH_VARIABLE_SERVICE_SHORT_NAME) String serviceShortName) {
        log.debug("Delete Mico service '{}' from Mico application '{}' in version '{}'", serviceShortName, shortName, version);

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

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_INFORMATION + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
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
            linkTo(methodOn(ApplicationResource.class)
                .getServiceDeploymentInformation(shortName, version, serviceShortName)).withSelfRel()));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPLOYMENT_INFORMATION + "/{" + PATH_VARIABLE_SERVICE_SHORT_NAME + "}")
    public ResponseEntity<Resource<MicoServiceDeploymentInfoDTO>> updateServiceDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
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

        applicationRepository.save(application);

        // TODO: Update actual Kubernetes deployment (see issue mico#416).

        return ResponseEntity.ok(new Resource<>(serviceDeploymentInfoDTO, linkTo(methodOn(ApplicationResource.class)
            .getServiceDeploymentInformation(shortName, version, serviceShortName)).withSelfRel()));
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
    
    /**
     * Returns the existing {@link MicoService} object from the database
     * for the given shortName and version.
     *
     * @param shortName the short name of the {@link MicoService}.
     * @param version the version of the {@link MicoService}.
     * @return the existing {@link MicoService} from the database if it exists.
     * @throws ResponseStatusException if no {@link MicoService} exists for the given shortName and version.
     */
    //TODO: remove?
    private MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> existingServciceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!existingServciceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' was not found!");
        }
        return existingServciceOptional.get();
    }

    //TODO: move to broker
    private List<Resource<MicoApplicationWithServicesDTO>> getApplicationWithServicesDTOResourceList(List<MicoApplication> applications) {
        return applications.stream().map(application -> getApplicationWithServicesDTOResourceWithDeploymentStatus(application)).collect(Collectors.toList());
    }

    //TODO: move to broker
    private Resource<MicoApplicationWithServicesDTO> getApplicationWithServicesDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationWithServicesDTO dto = MicoApplicationWithServicesDTO.valueOf(application);
        dto.setDeploymentStatus(getApplicationDeploymentStatus(application));
        return new Resource<MicoApplicationWithServicesDTO>(dto, getApplicationLinks(application));
    }

    //TODO: move to broker
    private Resource<MicoApplicationDTO> getApplicationDTOResourceWithDeploymentStatus(MicoApplication application) {
        MicoApplicationDTO dto = MicoApplicationDTO.valueOf(application);
        dto.setDeploymentStatus(getApplicationDeploymentStatus(application));
        return new Resource<MicoApplicationDTO>(dto, getApplicationLinks(application));
    }

    //TODO: move to broker
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

    //TODO: move to broker
    private Iterable<Link> getApplicationLinks(MicoApplication application) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationResource.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationResource.class).getAllApplications()).withRel("applications"));
        return links;
    }

}
