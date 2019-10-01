.. java:import:: io.github.ust.mico.core.broker KafkaFaasConnectorDeploymentInfoBroker

.. java:import:: io.github.ust.mico.core.dto.request KFConnectorDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response KFConnectorDeploymentInfoResponseDTO

.. java:import:: io.github.ust.mico.core.exception KafkaFaasConnectorInstanceNotFoundException

.. java:import:: io.github.ust.mico.core.exception MicoApplicationNotFoundException

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: javax.validation Valid

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

KafkaFaasConnectorDeploymentInfoResource
========================================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class KafkaFaasConnectorDeploymentInfoResource

Methods
-------
getKafkaFaasConnectorDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<KFConnectorDeploymentInfoResponseDTO>>> getKafkaFaasConnectorDeploymentInformation(String shortName, String version)
   :outertype: KafkaFaasConnectorDeploymentInfoResource

getKafkaFaasConnectorDeploymentInformationInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> getKafkaFaasConnectorDeploymentInformationInstance(String shortName, String version, String instanceId)
   :outertype: KafkaFaasConnectorDeploymentInfoResource

getKfConnectorDeploymentInfoResponseDTOResource
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: protected static Resource<KFConnectorDeploymentInfoResponseDTO> getKfConnectorDeploymentInfoResponseDTOResource(String applicationShortName, String applicationVersion, MicoServiceDeploymentInfo micoServiceDeploymentInfo)
   :outertype: KafkaFaasConnectorDeploymentInfoResource

   Wraps a \ :java:ref:`KFConnectorDeploymentInfoResponseDTO`\  into a HATEOAS resource with a link to the application and a self-link.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param micoServiceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: The resource containing the \ :java:ref:`KFConnectorDeploymentInfoResponseDTO`\ .

updateKafkaFaasConnectorDeploymentInfo
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> updateKafkaFaasConnectorDeploymentInfo(String shortName, String version, String instanceId, KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO)
   :outertype: KafkaFaasConnectorDeploymentInfoResource

