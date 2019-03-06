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

.. java:import:: io.github.ust.mico.core.dto KubernetesNodeMetricsDTO

.. java:import:: io.github.ust.mico.core.dto KubernetesPodInformationDTO

.. java:import:: io.github.ust.mico.core.dto KubernetesPodMetricsDTO

.. java:import:: io.github.ust.mico.core.dto MicoApplicationDTO

.. java:import:: io.github.ust.mico.core.dto MicoApplicationStatusDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceInterfaceStatusDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceStatusDTO

.. java:import:: io.github.ust.mico.core.dto PrometheusResponse

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception PrometheusRequestFailedException

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client ResourceAccessException

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.util UriComponentsBuilder

MicoStatusService
=================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Slf4j @Component public class MicoStatusService

   Provides functionality to retrieve status information for a \ :java:ref:`MicoApplication`\  or a particular \ :java:ref:`MicoService`\

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

.. java:method:: public MicoApplicationStatusDTO getApplicationStatus(MicoApplication micoApplication)
   :outertype: MicoStatusService

   Get status information for a \ :java:ref:`MicoApplication`\

   :param micoApplication: the application the status is requested for
   :return: \ :java:ref:`MicoApplicationStatusDTO`\  containing a list of \ :java:ref:`MicoServiceStatusDTO`\  for status information of a single \ :java:ref:`MicoService`\

getServiceInterfaceStatus
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceInterfaceStatusDTO> getServiceInterfaceStatus(MicoService micoService, List<String> errorMessages)
   :outertype: MicoStatusService

getServiceStatus
^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceStatusDTO getServiceStatus(MicoService micoService)
   :outertype: MicoStatusService

   Get status information for a single \ :java:ref:`MicoService`\ : # available replicas, # requested replicas, pod metrics (cpu load, memory load)

   :param micoService: is a \ :java:ref:`MicoService`\
   :return: \ :java:ref:`MicoServiceStatusDTO`\  which contains status information for a specific \ :java:ref:`MicoService`\

