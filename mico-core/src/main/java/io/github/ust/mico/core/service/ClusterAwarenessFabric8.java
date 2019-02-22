/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class ClusterAwarenessFabric8 {

    private final KubernetesClient client;

    @Autowired
    public ClusterAwarenessFabric8(KubernetesClient client) {
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

    public DeploymentList getDeploymentsByLabels(Map<String, String> labels, String namespace) {
        return client.apps().deployments().inNamespace(namespace).withLabels(labels).list();
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

    public PodList getPodsByLabels(Map<String, String> labels, String namespace) {
        return client.pods().inNamespace(namespace).withLabels(labels).list();
    }

    public Pod getPod(String name, String namespace) {
        return client.pods().inNamespace(namespace).withName(name).get();
    }

    public ServiceList getAllServices() {
        return client.services().inAnyNamespace().list();
    }

    public ServiceList getServicesByLabels(Map<String, String> labels, String namespace) {
        return client.services().inNamespace(namespace).withLabels(labels).list();
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
        return client.services().inNamespace(namespace).createOrReplace(service);
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

    public Secret createSecret(Secret secret, String namespace) {
        return client.secrets().inNamespace(namespace).createOrReplace(secret);
    }

    public SecretList getAllSecrets(String namespace) {
        return client.secrets().inNamespace(namespace).list();
    }

    public Secret getSecret(String name, String namespace) {
        return client.secrets().inNamespace(namespace).withName(name).get();
    }

    public ServiceAccount createServiceAccount(ServiceAccount serviceAccount, String namespace) {
        return client.serviceAccounts().inNamespace(namespace).createOrReplace(serviceAccount);
    }

    public ServiceAccountList getAllServiceAccounts(String namespace) {
        return client.serviceAccounts().inNamespace(namespace).list();
    }

    public ServiceAccount getServiceAccount(String name, String namespace) {
        return client.serviceAccounts().inNamespace(namespace).withName(name).get();
    }
}
