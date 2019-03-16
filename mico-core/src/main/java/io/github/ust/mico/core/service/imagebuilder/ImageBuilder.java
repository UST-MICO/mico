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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.service.imagebuilder.buildtypes.*;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Builds container images by using Knative Build and Kaniko
 */
@Slf4j
@Service
public class ImageBuilder {

    private static final String BUILD_STEP_NAME = "build-and-push";
    private static final String BUILD_CRD_GROUP = "build.knative.dev";
    private static final String BUILD_CRD_NAME = "builds." + BUILD_CRD_GROUP;

    private final MicoKubernetesBuildBotConfig buildBotConfig;
    private final KubernetesClient kubernetesClient;

    private NonNamespaceOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>> buildClient;
    private ScheduledExecutorService scheduledBuildStatusCheckService;


    /**
     * @param kubernetesClient The Kubernetes client object
     * @param buildBotConfig   The build bot configuration for the image builder
     */
    @Autowired
    public ImageBuilder(KubernetesClient kubernetesClient, MicoKubernetesBuildBotConfig buildBotConfig) {
        this.kubernetesClient = kubernetesClient;
        this.buildBotConfig = buildBotConfig;
    }

    /**
     * Initialize the image builder.
     *
     * @throws NotInitializedException if the image builder was not initialized
     */
    public void init() throws NotInitializedException {
        String namespace = buildBotConfig.getNamespaceBuildExecution();
        String serviceAccountName = buildBotConfig.getDockerRegistryServiceAccountName();

        Optional<CustomResourceDefinition> buildCRD = getBuildCRD();
        if (!buildCRD.isPresent()) {
            log.error("Custom Resource Definition `{}` is not available!", BUILD_CRD_NAME);
            throw new NotInitializedException("Build CRD not available!");
        }
        ServiceAccount buildServiceAccount = kubernetesClient.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get();
        if (buildServiceAccount == null) {
            log.error("Service account `{}` is not available!", serviceAccountName);
            throw new NotInitializedException("Service account not available!");
        }

        this.buildClient = kubernetesClient.customResources(buildCRD.get(),
            Build.class, BuildList.class, DoneableBuild.class);

        String resourceScope = buildCRD.get().getSpec().getScope();
        log.debug("Build CRD has scope `{}`", resourceScope);

        // Resource scope is either 'Namespaced' or 'Cluster'
        boolean resourceNamespaced = false;
        if (resourceScope.equals("Namespaced")) {
            resourceNamespaced = true;
        }
        if (resourceNamespaced) {
            buildClient = ((MixedOperation<Build, BuildList, DoneableBuild, Resource<Build, DoneableBuild>>)
                buildClient).inNamespace(namespace);
        }

        // TODO Use thread pool
        scheduledBuildStatusCheckService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Returns the build CRD if exists
     *
     * @return the build CRD
     */
    public Optional<CustomResourceDefinition> getBuildCRD() {
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
     * Returns the build object
     *
     * @param buildName the name of the build
     * @return the build object
     */
    public Build getBuild(String buildName) {

        return this.buildClient.withName(buildName).get();
    }


    /**
     * @param micoService the MICO service for which the image should be build
     * @return the resulting build
     * @throws NotInitializedException if the image builder was not initialized
     */
    public Build build(MicoService micoService) throws NotInitializedException, IllegalArgumentException {

        if (StringUtils.isEmpty(micoService.getGitCloneUrl())) {
            throw new IllegalArgumentException("Git clone url is missing");
        }

        String namespace = buildBotConfig.getNamespaceBuildExecution();

        String buildName = createBuildName(micoService.getShortName());
        String destination = createImageName(micoService.getShortName(), micoService.getVersion());
        String dockerfilePath;
        if (!StringUtils.isEmpty(micoService.getDockerfilePath())) {
            dockerfilePath = "/workspace/" + micoService.getDockerfilePath();
        } else {
            // TODO check if a Dockerfile is placed in the root directory of the Git repository
            log.warn("Path to Dockerfile of MicoService '{}' is unknown. Try to use 'Dockerfile' in the root directory.", micoService.getShortName());
            dockerfilePath = "/workspace/Dockerfile";
        }
        String gitUrl = micoService.getGitCloneUrl();
        String gitRevision = micoService.getVersion();

        return createBuild(buildName, destination, dockerfilePath, gitUrl, gitRevision, namespace);
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

        if (buildClient == null) {
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
        log.info("Build created with name '{}'", buildName);
        log.debug("Created build: {} ", createdBuild);

        return createdBuild;
    }

    public CompletableFuture<Boolean> waitUntilBuildIsFinished(String buildName) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> completionFuture = new CompletableFuture<>();

        // Create a future that polls every 5 seconds with a delay of 10 seconds.
        final ScheduledFuture<?> checkFuture = scheduledBuildStatusCheckService.scheduleAtFixedRate(() -> {

            Build build = getBuild(buildName);
            if (build.getStatus() != null && build.getStatus().getCluster() != null) {
                String buildPodName = build.getStatus().getCluster().getPodName();
                String buildNamespace = build.getStatus().getCluster().getNamespace();
                Pod buildPod = this.kubernetesClient.pods().inNamespace(buildNamespace).withName(buildPodName).get();

                String currentBuildPhase = buildPod.getStatus().getPhase();
                log.debug("Current phase of build pod '{}' is '{}'.", buildPodName, currentBuildPhase);
                if (currentBuildPhase.equals("Succeeded")) {
                    completionFuture.complete(true);
                } else if (currentBuildPhase.equals("Failed")) {
                    completionFuture.complete(false);
                }
            } else {
                log.error("Build was not started!");
                completionFuture.complete(false);
            }
        }, 10, 5, TimeUnit.SECONDS);

        // Add a timeout. This is synchronous and blocks the execution.
        completionFuture.get(buildBotConfig.getBuildTimeout(), TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }

    /**
     * Returns a list of custom resource definitions
     *
     * @return the list of custom resource definitions
     */
    private List<CustomResourceDefinition> getCustomResourceDefinitions() {
        CustomResourceDefinitionList crds = kubernetesClient.customResourceDefinitions().list();

        return crds.getItems();
    }

    /**
     * Creates a image name based on the service name and the service version (used as image tag).
     *
     * @param serviceName    the name of the MICO service
     * @param serviceVersion the version of the MICO service
     * @return the image name
     */
    public String createImageName(String serviceName, String serviceVersion) {
        return buildBotConfig.getDockerImageRepositoryUrl() + "/" + serviceName + ":" + serviceVersion;
    }

    /**
     * Creates a build name based on the service name.
     *
     * @param serviceName the name of the MICO service
     * @return the build name
     */
    private String createBuildName(String serviceName) {
        return "build-" + serviceName;
    }

    /**
     * Delete the build
     *
     * @param buildName the name of the build
     */
    public void deleteBuild(String buildName) {
        buildClient.withName(buildName).delete();
    }

    /**
     * Delete the build
     *
     * @param build the build object
     */
    public void deleteBuild(Build build) {
        buildClient.delete(build);
    }
}
