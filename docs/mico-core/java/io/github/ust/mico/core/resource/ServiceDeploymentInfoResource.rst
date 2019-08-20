.. java:import:: io.github.ust.mico.core.broker MicoServiceDeploymentInfoBroker

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDeploymentInfoResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: javax.validation Valid

ServiceDeploymentInfoResource
=============================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ServiceDeploymentInfoResource

Methods
-------
getServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> getServiceDeploymentInformation(String shortName, String version, String serviceShortName)
   :outertype: ServiceDeploymentInfoResource

updateServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> updateServiceDeploymentInformation(String shortName, String version, String serviceShortName, MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoRequestDto)
   :outertype: ServiceDeploymentInfoResource

