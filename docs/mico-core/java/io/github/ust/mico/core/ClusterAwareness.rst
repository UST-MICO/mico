.. java:import:: com.google.gson JsonSyntaxException

.. java:import:: io.kubernetes.client ApiClient

.. java:import:: io.kubernetes.client ApiException

.. java:import:: io.kubernetes.client.apis CoreV1Api

.. java:import:: io.kubernetes.client.apis ExtensionsV1beta1Api

.. java:import:: io.kubernetes.client.util Config

.. java:import:: io.kubernetes.client.util KubeConfig

.. java:import:: io.kubernetes.client.util.authenticators AzureActiveDirectoryAuthenticator

.. java:import:: io.kubernetes.client.util.authenticators GCPAuthenticator

.. java:import:: java.io IOException

.. java:import:: java.util List

ClusterAwareness
================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @Deprecated public class ClusterAwareness

Fields
------
api
^^^

.. java:field::  CoreV1Api api
   :outertype: ClusterAwareness

client
^^^^^^

.. java:field::  ApiClient client
   :outertype: ClusterAwareness

extApi
^^^^^^

.. java:field::  ExtensionsV1beta1Api extApi
   :outertype: ClusterAwareness

Methods
-------
buildNamespace
^^^^^^^^^^^^^^

.. java:method:: public V1Namespace buildNamespace(String name)
   :outertype: ClusterAwareness

buildPod
^^^^^^^^

.. java:method:: public V1Pod buildPod(String podName, List<V1Container> containers)
   :outertype: ClusterAwareness

createClient
^^^^^^^^^^^^

.. java:method:: public CoreV1Api createClient(String type) throws IOException
   :outertype: ClusterAwareness

   creates client to communicate with kubernetes cluster. google uses the GCPAuthentictor to connect to google. azure uses the AD auth to connect to azure. cluster is used when running on cluster itself.

   :param type: can be google, azure or cluster
   :throws IOException: if cant find config
   :return: object to communicate with cluster

createDeployment
^^^^^^^^^^^^^^^^

.. java:method:: public ExtensionsV1beta1Deployment createDeployment(ExtensionsV1beta1Deployment deployment, String nameSpace) throws ApiException
   :outertype: ClusterAwareness

createNamespace
^^^^^^^^^^^^^^^

.. java:method:: public V1Namespace createNamespace(String name) throws ApiException
   :outertype: ClusterAwareness

createNamespace
^^^^^^^^^^^^^^^

.. java:method:: public V1Namespace createNamespace(V1Namespace ns) throws ApiException
   :outertype: ClusterAwareness

createPod
^^^^^^^^^

.. java:method:: public V1Pod createPod(V1Pod pod, V1Namespace namespace) throws ApiException
   :outertype: ClusterAwareness

createService
^^^^^^^^^^^^^

.. java:method:: public V1Service createService(String serviceName, V1Namespace namespace) throws ApiException
   :outertype: ClusterAwareness

deleteNamespace
^^^^^^^^^^^^^^^

.. java:method:: public V1Status deleteNamespace(V1Namespace namespace) throws ApiException
   :outertype: ClusterAwareness

deletePod
^^^^^^^^^

.. java:method:: public V1Status deletePod(V1Namespace namespace, V1Pod pod) throws ApiException
   :outertype: ClusterAwareness

deleteService
^^^^^^^^^^^^^

.. java:method:: public V1Status deleteService(String serviceName, V1Namespace namespace) throws ApiException
   :outertype: ClusterAwareness

existsNamespace
^^^^^^^^^^^^^^^

.. java:method:: public Boolean existsNamespace(String namespace) throws ApiException
   :outertype: ClusterAwareness

getAllDeployments
^^^^^^^^^^^^^^^^^

.. java:method:: public ExtensionsV1beta1DeploymentList getAllDeployments(String nameSpaceName) throws ApiException
   :outertype: ClusterAwareness

getAllDeployments
^^^^^^^^^^^^^^^^^

.. java:method:: public ExtensionsV1beta1DeploymentList getAllDeployments() throws ApiException
   :outertype: ClusterAwareness

getAllNamespaces
^^^^^^^^^^^^^^^^

.. java:method:: public V1NamespaceList getAllNamespaces() throws ApiException
   :outertype: ClusterAwareness

getAllNodes
^^^^^^^^^^^

.. java:method:: public V1NodeList getAllNodes() throws ApiException
   :outertype: ClusterAwareness

getAllPods
^^^^^^^^^^

.. java:method:: public V1PodList getAllPods() throws ApiException
   :outertype: ClusterAwareness

getAllPods
^^^^^^^^^^

.. java:method:: public V1PodList getAllPods(String namespace) throws ApiException
   :outertype: ClusterAwareness

getAllServices
^^^^^^^^^^^^^^

.. java:method:: public V1ServiceList getAllServices() throws ApiException
   :outertype: ClusterAwareness

getDeployment
^^^^^^^^^^^^^

.. java:method:: public ExtensionsV1beta1Deployment getDeployment(String name, String nameSpaceName, Boolean exact) throws ApiException
   :outertype: ClusterAwareness

getNode
^^^^^^^

.. java:method:: public V1Node getNode(String name, Boolean exact) throws ApiException
   :outertype: ClusterAwareness

getPod
^^^^^^

.. java:method:: public V1Pod getPod(String name, String namespaceName, Boolean exact) throws ApiException
   :outertype: ClusterAwareness

getService
^^^^^^^^^^

.. java:method:: public V1Service getService(String name, String namespaceName, Boolean exact) throws ApiException
   :outertype: ClusterAwareness

