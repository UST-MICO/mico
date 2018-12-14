package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.build.ImageBuilder;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Ignore
public class ImageBuilderTests {
    @Rule
    public KubernetesServer mockServer = new KubernetesServer(true, true);
    String namespaceName = "unit-testing";
    ClusterAwarenessFabric8 cluster = new ClusterAwarenessFabric8();

    @Autowired
    ImageBuilder imageBuilder;

    @Before
    public void setUp() {
        // cluster = new ClusterAwarenessFabric8(mockServer.getClient());
        cluster = new ClusterAwarenessFabric8();

        //Namespaces
        mockServer.getClient().namespaces().createNew().withNewMetadata().withName("namespace1").endMetadata().done();
    }

    @After
    public void tearDown() throws Exception {

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

//    @Test
//    public void createCustomResourceDefinition() {
//
//        imageBuilder.createBuildCRD();
//        imageBuilder.getCustomResourceDefinitions();
//    }
}