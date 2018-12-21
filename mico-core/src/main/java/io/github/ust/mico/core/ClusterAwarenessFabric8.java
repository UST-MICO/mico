package io.github.ust.mico.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;

import java.io.InputStream;


public class ClusterAwarenessFabric8 {
    KubernetesClient client;

    /**
     * uses default kubernetes client
     */
    ClusterAwarenessFabric8() {
        if (client == null) {
            client = new DefaultKubernetesClient();
        }
    }

    /**
     * sets a specific client to use
     *
     * @param client
     */
    ClusterAwarenessFabric8(KubernetesClient client) {
        this.client = client;
    }

    /**
     * gets client to communicate with kubernetes cluster.
     *
     * @return object to communicate direct with cluster
     */
    public KubernetesClient getClient() {
        return client;
    }

    /**
     * creates object in kubernetes from a yaml describing it in specified namespace
     *
     * @param inputStream of yaml
     * @param namespace   has to exist or
     */
    public void createFromYaml(InputStream inputStream, String namespace) {
        client.load(inputStream).inNamespace(namespace).createOrReplace();
    }

    /**
     * returns yaml-String for the kubernetes cluster object
     *
     * @param kubernetesObject
     * @return
     * @throws JsonProcessingException
     */
    public String getYaml(HasMetadata kubernetesObject) throws JsonProcessingException {
        return SerializationUtils.dumpWithoutRuntimeStateAsYaml(kubernetesObject);
    }

    /**
     * creates object in kubernetes from a yaml describing it
     *
     * @param inputStream
     */
    public void createFromYaml(InputStream inputStream) {
        client.load(inputStream).createOrReplace();
    }

    /**
     * deletes resource in yaml from kubernetes cluster
     *
     * @param inputStream
     * @param namespace
     */
    public void deleteFromYaml(InputStream inputStream, String namespace) {
        client.load(inputStream).inNamespace(namespace).delete();
    }

    public Deployment getDeployment(String name, String namespace) {
        return client.apps().deployments().inNamespace(namespace).withName(name).get();
    }

    public DeploymentList getAllDeployments(String namespace) {
        return client.apps().deployments().inNamespace(namespace).list();
    }

    public DeploymentList getAllDeployments() {
        return client.apps().deployments().inAnyNamespace().list();
    }

    public Deployment createDeployment(Deployment deployment, String namespace) {
        return client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
    }

    public Boolean deleteDeployment(String deploymentName, String namespace) {
        return client.apps().deployments().inNamespace(namespace).withName(deploymentName).delete();
    }

    public NamespaceList getAllNamespaces() {
        return client.namespaces().list();
    }

    public PodList getAllPods() {
        return client.pods().inAnyNamespace().list();
    }

    public PodList getAllPods(String namespace) {
        return client.pods().inNamespace(namespace).list();
    }

    public Pod getPod(String name, String namespace) {
        return client.pods().inNamespace(namespace).withName(name).get();
    }

    public ServiceList getAllServices() {
        return client.services().inAnyNamespace().list();
    }

    public Service getService(String name, String namespace) {
        return client.services().inNamespace(namespace).withName(name).get();
    }

    public NodeList getAllNodes() {
        return client.nodes().list();
    }

    public Node getNode(String name) {
        return client.nodes().withName(name).get();
    }


    public Namespace createNamespace(String namespace) {
        return client.namespaces().createOrReplace(new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build());
    }

    public Service createService(Service service, String namespace) {
        return client.services().createOrReplace(service);
    }

    public Boolean deleteService(String serviceName, String namespace) {
        return client.services().inNamespace(namespace).withName(serviceName).delete();
    }

    public Pod createPod(Pod pod, String namespace) {
        return client.pods().inNamespace(namespace).createOrReplace(pod);
    }

    public Boolean deleteNamespace(String namespace) {
        return client.namespaces().withName(namespace).delete();
    }

    public Boolean deletePod(String podName, String namespace) {
        return client.pods().inNamespace(namespace).withName(podName).delete();
    }
}
