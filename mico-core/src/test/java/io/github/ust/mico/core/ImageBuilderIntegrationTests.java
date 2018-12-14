package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.github.ust.mico.core.build.ImageBuilder;
import io.github.ust.mico.core.build.ImageBuilderConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ImageBuilderIntegrationTests {

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    private ImageBuilder imageBuilder;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    @Autowired
    private IntegrationTestsConfig config;

    private CountDownLatch lock = new CountDownLatch(1);

    @Before
    public void setUp() {
        //Namespaces
        cluster.createNamespace(config.getNamespaceName());

        // TODO Why works only the `default` namespace?!
        // Override namespace
        //imageBuilderConfig.setBuildExecutionNamespace(config.getNamespaceName());
    }

    @After
    public void tearDown() {
        //Namespaces
        cluster.deleteNamespace(config.getNamespaceName());
    }

    @Test
    public void checkAvailableCustomResourceDefinitions() {
        List<CustomResourceDefinition> crdsItems = imageBuilder.getCustomResourceDefinitions();
        System.out.println("Found " + crdsItems.size() + " CRD(s)");

        assertTrue("There are no Custom Resource Definitions defined", crdsItems.size() > 0);
    }

    @Test
    public void checkBuildCustomResourceDefinition() {
        Optional<CustomResourceDefinition> buildCRD = imageBuilder.getBuildCRD();
        log.info("Build CRD: {}" + buildCRD);
        assertNotNull("No Build CRD defined", buildCRD);
    }

    @Test
    public void createBuildObjectWithYaml() throws IOException {

        File file = ResourceUtils.getFile("classpath:hello-build.yaml");
        InputStream yaml = new FileInputStream(file);
        System.out.println("Start Build");
        imageBuilder.createBuildWithYaml(yaml, config.getNamespaceName());
        System.out.println("Finished Build");
    }

    @Test(expected = Exception.class)
    public void withoutInitializingAnErrorIsThrown() throws Exception {
        imageBuilder.build("service-name", "1.0.0", "Dockerfile", "https://github.com/dgageot/hello.git", "master");
    }

    @Test
    public void createHelloBuildImage() throws Exception {

        imageBuilder.init();
        imageBuilder.build(config.getImageName(), "1.0", "Dockerfile", "https://github.com/dgageot/hello.git", "master");

        // Wait 20 seconds until build finished
        lock.await(60, TimeUnit.SECONDS);

        // TODO Add proper assertion

        // TODO Add clean up of `build-pod` the `build` itself (currently both in `default` namespace)
    }
}
