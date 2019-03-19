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

import io.github.ust.mico.core.dto.request.CrawlingInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphEdgeResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDependencyGraphResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceResponseDTO;
import io.github.ust.mico.core.dto.response.status.MicoServiceStatusResponseDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.GitHubCrawler;
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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceResource {

    public static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    public static final String PATH_VARIABLE_VERSION = "version";
    public static final String PATH_VARIABLE_DEPENDEE_SHORT_NAME = "dependeeShortName";
    public static final String PATH_VARIABLE_DEPENDEE_VERSION = "dependeeVersion";
    public static final String PATH_VARIABLE_IMPORT = "import";
    public static final String PATH_VARIABLE_GITHUB = "github";
    public static final String PATH_GITHUB_ENDPOINT = "/" + PATH_VARIABLE_IMPORT + "/" + PATH_VARIABLE_GITHUB;
    public static final String PATH_DEPENDEES = "dependees";
    public static final String PATH_DEPENDERS = "dependers";
    public static final String PATH_PROMOTE = "promote";
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
            new Resources<>(serviceResources, linkTo(methodOn(ServiceResource.class).getServiceList()).withSelfRel()));
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

        for (MicoService micoService : micoServiceList) {
            throwConflictIfServiceIsDeployed(micoService);
        }
        micoServiceList.forEach(service -> serviceRepository.delete(service));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/status")
    public ResponseEntity<Resource<MicoServiceStatusResponseDTO>> getStatusOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoService> micoServiceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!micoServiceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' was not found!");
        }
        log.debug("Retrieve status information of Mico service '{}' '{}'", shortName, version);
        MicoServiceStatusResponseDTO serviceStatus = micoStatusService.getServiceStatus(micoServiceOptional.get());

        return ResponseEntity.ok(new Resource<>(serviceStatus));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoService> services = serviceRepository.findByShortName(shortName);
        List<Resource<MicoServiceResponseDTO>> serviceResources = getServiceResponseDTOResourcesList(services);
        return ResponseEntity.ok(
            new Resources<>(serviceResources,
                linkTo(methodOn(ServiceResource.class).getVersionsOfService(shortName)).withSelfRel()));
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
            .created(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(savedService.getShortName(), savedService.getVersion())).toUri())
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
                linkTo(methodOn(ServiceResource.class).getDependees(shortName, version)).withSelfRel()));
    }

    /**
     * Creates a new dependency edge between the Service and the depended service.
     */
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDEES
        + "/{" + PATH_VARIABLE_DEPENDEE_SHORT_NAME + "}/{" + PATH_VARIABLE_DEPENDEE_VERSION + "}")
    public ResponseEntity<Void> createNewDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                  @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                  @PathVariable(PATH_VARIABLE_DEPENDEE_SHORT_NAME) String dependeeShortName,
                                                  @PathVariable(PATH_VARIABLE_DEPENDEE_VERSION) String dependeeVersion) {
        MicoService service = getServiceFromDatabase(shortName, version);

        Optional<MicoService> serviceDependeeOpt = serviceRepository.findByShortNameAndVersion(dependeeShortName, dependeeVersion);
        if (!serviceDependeeOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The dependee service was not found!");
        }

        // Check if dependency is already set
        boolean dependencyAlreadyExists = (service.getDependencies() != null) && service.getDependencies().stream().anyMatch(
            dependency -> dependency.getDependedService().getShortName().equals(dependeeShortName)
                && dependency.getDependedService().getVersion().equals(dependeeVersion));
        if (dependencyAlreadyExists) {
            return ResponseEntity.noContent().build();
        }

        final MicoServiceDependency processedServiceDependee = new MicoServiceDependency()
            .setDependedService(serviceDependeeOpt.get())
            .setService(service);

        log.info("New dependency for MicoService '{}' '{}' -[:DEPENDS_ON]-> '{}' '{}'", shortName, version,
            processedServiceDependee.getDependedService().getShortName(),
            processedServiceDependee.getDependedService().getVersion());

        service.getDependencies().add(processedServiceDependee);
        serviceRepository.save(service);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDEES)
    public ResponseEntity<Void> deleteAllDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                   @PathVariable(PATH_VARIABLE_VERSION) String version) {
        // We only want to delete the relationships (the edges),
        // not the actual depended services.
        MicoService service = getServiceFromDatabase(shortName, version);
        service.getDependencies().clear();

        serviceRepository.save(service);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDEES
        + "/{" + PATH_VARIABLE_DEPENDEE_SHORT_NAME + "}/{" + PATH_VARIABLE_DEPENDEE_VERSION + "}")
    public ResponseEntity<Void> deleteDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                               @PathVariable(PATH_VARIABLE_VERSION) String version,
                                               @PathVariable(PATH_VARIABLE_DEPENDEE_SHORT_NAME) String dependeeShortName,
                                               @PathVariable(PATH_VARIABLE_DEPENDEE_VERSION) String dependeeVersion) {
        // We only want to delete the relationship (the edge),
        // not the actual depended service.
        MicoService service = getServiceFromDatabase(shortName, version);

        // Check whether dependee to delete exists
        Optional<MicoService> serviceOptToDelete = serviceRepository.findByShortNameAndVersion(dependeeShortName, dependeeVersion);
        if (!serviceOptToDelete.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service dependee '" + dependeeShortName + "' '" + dependeeVersion + "'  was not found!");
        }
        MicoService serviceToDelete = serviceOptToDelete.get();

        service.getDependencies().removeIf(dependency -> dependency.getDependedService().getId() == serviceToDelete.getId());
        serviceRepository.save(service);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_DEPENDERS)
    public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getDependers(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                    @PathVariable(PATH_VARIABLE_VERSION) String version) {
        return ResponseEntity.ok(
            new Resources<>(getServiceResponseDTOResourcesList(serviceRepository.findDependers(shortName, version)),
                linkTo(methodOn(ServiceResource.class).getDependers(shortName, version)).withSelfRel()));
    }

    @PostMapping(PATH_GITHUB_ENDPOINT)
    public ResponseEntity<Resource<MicoServiceResponseDTO>> importMicoServiceFromGitHub(@Valid @RequestBody CrawlingInfoRequestDTO crawlingInfo) {
        String url = crawlingInfo.getUrl();
        String version = crawlingInfo.getVersion();
        String dockerfilePath = crawlingInfo.getDockerfilePath();
        log.debug("Start importing MicoService from URL '{}'", url);

        try {
            if (version.equals("latest")) {
                MicoService service = crawler.crawlGitHubRepoLatestRelease(url, dockerfilePath);
                return createService(new MicoServiceRequestDTO(service));
            } else {
                MicoService service = crawler.crawlGitHubRepoSpecificRelease(url, version, dockerfilePath);
                return createService(new MicoServiceRequestDTO(service));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PROMOTE)
    public ResponseEntity<Resource<MicoServiceResponseDTO>> promoteService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                           @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                           @Valid @RequestBody MicoVersionRequestDTO newVersionDto) {
        log.debug("Received request to promote MicoService '{}' '{}' to version '{}'", shortName, version, newVersionDto.getVersion());

        // Service to promote (copy)
        MicoService service = getServiceFromDatabase(shortName, version);
        log.debug("Retrieved following MicoService from database: {}", service);

        // Update the version of the service.
        service.setVersion(newVersionDto.getVersion());

        // In order to copy the service along with all service interfaces nodes
        // and all port nodes of the service interface we need to set the id of
        // service interfaces and ports to null.
        // That way, Neo4j will create new entities instead of updating the existing ones.
        service.setId(null);
        service.getServiceInterfaces().forEach(serviceInterface -> serviceInterface.setId(null));
        service.getServiceInterfaces().forEach(serviceInterface -> serviceInterface.getPorts().forEach(port -> port.setId(null)));

        // Save the new (promoted) service in the database.
        MicoService updatedService = serviceRepository.save(service);
        log.debug("Saved following MicoService in database: {}", updatedService);
        log.info("Promoted service '{}': {} → {}", shortName, version, updatedService.getVersion());

        return ResponseEntity.ok(getServiceResponseDTOResource(updatedService));
    }

    @GetMapping(PATH_GITHUB_ENDPOINT)
    public ResponseEntity<Resources<Resource<MicoVersionRequestDTO>>> getVersionsFromGitHub(@RequestParam("url") String url) {
        try {
            log.debug("Start getting versions from URL '{}'.", url);
            List<Resource<MicoVersionRequestDTO>> versions = crawler.getVersionsFromGitHubRepo(url).stream()
                .map(version -> new Resource<>(new MicoVersionRequestDTO(version)))
                .collect(Collectors.toList());
            return ResponseEntity.ok(new Resources<>(versions, linkTo(methodOn(ServiceResource.class).getVersionsFromGitHub(url)).withSelfRel()));
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
        List<MicoServiceResponseDTO> micoServiceDTOS = micoServices.stream().map(MicoServiceResponseDTO::new).collect(Collectors.toList());
        MicoServiceDependencyGraphResponseDTO micoServiceDependencyGraph = new MicoServiceDependencyGraphResponseDTO().setMicoServices(micoServiceDTOS);
        LinkedList<MicoServiceDependencyGraphEdgeResponseDTO> micoServiceDependencyGraphEdgeList = new LinkedList<>();
        for (MicoService micoService : micoServices) {
            //Request each mico service again from the db, because the dependencies are not included
            //in the result of the custom query. TODO improve query to also include the dependencies (Depth parameter)
            MicoService micoServiceFromDB = getServiceFromDatabase(micoService.getShortName(), micoService.getVersion());
            micoServiceFromDB.getDependencies().forEach(micoServiceDependency -> {
                MicoServiceDependencyGraphEdgeResponseDTO edge = new MicoServiceDependencyGraphEdgeResponseDTO(micoService, micoServiceDependency.getDependedService());
                micoServiceDependencyGraphEdgeList.add(edge);
            });
        }
        micoServiceDependencyGraph.setMicoServiceDependencyGraphEdgeList(micoServiceDependencyGraphEdgeList);
        return ResponseEntity.ok(new Resource<>(micoServiceDependencyGraph,
            linkTo(methodOn(ServiceResource.class).getDependencyGraph(shortName, version)).withSelfRel()));
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
     *
     * @param service Checks if this {@link MicoService} is deployed
     * @throws KubernetesResourceException if the service is deployed. It uses the http status CONFLICT
     */
    private void throwConflictIfServiceIsDeployed(MicoService service) throws KubernetesResourceException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            log.info("Micoservice '{}' in version '{}' is deployed. It is not possible to delete a deployed service.", service.getShortName(), service.getVersion());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service is currently deployed!");
        }
    }

    protected static Resource<MicoServiceResponseDTO> getServiceResponseDTOResource(MicoService service) {
        return new Resource<>(new MicoServiceResponseDTO(service), getServiceLinks(service));
    }

    protected static List<Resource<MicoServiceResponseDTO>> getServiceResponseDTOResourcesList(List<MicoService> services) {
        return services.stream().map(service -> getServiceResponseDTOResource(service)).collect(Collectors.toList());
    }

    protected static Iterable<Link> getServiceLinks(MicoService service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ServiceResource.class).getServiceList()).withRel("services"));
        return links;
    }

}
