package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.imagebuilder.ImageBuilderConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageBuilderTests {

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(true, true);

    private ImageBuilder imageBuilder;

    @Before
    public void setUp() {
        ClusterAwarenessFabric8 cluster = new ClusterAwarenessFabric8(mockServer.getClient());

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
    public void withoutInitializingAnErrorIsThrown() throws NotInitializedException {
        imageBuilder.build("service-name", "1.0.0", "Dockerfile", "https://github.com/dgageot/hello.git", "master");
    }

    @Test
    public void listCustomResourceDefinitions() {

        List<CustomResourceDefinition> crdsItems = imageBuilder.getCustomResourceDefinitions();
        System.out.println("Found " + crdsItems.size() + " CRD(s)");
        CustomResourceDefinition dummyCRD = null;
        for (CustomResourceDefinition crd : crdsItems) {
            ObjectMeta metadata = crd.getMetadata();
            if (metadata != null) {
                String name = metadata.getName();
                System.out.println("    " + name + " => " + metadata.getSelfLink());
            }
        }
    }
}
