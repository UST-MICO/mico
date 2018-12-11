package io.github.ust.mico.core;

import com.google.gson.JsonSyntaxException;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.authenticators.AzureActiveDirectoryAuthenticator;
import io.kubernetes.client.util.authenticators.GCPAuthenticator;

import java.io.IOException;
import java.util.List;

@Deprecated
public class ClusterAwareness {
    ApiClient client;
    CoreV1Api api;
    ExtensionsV1beta1Api extApi;


    /**
     * creates client to communicate with kubernetes cluster.
     * google uses the GCPAuthentictor to connect to google.
     * azure uses the AD auth to connect to azure.
     * cluster is used when running on cluster itself.
     *
     * @param type can be google, azure or cluster
     * @return object to communicate with cluster
     * @throws IOException if cant find config
     */
    public CoreV1Api createClient(String type) throws IOException {
        if (type == "google") {
            KubeConfig.registerAuthenticator(new GCPAuthenticator());
        } else if (type == "azure") {
            KubeConfig.registerAuthenticator(new AzureActiveDirectoryAuthenticator());
        } else if (type == "cluster") {
            client = Config.fromCluster();
        }
        client = Config.defaultClient();

        api = new CoreV1Api(client);
        extApi = new ExtensionsV1beta1Api(client);
        return api;
    }

    public ExtensionsV1beta1Deployment getDeployment(String name, String nameSpaceName, Boolean exact) throws ApiException {
        return extApi.readNamespacedDeployment(name, nameSpaceName, null, exact, null);
    }

    public ExtensionsV1beta1DeploymentList getAllDeployments(String nameSpaceName) throws ApiException {
        return extApi.listNamespacedDeployment(nameSpaceName, null, null, null, null, null, null, null, null, null);
    }

    public ExtensionsV1beta1DeploymentList getAllDeployments() throws ApiException {
        return extApi.listDeploymentForAllNamespaces(null, null, null, null, null, null, null, null, null);
    }

    public ExtensionsV1beta1Deployment createDeployment(ExtensionsV1beta1Deployment deployment, String nameSpace) throws ApiException {
        return extApi.createNamespacedDeployment(nameSpace, deployment, null);
    }

    public V1NamespaceList getAllNamespaces() throws ApiException {
        return api.listNamespace(null, null, null, null, null, null, null, null, null);
    }

    public V1PodList getAllPods() throws ApiException {
        return api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
    }

    public V1PodList getAllPods(String namespace) throws ApiException {
        return api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null);
    }

    public V1Pod getPod(String name, String namespaceName, Boolean exact) throws ApiException {
        return api.readNamespacedPod(name, namespaceName, null, exact, null);
    }

    public V1ServiceList getAllServices() throws ApiException {
        return api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null);
    }

    public V1Service getService(String name, String namespaceName, Boolean exact) throws ApiException {
        return api.readNamespacedService(name, namespaceName, null, exact, null);
    }

    public V1NodeList getAllNodes() throws ApiException {
        return api.listNode(null, null, null, null, null, null, null, null, null);
    }

    public V1Node getNode(String name, Boolean exact) throws ApiException {
        return api.readNode(name, null, exact, null);
    }

    public V1Namespace buildNamespace(String name) {
        return new V1NamespaceBuilder().withNewMetadata().withName(name).endMetadata().build();
    }

    public V1Namespace createNamespace(String name) throws ApiException {
        return createNamespace(buildNamespace(name));
    }

    public V1Namespace createNamespace(V1Namespace ns) throws ApiException {
        return api.createNamespace(ns, null);
    }

    public V1Service createService(String serviceName, V1Namespace namespace) throws ApiException {
        V1Service service = new V1Service().metadata(new V1ObjectMeta().name(serviceName)).spec(new V1ServiceSpec());
        return api.createNamespacedService(namespace.getMetadata().getName(), service, null);
    }

    public V1Status deleteService(String serviceName, V1Namespace namespace) throws ApiException {
        V1Status status = new V1Status();
        try {
            status = api.deleteNamespacedService(serviceName, namespace.getMetadata().getName(), new V1DeleteOptions(), null, null, null, null);
        } catch (JsonSyntaxException e) {
            handleDeleteException(e);
        }
        return status;
    }

    public V1Pod buildPod(String podName, List<V1Container> containers) {
        return new V1Pod()
                .metadata(new V1ObjectMeta().name(podName))
                .spec(
                        new V1PodSpec()
                                .containers(containers));
    }

    public V1Pod createPod(V1Pod pod, V1Namespace namespace) throws ApiException {
        return api.createNamespacedPod(namespace.getMetadata().getName(), pod, null);
    }

    public V1Status deleteNamespace(V1Namespace namespace) throws ApiException {
        V1Status status = new V1Status();
        try {
            status = api.deleteNamespace(namespace.getMetadata().getName(), new V1DeleteOptions(), null, null, false, null);
        } catch (JsonSyntaxException e) {
            handleDeleteException(e);
        }
        return status;
    }

    public V1Status deletePod(V1Namespace namespace, V1Pod pod) throws ApiException {
        V1Status status = new V1Status();
        try {
            status = api.deleteNamespacedPod(pod.getMetadata().getName(), namespace.getMetadata().getName(), new V1DeleteOptions(), null, null, null, null);
        } catch (JsonSyntaxException e) {
            handleDeleteException(e);
        }
        return status;
    }

    private void handleDeleteException(JsonSyntaxException e) {
        if (e.getCause() instanceof IllegalStateException) {
            IllegalStateException ise = (IllegalStateException) e.getCause();
            if (ise.getMessage() != null && ise.getMessage().contains("Expected a string but was BEGIN_OBJECT")) {
                System.out.println("Catching exception because of issue https://github.com/kubernetes-client/java/issues/86"
                );
                e.printStackTrace();
            } else throw e;
        } else throw e;
    }

    public Boolean existsNamespace(String namespace) throws ApiException {
        V1NamespaceList list = api.listNamespace(null, null, null, null, null, null, null, null, null);
        for (V1Namespace ns : list.getItems()) {
            System.out.println(ns);
            if (ns.getMetadata().getName().equals(namespace)) {
                return true;
            }
        }
        return false;
    }
}
