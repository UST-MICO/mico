package io.github.ust.mico.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoVersion;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageBuilderTests {

    private static final String GIT_URI = "https://github.com/UST-MICO/hello.git";
    private static final String RELEASE = "v1.0.0";

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(false, true);

    private ImageBuilder imageBuilder;

    @Before
    public void setUp() {
        ClusterAwarenessFabric8 cluster = new ClusterAwarenessFabric8(mockServer.getClient());

        MicoKubernetesBuildBotConfig buildBotConfig = new MicoKubernetesBuildBotConfig();
        buildBotConfig.setNamespaceBuildExecution("build-execution-namespace");
        buildBotConfig.setKanikoExecutorImageUrl("kaniko-executor-image-url");
        buildBotConfig.setDockerRegistryServiceAccountName("service-account-name");
        buildBotConfig.setDockerImageRepositoryUrl("image-repository-url");

        imageBuilder = new ImageBuilder(cluster, buildBotConfig);
    }

    @After
    public void tearDown() {

    }

    @Test(expected = NotInitializedException.class)
    public void withoutInitializingAnErrorIsThrown() throws NotInitializedException, VersionNotSupportedException {

        MicoService micoService = new MicoService()
            .setShortName("service-short-name")
            .setVersion(MicoVersion.valueOf(RELEASE).toString())
            .setGitCloneUrl(GIT_URI)
            .setDockerfilePath("Dockerfile");

        imageBuilder.build(micoService);
    }
}
