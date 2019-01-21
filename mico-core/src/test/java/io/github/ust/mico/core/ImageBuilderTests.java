package io.github.ust.mico.core;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.ImageBuilderConfig;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageBuilderTests {

    private static final String GIT_URI = "https://github.com/UST-MICO/hello.git";
    private static final String RELEASE = "v1.0.0";

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(true, true);

    private ImageBuilder imageBuilder;

    @Before
    public void setUp() {
        ClusterAwarenessFabric8 cluster = new ClusterAwarenessFabric8(mockServer.getClient());

        // cluster.getClient().customResources(buildCRD.get(),


        ImageBuilderConfig config = new ImageBuilderConfig();
        config.setBuildExecutionNamespace("build-execution-namespace");
        config.setImageRepositoryUrl("image-repository-url");
        config.setKanikoExecutorImageUrl("kaniko-executor-image-url");
        config.setServiceAccountName("service-account-name");

        imageBuilder = new ImageBuilder(cluster, config);
    }

    @After
    public void tearDown() {

    }

    @Test(expected = NotInitializedException.class)
    public void withoutInitializingAnErrorIsThrown() throws NotInitializedException, VersionNotSupportedException {

        MicoService micoService = MicoService.builder()
            .shortName("service-short-name")
            .version(MicoVersion.valueOf(RELEASE))
            .vcsRoot(GIT_URI)
            .dockerfilePath("Dockerfile")
            .build();

        imageBuilder.build(micoService);
    }
}
