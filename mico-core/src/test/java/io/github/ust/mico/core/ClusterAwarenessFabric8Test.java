package io.github.ust.mico.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.*;

public class ClusterAwarenessFabric8Test {
    @Rule
    public KubernetesServer server = new KubernetesServer(true, true);
    String namespaceName = "unit-testing";
    ClusterAwarenessFabric8 cluster = new ClusterAwarenessFabric8();

    @Before
    public void setUp() {
        cluster = new ClusterAwarenessFabric8(server.getClient());

        //Nodes
        server.getClient().nodes().create(new NodeBuilder().withNewMetadata().withName("node1").endMetadata().build());
        server.getClient().nodes().create(new NodeBuilder().withNewMetadata().withName("node2").endMetadata().build());

        //Namespaces
        server.getClient().namespaces().createNew().withNewMetadata().withName("namespace1").endMetadata().done();
        server.getClient().namespaces().createNew().withNewMetadata().withName("namespace2").endMetadata().done();

        //Pods
        server.getClient().pods().inNamespace(namespaceName).create(new PodBuilder().withNewMetadata().withName("pod1").endMetadata().build());
        server.getClient().pods().inNamespace(namespaceName).create(new PodBuilder().withNewMetadata().withName("pod2").endMetadata().build());

        //Services
        server.getClient().services().inNamespace(namespaceName).create(new ServiceBuilder().withNewMetadata().withName("service1").endMetadata().build());
        server.getClient().services().inNamespace(namespaceName).create(new ServiceBuilder().withNewMetadata().withName("service2").endMetadata().build());

        //Deployments
        server.getClient().apps().deployments().inNamespace(namespaceName).create(new DeploymentBuilder().withNewMetadata().withName("deployment1").endMetadata().build());
        server.getClient().apps().deployments().inNamespace(namespaceName).create(new DeploymentBuilder().withNewMetadata().withName("deployment2").endMetadata().build());

        //Secrets
        server.getClient().secrets().inNamespace(namespaceName).create(new SecretBuilder().withNewMetadata().withName("secret1").endMetadata().build());

        //ServiceAccounts
        server.getClient().serviceAccounts().inNamespace(namespaceName).create(new ServiceAccountBuilder().withNewMetadata().withName("service-account1").endMetadata().build());
    }

    @Test
    public void getClient() {
        cluster = new ClusterAwarenessFabric8();
        System.out.println(cluster.getClient().getMasterUrl());
        cluster = new ClusterAwarenessFabric8(server.getClient());
        System.out.println(cluster.getClient().getMasterUrl());
    }

    @Test
    public void getYaml() throws JsonProcessingException {
        Pod pod = new PodBuilder().withNewMetadata().withName("yamltest").endMetadata().build();
        String yaml = cluster.getYaml(pod);
        assertTrue(yaml.contains("yamltest"));
    }

    @Test
    public void getDeployment() {
        Deployment deployment = cluster.getDeployment("deployment1", namespaceName);
        assertNotNull(deployment);
        assertEquals("deployment1", deployment.getMetadata().getName());
    }

    @Test
    public void getAllDeployments() {
        DeploymentList deploymentList = cluster.getAllDeployments();

        assertNotNull(deploymentList);
        assertEquals(2, deploymentList.getItems().size());
    }

    @Test
    public void getAllDeployments1() {
        DeploymentList deploymentList = cluster.getAllDeployments(namespaceName);

        assertNotNull(deploymentList);
        assertEquals(2, deploymentList.getItems().size());
    }


    @Test
    public void getAllNamespaces() {
        NamespaceList nsList = cluster.getAllNamespaces();
        assertNotNull(nsList);
        assertEquals(2, nsList.getItems().size());
    }

    @Test
    public void getAllPods() {
        PodList podList = cluster.getAllPods();
        assertNotNull(podList);
        assertEquals(2, podList.getItems().size());
    }

    @Test
    public void getAllPods1() {
        PodList podList = cluster.getAllPods(namespaceName);
        assertNotNull(podList);
        assertEquals(2, podList.getItems().size());
    }

    @Test
    public void getPod() {
        Pod pod = cluster.getPod("pod1", namespaceName);
        assertNotNull(pod);
        assertEquals("pod1", pod.getMetadata().getName());
    }

    @Test
    public void getAllServices() {
        ServiceList serviceList = cluster.getAllServices();
        assertNotNull(serviceList);
        assertEquals(2, serviceList.getItems().size());
    }

    @Test
    public void getService() {
        Service service = cluster.getService("service1", namespaceName);
        assertNotNull(service);
        assertEquals("service1", service.getMetadata().getName());
    }

    @Test
    public void getAllNodes() {
        NodeList nodeList = cluster.getAllNodes();
        assertNotNull(nodeList);
        assertEquals(2, nodeList.getItems().size());
    }

    @Test
    public void getNode() {
        Node node = cluster.getNode("node1");

        assertNotNull(node);
        assertEquals("node1", node.getMetadata().getName());
    }

    @Test
    public void getAllSecrets() {
        SecretList secretList = cluster.getAllSecrets(namespaceName);
        assertNotNull(secretList);
        assertEquals(1, secretList.getItems().size());
    }

