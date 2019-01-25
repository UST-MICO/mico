package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.*;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.concurrent.*;

@Slf4j
@Component
public class IntegrationTestsUtils {

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    private MicoKubernetesBuildBotConfig buildBotConfig;

    @Autowired
    private IntegrationTestsConfig integrationTestsConfig;

    @Getter
    private String dockerRegistrySecretName;

    private ScheduledExecutorService podStatusChecker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService buildPodStatusChecker = Executors.newSingleThreadScheduledExecutor();

    String setUpEnvironment() {
        // Integration test namespace, use a random ID as a suffix to prevent errors if concurrent integration tests are executed
        String shortId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        String namespace = integrationTestsConfig.getKubernetesNamespaceName() + "-" + shortId;
        cluster.createNamespace(namespace);
        // Override config of the image builder so that it uses also the same namespace
        buildBotConfig.setNamespaceBuildExecution(namespace);
        return namespace;
    }

    void cleanUpEnvironment(String namespace) {
        cluster.deleteNamespace(namespace);
    }

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

        // Set up connection to Docker Hub
        Secret dockerRegistrySecret = new SecretBuilder()
            .withApiVersion("v1")
            .withType("kubernetes.io/basic-auth")
            .withNewMetadata().withName("dockerhub-secret").withNamespace(namespace).withAnnotations(
                new HashMap<String, String>() {{
                    put("build.knative.dev/docker-0", "https://index.docker.io/v1/");
                }}).endMetadata()
            .withData(new HashMap<String, String>() {{
                put("username", usernameBase64Encoded);
                put("password", passwordBase64Encoded);
            }})
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
     * Create a future that polls  the pod until it is running.
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
            Pod pod = cluster.getPod(podName, namespace);
            log.info("Current Phase: {}", pod.getStatus().getPhase());
            if (pod.getStatus().getPhase().equals("Running")) {
                completionFuture.complete(true);
            } else {
                String reason = pod.getStatus().getContainerStatuses().get(0).getState().getWaiting().getReason();
                log.info("Reason: {}", reason);
                if (reason.equals("ErrImagePull") || reason.equals("ImagePullBackOff")) {
                    completionFuture.complete(false);
                }
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        // Add a timeout: Abort after 20 seconds
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
}
