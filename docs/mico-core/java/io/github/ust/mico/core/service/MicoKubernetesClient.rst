.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentBuilder

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesConfig

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.model MicoServicePort

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: io.github.ust.mico.core.util UIDUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Map

MicoKubernetesClient
====================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Slf4j @Component public class MicoKubernetesClient

   Provides accessor methods for creating deployment and services in Kubernetes.

Fields
------
LABEL_APP_KEY
^^^^^^^^^^^^^

.. java:field:: public static final String LABEL_APP_KEY
   :outertype: MicoKubernetesClient

   Labels are used as selectors for Kubernetes deployments, services and pods. The `app` label references to the shortName of the \ :java:ref:`MicoService`\ . The `version` label references to the version of the \ :java:ref:`MicoService`\ . The `interface` label references to the name of the \ :java:ref:`MicoServiceInterface`\ . The `run` label references to the UID that is created for each \ :java:ref:`MicoService`\ .

LABEL_INTERFACE_KEY
^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String LABEL_INTERFACE_KEY
   :outertype: MicoKubernetesClient

LABEL_RUN_KEY
^^^^^^^^^^^^^

.. java:field:: public static final String LABEL_RUN_KEY
   :outertype: MicoKubernetesClient

LABEL_VERSION_KEY
^^^^^^^^^^^^^^^^^

.. java:field:: public static final String LABEL_VERSION_KEY
   :outertype: MicoKubernetesClient

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

.. java:method:: public Deployment createMicoService(MicoService micoService, MicoServiceDeploymentInfo deploymentInfo) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Create a Kubernetes deployment based on a MICO service.

   :param micoService: the \ :java:ref:`MicoService`\
   :param deploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the Kubernetes \ :java:ref:`Deployment`\  resource object

createMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service createMicoServiceInterface(MicoServiceInterface micoServiceInterface, MicoService micoService) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Create a Kubernetes service based on a MICO service interface.

   :param micoServiceInterface: the \ :java:ref:`MicoServiceInterface`\
   :param micoService: the \ :java:ref:`MicoService`\
   :return: the Kubernetes \ :java:ref:`Service`\  resource

