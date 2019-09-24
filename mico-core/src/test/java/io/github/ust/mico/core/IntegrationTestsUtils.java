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

package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.DeploymentException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class IntegrationTestsUtils {

    @Autowired
    private KubernetesClient kubernetesClient;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoKubernetesConfig kubernetesConfig;

    @Autowired
    private MicoKubernetesBuildBotConfig buildBotConfig;

    @Autowired
    private IntegrationTestsConfig integrationTestsConfig;

    @Getter
    private String dockerRegistrySecretName;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Set up the Kubernetes environment.
     *
     * @param uniqueNamespace if true, an random ID will be added to the namespace name
     * @return The Kubernetes namespace that is used for the integration test
     */
    String setUpEnvironment(Boolean uniqueNamespace) {
        String namespace = integrationTestsConfig.getKubernetesNamespaceName();
        if (uniqueNamespace) {
            // Integration test namespace, use a random ID as a suffix to prevent errors if concurrent integration tests are executed
            String shortId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
            namespace = namespace + "-" + shortId;
        }
        kubernetesClient.namespaces().createOrReplace(new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build());
        // Override config so all the Kubernetes objects will be created in the integration test namespace
        buildBotConfig.setNamespaceBuildExecution(namespace);
        kubernetesConfig.setNamespaceMicoWorkspace(namespace);
        return namespace;
    }

    void cleanUpEnvironment(String namespace) {
        kubernetesClient.namespaces().withName(namespace).delete();
    }

    /**
     * Set up the connection to the docker registry.
     * Docker registry is required for pushing the images that are build by {@link ImageBuilder}.
     *
     * @param namespace the Kubernetes namespace
     */
    void setUpDockerRegistryConnection(String namespace) {
        String serviceAccountName = buildBotConfig.getDockerRegistryServiceAccountName();
        String usernameBase64Encoded = integrationTestsConfig.getDockerHubUsernameBase64();
        String passwordBase64Encoded = integrationTestsConfig.getDockerHubPasswordBase64();

        if (StringUtils.isEmpty(usernameBase64Encoded)) {
            throw new IllegalArgumentException("Environment variable 'DOCKERHUB_USERNAME_BASE64' is missing");
        }
        if (StringUtils.isEmpty(passwordBase64Encoded)) {
            throw new IllegalArgumentException("Environment variable 'DOCKERHUB_PASSWORD_BASE64' is missing");
        }
        if (!Base64.isBase64(usernameBase64Encoded.getBytes())) {
            throw new IllegalArgumentException("Environment variable 'DOCKERHUB_USERNAME_BASE64' is not Base64 encoded");
        }
        if (!Base64.isBase64(passwordBase64Encoded.getBytes())) {
            throw new IllegalArgumentException("Environment variable 'DOCKERHUB_PASSWORD_BASE64' is not Base64 encoded");
        }

        // Set up connection to Docker Hub
        Secret dockerRegistrySecret = new SecretBuilder()
            .withApiVersion("v1")
            .withType("kubernetes.io/basic-auth")
            .withNewMetadata().withName("dockerhub-secret").withNamespace(namespace)
            .addToAnnotations("build.knative.dev/docker-0", "https://index.docker.io/v1/")
            .endMetadata()
            .addToData("username", usernameBase64Encoded)
            .addToData("password", passwordBase64Encoded)
            .build();
        kubernetesClient.secrets().inNamespace(namespace).createOrReplace(dockerRegistrySecret);

        ServiceAccount buildServiceAccount = new ServiceAccountBuilder()
            .withApiVersion("v1")
            .withNewMetadata().withName(serviceAccountName).withNamespace(namespace).endMetadata()
            .withSecrets(new ObjectReferenceBuilder().withName(dockerRegistrySecret.getMetadata().getName()).build())
            .build();
        kubernetesClient.serviceAccounts().inNamespace(namespace).createOrReplace(buildServiceAccount);

        dockerRegistrySecretName = dockerRegistrySecret.getMetadata().getName();
    }

    /**
     * Create a future that polls for all pods in the specified namespace until all are running.
     *
     * @param namespace    the Kubernetes namespace
     * @param initialDelay the initial delay in seconds
     * @param period       the period in seconds
     * @param timeout      the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Boolean> waitUntilAllPodsInNamespaceAreRunning(String namespace, int initialDelay, int period, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> completionFuture = new CompletableFuture<>();

        final ScheduledFuture<?> pollingFuture = executorService.scheduleAtFixedRate(() -> {
            PodList podList = kubernetesClient.pods().inNamespace(namespace).list();
            List<Pod> pods = podList.getItems();
            int numberOfPodsInNamespace = pods.size();
            log.debug("Number of pods in namespace '{}': {}", namespace, numberOfPodsInNamespace);
            if (pods.isEmpty()) {
                log.error("No pods found in namespace '" + namespace + "'");
                completionFuture.cancel(true);
            }

            AtomicInteger runningPods = new AtomicInteger();
            for (Pod pod : pods) {
                String podName = pod.getMetadata().getName();
                try {
                    Boolean running = checkIfPodIsRunning(podName, namespace);
                    if (running) {
                        int currentlyRunningPods = runningPods.incrementAndGet();

                        log.debug("Pod '{}' in namespace '{}' is now running", podName, namespace);
                        log.info("Currently {} out of {} pods in namespace '{}' are running",
                            currentlyRunningPods, numberOfPodsInNamespace, namespace);
                    }
                } catch (DeploymentException e) {
                    completionFuture.cancel(true);
                }
            }

            if (runningPods.get() == numberOfPodsInNamespace) {
                log.info("All pods are running!");
                completionFuture.complete(true);
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);

        log.info("Finished with build.");
        pollingFuture.cancel(true);

        return completionFuture;
    }

    /**
     * Create a future that polls the pod until it is running.
     *
     * @param podName      the name of the pod
     * @param namespace    the Kubernetes namespace
     * @param initialDelay the initial delay in seconds
     * @param period       the period in seconds
     * @param timeout      the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Boolean> waitUntilPodIsRunning(String podName, String namespace, int initialDelay, int period, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> completionFuture = new CompletableFuture<>();

        log.info("Wait until pod '{}' is running", podName);
        final ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            try {
                Boolean running = checkIfPodIsRunning(podName, namespace);
                if (running) {
                    completionFuture.complete(true);
                }
            } catch (DeploymentException e) {
                completionFuture.cancel(true);
            }

        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);
        scheduledFuture.cancel(true);

        return completionFuture;
    }

    /**
     * Create a future that polls the deployment until it is created.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @param initialDelay          the initial delay in seconds
     * @param period                the period in seconds
     * @param timeout               the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Deployment> waitUntilDeploymentIsCreated(MicoServiceDeploymentInfo serviceDeploymentInfo, int initialDelay, int period, int timeout)
        throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<Deployment> completionFuture = new CompletableFuture<>();

        MicoService micoService = serviceDeploymentInfo.getService();
        log.info("Wait until deployment of MicoService '{}' '{}' with instance id '{}' is created",
            micoService.getShortName(), micoService.getVersion(), serviceDeploymentInfo.getInstanceId());
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            Optional<Deployment> deployment = micoKubernetesClient.getDeploymentOfMicoServiceInstance(serviceDeploymentInfo);
            deployment.ifPresent(completionFuture::complete);
        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);
        scheduledFuture.cancel(true);

        return completionFuture;
    }

    /**
     * Create a future that polls the deployment until it is created.
     *
     * @param micoService  the {@link MicoService}
     * @param initialDelay the initial delay in seconds
     * @param period       the period in seconds
     * @param timeout      the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Service> waitUntilServiceIsCreated(MicoService micoService, int initialDelay, int period, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Service> completionFuture = new CompletableFuture<>();

        log.info("Wait until Kubernetes Service for MicoService '{}' '{}' is created", micoService.getShortName(), micoService.getVersion());
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            List<Service> services = micoKubernetesClient.getInterfacesOfMicoService(micoService);
            if (!services.isEmpty()) {
                completionFuture.complete(services.get(0));
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);
        scheduledFuture.cancel(true);

        return completionFuture;
    }

    /**
     * Checks if the specified pod is running
     *
     * @param podName   the pod name
     * @param namespace the Kubernetes namespace
     * @return boolean flag that indicates the success of the deployment
     * @throws DeploymentException if error occurs during deployment of pod
     */
    private Boolean checkIfPodIsRunning(String podName, String namespace) throws DeploymentException {
        Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(podName).get();
        String phase = pod.getStatus().getPhase();
        log.debug("Pod '{}' is currently in phase: {}", podName, phase);

        // Pod is in phase 'Succeeded' if it is a build pod, otherwise we wait until it is running.
        if (phase.equals("Running") || phase.equals("Succeeded")) {
            return true;
        } else {
            List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
            for (ContainerStatus status : containerStatuses) {
                String reason = status.getState().getWaiting().getReason();
                if (reason.equals("ErrImagePull") || reason.equals("ImagePullBackOff")) {
                    throw new DeploymentException("Deployment failed: " + reason);
                }
            }
        }
        return false;
    }

}
