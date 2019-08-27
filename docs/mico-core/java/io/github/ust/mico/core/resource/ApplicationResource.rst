.. java:import:: io.github.ust.mico.core.broker MicoApplicationBroker

.. java:import:: io.github.ust.mico.core.dto.request KFConnectorDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.request MicoApplicationRequestDTO

.. java:import:: io.github.ust.mico.core.dto.request MicoVersionRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response KFConnectorDeploymentInfoResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoApplicationWithServicesResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDeploymentInfoResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationDeploymentStatusResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.swagger.annotations ApiOperation

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: javax.validation Valid

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

ApplicationResource
===================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ApplicationResource

Methods
-------
addKafkaFaasConnectorInstanceToApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ApiOperation @PostMapping public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> addKafkaFaasConnectorInstanceToApplication(String applicationShortName, String applicationVersion, String kfConnectorVersion)
   :outertype: ApplicationResource

addServiceToApplication
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ApiOperation @PostMapping public ResponseEntity<Resource<MicoServiceDeploymentInfoResponseDTO>> addServiceToApplication(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion)
   :outertype: ApplicationResource

createApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> createApplication(MicoApplicationRequestDTO applicationDto)
   :outertype: ApplicationResource

deleteAllVersionsOfApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllVersionsOfApplication(String shortName)
   :outertype: ApplicationResource

deleteApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteApplication(String shortName, String version)
   :outertype: ApplicationResource

deleteKafkaFaasConnectorInstanceFromApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteKafkaFaasConnectorInstanceFromApplication(String shortName, String version, String instanceId)
   :outertype: ApplicationResource

deleteServiceFromApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteServiceFromApplication(String shortName, String version, String serviceShortName)
   :outertype: ApplicationResource

getAllApplications
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getAllApplications()
   :outertype: ApplicationResource

getApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> getApplicationByShortNameAndVersion(String shortName, String version)
   :outertype: ApplicationResource

getApplicationDeploymentStatus
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplicationDeploymentStatusResponseDTO>> getApplicationDeploymentStatus(String shortName, String version)
   :outertype: ApplicationResource

getApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoApplicationWithServicesResponseDTO>>> getApplicationsByShortName(String shortName)
   :outertype: ApplicationResource

getServicesOfApplication
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getServicesOfApplication(String shortName, String version)
   :outertype: ApplicationResource

getStatusOfApplication
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplicationStatusResponseDTO>> getStatusOfApplication(String shortName, String version)
   :outertype: ApplicationResource

promoteApplication
^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> promoteApplication(String shortName, String version, MicoVersionRequestDTO newVersionDto)
   :outertype: ApplicationResource

updateApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoApplicationWithServicesResponseDTO>> updateApplication(String shortName, String version, MicoApplicationRequestDTO applicationRequestDto)
   :outertype: ApplicationResource

