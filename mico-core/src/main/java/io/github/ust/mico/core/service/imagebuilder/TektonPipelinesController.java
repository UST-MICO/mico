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

package io.github.ust.mico.core.service.imagebuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.fabric8.knative.v1.Condition;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.tekton.client.DefaultTektonClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunBuilder;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.exception.ImageBuildException;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Builds container images by using Tekton Pipelines and Kaniko.
 */
@Slf4j
@Service
public class TektonPipelinesController implements ImageBuilder {

    private static final String TEKTON_NS = "tekton-pipelines";

    // pipeline definitions (Tasks + Pipeline) reside in resources/tekton
    private static final String BUILD_PIPELINE_NAME = "build-and-push-pipeline";
    private static final String BUILD_PIPELINE_WORKSPACE_NAME = "git-source";

    private static final String PIPELINERUN_SUCCESS = "Succeeded";
    private static final String PIPELINERUN_RUNNING = "Running";
    private static final String PIPELINERUN_FAIL = "Failed";

    private final MicoKubernetesBuildBotConfig buildBotConfig;
    private final KubernetesNameNormalizer kubernetesNameNormalizer;
    private final KubernetesClient kubernetesClient;
    private final TektonClient tektonClient;

    private ScheduledExecutorService scheduledBuildStatusCheckService;

    @Getter
    private boolean isInitialized;

    /**
     * Create a {@code ImageBuilder} to be able to build Docker images in the cluster.
     *
     * @param kubernetesClient         the {@link KubernetesClient}
     * @param buildBotConfig           the build bot configuration for the image builder
     * @param kubernetesNameNormalizer the {@link KubernetesNameNormalizer}
     */
    @Autowired
    public TektonPipelinesController(KubernetesClient kubernetesClient, MicoKubernetesBuildBotConfig buildBotConfig, KubernetesNameNormalizer kubernetesNameNormalizer) {
        this.buildBotConfig = buildBotConfig;
        this.kubernetesNameNormalizer = kubernetesNameNormalizer;
        this.kubernetesClient = kubernetesClient;
        this.tektonClient = new DefaultTektonClient();
    }

