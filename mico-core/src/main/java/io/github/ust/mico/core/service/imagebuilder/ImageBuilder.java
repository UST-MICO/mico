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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.exception.ImageBuildException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.service.imagebuilder.buildtypes.*;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds container images by using Knative Build and Kaniko.
 */
@Slf4j
@Service
public class ImageBuilder {

    private static final String BUILD_STEP_NAME = "build-and-push";
    public static final String BUILD_CRD_GROUP = "build.knative.dev";
    private static final String BUILD_CRD_NAME = "builds." + BUILD_CRD_GROUP;

    private final MicoKubernetesBuildBotConfig buildBotConfig;
    private final KubernetesClient kubernetesClient;
    private final KubernetesNameNormalizer kubernetesNameNormalizer;

    private NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient;
    private ScheduledExecutorService scheduledBuildStatusCheckService;

    @Getter
    private boolean isInitialized = false;


    /**
     * Create a {@code ImageBuilder} to be able to build Docker images in the cluster.
     *
     * @param kubernetesClient         the {@link KubernetesClient}
     * @param buildBotConfig           the build bot configuration for the image builder
     * @param kubernetesNameNormalizer the {@link KubernetesNameNormalizer}
     */
    @Autowired
    public ImageBuilder(KubernetesClient kubernetesClient, MicoKubernetesBuildBotConfig buildBotConfig, KubernetesNameNormalizer kubernetesNameNormalizer) {
        this.kubernetesClient = kubernetesClient;
        this.buildBotConfig = buildBotConfig;
        this.kubernetesNameNormalizer = kubernetesNameNormalizer;
    }

