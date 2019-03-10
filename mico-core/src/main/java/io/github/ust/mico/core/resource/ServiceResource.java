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

import io.github.ust.mico.core.broker.ServiceBroker;
import io.github.ust.mico.core.dto.CrawlingInfoDTO;
import io.github.ust.mico.core.dto.MicoServiceDependencyGraphDTO;
import io.github.ust.mico.core.dto.MicoServiceDependencyGraphEdgeDTO;
import io.github.ust.mico.core.dto.MicoServiceStatusDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
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
import javax.validation.constraints.NotEmpty;
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
    public static final String PATH_VARIABLE_ID = "id";
    public static final String PATH_DELETE_SHORT_NAME = "shortNameToDelete";
    public static final String PATH_DELETE_VERSION = "versionToDelete";
    public static final String PATH_VARIABLE_IMPORT = "import";
    public static final String PATH_VARIABLE_GITHUB = "github";
    public static final String PATH_GITHUB_ENDPOINT = "/" + PATH_VARIABLE_IMPORT + "/" + PATH_VARIABLE_GITHUB;
    public static final String PATH_PROMOTE = "promote";

    @Autowired
    private ServiceBroker serviceBroker;

    //TODO: remove serviceRepository from ServiceResource
    @Autowired
    private MicoServiceRepository serviceRepository;

    //TODO. Verfiy if this object can be removed from ServiceResource
    @Autowired
    private MicoStatusService micoStatusService;

    //TODO. Verfiy if this object can be removed from ServiceResource
    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    //TODO. Verfiy if this object can be removed from ServiceResource
    @Autowired
    private GitHubCrawler crawler;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoService>>> getServiceList() {
        List<MicoService> services = serviceBroker.getAllServicesAsList();
        List<Resource<MicoService>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceResource.class).getServiceList()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoService>> getServiceByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService service = getServiceFromDatabase(shortName, version);
        return ResponseEntity.ok(new Resource<>(service, getServiceLinks(service)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<?> updateService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                           @PathVariable(PATH_VARIABLE_VERSION) String version,
                                           @Valid @RequestBody MicoService service) {
        if (!service.getShortName().equals(shortName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ShortName of the provided service does not match the request parameter");
        }
        if (!service.getVersion().equals(version)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Version of the provided service does not match the request parameter");
        }

        // Including interfaces must not be updated through this API. There is an own API for that purpose.
        if (service.getServiceInterfaces().size() > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Update of a service is only allowed without providing interfaces.");
        }

        //TODO: Replace with updateExistingService method inside ServiceBroker
        MicoService existingService = getServiceFromDatabase(shortName, version);
        service.setId(existingService.getId());
        service.setServiceInterfaces(existingService.getServiceInterfaces());
        MicoService updatedService = serviceRepository.save(service);

        return ResponseEntity.ok(new Resource<>(updatedService,
                linkTo(methodOn(ServiceResource.class).updateService(shortName, version, service)).withSelfRel()));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Void> deleteService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                              @PathVariable(PATH_VARIABLE_VERSION) String version) throws KubernetesResourceException {
        //TODO: Use ServiceBroker instead
        MicoService service = getServiceFromDatabase(shortName, version);
        throwConflictIfServiceIsDeployed(service);
        if (!getDependers(service).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Service '" + service.getShortName() + "' '" + service.getVersion() + "' has dependers, therefore it can't be deleted.");
        }
        //TODO: Replace with deleteService inside ServiceBroker
        serviceRepository.deleteServiceByShortNameAndVersion(shortName, version);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if a service is deployed and throws a ResponseStatusException with the http status CONFLICT (409) if
     * the service is deployed.
     *
     * @param service Checks if this service is deployed
     * @throws KubernetesResourceException if the service is deployed. It uses the http status CONFLICT
     */
    //TODO: Verify if this should be moved to ServiceBroker
    private void throwConflictIfServiceIsDeployed(MicoService service) throws KubernetesResourceException {
        if (micoKubernetesClient.isMicoServiceDeployed(service)) {
            log.info("Micoservice '{}' in version '{}' is deployed. It is not possible to delete a deployed service.", service.getShortName(), service.getVersion());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Service is currently deployed!");
        }
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Void> deleteAllVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) throws KubernetesResourceException {
        List<MicoService> micoServiceList = getAllVersionsOfServiceFromDatabase(shortName);
        log.debug("Got following services from database: {}", micoServiceList);
        for (MicoService micoService : micoServiceList) {
            throwConflictIfServiceIsDeployed(micoService);
        }
        micoServiceList.forEach(service -> serviceRepository.delete(service));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/status")
    public ResponseEntity<Resource<MicoServiceStatusDTO>> getStatusOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoServiceStatusDTO serviceStatus = new MicoServiceStatusDTO();
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
    public ResponseEntity<Resources<Resource<MicoService>>> getVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoService> services = serviceRepository.findByShortName(shortName);
        List<Resource<MicoService>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceResource.class).getVersionsOfService(shortName)).withSelfRel()));
    }

    @PostMapping
    public ResponseEntity<?> createService(@Valid @RequestBody MicoService newService) {
        Optional<MicoService> serviceOptional = serviceRepository.
                findByShortNameAndVersion(newService.getShortName(), newService.getVersion());
        if (serviceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Service '" + newService.getShortName() + "' '" + newService.getVersion() + "' already exists.");
        }
        for (MicoServiceInterface serviceInterface : newService.getServiceInterfaces()) {
            validateProvidedInterface(newService.getShortName(), newService.getVersion(), serviceInterface);
        }

        MicoService savedService = serviceRepository.save(newService);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceResource.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(newService, getServiceLinks(newService)));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resources<Resource<MicoService>>> getDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService service = getServiceFromDatabase(shortName, version);
        List<MicoServiceDependency> dependees = service.getDependencies();
        if (dependees == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service dependees should not be null");
        }

        LinkedList<MicoService> services = getDependentServices(dependees);

        List<Resource<MicoService>> resourceList = getServiceResourcesList(services);

        return ResponseEntity.ok(
                new Resources<>(resourceList,
                        linkTo(methodOn(ServiceResource.class).getDependees(shortName, version)).withSelfRel()));
    }

    /**
     * Create a new dependency edge between the Service and the dependee service.
     */
    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<MicoService>> createNewDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                   @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                   @Valid @RequestBody MicoServiceDependency newServiceDependee) {
        MicoService service = getServiceFromDatabase(shortName, version);

        Optional<MicoService> serviceDependeeOpt = serviceRepository.findByShortNameAndVersion(newServiceDependee.getDependedService().getShortName(),
                newServiceDependee.getDependedService().getVersion());
        if (!serviceDependeeOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The dependee service was not found!");
        }

        // Check if dependency is already set
        String localShortName = newServiceDependee.getDependedService().getShortName();
        String localVersion = newServiceDependee.getDependedService().getVersion();
        boolean dependencyAlreadyExists = (service.getDependencies() != null) && service.getDependencies().stream().anyMatch(
                dependency -> dependency.getDependedService().getShortName().equals(localShortName)
                        && dependency.getDependedService().getVersion().equals(localVersion));
        if (dependencyAlreadyExists) {
            return ResponseEntity
                    .created(linkTo(methodOn(ServiceResource.class).getServiceById(service.getId())).toUri())
                    .body(new Resource<>(service, getServiceLinks(service)));
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
                .created(linkTo(methodOn(ServiceResource.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees")
    public ResponseEntity<Resource<MicoService>> deleteAllDependees(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                    @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService service = getServiceFromDatabase(shortName, version);
        List<MicoServiceDependency> dependees = new LinkedList<>();
        service.setDependencies(dependees);

        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceResource.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependees"
            + "/{" + PATH_DELETE_SHORT_NAME + "}/{" + PATH_DELETE_VERSION + "}")
    public ResponseEntity<Resource<MicoService>> deleteDependee(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                @PathVariable(PATH_DELETE_SHORT_NAME) String shortNameToDelete,
                                                                @PathVariable(PATH_DELETE_VERSION) String versionToDelete) {
        MicoService service = getServiceFromDatabase(shortName, version);

        Optional<MicoService> serviceOptToDelete = serviceRepository.findByShortNameAndVersion(shortNameToDelete, versionToDelete);
        if (!serviceOptToDelete.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service dependee '" + shortNameToDelete + "' '" + versionToDelete + "'  was not found!");
        }
        MicoService serviceToDelete = serviceOptToDelete.get();

        List<MicoServiceDependency> newDependees = new LinkedList<>();
        List<MicoServiceDependency> dependees = service.getDependencies();

        if (dependees != null) {
            dependees.forEach(dependsOn -> {
                if (dependsOn.getDependedService().getId() != serviceToDelete.getId()) {
                    newDependees.add(dependsOn);
                }
            });
        }

        service.setDependencies(newDependees);
        MicoService savedService = serviceRepository.save(service);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceResource.class).getServiceById(savedService.getId())).toUri())
                .body(new Resource<>(savedService, getServiceLinks(savedService)));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependers")
    public ResponseEntity<Resources<Resource<MicoService>>> getDependers(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService serviceToLookFor = getServiceFromDatabase(shortName, version);
        List<MicoService> dependers = getDependers(serviceToLookFor);

        List<Resource<MicoService>> resourceList = getServiceResourcesList(dependers);
        return ResponseEntity.ok(
                new Resources<>(resourceList,
                        linkTo(methodOn(ServiceResource.class).getDependers(shortName, version)).withSelfRel()));
    }

    @PostMapping(PATH_GITHUB_ENDPOINT)
    public ResponseEntity<?> importMicoServiceFromGitHub(@RequestBody CrawlingInfoDTO crawlingInfo) {
        String url = crawlingInfo.getUrl();
        String version = crawlingInfo.getVersion();
        log.debug("Start importing MicoService from URL '{}'", url);

        try {
            if (version.equals("latest")) {
                MicoService service = crawler.crawlGitHubRepoLatestRelease(url);
                return createService(service);
            } else {
                MicoService service = crawler.crawlGitHubRepoSpecificRelease(url, version);
                return createService(service);
            }
        } catch (IOException e) {
            log.error(e.getStackTrace().toString());
            log.error("Getting exception '{}'", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PROMOTE)
    public ResponseEntity<Resource<MicoService>> promoteService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                @NotEmpty @RequestBody String newVersion) {
        // Service to promote (copy)
        MicoService micoService = getServiceFromDatabase(shortName, version);
        log.debug("Received following MicoService from database: {}", micoService);

        // Due to the merge capability of neo4j, we need to set the id to null
        // to prevent neo4j from overwriting the old version
        micoService.setVersion(newVersion).setId(null);

        // Save the new (promoted) service in the database
        MicoService updatedService = serviceRepository.save(micoService);
        log.debug("Saved following MicoService in database: {}", updatedService);

        return ResponseEntity
                .created(linkTo(methodOn(ServiceResource.class).getServiceById(updatedService.getId())).toUri())
                .body(new Resource<>(updatedService, getServiceLinks(updatedService)));
    }

    @GetMapping(PATH_GITHUB_ENDPOINT)
    @ResponseBody
    public LinkedList<String> getVersionsFromGitHub(@RequestParam String url) {
        log.debug("Start getting versions from URL '{}'", url);

        try {
            return crawler.getVersionsFromGitHubRepo(url);

        } catch (IOException e) {
            log.error(e.getStackTrace().toString());
            log.error("Getting exception '{}'", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/dependencyGraph")
    public ResponseEntity<Resource<MicoServiceDependencyGraphDTO>> getDependencyGraph(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                      @PathVariable(PATH_VARIABLE_VERSION) String version) {
        MicoService micoServiceRoot = getServiceFromDatabase(shortName, version);
        List<MicoService> micoServices = serviceRepository.getAllDependeesOfMicoService(micoServiceRoot.getShortName(), micoServiceRoot.getVersion());
        MicoServiceDependencyGraphDTO micoServiceDependencyGraph = new MicoServiceDependencyGraphDTO().setMicoServices(micoServices);
        LinkedList<MicoServiceDependencyGraphEdgeDTO> micoServiceDependencyGraphEdgeList = new LinkedList<>();
        for (MicoService micoService : micoServices) {
            //Request each mico service again from the db, because the dependencies are not included
            //in the result of the custom query. TODO improve query to also include the dependencies (Depth parameter)
            MicoService micoServiceFromDB = getServiceFromDatabase(micoService.getShortName(), micoService.getVersion());
            micoServiceFromDB.getDependencies().forEach(micoServiceDependency -> {
                MicoServiceDependencyGraphEdgeDTO edge = new MicoServiceDependencyGraphEdgeDTO(micoService, micoServiceDependency.getDependedService());
                micoServiceDependencyGraphEdgeList.add(edge);
            });
        }
        micoServiceDependencyGraph.setMicoServiceDependencyGraphEdgeList(micoServiceDependencyGraphEdgeList);
        return ResponseEntity.ok(new Resource<>(micoServiceDependencyGraph,
                linkTo(methodOn(ServiceResource.class).getDependencyGraph(shortName, version)).withSelfRel()));
    }

    //TODO: Remove here, this method is moved to ServiceBroker
    public List<MicoService> getDependers(MicoService serviceToLookFor) {
        List<MicoService> serviceList = serviceRepository.findAll(2);

        List<MicoService> dependers = new LinkedList<>();

        serviceList.forEach(service -> {
            List<MicoServiceDependency> dependees = service.getDependencies();
            if (dependees != null) {
                dependees.forEach(dependee -> {
                    if (dependee.getDependedService().equals(serviceToLookFor)) {
                        dependers.add(dependee.getService());
                    }
                });
            }
        });

        return dependers;
    }

    private MicoService getService(MicoService newService) {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(newService.getShortName(), newService.getVersion());
        return serviceOptional.orElse(null);
    }

    /**
     * Returns the existing {@link MicoService} object from the database for the given shortName and version.
     *
     * @param shortName the short name of a {@link MicoService}
     * @param version   the version of a {@link MicoService}
     * @return the existing {@link MicoService} from the database
     * @throws ResponseStatusException if a {@link MicoService} for the given shortName and version does not exist
     */
    // Remove here, already moved to ServiceBroker
    private MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' was not found!");
        }
        return serviceOpt.get();
    }

    //TODO: Remove, it already exists in ServiceBroker
    private List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) throws ResponseStatusException {
        List<MicoService> micoServiceList = serviceRepository.findByShortName(shortName);
        log.debug("Retrieve service list from database: {}", micoServiceList);
        if (micoServiceList.isEmpty()) {
            log.error("Service list is empty.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find any Service with name: '" + shortName);
        }
        return micoServiceList;
    }

    //TODO: Remove method, it already exists in ServiceBroker
    public MicoService getServiceById(Long id) {
        Optional<MicoService> serviceOpt = serviceRepository.findById(id);
        if (!serviceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service with id '" + id + "' was not found!");
        }
        return serviceOpt.get();
    }

    //Get the dependees of a service, check if they exists, if true get the ids and set the dependees
    public MicoService setServiceDependees(MicoService newService) {
        MicoService serviceToGetId = getService(newService);
        if (serviceToGetId == null) {
            // TODO: MicoService name is mandatory! Will be covered by issue mico#490
            MicoService savedService = serviceRepository.save(new MicoService().setShortName(newService.getShortName()).setVersion(newService.getVersion()));

            List<MicoServiceDependency> dependees = savedService.getDependencies();
            LinkedList<MicoService> services = getDependentServices(dependees);

            List<MicoServiceDependency> newDependees = new LinkedList<>();

            if (services != null) {
                services.forEach(service -> newDependees.add(new MicoServiceDependency().setService(savedService).setDependedService(service)));
            }

            savedService.setDependencies(newDependees);

            return savedService;
        } else {
            newService.setId(serviceToGetId.getId());
            List<MicoServiceDependency> dependees = newService.getDependencies();
            LinkedList<MicoService> services = getDependentServices(dependees);

            List<MicoServiceDependency> newDependees = new LinkedList<>();

            services.forEach(service -> newDependees.add(new MicoServiceDependency().setService(newService).setDependedService(service)));

            newService.setDependencies(newDependees);

            return newService;
        }
    }

    private LinkedList<MicoService> getDependentServices(List<MicoServiceDependency> dependees) {
        if (dependees == null) {
            return null;
        }

        LinkedList<MicoService> services = new LinkedList<>();

        dependees.forEach(dependee -> {
            String shortName = dependee.getDependedService().getShortName();
            String version = dependee.getDependedService().getVersion();

            Optional<MicoService> dependeeServiceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
            MicoService dependeeService = dependeeServiceOpt.orElse(null);
            if (dependeeService == null) {
                // TODO: MicoService name is mandatory! Will be covered by issue mico#490
                MicoService newService = serviceRepository.save(new MicoService().setShortName(shortName).setVersion(version));
                services.add(newService);
            } else {
                services.add(dependeeService);
            }
        });

        return services;
    }

    static List<Resource<MicoService>> getServiceResourcesList(List<MicoService> services) {
        return services.stream().map(service -> new Resource<>(service, getServiceLinks(service)))
                .collect(Collectors.toList());
    }

    private static Iterable<Link> getServiceLinks(MicoService service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ServiceResource.class).getServiceList()).withRel("services"));
        return links;
    }

    /**
     * Validates the {@link MicoServiceInterface} with the data that is stored in the database.
     * If the provided interface is valid, return the existing interface.
     *
     * @param providedInterface the {@link MicoServiceInterface}
     * @return the already existing {@link MicoServiceInterface}
     * @throws ResponseStatusException if a {@link MicoServiceInterface} does not exist or there is a conflict
     */
    private MicoServiceInterface validateProvidedInterface(
            String serviceName, String serviceVersion, MicoServiceInterface providedInterface) throws ResponseStatusException {

        // Check if the provided interface exists
        Optional<MicoServiceInterface> existingInterfaceOptional = serviceRepository.findInterfaceOfServiceByName(
                providedInterface.getServiceInterfaceName(), serviceName, serviceVersion);
        if (!existingInterfaceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Provided interface '" + providedInterface.getServiceInterfaceName() + "' of service '" +
                            serviceName + "' '" + serviceVersion + "' does not exist!");
        }

        // If more than the name of the interface is provided,
        // check if the data is consistent. If not throw a 409 conflict error.
        MicoServiceInterface existingInterface = existingInterfaceOptional.get();
        if (providedInterface.getId() != null
                && !providedInterface.getId().equals(existingInterface.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided interface '" + providedInterface.getServiceInterfaceName() + "' of service '" +
                            serviceName + "' '" + serviceVersion +
                            "' has a conflict in the property 'id' with the existing interface!");
        }
        if (providedInterface.getDescription() != null
                && !providedInterface.getDescription().equals(existingInterface.getDescription())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided interface '" + providedInterface.getServiceInterfaceName() + "' of service '" +
                            serviceName + "' '" + serviceVersion +
                            "' has a conflict in the property 'description' with the existing interface!");
        }
        if (providedInterface.getProtocol() != null
                && !providedInterface.getProtocol().equals(existingInterface.getProtocol())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided interface '" + providedInterface.getServiceInterfaceName() + "' of service '" +
                            serviceName + "' '" + serviceVersion +
                            "' has a conflict in the property 'protocol' with the existing interface!");
        }
        if (providedInterface.getPublicDns() != null
                && !providedInterface.getPublicDns().equals(existingInterface.getPublicDns())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided interface '" + providedInterface.getServiceInterfaceName() + "' of service '" +
                            serviceName + "' '" + serviceVersion +
                            "' has a conflict in the property 'publicDns' with the existing interface!");
        }
        if (providedInterface.getTransportProtocol() != null
                && !providedInterface.getTransportProtocol().equals(existingInterface.getTransportProtocol())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided interface '" + providedInterface.getServiceInterfaceName() + "' of service '" +
                            serviceName + "' '" + serviceVersion +
                            "' has a conflict in the property 'transportProtocol' with the existing interface!");
        }
        return existingInterface;
    }

}
