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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.broker.BackgroundJobBroker;
import io.github.ust.mico.core.dto.response.MicoApplicationJobStatusResponseDTO;
import io.github.ust.mico.core.exception.ImageBuildException;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoCoreBackgroundJobFactory;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.service.imagebuilder.buildtypes.Build;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/applications/{shortName}/{version}/deploy", produces = MediaTypes.HAL_JSON_VALUE)
public class DeploymentResource {
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private BackgroundJobBroker backgroundJobBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private MicoCoreBackgroundJobFactory backgroundJobFactory;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @PostMapping
    public ResponseEntity<Resource<MicoApplicationJobStatusResponseDTO>> deploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        try {
            imageBuilder.init();
        } catch (NotInitializedException e) {
            log.error(e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Initialization of the image build failed. Caused by: " + e.getMessage());
        }

        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!micoApplicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }

        MicoApplication micoApplication = micoApplicationOptional.get();
        checkIfMicoApplicationIsDeployable(micoApplication);

        log.info("Deploy MicoApplication '{}' in version '{}' with {} included MicoService(s).",
            shortName, version, micoApplication.getServices().size());
        for (MicoService micoService : micoApplication.getServices()) {
            // Check if a build for this MicoService is already running.
            // If yes no build is required, lock changes to running jobs.
            // If the current job status is done, error or cancel delete it and create a new job to get a new id.
            Optional<MicoServiceBackgroundJob> jobOptional = backgroundJobBroker.getJobByMicoService(
                micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD);
            if (jobOptional.isPresent()) {
                if (jobOptional.get().getStatus() != MicoServiceBackgroundJob.Status.RUNNING) {
                    backgroundJobBroker.deleteJob(jobOptional.get().getId());
                } else {
                    log.debug("Build job of MicoService '{}' '{}' is already running.",
                        micoService.getShortName(), micoService.getVersion());
                    continue;
                }
            }

            log.info("Start build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            MicoServiceBackgroundJob job = new MicoServiceBackgroundJob()
                .setServiceShortName(micoService.getShortName())
                .setServiceVersion(micoService.getVersion())
                .setType(MicoServiceBackgroundJob.Type.BUILD);
            backgroundJobBroker.saveJob(job);

            job.setFuture(backgroundJobFactory.runAsync(() -> buildImageAndWait(micoService), dockerImageUri -> {
                if (dockerImageUri != null) {
                    log.info("Build of MicoService '{}' in version '{}' finished with image '{}'.",
                        micoService.getShortName(), micoService.getVersion(), dockerImageUri);

                    micoService.setDockerImageUri(dockerImageUri);
                    MicoService savedMicoService = serviceRepository.save(micoService);
                    try {
                        createKubernetesResources(micoApplication, savedMicoService);
                        backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                            MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.DONE);
                    } catch (KubernetesResourceException kre) {
                        backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                            MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, kre.getMessage());
                        log.error(kre.getMessage(), kre);
                        exceptionHandler(kre);
                    }
                } else {
                    log.error("Build of MicoService '{}' in version '{}' failed.", micoService.getShortName(), micoService.getVersion());
                }
            }, this::exceptionHandler));