    /**
     * Initialize the image builder every time the application context is refreshed.
     *
     * @param cre the {@link ContextRefreshedEvent}
     * @throws NotInitializedException if there are errors during initialization
     */
    @EventListener
    public void init(ContextRefreshedEvent cre) throws NotInitializedException {
        log.info("Application context refreshed.");

        // Initialization must only be executed in an environment with a connection to Kubernetes.
        // Skip the initialization if we are in the 'local' profile (e.g. Travis CI).
        Environment environment = cre.getApplicationContext().getEnvironment();
        if (environment.acceptsProfiles(Profiles.of("local"))) {
            log.info("Local profile is active. Don't initialize image builder.");
            return;
        }
        try {
            init();
        } catch(Exception e) {
            log.error("Failed to initialize image builder. Caused by: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize the image builder.
     * This is required to be able to use the image builder.
     * It's not required to trigger the initialization manually,
     * because at every application context refresh the method is
     * called by the {@code @EventListener init} method.
     *
     * @throws NotInitializedException if there are errors during initialization
     */
    public void init() throws NotInitializedException {
        log.info("Initializing image builder...");
        isInitialized = false;

        String namespace = buildBotConfig.getNamespaceBuildExecution();
        String serviceAccountName = buildBotConfig.getDockerRegistryServiceAccountName();

        Optional<CustomResourceDefinition> buildCRD = getBuildCRD();
        if (!buildCRD.isPresent()) {
            log.error("Custom Resource Definition `{}` is not available!", BUILD_CRD_NAME);
            throw new NotInitializedException("Build CRD not available!");
        }
        ServiceAccount buildServiceAccount = kubernetesClient.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get();
        if (buildServiceAccount == null) {
            log.error("Service account `{}` in namespace '{}' is not available!", serviceAccountName, namespace);
            throw new NotInitializedException("Service account not available!");
        }

        this.buildClient = kubernetesClient.customResources(buildCRD.get(),
            Build.class, BuildList.class, DoneableBuild.class);

        String resourceScope = buildCRD.get().getSpec().getScope();

        // Resource scope is either 'Namespaced' or 'Cluster'
        boolean resourceNamespaced = false;
        if (resourceScope.equals("Namespaced")) {
            resourceNamespaced = true;
        }
        if (resourceNamespaced) {
            buildClient = ((MixedOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>>)
                buildClient).inNamespace(namespace);
        }

        scheduledBuildStatusCheckService = Executors.newSingleThreadScheduledExecutor();
        isInitialized = true;
        log.info("Finished initializing image builder.");
    }

    /**
     * Returns the build CRD if exists
     *
     * @return the build CRD
     * @throws KubernetesClientException if operation fails
     */
    public Optional<CustomResourceDefinition> getBuildCRD() throws KubernetesClientException {
        List<CustomResourceDefinition> crdsItems = getCustomResourceDefinitions();

        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                if (BUILD_CRD_NAME.equals(name)) {
                    log.debug("Found build CRD => {}", metadata.getSelfLink());
                    return Optional.of(crd);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Builds an OCI image based on a Git repository provided by a {@code MicoService}.
     * The result of the returned {@code CompletableFuture} is the Docker image URI.
     *
     * @param micoService the MICO service for which the image should be build
     * @return the {@link CompletableFuture} that executes the build. The result is the Docker image URI.
     * @throws NotInitializedException if the image builder was not initialized
     */
    public CompletableFuture<String> build(MicoService micoService) throws NotInitializedException, InterruptedException, ExecutionException, TimeoutException {
        if (StringUtils.isEmpty(micoService.getGitCloneUrl())) {
            throw new IllegalArgumentException("Git clone url is missing");
        }

        String namespace = buildBotConfig.getNamespaceBuildExecution();

        String buildName = kubernetesNameNormalizer.createBuildName(micoService.getShortName(), micoService.getVersion());
        String destination = createImageName(micoService.getShortName(), micoService.getVersion());
        String dockerfilePath;
        if (!StringUtils.isEmpty(micoService.getDockerfilePath())) {
            dockerfilePath = "/workspace/" + micoService.getDockerfilePath();
        } else {
            log.warn("Path to Dockerfile of MicoService '{}' is unknown. Try to use 'Dockerfile' in the root directory.", micoService.getShortName());
            dockerfilePath = "/workspace/Dockerfile";
        }
        String gitUrl = micoService.getGitCloneUrl();
        String gitRevision = micoService.getVersion();

        Build build = createBuild(buildName, destination, dockerfilePath, gitUrl, gitRevision, namespace);
        return waitUntilBuildIsFinished(build.getMetadata().getName(), micoService);
    }

    /**
     * @param buildName   the name of the build
     * @param destination the url of the image destination
     * @param dockerfile  the relative path to the Dockerfile
     * @param gitUrl      the URL to the remote git repository
     * @param gitRevision the revision of the git repository. e.g. `master`, commit id or a tag
     * @param namespace   the namespace in which the build is executed
     * @return the resulting build
     * @throws NotInitializedException if the image builder was not initialized
     */
    private Build createBuild(String buildName, String destination, String dockerfile, String gitUrl, String gitRevision, String namespace) throws NotInitializedException {
        if (!isInitialized) {
            throw new NotInitializedException("ImageBuilder is not initialized.");
        }

        Build build = new Build()
            .setSpec(new BuildSpec()
                .setServiceAccountName(buildBotConfig.getDockerRegistryServiceAccountName())
                .setSource(new SourceSpec()
                    .setGit(new GitSourceSpec()
                        .setUrl(gitUrl)
                        .setRevision(gitRevision)))
                .setSteps(CollectionUtils.listOf(new BuildStep()
                    .setName(BUILD_STEP_NAME)
                    .setImage(buildBotConfig.getKanikoExecutorImageUrl())
                    .setArgs(CollectionUtils.listOf(
                        "--dockerfile=" + dockerfile,
                        "--destination=" + destination)
                    )))
                .setTimeout(buildBotConfig.getBuildTimeout() + "s"));

        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(buildName);
        metadata.setNamespace(namespace);
        build.setMetadata(metadata);

        Build createdBuild = buildClient.createOrReplace(build);
        log.info("Started build with name '{}'", buildName);
        log.debug("Build resource: {} ", createdBuild);

        return createdBuild;
    }

    private CompletableFuture<String> waitUntilBuildIsFinished(String buildName, MicoService micoService) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> completionFuture = new CompletableFuture<>();

        // Create a future that polls every 5 seconds with a delay of 10 seconds.
        final ScheduledFuture<?> pollingFuture = scheduledBuildStatusCheckService.scheduleAtFixedRate(() -> {
            // Retrieve the build to get the current status of the build.
            Build build = getBuild(buildName);
            String message;
            if (build.getStatus() != null && build.getStatus().getCluster() != null) {
                String buildPodName = build.getStatus().getCluster().getPodName();
                String buildNamespace = build.getStatus().getCluster().getNamespace();
                Pod buildPod = kubernetesClient.pods().inNamespace(buildNamespace).withName(buildPodName).get();
                if (buildPod != null) {
                    String currentBuildPhase = buildPod.getStatus().getPhase();
                    // Typically there are 3 steps: build-step-credential-initializer, build-step-git-source-0, build-step-build-and-push
                    List<ContainerStatus> runningSteps = buildPod.getStatus().getInitContainerStatuses().stream()
                        .filter(p -> p.getState().getRunning() != null).collect(Collectors.toList());
                    if(runningSteps.isEmpty()) {
                        log.warn("No step of build of MicoService '{}' '{}' is currently running!",
                            micoService.getShortName(), micoService.getVersion());
                    }
                    log.debug("Current phase of build of MicoService '{}' '{}' is '{}'.{}",
                        micoService.getShortName(), micoService.getVersion(), currentBuildPhase,
                        !runningSteps.isEmpty() ? " (Step: " + runningSteps.get(0).getName() + ")" : "");

                    // During build the phase is 'Pending'.
                    // We wait until the phase is either 'Succeeded' or 'Failed'.
                    if (currentBuildPhase.equals("Succeeded")) {
                        String dockerImageUri = createImageName(micoService.getShortName(), micoService.getVersion());
                        completionFuture.complete(dockerImageUri);
                    } else if (currentBuildPhase.equals("Failed")) {
                        List<ContainerStatus> terminatedSteps = buildPod.getStatus().getInitContainerStatuses().stream()
                            .filter(p -> p.getState().getTerminated() != null).collect(Collectors.toList());
                        for (ContainerStatus terminatedStep : terminatedSteps) {
                            log.debug("Build step '{}' terminated with reason '{}'.", terminatedStep.getName(), terminatedStep.getState().getTerminated().getReason());
                        }
                        message = "Build of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' failed!";
                        log.warn(message);
                        completionFuture.completeExceptionally(new ImageBuildException(message));
                    }
                } else {
                    message = "Build Pod for build of MicoService '" + micoService.getShortName() + "' '"
                        + micoService.getVersion() + "' was not created!";
                    log.warn(message);
                    completionFuture.completeExceptionally(new ImageBuildException(message));
                }
            } else {
                message = "Build resource for the build of MicoService '" + micoService.getShortName() + "' '"
                    + micoService.getVersion() + "' was not created!";
                log.warn(message);
                completionFuture.completeExceptionally(new ImageBuildException(message));
            }
        }, 10, 5, TimeUnit.SECONDS);

        // Wait until build is considered to be finished (or the given timeout is reached).
        log.debug("Wait until Build of MicoService '{}' '{}' is finished.", micoService.getShortName(), micoService.getVersion());
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
     * Returns the build object
     *
     * @param buildName the name of the build
     * @return the build object
     */
    private Build getBuild(String buildName) {
        return this.buildClient.withName(buildName).get();
    }

    /**
     * Returns a list of custom resource definitions
     *
     * @return the list of custom resource definitions
     */
    private List<CustomResourceDefinition> getCustomResourceDefinitions() {
        return kubernetesClient.customResourceDefinitions().list().getItems();
    }

    /**
     * Creates an image name based on the short name and version of a service (used as image tag).
     *
     * @param serviceShortName the short name of the {@link MicoService}.
     * @param serviceVersion   the version of the {@link MicoService}.
     * @return the image name.
     */
    public String createImageName(String serviceShortName, String serviceVersion) {
        return buildBotConfig.getDockerImageRepositoryUrl() + "/" + serviceShortName + ":" + serviceVersion;
    }

    /**
     * Creates an image name based on a service (used as image tag).
     *
     * @param service the {@link MicoService}.
     * @return the image name.
     */
    public String createImageName(MicoService service) {
        return createImageName(service.getShortName(), service.getVersion());
    }

    /**
     * Creates a build name based on the service name and version
     * that is used for the build pod.
     *
     * @param serviceName    the name of the MICO service
     * @param serviceVersion the version of the MICO service
     * @return the name of the build pod
     */
    public String createBuildName(String serviceName, String serviceVersion) {
        return kubernetesNameNormalizer.normalizeName("build-" + serviceName + "-" + serviceVersion);
    }

    /**
     * Creates a build name based on the service name and version
     * that is used for the build pod.
     *
     * @param service the {@link MicoService}.
     * @return the image name.
     */
    public String createBuildName(MicoService service) {
        return createBuildName(service.getShortName(), service.getVersion());
    }

    /**
     * Deletes the build for a given build name.
     *
     * @param buildName the name of the build.
     */
    public void deleteBuild(String buildName) {
        buildClient.withName(buildName).delete();
    }

    /**
     * Deletes a given {@code Build}.
     *
     * @param build the {@link Build}.
     */
    public void deleteBuild(Build build) {
        buildClient.delete(build);
    }

    /**
     * Deletes the {@link Build} for a given service.
     *
     * @param service the {@link MicoService}.
     */
    public void deleteBuild(MicoService service) {
        deleteBuild(kubernetesNameNormalizer.createBuildName(service));
    }
}
