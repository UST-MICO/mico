.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentBuilder

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesConfig

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: io.github.ust.mico.core.util UIDUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: java.util Optional

MicoKubernetesClient
====================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Slf4j @Component public class MicoKubernetesClient

   Provides accessor methods for creating deployments and services in Kubernetes as well as getter methods to retrieve existing Kubernetes deployments and services.

Constructors
------------
MicoKubernetesClient
^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, MicoServiceRepository serviceRepository, KubernetesClient kubernetesClient)
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

getDeploymentOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<Deployment> getDeploymentOfMicoService(MicoService micoService) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Checks if the \ :java:ref:`MicoService`\  is already deployed to the Kubernetes cluster. Labels are used for the lookup.

   :param micoService: the \ :java:ref:`MicoService`\
   :return: an \ :java:ref:`Optional`\  with the \ :java:ref:`Deployment`\  of the Kubernetes service, or an empty \ :java:ref:`Optional`\  if there is no Kubernetes deployment of the \ :java:ref:`MicoService`\ .

getInterfaceByNameOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<Service> getInterfaceByNameOfMicoService(MicoService micoService, String micoServiceInterfaceName) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Check if the \ :java:ref:`MicoServiceInterface`\  is already created for the \ :java:ref:`MicoService`\  in the Kubernetes cluster. Labels are used for the lookup.

   :param micoService: the \ :java:ref:`MicoService`\
   :param micoServiceInterfaceName: the name of a \ :java:ref:`MicoServiceInterface`\
   :return: an \ :java:ref:`Optional`\  with the Kubernetes \ :java:ref:`Service`\ , or an emtpy \ :java:ref:`Optional`\  if there is no Kubernetes deployment of the \ :java:ref:`Service`\ .

getInterfacesOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Service> getInterfacesOfMicoService(MicoService micoService)
   :outertype: MicoKubernetesClient

   Looks up if there are any interfaces created for the \ :java:ref:`MicoService`\  in the Kubernetes cluster. If so, it returns them as a list of Kubernetes \ :java:ref:`Service`\  objects. Labels are used for the lookup.

   :param micoService: the \ :java:ref:`MicoService`\
   :return: the list of Kubernetes \ :java:ref:`Service`\  objects

getPodsCreatedByDeploymentOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Pod> getPodsCreatedByDeploymentOfMicoService(MicoService micoService)
   :outertype: MicoKubernetesClient

   Looks up if the \ :java:ref:`MicoService`\  is already deployed to the Kubernetes cluster. If so, it returns the list of Kubernetes \ :java:ref:`Pod`\  objects that belongs to the \ :java:ref:`Deployment`\ . Labels are used for the lookup.

   :param micoService: the \ :java:ref:`MicoService`\
   :return: the list of Kubernetes \ :java:ref:`Pod`\  objects

isApplicationDeployed
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isApplicationDeployed(MicoApplication micoApplication) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Checks if a MICO application is already deployed.

   :param micoApplication: the \ :java:ref:`MicoApplication`\
   :throws KubernetesResourceException: if there is an error while retrieving the Kubernetes objects
   :return: if true the application is deployed.

isMicoServiceDeployed
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isMicoServiceDeployed(MicoService micoService) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Checks if a MICO service is already deployed.

   :param micoService: the \ :java:ref:`MicoService`\
   :throws KubernetesResourceException: if there is an error while retrieving the Kubernetes objects
   :return: \ ``true``\  if the \ :java:ref:`MicoService`\  is deployed.

