.. java:import:: io.github.ust.mico.core.broker DeploymentBroker

.. java:import:: io.github.ust.mico.core.dto.response MicoApplicationJobStatusResponseDTO

.. java:import:: io.github.ust.mico.core.exception DeploymentRequirementsOfKafkaFaasConnectorNotMetException

.. java:import:: io.github.ust.mico.core.exception MicoApplicationIsDeployingException

.. java:import:: io.github.ust.mico.core.exception MicoApplicationNotFoundException

.. java:import:: io.github.ust.mico.core.exception MicoServiceInterfaceNotFoundException

.. java:import:: io.github.ust.mico.core.model MicoApplicationJobStatus

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation PostMapping

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RequestParam

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: org.springframework.web.server ResponseStatusException

DeploymentResource
==================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @RestController @RequestMapping public class DeploymentResource

Methods
-------
deploy
^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoApplicationJobStatusResponseDTO>> deploy(String shortName, String version, boolean rebuildImages)
   :outertype: DeploymentResource

undeploy
^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Void> undeploy(String shortName, String version)
   :outertype: DeploymentResource

