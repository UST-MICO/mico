package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.buildtypes.Build;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore
// TODO Upgrade to JUnit5
@Category(IntegrationTests.class)
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore
public class ImageBuilderIntegrationTests {

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    private String namespace;

    /**
     * Set up everything that is required to execute the integration tests for the image builder.
     */
    @Before
    public void setUp() {

        namespace = integrationTestsUtils.setUpEnvironment(true);
        integrationTestsUtils.setUpDockerRegistryConnection(namespace);
    }

    /**
     * Delete namespace cleans up everything.
     */
    @After
    public void tearDown() {

        integrationTestsUtils.cleanUpEnvironment(namespace);
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
     * @throws NotInitializedException      if ImageBuilder was not initialized
     * @throws InterruptedException         if the build process is interrupted unexpectedly
     * @throws TimeoutException             if the build does not finish or fail in the expected time
     * @throws ExecutionException           if the build process fails unexpectedly
     * @throws VersionNotSupportedException if the provided Git release tag is not supported as a MICO version
     */
    @Test
    public void buildAndPushImageWorks() throws NotInitializedException, InterruptedException, TimeoutException, ExecutionException, VersionNotSupportedException {

        imageBuilder.init();

        MicoService micoService = MicoService.builder()
            .shortName("hello-integration-test")
            .version(MicoVersion.valueOf(RELEASE).toString())
            .gitCloneUrl(GIT_TEST_REPO_URL)
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

        CompletableFuture<Boolean> buildPodResult = integrationTestsUtils.waitUntilBuildIsFinished(imageBuilder, buildName, namespace, 10, 1, 60);
        boolean success = buildPodResult.get();
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

    private boolean checkIfDockerImageExists(String imagePath) throws ExecutionException, InterruptedException, TimeoutException {
        String dockerRegistrySecretName = integrationTestsUtils.getDockerRegistrySecretName();
        Pod pod = new PodBuilder()
            .withNewMetadata().withName("testpod").withNamespace(namespace).endMetadata()
            .withSpec(new PodSpecBuilder()
                .withContainers(new ContainerBuilder().withName("testpod-container").withImage(imagePath).build())
                .withImagePullSecrets(
                    new LocalObjectReferenceBuilder().withName(dockerRegistrySecretName).build()
                ).build())
            .build();
        Pod createdPod = cluster.createPod(pod, namespace);
        String podName = createdPod.getMetadata().getName();
        CompletableFuture<Boolean> podCreationResult = integrationTestsUtils.waitUntilPodIsRunning(
            podName, namespace, 1, 1, 20);
        return podCreationResult.get();
    }

}
