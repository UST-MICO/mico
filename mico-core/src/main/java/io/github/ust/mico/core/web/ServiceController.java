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

import java.io.IOException;
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

import io.github.ust.mico.core.dto.request.CrawlingInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceDependencyRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceRequestDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphEdgeResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceStatusResponseDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.GitHubCrawler;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceController {

    public static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    public static final String PATH_VARIABLE_VERSION = "version";
    public static final String PATH_VARIABLE_ID = "id";
    public static final String PATH_DELETE_SHORT_NAME = "shortNameToDelete";
    public static final String PATH_DELETE_VERSION = "versionToDelete";
    public static final String PATH_VARIABLE_IMPORT = "import";
    public static final String PATH_VARIABLE_GITHUB = "github";
    public static final String PATH_GITHUB_ENDPOINT = "/" + PATH_VARIABLE_IMPORT + "/" + PATH_VARIABLE_GITHUB;
    public static final String PATH_DEPENDEES = "dependees";
    public static final String PATH_DEPENDERS = "dependers";
    public static final String PATH_DEPENDENCY_GRAPH = "dependencyGraph";

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoStatusService micoStatusService;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private GitHubCrawler crawler;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getServiceList() {
        List<MicoService> services = serviceRepository.findAll(2);
        List<Resource<MicoServiceResponseDTO>> serviceResources = getServiceResponseDTOResourcesList(services);
        return ResponseEntity.ok(
            new Resources<>(serviceResources,
                linkTo(methodOn(ServiceController.class).getServiceList()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoServiceResponseDTO>> getServiceByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService service = getServiceFromDatabase(shortName, version);
        return ResponseEntity.ok(getServiceResponseDTOResource(service));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoServiceResponseDTO>> updateService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                           @PathVariable(PATH_VARIABLE_VERSION) String version,
                                           @Valid @RequestBody MicoServiceRequestDTO serviceDto) {
        if (!serviceDto.getShortName().equals(shortName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "ShortName of the provided service does not match the request parameter");
        }
        if (!serviceDto.getVersion().equals(version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Version of the provided service does not match the request parameter");
        }

        MicoService existingService = getServiceFromDatabase(shortName, version);
        MicoService updatedService = serviceRepository.save(MicoService.valueOf(serviceDto).setId(existingService.getId()));

        return ResponseEntity.ok(getServiceResponseDTOResource(updatedService));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Void> deleteService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                              @PathVariable(PATH_VARIABLE_VERSION) String version) throws KubernetesResourceException {
        MicoService service = getServiceFromDatabase(shortName, version);

        throwConflictIfServiceIsDeployed(service);

//        if (!getDependers(service).isEmpty()) {
        if (!serviceRepository.findDependers(shortName, version).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Service '" + service.getShortName() + "' '" + service.getVersion() + "' has dependers, therefore it can't be deleted.");
        }

        serviceRepository.deleteServiceByShortNameAndVersion(shortName, version);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteAllVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) throws KubernetesResourceException {
        List<MicoService> micoServiceList = getAllVersionsOfServiceFromDatabase(shortName);

        for(MicoService micoService : micoServiceList){
            throwConflictIfServiceIsDeployed(micoService);
        }
        micoServiceList.forEach(service -> serviceRepository.delete(service));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/status")
    public ResponseEntity<Resource<MicoServiceStatusResponseDTO>> getStatusOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoServiceStatusResponseDTO serviceStatus = new MicoServiceStatusResponseDTO();
        Optional<MicoService> micoServiceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (micoServiceOptional.isPresent()) {
            log.debug("Retrieve status information of Mico service '{}' '{}'",
                shortName, version);
            serviceStatus = micoStatusService.getServiceStatus(micoServiceOptional.get());
        } else {
            log.error("MicoService not found in service repository.");
        }
        return ResponseEntity.ok(new Resource<>(serviceStatus));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoService> services = serviceRepository.findByShortName(shortName);
        List<Resource<MicoServiceResponseDTO>> serviceResources = getServiceResponseDTOResourcesList(services);
        return ResponseEntity.ok(
            new Resources<>(serviceResources,
                linkTo(methodOn(ServiceController.class).getVersionsOfService(shortName)).withSelfRel()));
    }

    @PostMapping
    public ResponseEntity<Resource<MicoServiceResponseDTO>> createService(@Valid @RequestBody MicoServiceRequestDTO serviceDto) {
        Optional<MicoService> serviceOptional = serviceRepository.
            findByShortNameAndVersion(serviceDto.getShortName(), serviceDto.getVersion());
        if (serviceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Service '" + serviceDto.getShortName() + "' '" + serviceDto.getVersion() + "' already exists.");
        }

        MicoService savedService = serviceRepository.save(MicoService.valueOf(serviceDto));

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(savedService.getShortName(), savedService.getVersion())).toUri())
            .body(new Resource<>(new MicoServiceResponseDTO(savedService), getServiceLinks(savedService)));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDEES)
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService service = getServiceFromDatabase(shortName, version);
        List<MicoServiceDependency> dependees = service.getDependencies();
        if (dependees == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service dependees must not be null.");
        }

        List<MicoService> services = serviceRepository.findDependees(shortName, version);

        return ResponseEntity.ok(
            new Resources<>(getServiceResponseDTOResourcesList(services),
                linkTo(methodOn(ServiceController.class).getDependees(shortName, version)).withSelfRel()));
    }

    /**
     * Creates a new dependency edge between the Service and the depended service.
     */
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDEES)
    public ResponseEntity<Resource<MicoServiceResponseDTO>> createNewDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                   @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                   @Valid @RequestBody MicoServiceDependencyRequestDTO serviceDependencyDto) {
        MicoService service = getServiceFromDatabase(shortName, version);

        Optional<MicoService> serviceDependeeOpt = serviceRepository.findByShortNameAndVersion(serviceDependencyDto.getDependedService().getShortName(),
        	serviceDependencyDto.getDependedService().getVersion());
        if (!serviceDependeeOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The dependee service was not found!");
        }

        // Check if dependency is already set
        String localShortName = serviceDependencyDto.getDependedService().getShortName();
        String localVersion = serviceDependencyDto.getDependedService().getVersion();
        boolean dependencyAlreadyExists = (service.getDependencies() != null) && service.getDependencies().stream().anyMatch(
            dependency -> dependency.getDependedService().getShortName().equals(localShortName)
                && dependency.getDependedService().getVersion().equals(localVersion));
        if (dependencyAlreadyExists) {
            return ResponseEntity
                .created(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(shortName, version)).toUri())
                .body(new Resource<>(new MicoServiceResponseDTO(service), getServiceLinks(service)));
        }

        final MicoServiceDependency processedServiceDependee = new MicoServiceDependency()
            .setDependedService(serviceDependeeOpt.get())
            .setService(service);

        log.info("New dependency for MicoService '{}' '{}' -[:DEPENDS_ON]-> '{}' '{}'", shortName, version,
            processedServiceDependee.getDependedService().getShortName(),
            processedServiceDependee.getDependedService().getVersion());

        service.getDependencies().add(processedServiceDependee);
        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(shortName, version)).toUri())
            .body(new Resource<>(new MicoServiceResponseDTO(savedService), getServiceLinks(savedService)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDEES)
    public ResponseEntity<Resource<MicoServiceResponseDTO>> deleteAllDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                    @PathVariable(PATH_VARIABLE_VERSION) String version) {
    	// We only want to delete the relationships (the edges),
    	// not the actual depended services.
        MicoService service = getServiceFromDatabase(shortName, version);
        service.getDependencies().clear();

        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(shortName, version)).toUri())
            .body(new Resource<>(new MicoServiceResponseDTO(savedService), getServiceLinks(savedService)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees"
        + "/{" + PATH_DELETE_SHORT_NAME + "}/{" + PATH_DELETE_VERSION + "}")
    public ResponseEntity<Resource<MicoServiceResponseDTO>> deleteDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                @PathVariable(PATH_DELETE_SHORT_NAME) String shortNameToDelete,
                                                                @PathVariable(PATH_DELETE_VERSION) String versionToDelete) {
    	// We only want to delete the relationship (the edge),
    	// not the actual depended service.
        MicoService service = getServiceFromDatabase(shortName, version);
        
        // Check whether dependee to delete exists
        Optional<MicoService> serviceOptToDelete = serviceRepository.findByShortNameAndVersion(shortNameToDelete, versionToDelete);
        if (!serviceOptToDelete.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service dependee '" + shortNameToDelete + "' '" + versionToDelete + "'  was not found!");
        }
        MicoService serviceToDelete = serviceOptToDelete.get();

        service.getDependencies().removeIf(dependency -> dependency.getDependedService().getId() == serviceToDelete.getId());
        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
            .created(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(shortName, version)).toUri())
            .body(new Resource<>(new MicoServiceResponseDTO(savedService), getServiceLinks(savedService)));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDERS)
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getDependers(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        return ResponseEntity.ok(
            new Resources<>(getServiceResponseDTOResourcesList(serviceRepository.findDependers(shortName, version)),
                linkTo(methodOn(ServiceController.class).getDependers(shortName, version)).withSelfRel()));
    }

    @PostMapping(PATH_GITHUB_ENDPOINT)
    public ResponseEntity<Resource<MicoServiceResponseDTO>> importMicoServiceFromGitHub(@RequestBody CrawlingInfoRequestDTO crawlingInfo) {
        String url = crawlingInfo.getUrl();
        String version = crawlingInfo.getVersion();
        log.debug("Start importing MicoService from URL '{}'", url);

        try {
            if (version.equals("latest")) {
                MicoService service = crawler.crawlGitHubRepoLatestRelease(url);
                return createService(new MicoServiceRequestDTO(service));
            } else {
                MicoService service = crawler.crawlGitHubRepoSpecificRelease(url, version);
                return createService(new MicoServiceRequestDTO(service));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping(PATH_GITHUB_ENDPOINT)
    @ResponseBody
    public LinkedList<String> getVersionsFromGitHub(@RequestParam String url) {
        try {
        	log.debug("Start getting versions from URL '{}'", url);
            return crawler.getVersionsFromGitHubRepo(url);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDENCY_GRAPH)
    public ResponseEntity<Resource<MicoServiceDependencyGraphResponseDTO>> getDependencyGraph(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                               @PathVariable(PATH_VARIABLE_VERSION) String version) {
       MicoService micoServiceRoot = getServiceFromDatabase(shortName, version);
       List<MicoService> micoServices = serviceRepository.findDependeesIncludeDepender(micoServiceRoot.getShortName(), micoServiceRoot.getVersion());
       MicoServiceDependencyGraphResponseDTO micoServiceDependencyGraph = new MicoServiceDependencyGraphResponseDTO().setMicoServices(micoServices);
       LinkedList<MicoServiceDependencyGraphEdgeResponseDTO> micoServiceDependencyGraphEdgeList = new LinkedList<>();
       for (MicoService micoService : micoServices) {
           //Request each mico service again from the db, because the dependencies are not included
           //in the result of the custom query. TODO improve query to also include the dependencies (Depth parameter)
           MicoService micoServiceFromDB = getServiceFromDatabase(micoService.getShortName(), micoService.getVersion());
           micoServiceFromDB.getDependencies().forEach(micoServiceDependency -> {
               MicoServiceDependencyGraphEdgeResponseDTO edge = new MicoServiceDependencyGraphEdgeResponseDTO(micoService,micoServiceDependency.getDependedService());
               micoServiceDependencyGraphEdgeList.add(edge);
           });
       }
       micoServiceDependencyGraph.setMicoServiceDependencyGraphEdgeList(micoServiceDependencyGraphEdgeList);
       return ResponseEntity.ok(new Resource<>(micoServiceDependencyGraph,
           linkTo(methodOn(ServiceController.class).getDependencyGraph(shortName, version)).withSelfRel()));
	}

    /**
     * Returns the existing {@link MicoService} object from the database for the given shortName and version.
     *
     * @param shortName the short name of a {@link MicoService}
     * @param version   the version of a {@link MicoService}
     * @return the existing {@link MicoService} from the database
     * @throws ResponseStatusException if a {@link MicoService} for the given shortName and version does not exist
     */
    private MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' was not found!");
        }
        return serviceOpt.get();
    }

    private List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) throws ResponseStatusException {
        List<MicoService> micoServiceList = serviceRepository.findByShortName(shortName);
        log.debug("Retrieve service list from database: {}", micoServiceList);
        if (micoServiceList.isEmpty()) {
            log.error("Service list is empty.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find any Service with name: '" + shortName);
        }
        return micoServiceList;
    }

    /**
     * Checks if a service is deployed and throws a ResponseStatusException with the http status CONFLICT (409) if
     * the service is deployed.
     * @param service Checks if this service is deployed
     * @throws KubernetesResourceException if the service is deployed. It uses the http status CONFLICT
     */
    private void throwConflictIfServiceIsDeployed(MicoService service) throws KubernetesResourceException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            log.info("Micoservice '{}' in version '{}' is deployed. It is not possible to delete a deployed service.", service.getShortName(), service.getVersion());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service is currently deployed!");
        }
    }
    
    protected static Resource<MicoServiceResponseDTO> getServiceResponseDTOResource(MicoService service) {
		return new Resource<MicoServiceResponseDTO>(new MicoServiceResponseDTO(service), getServiceLinks(service));
    }

    protected static List<Resource<MicoServiceResponseDTO>> getServiceResponseDTOResourcesList(List<MicoService> services) {
		return services.stream().map(service -> getServiceResponseDTOResource(service)).collect(Collectors.toList());
    }

    protected static Iterable<Link> getServiceLinks(MicoService service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getServiceList()).withRel("services"));
        return links;
    }

}
