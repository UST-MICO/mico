.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentList

.. java:import:: io.fabric8.kubernetes.client DefaultKubernetesClient

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.fabric8.kubernetes.client.internal SerializationUtils

.. java:import:: java.io InputStream

ClusterAwarenessFabric8
=======================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: public class ClusterAwarenessFabric8

Fields
------
client
^^^^^^

.. java:field::  KubernetesClient client
   :outertype: ClusterAwarenessFabric8

Constructors
------------
ClusterAwarenessFabric8
^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor::  ClusterAwarenessFabric8()
   :outertype: ClusterAwarenessFabric8

   uses default kubernetes client

ClusterAwarenessFabric8
^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor::  ClusterAwarenessFabric8(KubernetesClient client)
   :outertype: ClusterAwarenessFabric8

   sets a specific client to use

   :param client:

Methods
-------
createDeployment
^^^^^^^^^^^^^^^^

.. java:method:: public Deployment createDeployment(Deployment deployment, String namespace)
   :outertype: ClusterAwarenessFabric8

createFromYaml
^^^^^^^^^^^^^^

.. java:method:: public void createFromYaml(InputStream inputStream, String namespace)
   :outertype: ClusterAwarenessFabric8

   creates object in kubernetes from a yaml describing it in specified namespace

   :param inputStream: of yaml
   :param namespace: has to exist or

createFromYaml
^^^^^^^^^^^^^^

.. java:method:: public void createFromYaml(InputStream inputStream)
   :outertype: ClusterAwarenessFabric8

   creates object in kubernetes from a yaml describing it

   :param inputStream:

createNamespace
^^^^^^^^^^^^^^^

.. java:method:: public Namespace createNamespace(String namespace)
   :outertype: ClusterAwarenessFabric8

createPod
^^^^^^^^^

.. java:method:: public Pod createPod(Pod pod, String namespace)
   :outertype: ClusterAwarenessFabric8

createService
^^^^^^^^^^^^^

.. java:method:: public Service createService(Service service, String namespace)
   :outertype: ClusterAwarenessFabric8

deleteDeployment
^^^^^^^^^^^^^^^^

.. java:method:: public Boolean deleteDeployment(String deploymentName, String namespace)
   :outertype: ClusterAwarenessFabric8

deleteFromYaml
^^^^^^^^^^^^^^

.. java:method:: public void deleteFromYaml(InputStream inputStream, String namespace)
   :outertype: ClusterAwarenessFabric8

   deletes resource in yaml from kubernetes cluster

   :param inputStream:
   :param namespace:

deleteNamespace
^^^^^^^^^^^^^^^

.. java:method:: public Boolean deleteNamespace(String namespace)
   :outertype: ClusterAwarenessFabric8

deletePod
^^^^^^^^^

.. java:method:: public Boolean deletePod(String podName, String namespace)
   :outertype: ClusterAwarenessFabric8

deleteService
^^^^^^^^^^^^^

.. java:method:: public Boolean deleteService(String serviceName, String namespace)
   :outertype: ClusterAwarenessFabric8

getAllDeployments
^^^^^^^^^^^^^^^^^

.. java:method:: public DeploymentList getAllDeployments(String namespace)
   :outertype: ClusterAwarenessFabric8

getAllDeployments
^^^^^^^^^^^^^^^^^

.. java:method:: public DeploymentList getAllDeployments()
   :outertype: ClusterAwarenessFabric8

getAllNamespaces
^^^^^^^^^^^^^^^^

.. java:method:: public NamespaceList getAllNamespaces()
   :outertype: ClusterAwarenessFabric8

getAllNodes
^^^^^^^^^^^

.. java:method:: public NodeList getAllNodes()
   :outertype: ClusterAwarenessFabric8

getAllPods
^^^^^^^^^^

.. java:method:: public PodList getAllPods()
   :outertype: ClusterAwarenessFabric8

getAllPods
^^^^^^^^^^

.. java:method:: public PodList getAllPods(String namespace)
   :outertype: ClusterAwarenessFabric8

getAllServices
^^^^^^^^^^^^^^

.. java:method:: public ServiceList getAllServices()
   :outertype: ClusterAwarenessFabric8

getClient
^^^^^^^^^

.. java:method:: public KubernetesClient getClient()
   :outertype: ClusterAwarenessFabric8

   gets client to communicate with kubernetes cluster.

   :return: object to communicate direct with cluster

getDeployment
^^^^^^^^^^^^^

.. java:method:: public Deployment getDeployment(String name, String namespace)
   :outertype: ClusterAwarenessFabric8

getNode
^^^^^^^

.. java:method:: public Node getNode(String name)
   :outertype: ClusterAwarenessFabric8

getPod
^^^^^^

.. java:method:: public Pod getPod(String name, String namespace)
   :outertype: ClusterAwarenessFabric8

getService
^^^^^^^^^^

.. java:method:: public Service getService(String name, String namespace)
   :outertype: ClusterAwarenessFabric8

getYaml
^^^^^^^

.. java:method:: public String getYaml(HasMetadata kubernetesObject) throws JsonProcessingException
   :outertype: ClusterAwarenessFabric8

   returns yaml-String for the kubernetes cluster object

   :param kubernetesObject:
   :throws JsonProcessingException:

