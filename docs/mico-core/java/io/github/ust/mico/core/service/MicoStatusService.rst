.. java:import:: java.net URI

.. java:import:: java.util ArrayList

.. java:import:: java.util HashMap

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: java.util Optional

.. java:import:: javax.validation.constraints NotNull

.. java:import:: io.fabric8.kubernetes.api.model ContainerStatus

.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerIngress

.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerStatus

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration PrometheusConfig

.. java:import:: io.github.ust.mico.core.dto.response MicoApplicationResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.internal PrometheusResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status KubernetesNodeMetricsResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status KubernetesPodInformationResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status KubernetesPodMetricsResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationStatusResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoMessageResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoServiceInterfaceStatusResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoServiceStatusResponseDTO

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception PrometheusRequestFailedException

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoMessage

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceInterfaceRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client ResourceAccessException

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: org.springframework.web.util UriComponentsBuilder

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

.. java:constructor:: @Autowired public MicoStatusService(PrometheusConfig prometheusConfig, MicoKubernetesClient micoKubernetesClient, RestTemplate restTemplate, MicoServiceRepository serviceRepository, MicoServiceInterfaceRepository serviceInterfaceRepository, MicoApplicationRepository micoApplicationRepository)
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

.. java:method:: public MicoServiceInterfaceStatusResponseDTO getPublicIpOfKubernetesService(MicoService micoService, String serviceInterfaceName)
   :outertype: MicoStatusService

   Get the public IP of a \ :java:ref:`MicoServiceInterface`\  by providing the corresponding Kubernetes \ :java:ref:`Service`\ .

   :param micoService: is the \ :java:ref:`MicoService`\ , that has a \ :java:ref:`MicoServiceInterface`\ , which is deployed on Kubernetes.
   :param serviceInterfaceName: is the MicoServiceInterface, that is deployed as Kubernetes service .
   :return: the public IP of the provided Kubernetes Service

getServiceInterfaceStatus
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceInterfaceStatusResponseDTO> getServiceInterfaceStatus(MicoService micoService, List<MicoMessageResponseDTO> errorMessages)
   :outertype: MicoStatusService

   Get the status information for all \ :java:ref:`MicoServiceInterfaces <MicoServiceInterface>`\  of the \ :java:ref:`MicoService`\ .

   :param micoService: is the \ :java:ref:`MicoService`\  for which the status information of the MicoServiceInterfaces is requested.
   :param errorMessages: is the list of error messages, which is empty if no error occurs.
   :return: a list of \ :java:ref:`MicoServiceInterfaceStatusResponseDTO`\ , one DTO per MicoServiceInterface.

getServiceStatus
^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceStatusResponseDTO getServiceStatus(MicoService micoService)
   :outertype: MicoStatusService

   Get status information for a single \ :java:ref:`MicoService`\ : # available replicas, # requested replicas, pod metrics (CPU load, memory usage).

   :param micoService: is a \ :java:ref:`MicoService`\ .
   :return: \ :java:ref:`MicoServiceStatusResponseDTO`\  which contains status information for a specific \ :java:ref:`MicoService`\ .

