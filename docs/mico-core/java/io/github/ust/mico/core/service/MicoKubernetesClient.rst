.. java:import:: java.net PasswordAuthentication

.. java:import:: java.util ArrayList

.. java:import:: java.util Base64

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: io.fabric8.kubernetes.api.model Container

.. java:import:: io.fabric8.kubernetes.api.model ContainerBuilder

.. java:import:: io.fabric8.kubernetes.api.model ContainerPort

.. java:import:: io.fabric8.kubernetes.api.model ContainerPortBuilder

.. java:import:: io.fabric8.kubernetes.api.model DoneableSecret

.. java:import:: io.fabric8.kubernetes.api.model EnvVar

.. java:import:: io.fabric8.kubernetes.api.model EnvVarBuilder

.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerIngress

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model Secret

.. java:import:: io.fabric8.kubernetes.api.model SecretList

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model ServiceBuilder

.. java:import:: io.fabric8.kubernetes.api.model ServicePort

.. java:import:: io.fabric8.kubernetes.api.model ServicePortBuilder

.. java:import:: io.fabric8.kubernetes.api.model ServiceStatus

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentBuilder

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.fabric8.kubernetes.client.dsl NonNamespaceOperation

.. java:import:: io.fabric8.kubernetes.client.dsl Resource

.. java:import:: io.fabric8.kubernetes.client.internal SerializationUtils

.. java:import:: io.github.ust.mico.core.broker BackgroundJobBroker

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesBuildBotConfig

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesConfig

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception MicoApplicationNotFoundException

.. java:import:: io.github.ust.mico.core.model KubernetesDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus.Value

.. java:import:: io.github.ust.mico.core.model MicoApplicationJobStatus

.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable

.. java:import:: io.github.ust.mico.core.model MicoInterfaceConnection

.. java:import:: io.github.ust.mico.core.model MicoLabel

.. java:import:: io.github.ust.mico.core.model MicoMessage

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.model MicoServicePort

.. java:import:: io.github.ust.mico.core.model MicoTopicRole

.. java:import:: io.github.ust.mico.core.persistence KubernetesDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.service.imagebuilder TektonPipelinesController

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

MicoKubernetesClient
====================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Slf4j @Component public class MicoKubernetesClient

   Provides accessor methods for creating deployments and services in Kubernetes as well as getter methods to retrieve existing Kubernetes deployments and services.

Fields
------
OPEN_FAAS_SECRET_DATA_PASSWORD_NAME
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_SECRET_DATA_PASSWORD_NAME
   :outertype: MicoKubernetesClient

   The name of the data element which holds the OpenFaaS password inside the secret.

OPEN_FAAS_SECRET_DATA_USERNAME_NAME
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_SECRET_DATA_USERNAME_NAME
   :outertype: MicoKubernetesClient

   The name of the data element which holds the OpenFaaS username inside the secret

OPEN_FAAS_SECRET_NAME
^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_SECRET_NAME
   :outertype: MicoKubernetesClient

   The name of the secret which holds the OpenFaaS username and password.

Constructors
------------
MicoKubernetesClient
^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MicoKubernetesClient(MicoKubernetesConfig micoKubernetesConfig, MicoKubernetesBuildBotConfig buildBotConfig, KubernetesClient kubernetesClient, TektonPipelinesController imageBuilder, BackgroundJobBroker backgroundJobBroker, MicoApplicationRepository applicationRepository, MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository, KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository)
   :outertype: MicoKubernetesClient

