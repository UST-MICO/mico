package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class IntegrationTestsUtils {

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    private MicoKubernetesConfig kubernetesConfig;

    @Autowired
    private MicoKubernetesBuildBotConfig buildBotConfig;

    @Autowired
    private IntegrationTestsConfig integrationTestsConfig;

    @Getter
    private String dockerRegistrySecretName;

    private ScheduledExecutorService podStatusChecker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService buildPodStatusChecker = Executors.newSingleThreadScheduledExecutor();

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
        cluster.createNamespace(namespace);
        // Override config so all the Kubernetes objects will be created in the integration test namespace
        buildBotConfig.setNamespaceBuildExecution(namespace);
        kubernetesConfig.setNamespaceMicoWorkspace(namespace);
        return namespace;
    }

    void cleanUpEnvironment(String namespace) {
        cluster.deleteNamespace(namespace);
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
        cluster.createSecret(dockerRegistrySecret, namespace);

        ServiceAccount buildServiceAccount = new ServiceAccountBuilder()
            .withApiVersion("v1")
            .withNewMetadata().withName(serviceAccountName).withNamespace(namespace).endMetadata()
            .withSecrets(new ObjectReferenceBuilder().withName(dockerRegistrySecret.getMetadata().getName()).build())
            .build();
        cluster.createServiceAccount(buildServiceAccount, namespace);

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

        final ScheduledFuture<?> checkFuture = podStatusChecker.scheduleAtFixedRate(() -> {

            PodList podList = cluster.getAllPods(namespace);
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
                completionFuture.complete(true);
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

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

        final ScheduledFuture<?> checkFuture = podStatusChecker.scheduleAtFixedRate(() -> {
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

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }

    /**
     * Create a future that polls the deployment until it is created.
     *
     * @param deploymentName the name of the deployment
     * @param namespace      the Kubernetes namespace
     * @param initialDelay   the initial delay in seconds
     * @param period         the period in seconds
     * @param timeout        the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Deployment> waitUntilDeploymentIsCreated(String deploymentName, String namespace, int initialDelay, int period, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Deployment> completionFuture = new CompletableFuture<>();

        final ScheduledFuture<?> checkFuture = podStatusChecker.scheduleAtFixedRate(() -> {
            Deployment deployment = cluster.getDeployment(deploymentName, namespace);
            if (deployment != null) {
                completionFuture.complete(deployment);
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }

    /**
     * Create a future that polls the deployment until it is created.
     *
     * @param serviceName  the name of the service
     * @param namespace    the Kubernetes namespace
     * @param initialDelay the initial delay in seconds
     * @param period       the period in seconds
     * @param timeout      the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Service> waitUntilServiceIsCreated(String serviceName, String namespace, int initialDelay, int period, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Service> completionFuture = new CompletableFuture<>();

        final ScheduledFuture<?> checkFuture = podStatusChecker.scheduleAtFixedRate(() -> {
            Service service = cluster.getService(serviceName, namespace);
            if (service != null) {
                completionFuture.complete(service);
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        // Waits until timeout is reached or the future completes.
        completionFuture.get(timeout, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }

    /**
     * Create a future that polls the build pod until the build is finished.
     *
     * @param imageBuilder the {@link ImageBuilder} object
     * @param buildName    the build name
     * @param namespace    the Kubernetes namespace
     * @param initialDelay the initial delay in seconds
     * @param period       the period in seconds
     * @param timeout      the timeout in seconds
     * @return CompletableFuture with a boolean. True indicates that it finished successful.
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException     if the build does not finish or fail in the expected time
     * @throws ExecutionException   if the build process fails unexpectedly
     */
    CompletableFuture<Boolean> waitUntilBuildIsFinished(ImageBuilder imageBuilder, String buildName, String namespace, int initialDelay, int period, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> completionFuture = new CompletableFuture<>();

        // Create a future that polls every second with a delay of 10 seconds.
        final ScheduledFuture<?> checkFuture = buildPodStatusChecker.scheduleAtFixedRate(() -> {

            Build build = imageBuilder.getBuild(buildName);
            if (build.getStatus() != null && build.getStatus().getCluster() != null) {
                String buildPodName = build.getStatus().getCluster().getPodName();
                Pod buildPod = cluster.getPod(buildPodName, namespace);

                log.debug("Current build phase: {}", buildPod.getStatus().getPhase());
                if (buildPod.getStatus().getPhase().equals("Succeeded")) {
                    completionFuture.complete(true);
                } else if (buildPod.getStatus().getPhase().equals("Failed")) {
                    completionFuture.complete(false);
                }
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        completionFuture.get(timeout, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

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
        Pod pod = cluster.getPod(podName, namespace);
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
