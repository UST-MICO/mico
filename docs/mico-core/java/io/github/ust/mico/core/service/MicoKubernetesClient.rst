.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentBuilder

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.github.ust.mico.core.broker BackgroundJobBroker

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesBuildBotConfig

.. java:import:: io.fabric8.kubernetes.client.internal SerializationUtils

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesConfig

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus.Value

.. java:import:: io.github.ust.mico.core.persistence KubernetesDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.service.imagebuilder ImageBuilder

.. java:import:: io.github.ust.mico.core.service.imagebuilder.buildtypes Build

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: io.github.ust.mico.core.util UIDUtils

.. java:import:: lombok.extern.slf4j Slf4j

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

.. java:constructor:: @Autowired public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, MicoKubernetesBuildBotConfig buildBotConfig, KubernetesClient kubernetesClient, ImageBuilder imageBuilder, BackgroundJobBroker backgroundJobBroker, MicoApplicationRepository applicationRepository, MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository, KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository)
   :outertype: MicoKubernetesClient

Methods
-------
createMicoService
^^^^^^^^^^^^^^^^^

.. java:method:: public Deployment createMicoService(MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Create a Kubernetes deployment based on a \ :java:ref:`MicoServiceDeploymentInfo`\ .

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the Kubernetes \ :java:ref:`Deployment`\  resource object

createMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service createMicoServiceInterface(MicoServiceInterface micoServiceInterface, MicoService micoService) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Create a Kubernetes service based on a MICO service interface.

   :param micoServiceInterface: the \ :java:ref:`MicoServiceInterface`\
   :param micoService: the \ :java:ref:`MicoService`\
   :return: the Kubernetes \ :java:ref:`Service`\  resource

createOrUpdateInterfaceConnections
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void createOrUpdateInterfaceConnections(MicoApplication micoApplication)
   :outertype: MicoKubernetesClient

   Creates or updates all interface connections of the given \ ``MicoApplication``\ .

   :param micoApplication: the \ :java:ref:`MicoApplication`\

getApplicationDeploymentStatus
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationDeploymentStatus getApplicationDeploymentStatus(MicoApplication micoApplication)
   :outertype: MicoKubernetesClient

   Indicates whether a \ ``MicoApplication``\  is currently deployed.

   In order to determine the application deployment status of the given \ ``MicoApplication``\  the following points are checked:

   ..

   * the current \ :java:ref:`MicoApplicationJobStatus`\  (deployment may be scheduled, running or finished with an error
   * the stored \ :java:ref:`MicoServiceDeploymentInfo`\  and \ :java:ref:`KubernetesDeploymentInfo`\
   * the actual information retrieved from Kubernetes regarding deployments for \ :java:ref:`MicoServices <MicoService>`\  and Kubernetes Services for \ :java:ref:`MicoServiceInterfaces <MicoServiceInterface>`\

   Note that the returned \ ``MicoApplicationDeploymentStatus``\  contains info messages with further information in case the \ ``MicoApplication``\  currently is not deployed.

   :param micoApplication: the \ :java:ref:`MicoApplication`\ .
   :return: the \ :java:ref:`MicoApplicationDeploymentStatus`\ .

getApplicationDeploymentStatus
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationDeploymentStatus getApplicationDeploymentStatus(String applicationShortName, String applicationVersion)
   :outertype: MicoKubernetesClient

   Indicates whether a \ ``MicoApplication``\  is currently deployed.

   In order to determine the application deployment status of the given \ ``MicoApplication``\  the following points are checked:

   ..

   * the current \ :java:ref:`MicoApplicationJobStatus`\  (deployment may be scheduled, running or finished with an error
   * the stored \ :java:ref:`MicoServiceDeploymentInfo`\  and \ :java:ref:`KubernetesDeploymentInfo`\
   * the actual information retrieved from Kubernetes regarding deployments for \ :java:ref:`MicoServices <MicoService>`\  and Kubernetes Services for \ :java:ref:`MicoServiceInterfaces <MicoServiceInterface>`\

   Note that the returned \ ``MicoApplicationDeploymentStatus``\  contains info messages with further information in case the \ ``MicoApplication``\  currently is not deployed.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :return: the \ :java:ref:`MicoApplicationDeploymentStatus`\ .

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
   :return: an \ :java:ref:`Optional`\  with the Kubernetes \ :java:ref:`Service`\ , or an empty \ :java:ref:`Optional`\  if there is no Kubernetes \ :java:ref:`Service`\  for this \ :java:ref:`MicoServiceInterface`\ .

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

getYaml
^^^^^^^

.. java:method:: public String getYaml(MicoService micoService) throws KubernetesResourceException, JsonProcessingException
   :outertype: MicoKubernetesClient

   Retrieves the yaml for a MicoService, contains the interfaces if they exist.

   :param micoService: the \ :java:ref:`MicoService`\
   :throws KubernetesResourceException: if there is an error while retrieving the Kubernetes objects
   :throws JsonProcessingException: if there is a error processing the content.
   :return: the kubernetes YAML for the \ :java:ref:`MicoService`\ .

isApplicationDeployed
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isApplicationDeployed(MicoApplication micoApplication)
   :outertype: MicoKubernetesClient

   Checks whether a given \ ``MicoApplication``\  is currently deployed.

   :param micoApplication: the \ :java:ref:`MicoApplication`\ .
   :return: \ ``true``\  if and only if \ :java:ref:`getApplicationDeploymentStatus(MicoApplication)`\  returns a \ :java:ref:`MicoApplicationDeploymentStatus`\  with \ :java:ref:`Value.DEPLOYED`\ ; \ ``false``\  otherwise.

isMicoServiceDeployed
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isMicoServiceDeployed(MicoService micoService) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Checks if a MICO service is already deployed.

   :param micoService: the \ :java:ref:`MicoService`\
   :throws KubernetesResourceException: if there is an error while retrieving the Kubernetes objects
   :return: \ ``true``\  if the \ :java:ref:`MicoService`\  is deployed.

scaleIn
^^^^^^^

.. java:method:: public Optional<Deployment> scaleIn(MicoServiceDeploymentInfo serviceDeploymentInfo, int numberOfReplicas) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Performs a scale in of a Kubernetes deployment based on some service deployment information by a given number of replicas to remove.

   Note that the Kubernetes deployment will be undeployed if and only if the given number of replicas is less than or equal to 0.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .
   :param numberOfReplicas: the number of replicas to remove.
   :throws KubernetesResourceException: if the Kubernetes deployment can't be found

scaleOut
^^^^^^^^

.. java:method:: public Optional<Deployment> scaleOut(MicoServiceDeploymentInfo serviceDeploymentInfo, int numberOfReplicas)
   :outertype: MicoKubernetesClient

   Performs a scale out of a Kubernetes deployment based on some service deployment information by a given number of replicas to add.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .
   :param numberOfReplicas: the number of replicas to add.
   :return: the Kubernetes \ :java:ref:`Deployment`\ .

undeployApplication
^^^^^^^^^^^^^^^^^^^

.. java:method:: public void undeployApplication(MicoApplication application)
   :outertype: MicoKubernetesClient

   Undeploys an application. Note that \ :java:ref:`MicoServices <MicoService>`\  included in this application will not be undeployed, if and only if they are included in at least one other application. In this case the corresponding Kubernetes deployment will be scaled in.

   :param application: the \ :java:ref:`MicoApplication`\ .