Methods
-------
createMicoServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Deployment createMicoServiceInstance(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoKubernetesClient

   Create a Kubernetes deployment based on a \ :java:ref:`MicoServiceDeploymentInfo`\ .

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the Kubernetes \ :java:ref:`Deployment`\  resource object

createMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service createMicoServiceInterface(MicoServiceInterface micoServiceInterface, MicoServiceDeploymentInfo micoServiceDeploymentInfo) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Create a Kubernetes service based on a MICO service interface.

   :param micoServiceInterface: the \ :java:ref:`MicoServiceInterface`\
   :param micoServiceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the Kubernetes \ :java:ref:`Service`\  resource

createOrUpdateInterfaceConnections
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void createOrUpdateInterfaceConnections(MicoApplication micoApplication)
   :outertype: MicoKubernetesClient

   Creates or updates all interface connections of the given \ ``MicoApplication``\ .

   :param micoApplication: the \ :java:ref:`MicoApplication`\

createServiceName
^^^^^^^^^^^^^^^^^

.. java:method:: public String createServiceName(MicoServiceDeploymentInfo serviceDeploymentInfo, MicoServiceInterface serviceInterface)
   :outertype: MicoKubernetesClient

   Creates the name of the Kubernetes service based on the \ ``serviceDeploymentInfo``\  and the \ ``serviceInterfaceName``\ .

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :param serviceInterface: the \ :java:ref:`MicoServiceInterface`\
   :return: the created string that should be used as the name of the Kubernetes service

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

getDeploymentOfMicoServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<Deployment> getDeploymentOfMicoServiceInstance(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoKubernetesClient

   Returns a Kubernetes \ :java:ref:`Deployment`\  instance that corresponds to the provided \ :java:ref:`MicoServiceDeploymentInfo`\ , if it is already deployed to the Kubernetes cluster. Labels are used for the lookup.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: an \ :java:ref:`Optional`\  with the \ :java:ref:`Deployment`\  of the Kubernetes service, or an empty \ :java:ref:`Optional`\  if there is no Kubernetes deployment of the \ :java:ref:`MicoService`\ .

getDeploymentsOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Deployment> getDeploymentsOfMicoService(MicoService micoService)
   :outertype: MicoKubernetesClient

   Returns a list of Kubernetes \ :java:ref:`Deployment`\  instances that corresponds to the \ :java:ref:`MicoService`\  Labels are used for the lookup.

   :param micoService: the \ :java:ref:`MicoService`\
   :return: a list of Kubernetes \ :java:ref:`Deployments <Deployment>`\ . It is empty if there is no Kubernetes deployment of the \ :java:ref:`MicoService`\ .

getInterfaceByNameOfMicoServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<Service> getInterfaceByNameOfMicoServiceInstance(MicoServiceDeploymentInfo serviceDeploymentInfo, String micoServiceInterfaceName)
   :outertype: MicoKubernetesClient

   Check if the \ :java:ref:`MicoServiceInterface`\  is already created for the \ :java:ref:`MicoService`\  in the Kubernetes cluster. Labels are used for the lookup.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :param micoServiceInterfaceName: the name of a \ :java:ref:`MicoServiceInterface`\
   :return: an \ :java:ref:`Optional`\  with the Kubernetes \ :java:ref:`Service`\ , or an empty \ :java:ref:`Optional`\  if there is no Kubernetes \ :java:ref:`Service`\  for this \ :java:ref:`MicoServiceInterface`\ .

getInterfacesOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Service> getInterfacesOfMicoService(MicoService micoService)
   :outertype: MicoKubernetesClient

   Looks up if there are any interfaces created for the \ :java:ref:`MicoService`\  in the Kubernetes cluster. If so, it returns them as a list of Kubernetes \ :java:ref:`Service`\  objects. Labels are used for the lookup.

   :param micoService: the \ :java:ref:`MicoService`\
   :return: the list of Kubernetes \ :java:ref:`Service`\  objects

getInterfacesOfMicoServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Service> getInterfacesOfMicoServiceInstance(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoKubernetesClient

   Looks up if there are any interfaces created for the \ :java:ref:`MicoServiceDeploymentInfo`\  in the Kubernetes cluster. If so, it returns them as a list of Kubernetes \ :java:ref:`Service`\  objects. Labels are used for the lookup.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the list of Kubernetes \ :java:ref:`Service`\  objects

getOpenFaasCredentials
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public PasswordAuthentication getOpenFaasCredentials()
   :outertype: MicoKubernetesClient

   Requests the OpenFaaS credentials from a Kubernetes secret.

   :return: the username and the password

getPodsCreatedByDeploymentOfMicoServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Pod> getPodsCreatedByDeploymentOfMicoServiceInstance(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoKubernetesClient

   Looks up if the \ :java:ref:`MicoServiceDeploymentInfo`\  is already deployed to the Kubernetes cluster. If so, it returns the list of Kubernetes \ :java:ref:`Pod`\  objects that belongs to the \ :java:ref:`Deployment`\ . Labels are used for the lookup.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: the list of Kubernetes \ :java:ref:`Pod`\  objects

getPublicIpOfKubernetesService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<String> getPublicIpOfKubernetesService(String name, String namespace) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Requests the public IP of a Kubernetes service and returns it or an empty \ ``Optional``\  if the service has no public IP.

   :param name: the name of the service.
   :param namespace: the namespace which contains the service.
   :throws KubernetesResourceException: if there is no such service.
   :return: the public ip of a service or an empty optional.

getPublicPortsOfKubernetesService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<Integer> getPublicPortsOfKubernetesService(String name, String namespace) throws KubernetesResourceException
   :outertype: MicoKubernetesClient

   Requests the list of public ports of a service. It returns the list of ports or an empty list if there are none.

   :param name: the name of the service.
   :param namespace: the namespace which contains the service.
   :throws KubernetesResourceException: if there is no such service.
   :return: a list of ports or an empty list.

getService
^^^^^^^^^^

.. java:method:: public Optional<Service> getService(String name, String namespace)
   :outertype: MicoKubernetesClient

   Requests the service with the given name in the given namespace or \ ``null``\  if there is no such service

   :param name: the name of the service.
   :param namespace: the namespace which contains the service.
   :return: the service in the namespace and with the given name or \ ``null``\ .

getYaml
^^^^^^^

.. java:method:: public String getYaml(MicoService micoService) throws JsonProcessingException
   :outertype: MicoKubernetesClient

   Retrieves the yaml(s) for all Kubernetes deployments of a MicoService and the yaml(s) for all Kubernetes services of the including interfaces (if there are any).

   :param micoService: the \ :java:ref:`MicoService`\
   :throws JsonProcessingException: if there is a error processing the content.
   :return: the kubernetes YAML for the \ :java:ref:`MicoService`\ .

getYaml
^^^^^^^

.. java:method:: public String getYaml(MicoServiceDeploymentInfo serviceDeploymentInfo) throws JsonProcessingException
   :outertype: MicoKubernetesClient

   Retrieves the yaml for a MicoServiceDeploymentInfo, contains the interfaces if they exist.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :throws JsonProcessingException: if there is a error processing the content.
   :return: the kubernetes YAML for the \ :java:ref:`MicoService`\ .

isApplicationDeployed
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isApplicationDeployed(MicoApplication micoApplication)
   :outertype: MicoKubernetesClient

   Checks whether a given \ ``MicoApplication``\  is currently deployed.

   :param micoApplication: the \ :java:ref:`MicoApplication`\ .
   :return: \ ``true``\  if and only if \ :java:ref:`getApplicationDeploymentStatus(MicoApplication)`\  returns a \ :java:ref:`MicoApplicationDeploymentStatus`\  with \ :java:ref:`Deployed <Value.DEPLOYED>`\ ; \ ``false``\  otherwise.

isApplicationUndeployed
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isApplicationUndeployed(MicoApplication micoApplication)
   :outertype: MicoKubernetesClient

   Checks whether a given \ ``MicoApplication``\  is currently undeployed.

   :param micoApplication: the \ :java:ref:`MicoApplication`\ .
   :return: \ ``true``\  if and only if \ :java:ref:`getApplicationDeploymentStatus(MicoApplication)`\  returns a \ :java:ref:`MicoApplicationDeploymentStatus`\  with \ :java:ref:`Undeployed <Value.UNDEPLOYED>`\ ; \ ``false``\  otherwise.

isMicoServiceDeployed
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isMicoServiceDeployed(MicoService micoService)
   :outertype: MicoKubernetesClient

   Checks if a \ :java:ref:`MicoService`\  is already deployed at least with one instance.

   :param micoService: the \ :java:ref:`MicoService`\
   :return: \ ``true``\  if the \ :java:ref:`MicoService`\  is deployed.

isMicoServiceInstanceDeployed
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean isMicoServiceInstanceDeployed(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoKubernetesClient

   Checks if a \ :java:ref:`MicoService`\  instance is already deployed.

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
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

