package io.github.ust.mico.core;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

//TODO make real test on a cluster, this gives a rough overview of the functionality
public class ClusterAwarenessTest {
    ClusterAwareness cluster;
    CoreV1Api api;
    //String namespaceName = "unit-testing";
    String namespaceName = "wursteml-hello";

    @Before
    public void setUp() throws Exception {
        cluster = new ClusterAwareness();
        api = cluster.createClient("google");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void readNamespaces() throws Exception {
        V1NamespaceList list = cluster.getAllNamespaces();

    }

    @Test
    public void checkNamespace() throws Exception {
        Assert.assertTrue(cluster.existsNamespace("kube-system"));
        Assert.assertFalse(cluster.existsNamespace("nonExistantNamespace"));
    }

    @Test
    public void deleteNamespace() throws ApiException {
        cluster.deleteNamespace(cluster.buildNamespace(namespaceName));
    }

    @Test
    public void deletePod() throws ApiException {

    }

    @Test
    public void createPods() throws ApiException {
        V1Namespace ns = cluster.createNamespace(namespaceName);
        V1Pod pod =
                new V1PodBuilder()
                        .withNewMetadata()
                        .withName("testpod")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("www")
                        .withImage("nginx")
                        .endContainer()
                        .endSpec()
                        .build();

        api.createNamespacedPod(namespaceName, pod, null);

        V1Pod pod2 = cluster.buildPod("testpodname", Arrays.asList(new V1Container().name("www").image("nginx")));

        cluster.createPod(pod2, ns);
    }

    @Test
    public void getAllPods() {
        try {
            V1PodList pods = cluster.getAllPods();
            for (V1Pod pod : pods.getItems()) {
                System.out.println(pod.getMetadata());
                System.out.println(pod.getSpec());
                System.out.println(pod.getStatus().getPodIP());
            }
            //Assert.assertEquals(2,cluster.getAllPods().getItems().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getPod() {
        try {
            V1Pod pod = cluster.getPod("hello-kubernetes-f88c56694-xs2sv", namespaceName, false);
            System.out.println(pod.getSpec());
            System.out.println(pod.getMetadata());
            System.out.println(pod.getStatus());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDeployment() {
        try {
            ExtensionsV1beta1Deployment dep = cluster.getDeployment("hello-kubernetes", namespaceName, false);
            System.out.println(dep);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllPodsNamespace() {
        try {
            V1PodList pods = cluster.getAllPods(namespaceName);
            for (V1Pod pod : pods.getItems()) {
                //System.out.println(pod.getMetadata());
                System.out.println(pod.getSpec());
            }
            //Assert.assertEquals(2,cluster.getAllPods().getItems().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllServices() {
        try {
            V1ServiceList services = cluster.getAllServices();
            for (V1Service service : services.getItems()) {
                System.out.println(service.getSpec());
                System.out.println(service.getMetadata());
                System.out.println(service.getStatus());
                System.out.println(" ");
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getService() {
        try {
            V1Service service = cluster.getService("hello-kubernetes", namespaceName, false);
            service.getSpec();
            service.getMetadata();
            service.getStatus();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllNodes() {
        try {
            V1NodeList nodes = cluster.getAllNodes();
            for (V1Node node : nodes.getItems()) {
                System.out.println(node.getMetadata());
                System.out.println(node.getStatus());
                System.out.println(node.getSpec());
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getNode() {
        try {
            V1Node node = cluster.getNode("gke-mico-cluster-pool-2-1dcd8373-mrml", false);
            System.out.println(node.getSpec());
            System.out.println(node.getStatus());
            System.out.println(node.getMetadata());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}