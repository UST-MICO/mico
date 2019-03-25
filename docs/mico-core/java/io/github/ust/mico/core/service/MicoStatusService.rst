.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration PrometheusConfig

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

.. java:import:: javax.validation.constraints NotNull

.. java:import:: java.net URI

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