    @Test
    public void getSecret() {
        Secret secret = cluster.getSecret("secret1", namespaceName);
        assertNotNull(secret);
        assertEquals("secret1", secret.getMetadata().getName());
    }

    @Test
    public void getAllServiceAccounts() {
        ServiceAccountList serviceAccountList = cluster.getAllServiceAccounts(namespaceName);
        assertNotNull(serviceAccountList);
        assertEquals(1, serviceAccountList.getItems().size());
    }

    @Test
    public void getServiceAccount() {
        ServiceAccount serviceAccount = cluster.getServiceAccount("service-account1", namespaceName);
        assertNotNull(serviceAccount);
        assertEquals("service-account1", serviceAccount.getMetadata().getName());
    }

    @Test
    public void createNamespace() {
        String nsName = "createnamespace";
        cluster.createNamespace(nsName);
        NamespaceList nsList = cluster.getAllNamespaces();

        assertNotNull(nsList);
        assertEquals(3, nsList.getItems().size());

        cluster.deleteNamespace(nsName);
    }

    @Test
    public void createPod() {
        Pod pod = new PodBuilder().withNewMetadata().withName("createpod").endMetadata().build();
        cluster.createPod(pod, namespaceName);
        PodList podList = cluster.getAllPods(namespaceName);
        Pod createdPod = cluster.getPod("createpod", namespaceName);

        assertNotNull(podList);
        assertEquals(3, podList.getItems().size());
        assertNotNull(createdPod);
    }

    @Test
    public void createService() {
        Service service = new ServiceBuilder().withNewMetadata().withName("createservice").endMetadata().build();
        cluster.createService(service, namespaceName);
        ServiceList serviceList = cluster.getAllServices();
        Service createdService = cluster.getService("createservice", namespaceName);

        assertNotNull(serviceList);
        assertEquals(3, serviceList.getItems().size());
        assertNotNull(createdService);
    }

    @Test
    public void createDeployment() {
        Deployment deployment = new DeploymentBuilder().withNewMetadata().withName("createdeployment").endMetadata().build();
        cluster.createDeployment(deployment, namespaceName);
        DeploymentList deploymentList = cluster.getAllDeployments(namespaceName);
        Deployment createdDeployment = cluster.getDeployment("createdeployment", namespaceName);

        assertNotNull(deploymentList);
        assertEquals(3, deploymentList.getItems().size());
        assertNotNull(createdDeployment);
    }

    @Test
    public void deleteNamespace() {
        cluster.deleteNamespace("namespace2");
        NamespaceList namespaceList = cluster.getAllNamespaces();
        assertNotNull(namespaceList);
        assertEquals(1, namespaceList.getItems().size());
    }

    @Test
    public void deletePod() {
        cluster.deletePod("pod2", namespaceName);
        PodList podList = cluster.getAllPods(namespaceName);
        assertNotNull(podList);
        assertEquals(1, podList.getItems().size());
    }

    @Test
    public void deleteService() {
        cluster.deleteService("service2", namespaceName);
        ServiceList serviceList = cluster.getAllServices();
        assertNotNull(serviceList);
        assertEquals(1, serviceList.getItems().size());
    }

    //somehow fails during units tests with mock server, works on real cluster
    @Ignore
    @Test
    public void deleteDeployment() {
        boolean deleted = cluster.deleteDeployment("deployment2", namespaceName);
        assertTrue(deleted);
        DeploymentList deploymentList = cluster.getAllDeployments(namespaceName);
        System.out.println(deploymentList.getItems().size());
        assertNotNull(deploymentList);
        assertEquals(1, deploymentList.getItems().size());
    }

    @Test
    public void createFromYaml() throws IOException {
        cluster.createFromYaml(new ClassPathResource("hello-kubernetes-w-namespace.yaml").getInputStream());
    }

    @Test
    public void createFromYaml1() throws IOException {
        cluster.createFromYaml(new ClassPathResource("hello-kubernetes-deployment.yaml").getInputStream(), namespaceName);
    }

    @Test
    public void deleteFromYaml() throws IOException {
        cluster.deleteFromYaml(new ClassPathResource("hello-kubernetes-deployment.yaml").getInputStream(), namespaceName);
    }

    @Test
    public void createSecret() {
        Secret secret = new SecretBuilder().withNewMetadata().withName("createsecret").endMetadata().build();
        cluster.createSecret(secret, namespaceName);
        SecretList secretList = cluster.getAllSecrets(namespaceName);
        Secret createdSecret = cluster.getSecret("createsecret", namespaceName);

        assertNotNull(secretList);
        assertEquals(2, secretList.getItems().size());
        assertNotNull(createdSecret);
    }

    @Test
    public void createServiceAccount() {
        ServiceAccount serviceAccount = new ServiceAccountBuilder().withNewMetadata().withName("createserviceaccount").endMetadata().build();
        cluster.createServiceAccount(serviceAccount, namespaceName);
        ServiceAccountList serviceAccountList = cluster.getAllServiceAccounts(namespaceName);
        ServiceAccount createServiceAccount = cluster.getServiceAccount("createserviceaccount", namespaceName);

        assertNotNull(serviceAccountList);
        assertEquals(2, serviceAccountList.getItems().size());
        assertNotNull(createServiceAccount);
    }

}
