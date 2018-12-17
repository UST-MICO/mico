package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.github.ust.mico.core.build.ImageBuilder;
import io.github.ust.mico.core.build.ImageBuilderConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTests.class)
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

    private static boolean setUpFinished = false;
    private static boolean tearDownFinished = false;

    @Before
    public void setUp() {
        if (!setUpFinished) {
            //Namespaces
            cluster.createNamespace(config.getNamespaceName());

            // TODO Why works only the `default` namespace?!
            // Override namespace
            //imageBuilderConfig.setBuildExecutionNamespace(config.getNamespaceName());
            setUpFinished = true;
        }
    }

    @After
    public void tearDown() {
        if (!tearDownFinished) {
            //Namespaces
            cluster.deleteNamespace(config.getNamespaceName());
            tearDownFinished = true;
        }
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

    @Test(expected = Exception.class)
    public void withoutInitializingAnErrorIsThrown() throws Exception {
        imageBuilder.build("service-name", "1.0.0", "Dockerfile", "https://github.com/dgageot/hello.git", "master");
    }

    @Ignore
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
