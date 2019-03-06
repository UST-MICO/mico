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

import io.github.ust.mico.core.broker.BackgroundTaskBroker;
import io.github.ust.mico.core.dto.response.MicoApplicationJobStatusResponseDTO;
import io.github.ust.mico.core.exception.ImageBuildException;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundTaskRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoCoreBackgroundTaskFactory;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.service.imagebuilder.buildtypes.Build;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping(value = "/applications/{shortName}/{version}/deploy", produces = MediaTypes.HAL_JSON_VALUE)
public class DeploymentResource {
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    @Autowired
    private BackgroundTaskBroker backgroundTaskBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private MicoCoreBackgroundTaskFactory backgroundTaskFactory;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoBackgroundTaskRepository backgroundTaskRepo;

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
        List<MicoService> micoServices = serviceRepository.findAllByApplication(shortName, version);

        log.info("MicoApplication '{}' in version '{}' includes {} MicoService(s).",
            shortName, version, micoServices.size());

        for (MicoService micoService : micoServices) {

            // Check if a build for this MicoService is already running.
            // If yes no build is required, lock changes to running jobs.
            // If the current job status is done, error or cancel delete it and create a new job to get a new id.
            Optional<MicoServiceBackgroundTask> taskOptional = getTaskByMicoService(
                micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundTask.Type.BUILD);
            if (taskOptional.isPresent()) {
                if (taskOptional.get().getStatus() != MicoServiceBackgroundTask.Status.RUNNING) {
                    backgroundTaskRepo.delete(taskOptional.get());
                } else {
                    log.debug("Build task of MicoService '{}' '{}' is already running.",
                        micoService.getShortName(), micoService.getVersion());
                    continue;
                }
            }

            log.info("Start build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            MicoServiceBackgroundTask task = new MicoServiceBackgroundTask()
                .setServiceShortName(micoService.getShortName())
                .setServiceVersion(micoService.getVersion())
                .setType(MicoServiceBackgroundTask.Type.BUILD);
            backgroundTaskRepo.save(task);
            task.setJob(backgroundTaskFactory.runAsync(() -> buildImageAndWait(micoService), dockerImageUri -> {
                log.info("Build of MicoService '{}' in version '{}' finished with image '{}'.",
                    micoService.getShortName(), micoService.getVersion(), dockerImageUri);

                micoService.setDockerImageUri(dockerImageUri);
                MicoService savedMicoService = serviceRepository.save(micoService);
                try {
                    createKubernetesResources(micoApplication, savedMicoService);
                    saveMicoBackgroundTaskStatus(micoService.getShortName(), micoService.getVersion(),
                        MicoServiceBackgroundTask.Status.DONE, MicoServiceBackgroundTask.Type.BUILD, null);
                } catch (KubernetesResourceException kre) {
                    saveMicoBackgroundTaskStatus(micoService.getShortName(), micoService.getVersion(),
                        MicoServiceBackgroundTask.Status.ERROR, MicoServiceBackgroundTask.Type.BUILD, kre.getMessage());
                    log.error(kre.getMessage(), kre);
                    exceptionHandler(kre);
                }
            }, this::exceptionHandler));
            backgroundTaskRepo.save(task);

        }
        return ResponseEntity
            .accepted()
            .body(new Resource<>(new MicoApplicationJobStatusResponseDTO(backgroundTaskBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version))));
    }

    private Optional<MicoServiceBackgroundTask> getTaskByMicoService(String micoServiceShortName, String micoServiceVersion, MicoServiceBackgroundTask.Type type) {
        return backgroundTaskRepo.findByMicoServiceShortNameAndMicoServiceVersionAndType(micoServiceShortName, micoServiceVersion, type);
    }

    private void saveMicoBackgroundTaskStatus(String micoServiceShortName, String micoServiceVersion,
                                              MicoServiceBackgroundTask.Status status, MicoServiceBackgroundTask.Type type,
                                              @Nullable String errorMessage) {
        Optional<MicoServiceBackgroundTask> taskOptional = getTaskByMicoService(micoServiceShortName, micoServiceVersion, type);
        if (taskOptional.isPresent()) {
            MicoServiceBackgroundTask t = taskOptional.get();
            log.debug("Saving status of '{}'", taskOptional);
            t.setErrorMessage(errorMessage);
            t.setStatus(status);
            backgroundTaskRepo.save(t);
            log.info("Job status of '{}' is '{}'", getTaskByMicoService(micoServiceShortName, micoServiceVersion, type),
                getTaskByMicoService(micoServiceShortName, micoServiceVersion, type).get().getStatus());
        }
    }

    private String buildImageAndWait(MicoService micoService) {
        saveMicoBackgroundTaskStatus(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundTask.Status.RUNNING, MicoServiceBackgroundTask.Type.BUILD, null);
        try {
            Build build = imageBuilder.build(micoService);
            String buildName = build.getMetadata().getName();

            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<Boolean> booleanCompletableFuture = imageBuilder.waitUntilBuildIsFinished(buildName);
            if (booleanCompletableFuture.get()) {
                return imageBuilder.createImageName(micoService.getShortName(), micoService.getVersion());
            } else {
                booleanCompletableFuture.cancel(true);
                throw new ImageBuildException("Build for service " + micoService.getShortName() + " in version " + micoService.getVersion() + " failed");
            }
        } catch (NotInitializedException | InterruptedException | ExecutionException | ImageBuildException | TimeoutException e) {
            saveMicoBackgroundTaskStatus(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundTask.Status.ERROR, MicoServiceBackgroundTask.Type.BUILD, e.getMessage());
            log.error(e.getMessage(), e);
            // TODO Handle NotInitializedException in async task properly
            return null;
        }
    }

    /**
     * Creates the Kubernetes resources based on the deployment
     * information of the provided {@link MicoApplication}.
     *
     * @param micoApplication the {@link MicoApplication}.
     * @param micoService     the {@link MicoService}.
     * @throws KubernetesResourceException
     */
    private void createKubernetesResources(MicoApplication micoApplication, MicoService micoService) throws KubernetesResourceException {
        log.debug("Start creating Kubernetes resources for MicoService '{}' in version '{}'", micoService.getShortName(), micoService.getVersion());

        // Kubernetes Deployment
		Optional<MicoServiceDeploymentInfo> serviceDeploymentInfoOptional = serviceDeploymentInfoRepository
		    .findByApplicationAndService(micoApplication.getShortName(), micoApplication.getVersion(),
		        micoService.getShortName(), micoService.getVersion());
        new MicoServiceDeploymentInfo();
        MicoServiceDeploymentInfo serviceDeploymentInfo;
        if (serviceDeploymentInfoOptional.isPresent()) {
            serviceDeploymentInfo = serviceDeploymentInfoOptional.get();
            log.debug("Using deployment information for MICO Service '{}' in version '{}': {}",
                micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.toString());
        } else {
            serviceDeploymentInfo = new MicoServiceDeploymentInfo().setService(micoService);
            log.warn("MicoApplication '{}' in version '{}' doesn't have a service deployment information for service '{}' in version '{}' stored.",
                micoApplication.getShortName(), micoApplication.getShortName(), micoService.getShortName(), micoService.getVersion());
        }
        log.info("Creating Kubernetes deployment for MicoService '{}' in version '{}'",
            micoService.getShortName(), micoService.getVersion());
        log.debug("Details of MicoService: {}", micoService.toString());
        micoKubernetesClient.createMicoService(serviceDeploymentInfo);

        log.debug("Creating {} Kubernetes service(s) for MicoService '{}' in version '{}'",
            micoService.getServiceInterfaces().size(), micoService.getShortName(), micoService.getVersion());
        // Kubernetes Service(s)
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            micoKubernetesClient.createMicoServiceInterface(serviceInterface, micoService);
        }

        log.info("Created Kubernetes resources for MicoService '{}' in version '{}'",
            micoService.getShortName(), micoService.getVersion());
    }

    private Void exceptionHandler(Throwable e) {

        // TODO: Handle exceptions in async task properly, e.g., via message queue (RabbitMQ).
        // TODO: Also handle KubernetesResourceExceptions.

        log.error(e.getMessage(), e);
        return null;
    }

}
