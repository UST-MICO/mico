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
     * @param inputStream   of yaml
     * @param namespaceName has to exist or
     */
    public void createFromYaml(InputStream inputStream, String namespaceName) {
        client.load(inputStream).inNamespace(namespaceName).createOrReplace();
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
     * @param namespaceName
     */
    public void deleteFromYaml(InputStream inputStream, String namespaceName) {
        client.load(inputStream).inNamespace(namespaceName).delete();
    }

    public Deployment getDeployment(String name, String nameSpaceName) {
        return client.apps().deployments().inNamespace(nameSpaceName).withName(name).get();
    }

    public DeploymentList getAllDeployments(String nameSpaceName) {
        return client.apps().deployments().inNamespace(nameSpaceName).list();
    }

    public DeploymentList getAllDeployments() {
        return client.apps().deployments().inAnyNamespace().list();
    }

    public Deployment createDeployment(Deployment deployment, String nameSpace) {
        return client.apps().deployments().inNamespace(nameSpace).createOrReplace(deployment);
    }

    public Boolean deleteDeployment(String deploymentName, String namespaceName) {
        return client.apps().deployments().inNamespace(namespaceName).withName(deploymentName).delete();
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

    public Pod getPod(String name, String namespaceName) {
        return client.pods().inNamespace(namespaceName).withName(name).get();
    }

    public ServiceList getAllServices() {
        return client.services().inAnyNamespace().list();
    }

    public Service getService(String name, String namespaceName) {
        return client.services().inNamespace(namespaceName).withName(name).get();
    }

    public NodeList getAllNodes() {
        return client.nodes().list();
    }

    public Node getNode(String name) {
        return client.nodes().withName(name).get();
    }


    public Namespace createNamespace(String ns) {
        return client.namespaces().createOrReplace(new NamespaceBuilder().withNewMetadata().withName(ns).endMetadata().build());
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

    public Boolean deletePod(String namespace, String podName) {
        return client.pods().inNamespace(namespace).withName(podName).delete();
    }
}
