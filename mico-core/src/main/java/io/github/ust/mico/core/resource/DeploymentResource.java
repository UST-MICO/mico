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
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.FutureUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CompletionException;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping(value = "/applications/{shortName}/{version}", produces = MediaTypes.HAL_JSON_VALUE)
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
    private MicoKubernetesClient micoKubernetesClient;

    @PostMapping("/deploy")
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
        List<CompletableFuture<MicoServiceDeploymentInfo>> buildJobs = new ArrayList<>();
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
                    log.info("Build job of MicoService '{}' '{}' is already running.",
                        micoService.getShortName(), micoService.getVersion());
                    continue;
                }
            }
            MicoServiceBackgroundJob job = new MicoServiceBackgroundJob()
                .setServiceShortName(micoService.getShortName())
                .setServiceVersion(micoService.getVersion())
                .setType(MicoServiceBackgroundJob.Type.BUILD)
                .setStatus(MicoServiceBackgroundJob.Status.RUNNING);
            backgroundJobBroker.saveJob(job);

            Optional<MicoServiceDeploymentInfo> serviceDeploymentInfoOptional = serviceDeploymentInfoRepository
                .findByApplicationAndService(micoApplication.getShortName(), micoApplication.getVersion(),
                    micoService.getShortName(), micoService.getVersion());
            MicoServiceDeploymentInfo serviceDeploymentInfo = serviceDeploymentInfoOptional.orElseThrow(() ->
                new IllegalStateException("Service deployment information for service '" + micoService.getShortName()
                    + "' in application '" + micoApplication.getShortName() + "' '" + micoApplication.getVersion()
                    + "' could not be found."));

            log.info("Start build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            CompletableFuture<MicoServiceDeploymentInfo> buildJob = CompletableFuture.supplyAsync(() -> buildMicoService(serviceDeploymentInfo))
                .handle((returnedServiceDeploymentInfo, ex) -> {
                    if (ex == null) {
                        // There was no exception during the build.
                        // Save the MicoService with the updated Docker image URI to the database.
                        serviceRepository.save(returnedServiceDeploymentInfo.getService());
                        return returnedServiceDeploymentInfo;
                    } else {
                        log.error(ex.getMessage(), ex);
                        backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                            MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, ex.getMessage());
                        return null;
                    }
                });
            log.debug("Started build of MicoService '{}' in version '{}'.", micoService.getShortName(), micoService.getVersion());
            buildJobs.add(buildJob);
            backgroundJobBroker.saveFutureOfJob(micoService.getShortName(), micoService.getVersion(), MicoServiceBackgroundJob.Type.BUILD, buildJob);
        }

        // When all build jobs are finished, create the Kubernetes resources for the deployment of a MicoService
        CompletableFuture<List<MicoServiceDeploymentInfo>> allBuildJobs = FutureUtils.all(buildJobs);
        allBuildJobs.whenComplete((serviceDeploymentInfosWithNullValues, throwable) -> {
            log.info("All build jobs for deployment of MicoApplication '{}' '{}' are finished. Start creating or updating Kubernetes resources.",
                micoApplication.getShortName(), micoApplication.getVersion());
            // All failed builds lead to a null in the service deployment list.
            List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfosWithNullValues.stream().filter(Objects::nonNull).collect(toList());
            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                MicoService micoService = serviceDeploymentInfo.getService();
                try {
                    KubernetesDeploymentInfo kubernetesDeploymentInfo = createKubernetesResources(serviceDeploymentInfo);
                    serviceDeploymentInfo.setKubernetesDeploymentInfo(kubernetesDeploymentInfo);
                    backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                        MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.DONE);
                } catch (Exception e) {
                    backgroundJobBroker.saveNewStatus(micoService.getShortName(), micoService.getVersion(),
                        MicoServiceBackgroundJob.Type.BUILD, MicoServiceBackgroundJob.Status.ERROR, e.getMessage());
                    log.error(e.getMessage(), e);
                }
            }

            // After the Kubernetes deployments are created, save the actual deployment information to the database.
            for (MicoServiceDeploymentInfo serviceDeploymentInfo : serviceDeploymentInfos) {
                log.debug("Saved new Kubernetes deployment information of '{}' '{}' to database: {}",
                    serviceDeploymentInfo.getService().getShortName(), serviceDeploymentInfo.getService().getVersion(),
                    serviceDeploymentInfo.getKubernetesDeploymentInfo());
                serviceDeploymentInfoRepository.save(serviceDeploymentInfo);
            }
            log.info("Finished creating or updating Kubernetes resources for deployment of MicoApplication '{}' '{}'. " +
                "Start creating or updating interface connections.", micoApplication.getShortName(), micoApplication.getVersion());

            // At last set up the connections between the deployed MicoServices
            micoKubernetesClient.createOrUpdateInterfaceConnections(micoApplication);
        });

        return ResponseEntity.accepted()
            .body(new Resource<>(new MicoApplicationJobStatusResponseDTO(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(shortName, version))));
    }

    @PostMapping("/undeploy")
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

        if (!micoKubernetesClient.isApplicationDeployed(application)) {
            // Currently we undeploy all MicoServices regardless whether the application is considered
            // to be deployed or not.
            // The reason is that there are possible some MicoServices deployed successfully and some not.
            // This undeployment should delete/scale the actually existing deployments.
            log.info("MicoApplication '{}' in version '{}' is considered to be not deployed. " +
                "Nevertheless check if there are any MicoServices that should be undeployed.",
                application.getShortName(), application.getVersion());
        }
        // TODO: Undeploy only if application is deployed or it is in a conflicted state. Covered by mico#535
        micoKubernetesClient.undeployApplication(application);

        return ResponseEntity.noContent().build();
    }

    private MicoServiceDeploymentInfo buildMicoService(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        MicoService micoService = serviceDeploymentInfo.getService();
        try {
            // Blocks this thread until build is finished, failed or TimeoutException is thrown
            CompletableFuture<String> buildFuture = imageBuilder.build(micoService);
            log.debug("Build of MicoService '{}' in version '{}' finished.", micoService.getShortName(), micoService.getVersion());

            if (buildFuture.get() != null) {
                String dockerImageUri = buildFuture.get();
                log.info("Build of MicoService '{}' in version '{}' finished with image '{}'.",
                    micoService.getShortName(), micoService.getVersion(), dockerImageUri);
                micoService.setDockerImageUri(dockerImageUri);
            } else {
                String errorMessage = "Build of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' didn't return a Docker image URI.";
                throw new CompletionException(new RuntimeException(errorMessage));
            }
        } catch (InterruptedException | ExecutionException | NotInitializedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return serviceDeploymentInfo;
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

    /**
     * Creates the Kubernetes resources based on the {@code MicoServiceDeploymentInfo}.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the {@link KubernetesDeploymentInfo}
     * @throws KubernetesResourceException if there is an error during the creation of Kubernetes resources
     */
    private KubernetesDeploymentInfo createKubernetesResources(MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException {
        MicoService micoService = serviceDeploymentInfo.getService();
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
        return new KubernetesDeploymentInfo()
            .setNamespace(createdDeployment.getMetadata().getNamespace())
            .setDeploymentName(createdDeployment.getMetadata().getName())
            .setServiceNames(createdServices.stream().map(service -> service.getMetadata().getName()).collect(toList()));
    }

}
