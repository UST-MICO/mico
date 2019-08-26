.. java:import:: java.net URI

.. java:import:: javax.validation.constraints NotNull

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client ResourceAccessException

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.util UriComponentsBuilder

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration PrometheusConfig

.. java:import:: io.github.ust.mico.core.dto.response MicoApplicationResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.internal PrometheusResponseDTO

.. java:import:: io.github.ust.mico.core.exception PrometheusRequestFailedException

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok.extern.slf4j Slf4j

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

.. java:constructor:: @Autowired public MicoStatusService(PrometheusConfig prometheusConfig, MicoKubernetesClient micoKubernetesClient, RestTemplate restTemplate, MicoServiceRepository serviceRepository, MicoApplicationRepository micoApplicationRepository)
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

.. java:method:: public MicoServiceInterfaceStatusResponseDTO getPublicIpOfKubernetesService(MicoService micoService, MicoServiceInterface serviceInterface) throws KubernetesResourceException
   :outertype: MicoStatusService

   Get the public IP of a \ :java:ref:`MicoServiceInterface`\  by providing the corresponding Kubernetes \ :java:ref:`Service`\ .

   :param micoService: is the \ :java:ref:`MicoService`\ , that has a \ :java:ref:`MicoServiceInterface`\ , which is deployed on Kubernetes
   :param serviceInterface: the \ :java:ref:`MicoServiceInterface`\ , that is deployed as a Kubernetes service
   :throws KubernetesResourceException: if it's not possible to get the Kubernetes service
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

