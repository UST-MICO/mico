.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: io.fabric8.kubernetes.api.model ContainerBuilder

.. java:import:: io.fabric8.kubernetes.api.model ContainerPort

.. java:import:: io.fabric8.kubernetes.api.model ContainerPortBuilder

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model ServiceBuilder

.. java:import:: io.fabric8.kubernetes.api.model ServicePort

.. java:import:: io.fabric8.kubernetes.api.model ServicePortBuilder

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentBuilder

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.github.ust.mico.core ClusterAwarenessFabric8

.. java:import:: io.github.ust.mico.core MicoKubernetesConfig

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

MicoKubernetesClient
====================

.. java:package:: io.github.ust.mico.core.mapping
   :noindex:

.. java:type:: @Component public class MicoKubernetesClient

   Provides accessor methods for creating deployment and services in Kubernetes.

Constructors
------------
MicoKubernetesClient
^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, ClusterAwarenessFabric8 cluster)
   :outertype: MicoKubernetesClient

Methods
-------
createMicoService
^^^^^^^^^^^^^^^^^

.. java:method:: public Deployment createMicoService(MicoService service, MicoServiceDeploymentInfo deploymentInfo)
   :outertype: MicoKubernetesClient

   Create a Kubernetes deployment based on a MICO service.

   :param service: the \ :java:ref:`MicoService`\
   :param deploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the Kubernetes \ :java:ref:`Deployment`\  resource object

createMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service createMicoServiceInterface(MicoServiceInterface serviceInterface, String micoServiceName, String micoServiceVersion)
   :outertype: MicoKubernetesClient

   Create a Kubernetes service based on a MICO service interface.

   :param serviceInterface: the \ :java:ref:`MicoServiceInterface`\
   :param micoServiceName: the name of the \ :java:ref:`MicoService`\
   :param micoServiceVersion: the version of the \ :java:ref:`MicoService`\
   :return: the Kubernetes \ :java:ref:`Service`\  resource

