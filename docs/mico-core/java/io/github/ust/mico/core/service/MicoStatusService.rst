.. java:import:: io.fabric8.kubernetes.api.model ContainerStatus

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration PrometheusConfig

.. java:import:: io.github.ust.mico.core.dto.response MicoApplicationResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.internal PrometheusResponseDTO

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception PrometheusRequestFailedException

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client ResourceAccessException

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.util UriComponentsBuilder

.. java:import:: javax.validation.constraints NotNull

.. java:import:: java.net URI

MicoStatusService
=================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Slf4j @Component public class MicoStatusService

   Provides functionality to retrieve status information for a \ :java:ref:`MicoApplication`\  or a particular \ :java:ref:`MicoService`\ .

Constructors
------------
MicoStatusService
^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MicoStatusService(PrometheusConfig prometheusConfig, MicoKubernetesClient micoKubernetesClient, RestTemplate restTemplate, MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository, MicoApplicationRepository micoApplicationRepository)
   :outertype: MicoStatusService

Methods
-------
getApplicationStatus
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationStatusResponseDTO getApplicationStatus(MicoApplication micoApplication)
   :outertype: MicoStatusService

   Get status information for a \ :java:ref:`MicoApplication`\ .

   :param micoApplication: the application the status is requested for
   :return: \ :java:ref:`MicoApplicationStatusResponseDTO`\  containing a list of \ :java:ref:`MicoServiceStatusResponseDTO`\  for status information of a single \ :java:ref:`MicoService`\ .

getPublicIpOfKubernetesService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceInterfaceStatusResponseDTO getPublicIpOfKubernetesService(MicoServiceDeploymentInfo serviceDeploymentInfo, MicoServiceInterface serviceInterface) throws KubernetesResourceException
   :outertype: MicoStatusService

   Get the public IP of a \ :java:ref:`MicoServiceInterface`\  by providing the corresponding Kubernetes \ :java:ref:`Service`\ .

   :param serviceDeploymentInfo: is the \ :java:ref:`MicoServiceDeploymentInfo`\ , that has a \ :java:ref:`MicoServiceInterface`\ , which is deployed on Kubernetes
   :param serviceInterface: the \ :java:ref:`MicoServiceInterface`\ , that is deployed as a Kubernetes service
   :throws KubernetesResourceException: if it's not possible to get the Kubernetes service
   :return: the public IP of the provided Kubernetes Service

getServiceInstanceStatus
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceStatusResponseDTO getServiceInstanceStatus(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoStatusService

   Get status information for a single \ :java:ref:`MicoServiceDeploymentInfo`\ : # available replicas, # requested replicas, pod metrics (CPU load, memory usage).

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .
   :return: the \ :java:ref:`MicoServiceStatusResponseDTO`\  which contains status information for a specific instance of a \ :java:ref:`MicoService`\ .

getServiceInterfaceStatus
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceInterfaceStatusResponseDTO> getServiceInterfaceStatus(MicoServiceDeploymentInfo serviceDeploymentInfo, List<MicoMessageResponseDTO> errorMessages)
   :outertype: MicoStatusService

   Get the status information for all \ :java:ref:`MicoServiceInterfaces <MicoServiceInterface>`\  of the \ :java:ref:`MicoService`\ .

   :param serviceDeploymentInfo: is the \ :java:ref:`MicoServiceDeploymentInfo`\  that includes the service for which the status information of the MicoServiceInterfaces is requested.
   :param errorMessages: is the list of error messages, which is empty if no error occurs.
   :return: a list of \ :java:ref:`MicoServiceInterfaceStatusResponseDTO`\ , one DTO per MicoServiceInterface.

getServiceStatus
^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceStatusResponseDTO> getServiceStatus(MicoService micoService)
   :outertype: MicoStatusService

   Get status information for all instances of a \ :java:ref:`MicoService`\  and return them as a list: # available replicas, # requested replicas, pod metrics (CPU load, memory usage).

   :param micoService: the \ :java:ref:`MicoService`\ .
   :return: the list of \ :java:ref:`MicoServiceStatusResponseDTOs <MicoServiceStatusResponseDTO>`\  which contains status information for all instances of a \ :java:ref:`MicoService`\ .