            backgroundJobBroker.saveJob(job);
        }

        return ResponseEntity
            .accepted()
            .body(new Resource<>(new MicoApplicationJobStatusResponseDTO(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version))));
    }

    @DeleteMapping
    public ResponseEntity<Void> undeploy(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
    	// Retrieve application from database and check whether it exists
    	Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }

        MicoApplication application = applicationOptional.get();
        log.info("Undeploy MicoApplication '{}' in version '{}' with {} included MicoService(s).",
        	shortName, version, application.getServices().size());
        
        // Check if application is deployed (only for debugging purposes)
        if (!micoKubernetesClient.isApplicationDeployed(application)) {
        	log.debug("MicoApplication '{}' in version '{}' currently is not deployed.", application.getShortName(), application.getVersion());
        } else {
        	micoKubernetesClient.undeployApplication(application);
        }
        
        return ResponseEntity.noContent().build();
    }

    private void checkIfMicoApplicationIsDeployable(MicoApplication micoApplication) {
        if (micoApplication.getServices() == null || micoApplication.getServices().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Application '" + micoApplication.getShortName() + "' '" + micoApplication.getVersion() + "' does not include any services!");
        }
        for (MicoService micoService : micoApplication.getServices()) {
            if (micoService.getServiceInterfaces() == null || micoService.getServiceInterfaces().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Application '" + micoApplication.getShortName() + "' '" + micoApplication.getVersion() + "' includes the service '"
                        + micoService.getShortName() + "' '" + micoService.getVersion() + "' that does not include any interfaces!");
            }
            if (!micoService.getDependencies().isEmpty()) {
                // TODO: Check if dependencies are valid. Covered by mico#583
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                    "The deployment of service dependencies is currently not implemented. " +
                        "See https://github.com/UST-MICO/mico/issues/583");
            }
        }
    }

    private String buildImageAndWait(MicoService micoService) {
        backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
            MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.RUNNING);
        try {
            Build build = imageBuilder.build(micoService);
            String buildName = build.getMetadata().getName();

            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<Boolean> booleanCompletableFuture = imageBuilder.waitUntilBuildIsFinished(buildName);
            backgroundJobBroker.saveFutureOfJob(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD, booleanCompletableFuture);
            if (booleanCompletableFuture.get()) {
                return imageBuilder.createImageName(micoService.getShortName(), micoService.getVersion());
            } else {
                booleanCompletableFuture.cancel(true);
                throw new ImageBuildException("Build for service " + micoService.getShortName() + " in version " + micoService.getVersion() + " failed");
            }
        } catch (NotInitializedException | InterruptedException | ExecutionException | ImageBuildException | TimeoutException e) {
            log.error(e.getMessage(), e);
            backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, e.getMessage());
            // TODO Handle NotInitializedException in async Job properly
            return null;
        }
    }

    /**
     * Creates the Kubernetes resources based on the service deployment
     * information of the provided {@link MicoApplication}.
     *
     * @param micoApplication the {@link MicoApplication}.
     * @param micoService     the {@link MicoService}.
     * @throws KubernetesResourceException if there is an error during the creation of Kubernetes resources
     */
    private void createKubernetesResources(MicoApplication micoApplication, MicoService micoService) throws KubernetesResourceException {

        // Get service deployment information
        Optional<MicoServiceDeploymentInfo> serviceDeploymentInfoOptional = serviceDeploymentInfoRepository
            .findByApplicationAndService(micoApplication.getShortName(), micoApplication.getVersion(),
                micoService.getShortName(), micoService.getVersion());
        MicoServiceDeploymentInfo serviceDeploymentInfo = serviceDeploymentInfoOptional.orElseThrow(() ->
            new RuntimeException("Service deployment information for service '" + micoService.getShortName()
                + "' in application '" + micoApplication.getShortName() + "' '" + micoApplication.getVersion()
                + "' could not be found."));

        log.info("Creating Kubernetes resources for MicoService '{}' in version '{}'", micoService.getShortName(), micoService.getVersion());
        log.debug("Using deployment information for MicoService '{}' in version '{}': {}",
            micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.toString());

        // TODO: Scale in/out existing Kubernetes resources instead of replacing existing resources (issue mico#416)
        Deployment createdDeployment = micoKubernetesClient.createMicoService(serviceDeploymentInfo);

        List<Service> createdServices = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            Service createdService = micoKubernetesClient.createMicoServiceInterface(serviceInterface, micoService);
            createdServices.add(createdService);
        }
        log.info("Successfully created Kubernetes resources for MicoService '{}' in version '{}'",
            micoService.getShortName(), micoService.getVersion());

        // Store the names of the created Kubernetes resources in the database
        serviceDeploymentInfo.setKubernetesDeploymentInfo(new KubernetesDeploymentInfo()
            .setNamespace(createdDeployment.getMetadata().getNamespace())
            .setDeploymentName(createdDeployment.getMetadata().getName())
            .setServiceNames(createdServices.stream().map(service -> service.getMetadata().getName()).collect(Collectors.toList()))
        );
        MicoServiceDeploymentInfo savedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(serviceDeploymentInfo);
        log.debug("Saved new Kubernetes deployment information of '{}' '{}' to database: {}",
            micoService.getShortName(), micoService.getVersion(), savedServiceDeploymentInfo.getKubernetesDeploymentInfo());
    }

    private Void exceptionHandler(Throwable e) {

        // TODO: Handle exceptions in async job properly, e.g., via message queue (RabbitMQ).
        // TODO: Also handle KubernetesResourceExceptions.

        log.error(e.getMessage(), e);
        return null;
    }

}