    /**
     * Initialize the image builder every time the application context is refreshed.
     *
     * @param cre the {@link ContextRefreshedEvent}
     */
    @Override
    @EventListener
    public void init(ContextRefreshedEvent cre) {
        // Initialization must only be executed in an environment with a connection to Kubernetes.
        // Skip the initialization if we are in the 'local' or 'unit-testing' profile (e.g. Travis CI).
        Environment environment = cre.getApplicationContext().getEnvironment();
        if (environment.acceptsProfiles(Profiles.of("local", "unit-testing"))) {
            log.info("Profile(s) {} is/are active. Don't initialize image builder.", Arrays.toString(environment.getActiveProfiles()));
            return;
        }
        try {
            init();
        } catch (Exception e) {
            log.error("Failed to initialize image builder. Caused by: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize the image builder. This is required to be able to use the image builder. It's not required to trigger
     * the initialization manually, because the method is triggered by application context refresh events.
     *
     * @throws NotInitializedException if there are errors during initialization
     */
    @Override
    public void init() throws NotInitializedException {
        log.info("Initializing image builder...");

        String namespace = buildBotConfig.getNamespaceBuildExecution();
        String serviceAccountName = buildBotConfig.getDockerRegistryServiceAccountName();

        List<Pod> tektonPods = kubernetesClient.pods().inNamespace(TEKTON_NS).list().getItems();
        if (tektonPods.isEmpty()) {
            log.error("No Tekton Pipeline components are available");
            throw new NotInitializedException("ImageBuilder cannot be initialized without Tekton Pipelines installed in the cluster");
        }

        ServiceAccount buildServiceAccount = kubernetesClient.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get();
        if (buildServiceAccount == null) {
            log.error("Service account `{}` in namespace '{}' is not available!", serviceAccountName, namespace);
            throw new NotInitializedException("Service account not configured in the cluster: images cannot be pushed to DockerHub!");
        }

        try {
            initilizeBuildPipeline(namespace);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to initialize the build-and-push pipeline: {}", e.getMessage());
            throw new NotInitializedException("Failed to initialize the build-and-push pipeline: " + e.getMessage());
        }

        scheduledBuildStatusCheckService = Executors.newSingleThreadScheduledExecutor();
        isInitialized = true;
        log.info("Finished initializing image builder.");
    }

    /**
     * Initialize the Tekton build-and-push pipeline. This is required to be able to use the image builder.
     *
     * @param namespace the namespace for Tekton pipeline resources.
     * @throws IOException if there are errors when reading Tekton definition files
     */
    public void initilizeBuildPipeline(String namespace) throws IOException {
        log.info("Initializing the Tekton build-and-push pipeline");
        File cloneTaskFile = new ClassPathResource("tekton/git-clone.yml").getFile();
        File buildTaskFile = new ClassPathResource("tekton/kaniko.yml").getFile();
        File pipelineFile = new ClassPathResource("tekton/build-and-push.yml").getFile();

        Task cloneTask = tektonClient.v1beta1().tasks().load(cloneTaskFile).get();
        Task buildTask = tektonClient.v1beta1().tasks().load(buildTaskFile).get();
        Pipeline pipeline = tektonClient.v1beta1().pipelines().load(pipelineFile).get();

        tektonClient.v1beta1().tasks().inNamespace(namespace).createOrReplace(cloneTask);
        tektonClient.v1beta1().tasks().inNamespace(namespace).createOrReplace(buildTask);
        tektonClient.v1beta1().pipelines().inNamespace(namespace).createOrReplace(pipeline);

        log.info("Successfully initialized Tekton build-and-push pipeline");
    }

    /**
     * Builds an OCI image based on a Git repository provided by a {@code MicoService}. The result of the returned
     * {@code CompletableFuture} is the Docker image URI.
     *
     * @param micoService the MICO service for which the image should be build
     * @return the {@link CompletableFuture} that executes the build. The result is the Docker image URI.
     * @throws NotInitializedException if the image builder was not initialized
     */
    @Override
    public CompletableFuture<String> build(MicoService micoService) throws InterruptedException, ExecutionException, TimeoutException, KubernetesResourceException, NotInitializedException {
        if (!isInitialized) {
            throw new NotInitializedException("Cannot trigger the build pipeline: tthe image builder is not initialized");
        }

        if (StringUtils.isEmpty(micoService.getGitCloneUrl())) {
            throw new IllegalArgumentException("Git clone url is missing");
        }
        String namespace = buildBotConfig.getNamespaceBuildExecution();

        String pvcName = micoService.getShortName() + UUID.randomUUID();
        PersistentVolumeClaim pvc = createPersistentVolumeClaim(namespace, pvcName);
        PipelineRun pipelineRun = createPipelineRun(micoService, pvcName);
        log.info("Started build pipeline with name '{}'", pipelineRun.getMetadata().getName());
        log.debug("PipelineRun resource: {} ", pipelineRun);

        CompletableFuture<String> completionFuture = completePipelineRun(micoService, pipelineRun.getMetadata().getName(), namespace);

        // clean up: delete PipelineRun and PersistentVolumeClaim
        tektonClient.v1beta1().pipelineRuns().inNamespace(namespace).delete(pipelineRun);
        kubernetesClient.persistentVolumeClaims().inNamespace(namespace).delete(pvc);

        return completionFuture;
    }

    /**
     * Run a Tekton PipelineRun for a given {@code MicoService}. The result of the returned {@code CompletableFuture} is
     * the Docker image URI.
     *
     * @param micoService     the MICO service for which the image should be build
     * @param pipelineRunName the name of the {@link PipelineRun} to complete
     * @param namespace       the namespace in which to run the build pipeline
     * @return the {@link CompletableFuture} that executes the build. The result is the Docker image URI.
     */
    private CompletableFuture<String> completePipelineRun(MicoService micoService, String pipelineRunName, String namespace) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> completionFuture = new CompletableFuture<>();

        // Create a future that polls every 5 seconds with a delay of 10 seconds.
        final ScheduledFuture<?> pollingFuture = scheduledBuildStatusCheckService.scheduleAtFixedRate(() -> {
            PipelineRun run = tektonClient.v1beta1().pipelineRuns().inNamespace(namespace).withName(pipelineRunName).get();
            if (Objects.nonNull(run.getStatus())) {

                if (Objects.nonNull(run.getStatus().getConditions()) && !run.getStatus().getConditions().isEmpty()) {
                    Condition condition = run.getStatus().getConditions().get(0);
                    if (PIPELINERUN_SUCCESS.equals(condition.getReason())) {
                        String dockerImageUri = createImageUrl(micoService.getShortName()) + ":" + micoService.getVersion();
                        completionFuture.complete(dockerImageUri);
                    } else if (PIPELINERUN_FAIL.equals(condition.getReason())) {
                        log.warn(condition.getMessage());
                        completionFuture.completeExceptionally(new ImageBuildException("PipelineRun failed: " + condition.getMessage()));
                    } else {
                        log.info("PipelineRun " + pipelineRunName + ", status: " + condition.getReason() + ", message: " + condition.getMessage());
                    }
                }
            } else {
                log.warn("PipelineRun resource for MicoService '{}' '{}' was not created", micoService.getShortName(), micoService.getVersion());
                completionFuture.completeExceptionally(new ImageBuildException("PipelineRun resource was not created: build cannot be completed"));
            }
        }, 10, 5, TimeUnit.SECONDS);

        // Wait until build is considered to be finished (or the given timeout is reached).
        log.debug("Wait until PipelineRun for MicoService '{}' '{}' is finished.", micoService.getShortName(), micoService.getVersion());
        try {
            // This is synchronous and blocks the execution.
            completionFuture.get(buildBotConfig.getBuildTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pollingFuture.cancel(true);
            throw e;
        }

        // When completed cancel polling future
        completionFuture.whenComplete((result, thrown) -> pollingFuture.cancel(true));

        return completionFuture;
    }

    /**
     * Creates a {@link PipelineRun} for a given {@link MicoService}.
     *
     * @param micoService the {@link MicoService} used in a build-and-push pipeline.
     * @param pvcName     the name of the {@link PersistentVolumeClaim} used for running the pipeline.
     * @return the created PipelineRun object.
     */
    private PipelineRun createPipelineRun(MicoService micoService, String pvcName) throws KubernetesResourceException {
        String buildName = kubernetesNameNormalizer.createBuildName(micoService);
        String imageUrl = createImageUrl(micoService.getShortName());
        String imageTag = micoService.getVersion();
        String namespace = buildBotConfig.getNamespaceBuildExecution();
        String serviceAccountName = buildBotConfig.getDockerRegistryServiceAccountName();

        // current assumption: Dockerfile resides in the root folder of the git repository
        PipelineRun pipelineRun = new PipelineRunBuilder()
            .withNewMetadata().withNamespace(namespace).withNewName(buildName).endMetadata()
            .withNewSpec()
            .withNewPipelineRef().withName(BUILD_PIPELINE_NAME).endPipelineRef()
            .addNewParam().withName("gitUrl").withNewValue(micoService.getGitCloneUrl()).endParam()
            .addNewParam().withName("gitRevision").withNewValue(micoService.getVersion()).endParam()
            .addNewParam().withName("imageUrl").withNewValue(imageUrl).endParam()
            .addNewParam().withName("imageTag").withNewValue(imageTag).endParam()
            .withServiceAccountName(serviceAccountName)
            .addNewWorkspace().withName(BUILD_PIPELINE_WORKSPACE_NAME)
            .withNewPersistentVolumeClaim(pvcName, false)
            .endWorkspace()
            .endSpec()
            .build();

        try {
            return tektonClient.v1beta1().pipelineRuns().inNamespace(namespace).withName(buildName + "-PipelineRun").createOrReplace(pipelineRun);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to create build-and-push PipelineRun: {}", e.getMessage());
            throw new KubernetesResourceException("Failed to create the build-and-push PipelineRun for MicoService: " + micoService.getShortName());
        }
    }

    /**
     * Creates a persistent volume claim with the given name and in a given namespace.
     *
     * @param namespace the namespace for {@link PersistentVolumeClaim}.
     * @param name      the name of the {@link PersistentVolumeClaim}.
     * @return the created PersistentVolumeClaim object.
     */
    private PersistentVolumeClaim createPersistentVolumeClaim(String namespace, String name) throws KubernetesResourceException {
        ResourceRequirements reqs = new ResourceRequirements();
        reqs.setRequests(Collections.singletonMap("storage", new Quantity("2", "G")));
        PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
            .withNewMetadata().withName(name).endMetadata()
            .withNewSpec()
            .addNewAccessMode("ReadWriteOnce")
            .withResources(reqs)
            .endSpec()
            .build();
        try {
            return kubernetesClient.persistentVolumeClaims().inNamespace(namespace).createOrReplace(pvc);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to create a persistent volume claim: {}", e.getMessage());
            throw new KubernetesResourceException("Failed to create a persistent volume claim with name " + name);
        }
    }

    /**
     * Creates an image name based on the DockerHub registry name and service's short name.
     *
     * @param serviceShortName the short name of the {@link MicoService}.
     * @return the image name.
     */
    public String createImageUrl(String serviceShortName) {
        return buildBotConfig.getDockerImageRepositoryUrl().trim() + "/" + serviceShortName;
    }
}
