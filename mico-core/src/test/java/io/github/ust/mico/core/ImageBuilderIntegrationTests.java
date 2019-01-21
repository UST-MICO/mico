package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.ImageBuilderConfig;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// TODO Upgrade to Junit5
@Category(IntegrationTests.class)
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ImageBuilderIntegrationTests {

    /**
     * Git repository that is used for testing.
     * It must contain a Dockerfile and at least one release.
     */
    private static final String GIT_URI = "https://github.com/UST-MICO/hello.git";
    /**
     * Path to the Dockerfile.
     * It must be relative to the root of the Git repository.
     */
    private static final String DOCKERFILE = "Dockerfile";
    /**
     * Release tag of the release that should be used for testing.
     * Must be in in supported version format (semantic version).
     */
    private static final String RELEASE = "v1.0.0";

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    @Autowired
    private IntegrationTestsConfig integrationTestsConfig;

    private ScheduledExecutorService podStatusChecker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService buildPodStatusChecker = Executors.newSingleThreadScheduledExecutor();

    private Secret dockerRegistrySecret;
    private String namespace;

    /**
     * Set up everything that is required to execute the integration tests for the image builder.
     */
    @Before
    public void setUp() throws Exception {

        String serviceAccountName = imageBuilderConfig.getServiceAccountName();
        String usernameBase64Encoded = integrationTestsConfig.getDockerHubUsernameBase64();
        String passwordBase64Encoded = integrationTestsConfig.getDockerHubPasswordBase64();

        if (StringUtils.isEmpty(usernameBase64Encoded)) {
            throw new Exception("Environment variable 'DOCKERHUB_USERNAME_BASE64' is missing");
        }
        if (StringUtils.isEmpty(passwordBase64Encoded)) {
            throw new Exception("Environment variable 'DOCKERHUB_PASSWORD_BASE64' is missing");
        }

        // Integration test namespace, use a random ID as a suffix to prevent errors if concurrent integration tests are executed
        String shortId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        namespace = integrationTestsConfig.getKubernetesNamespaceName() + "-" + shortId;
        cluster.createNamespace(namespace);
        // Override config of the image builder so that it uses also the same namespace
        imageBuilderConfig.setBuildExecutionNamespace(namespace);

        // Set up connection to Docker Hub
        dockerRegistrySecret = new SecretBuilder()
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
    }

    /**
     * Delete namespace cleans up everything.
     */
    @After
    public void tearDown() {
        cluster.deleteNamespace(namespace);
    }

    /**
     * Test if the connected Kubernetes cluster has the required Build CRD defined.
     */
    @Test
    public void checkBuildCustomResourceDefinition() {
        Optional<CustomResourceDefinition> buildCRD = imageBuilder.getBuildCRD();
        log.info("Build CRD: {}" + buildCRD);
        assertNotNull("No Build CRD defined", buildCRD);
    }

    /**
     * Test the ImageBuilder if the build and push of an image works.
     * It uses the provided Git repository that contains a Dockerfile to build a Docker image.
     * Afterwards it pushes it to the provided Docker registry (e.g. DockerHub).
     *
     * @throws NotInitializedException if ImageBuilder was not initialized
     * @throws InterruptedException if the build process is interrupted unexpectedly
     * @throws TimeoutException if the build does not finish or fail in the expected time
     * @throws ExecutionException if the build process fails unexpectedly
     * @throws VersionNotSupportedException if the provided Git release tag is not supported as a MICO version
     */
    @Test
    public void buildAndPushImageWorks() throws NotInitializedException, InterruptedException, TimeoutException, ExecutionException, VersionNotSupportedException {

        imageBuilder.init();

        MicoService micoService = MicoService.builder()
            .shortName("hello-integration-test")
            .version(MicoVersion.valueOf(RELEASE).toString())
            .vcsRoot(GIT_URI)
            .dockerfilePath(DOCKERFILE)
            .build();

        Build build = imageBuilder.build(micoService);

        try {
            ObjectMapper mapper = new YAMLMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, build);
            log.debug("Build: {}{}", System.lineSeparator(), sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String buildName = build.getMetadata().getName();
        boolean success = checkIfBuildPodIsFinished(buildName);
        assertTrue("Build failed!", success);
    }

    // Test if docker image exists is currently not required
    @Ignore
    @Test
    public void dockerImageExists() throws ExecutionException, InterruptedException, TimeoutException, VersionNotSupportedException {
        String imageName = imageBuilder.createImageName("hello-integration-test", MicoVersion.valueOf("v1.0").toString());
        boolean result = checkIfDockerImageExists(imageName);
        assertTrue("Pod creation failed!", result);
    }

    private boolean checkIfBuildPodIsFinished(String buildName) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> buildPodResult = pollForBuildPodFinished(buildName);
        return buildPodResult.get();
    }

    private boolean checkIfDockerImageExists(String imagePath) throws ExecutionException, InterruptedException, TimeoutException {
        Pod pod = new PodBuilder()
            .withNewMetadata().withName("testpod").withNamespace(namespace).endMetadata()
            .withSpec(new PodSpecBuilder()
                .withContainers(new ContainerBuilder().withName("testpod-container").withImage(imagePath).build())
                .withImagePullSecrets(
                    new LocalObjectReferenceBuilder().withName(dockerRegistrySecret.getMetadata().getName()).build()
                ).build())
            .build();
        Pod createdPod = cluster.createPod(pod, namespace);

        CompletableFuture<Boolean> podCreationResult = pollForPodCreationCompletion(createdPod.getMetadata().getName());
        return podCreationResult.get();
    }

    // Create a future that polls every 500 milliseconds with a delay of 500 milliseconds.
    private CompletableFuture<Boolean> pollForPodCreationCompletion(String podName) throws InterruptedException, ExecutionException, TimeoutException {
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
        }, 500, 500, TimeUnit.MILLISECONDS);

        // Add a timeout: Abort after 20 seconds
        completionFuture.get(20, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }

    private CompletableFuture<Boolean> pollForBuildPodFinished(String buildName) throws InterruptedException, ExecutionException, TimeoutException {
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
        }, 10, 1, TimeUnit.SECONDS);

        // Add a timeout: Abort after 60 seconds
        completionFuture.get(60, TimeUnit.SECONDS);

        // When completed cancel future
        completionFuture.whenComplete((result, thrown) -> checkFuture.cancel(true));

        return completionFuture;
    }
}
