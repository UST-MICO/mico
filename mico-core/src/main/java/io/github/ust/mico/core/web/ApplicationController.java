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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.*;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.PrometheusRequestFailedException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = "/" + ApplicationController.PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationController {

    private static final String SERVICE_SHORT_NAME = "serviceShortName";

    public static final String PATH_SERVICES = "services";
    public static final String PATH_APPLICATIONS = "applications";

    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    private static final int MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT = 1;
    private static final String PROMETHEUS_QUERY_FOR_MEMORY_USAGE = "sum(container_memory_working_set_bytes{pod_name=\"%s\",container_name=\"\"})";
    private static final String PROMETHEUS_QUERY_FOR_CPU_USAGE = "sum(container_cpu_load_average_10s{pod_name=\"%s\"})";
    private static final String PROMETHEUS_QUERY_PARAMETER_NAME = "query";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private PrometheusConfig prometheusConfig;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplication>>> getAllApplications() {
        List<MicoApplication> allApplications = applicationRepository.findAll(3);
        List<Resource<MicoApplication>> applicationResources = getApplicationResourceList(allApplications);

        return ResponseEntity.ok(
            new Resources<>(applicationResources,
                linkTo(methodOn(ApplicationController.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplication>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        List<Resource<MicoApplication>> applicationResourceList = getApplicationResourceList(micoApplicationList);

        return ResponseEntity.ok(
            new Resources<>(applicationResourceList,
                linkTo(methodOn(ApplicationController.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        return applicationOptional.map(application -> new Resource<>(application, getApplicationLinks(application)))
            .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' links not found!"));
    }

    @PostMapping
    public ResponseEntity<Resource<MicoApplication>> createApplication(@RequestBody MicoApplication newApplication) {
        Optional<MicoApplication> applicationOptional = applicationRepository.
            findByShortNameAndVersion(newApplication.getShortName(), newApplication.getVersion());
        if (applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Application '" + newApplication.getShortName() + "' '" + newApplication.getVersion() + "' already exists.");
        }
        for (MicoService providedService : newApplication.getServices()) {
            validateProvidedService(providedService);
        }

        // TODO Update deploy info here if necessary

        MicoApplication savedApplication = applicationRepository.save(newApplication);

        return ResponseEntity
            .created(linkTo(methodOn(ApplicationController.class)
                .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
            .body(new Resource<>(savedApplication, getApplicationLinks(savedApplication)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                       @RequestBody MicoApplication application) {
        if (!application.getShortName().equals(shortName) || !application.getVersion().equals(version)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application shortName or version does not match request body.");
        }
        Optional<MicoApplication> existingApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!existingApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        MicoApplication existingApplication = existingApplicationOptional.get();

        // Including services must not be updated through this API. There is an own API for that purpose.
        if (application.getServices().size() > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Update of an application is only allowed without services.");
        }

        application.setId(existingApplication.getId());
        application.setServices(existingApplication.getServices());
        MicoApplication updatedApplication = applicationRepository.save(application);

        return ResponseEntity.ok(new Resource<>(updatedApplication, linkTo(methodOn(ApplicationController.class).updateApplication(shortName, version, application)).withSelfRel()));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> deleteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            // application already deleted
            return ResponseEntity.noContent().build();
        }
        applicationOptional.map(application -> {
            if (application.getDeploymentInfo() == null || application.getDeploymentInfo().getServiceDeploymentInfos() != null) {
                return application; // TODO better deployment detection
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is currently deployed!");
        }).map(application -> {
            application.getServices().clear();
            return application;
        }).map(application -> {
            applicationRepository.save(application);
            return application;
        }).map(application -> {
            applicationRepository.delete(application);
            return application;
        });
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
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Application '" + shortName + "' '" + version + "' was not found!");
        }
        MicoApplication micoApplication = applicationOptional.get();
        List<MicoService> micoServices = micoApplication.getServices();
        List<Resource<MicoService>> micoServicesWithLinks = ServiceController.getServiceResourcesList(micoServices);
        return ResponseEntity.ok(
            new Resources<>(micoServicesWithLinks,
                linkTo(methodOn(ApplicationController.class).getServicesFromApplication(shortName, version)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                        @RequestBody MicoService providedService) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(applicationShortName, applicationVersion);
        if (!applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Application '" + applicationShortName + "' '" + applicationVersion + "' does not exist!");
        }
        MicoService existingService = validateProvidedService(providedService);

        MicoApplication application = applicationOptional.get();
        if (!application.getServices().contains(existingService)) {
            log.info("Add service '" + existingService.getShortName() + "' '" + existingService.getVersion() +
                "' to application '" + applicationShortName + "' '" + applicationVersion + "'.");
            application.getServices().add(existingService);
            applicationRepository.save(application);
        } else {
            log.info("Application '" + applicationShortName + "' '" + applicationVersion +
                "' already contains service '" + existingService.getShortName() + "' '" + existingService.getVersion() + "'.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/services/{" + SERVICE_SHORT_NAME + "}")
    public ResponseEntity deleteServiceFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                       @PathVariable(SERVICE_SHORT_NAME) String serviceShortName) {
        log.debug("Delete Mico service '{}' from Mico application '{}' in version '{}'", SERVICE_SHORT_NAME, shortName, version);

        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (micoApplicationOptional.isPresent()) {
            log.debug("Application is present");
            MicoApplication micoApplication = micoApplicationOptional.get();
            List<MicoService> services = micoApplication.getServices();
            log.debug("Service list has size: {}", services.size());
            Predicate<MicoService> matchServiceShortName = service -> service.getShortName().equals(serviceShortName);
            services.removeIf(matchServiceShortName);
            applicationRepository.save(micoApplication);

            // TODO Update Kubernetes deployment

            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no such application");
        }
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/status")
    public ResponseEntity<Resource<MicoApplicationDeploymentInformationDTO>> getStatusOfApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                    @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!micoApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }

        List<MicoService> micoServices = micoApplicationOptional.get().getServices();
        log.debug("Aggregate status information of Mico application '{}' '{}' with {} included services",
            shortName, version, micoServices.size());

        MicoApplicationDeploymentInformationDTO applicationDeploymentInformation = new MicoApplicationDeploymentInformationDTO();
        for (MicoService micoService : micoServices) {
            Optional<Deployment> deploymentOptional;
            try {
                deploymentOptional = micoKubernetesClient.getDeploymentOfMicoService(micoService);
            } catch (KubernetesResourceException e) {
                log.error("Error while retrieving Kubernetes deployment of MicoService '{}' '{}'. Continue with next one. Caused by: {}",
                    micoService.getShortName(), micoService.getVersion(), e.getMessage());
                continue;
            }
            if (!deploymentOptional.isPresent()) {
                log.warn("There is no deployment of the MicoService '{}' '{}'. Continue with next one.",
                    micoService.getShortName(), micoService.getVersion());
                continue;
            }

            Deployment deployment = deploymentOptional.get();
            MicoServiceDeploymentInformationDTO serviceDeploymentInformation = new MicoServiceDeploymentInformationDTO();
            int requestedReplicas = deployment.getSpec().getReplicas();
            int availableReplicas = deployment.getStatus().getAvailableReplicas();
            serviceDeploymentInformation.setAvailableReplicas(availableReplicas);
            serviceDeploymentInformation.setRequestedReplicas(requestedReplicas);

            List<Pod> podList = micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(micoService);
            List<KubernetesPodInfoDTO> podInfos = new LinkedList<>();
            for (Pod pod : podList) {
                KubernetesPodInfoDTO podInfo = getUiPodInfo(pod);
                podInfos.add(podInfo);
            }
            serviceDeploymentInformation.setPodInfo(podInfos);
            applicationDeploymentInformation.getServiceDeploymentInformation().add(serviceDeploymentInformation);

            List<MicoServiceInterface> serviceInterfaces = micoService.getServiceInterfaces();
            if (serviceInterfaces.size() < MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "There are " + serviceInterfaces.size() + " service interfaces of MicoService '" +
                        micoService.getShortName() + "' '" + micoService.getVersion() + "'. That is less than the required " +
                        MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT + " service interfaces.");
            }

            for (MicoServiceInterface micoServiceInterface : serviceInterfaces) {
                String serviceInterfaceName = micoServiceInterface.getServiceInterfaceName();
                Optional<Service> kubernetesServiceOptional;
                try {
                    kubernetesServiceOptional = micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, serviceInterfaceName);
                } catch (KubernetesResourceException e) {
                    log.error("Error while retrieving Kubernetes services of MicoServiceInterface '{}' of MicoService '{}' '{}'. " +
                            "Continue with next one. Caused by: {}",
                        serviceInterfaceName, micoService.getShortName(), micoService.getVersion(), e.getMessage());
                    continue;
                }
                if (!kubernetesServiceOptional.isPresent()) {
                    log.warn("There is no service of the MicoServiceInterface '{}' of MicoService '{}' '{}'.",
                        micoService.getShortName(), micoService.getVersion());
                    continue;
                }

                Service kubernetesService = kubernetesServiceOptional.get();
                List<MicoServiceInterfaceDTO> interfacesInformation = new LinkedList<>();
                String serviceName = kubernetesService.getMetadata().getName();
                MicoServiceInterfaceDTO interfaceInformation = new MicoServiceInterfaceDTO(serviceName);
                interfacesInformation.add(interfaceInformation);
                serviceDeploymentInformation.setInterfacesInformation(interfacesInformation);
            }
        }
        return ResponseEntity.ok(new Resource<>(applicationDeploymentInformation));
    }

    private boolean serviceExists(MicoApplication micoApplication, String serviceShortName) {
        return micoApplication.getServices().stream().anyMatch(existingService -> existingService.getShortName().equals(serviceShortName));
    }

    private List<Resource<MicoApplication>> getApplicationResourceList(List<MicoApplication> applications) {
        return applications.stream().map(application -> new Resource<>(application, getApplicationLinks(application)))
            .collect(Collectors.toList());
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

    private KubernetesPodInfoDTO getUiPodInfo(Pod pod) {
        String nodeName = pod.getSpec().getNodeName();
        String podName = pod.getMetadata().getName();
        String phase = pod.getStatus().getPhase();
        String hostIp = pod.getStatus().getHostIP();
        int memoryUsage = -1;
        int cpuLoad = -1;
        KuberenetesPodMetricsDTO podMetrics = new KuberenetesPodMetricsDTO();
        try {
            memoryUsage = getMemoryUsageForPod(podName);
            cpuLoad = getCpuLoadForPod(podName);
            podMetrics.setAvailable(true);
        } catch (PrometheusRequestFailedException | ResourceAccessException e) {
            podMetrics.setAvailable(false);
            log.error(e.getMessage(), e);
        }
        podMetrics.setMemoryUsage(memoryUsage);
        podMetrics.setCpuLoad(cpuLoad);
        return new KubernetesPodInfoDTO(podName, phase, hostIp, nodeName, podMetrics);
    }

    private int getMemoryUsageForPod(String podName) throws PrometheusRequestFailedException {
        URI prometheusUri = getPrometheusUri(PROMETHEUS_QUERY_FOR_MEMORY_USAGE, podName);
        return requestValueFromPrometheus(prometheusUri);
    }

    private int getCpuLoadForPod(String podName) throws PrometheusRequestFailedException {
        URI prometheusUri = getPrometheusUri(PROMETHEUS_QUERY_FOR_CPU_USAGE, podName);
        return requestValueFromPrometheus(prometheusUri);
    }

    private int requestValueFromPrometheus(URI prometheusUri) throws PrometheusRequestFailedException {

        ResponseEntity<PrometheusResponse> response
            = restTemplate.getForEntity(prometheusUri, PrometheusResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PrometheusResponse prometheusMemoryResponse = response.getBody();
            if (prometheusMemoryResponse != null) {
                if (prometheusMemoryResponse.wasSuccessful()) {
                    return prometheusMemoryResponse.getValue();
                } else {
                    throw new PrometheusRequestFailedException("The status of the prometheus response was " + prometheusMemoryResponse.getStatus(), response.getStatusCode(), prometheusMemoryResponse.getStatus());
                }
            } else {
                throw new PrometheusRequestFailedException("There was no response body", response.getStatusCode(), null);
            }
        } else {
            throw new PrometheusRequestFailedException("The http status code was not 2xx", response.getStatusCode(), null);
        }
    }

    private URI getPrometheusUri(String query, String podName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(prometheusConfig.getUri());
        uriBuilder.queryParam(PROMETHEUS_QUERY_PARAMETER_NAME, String.format(query, podName));
        URI prometheusUri = uriBuilder.build().toUri();
        log.debug("Using Prometheus URI '{}'", prometheusUri);
        return prometheusUri;
    }

}
