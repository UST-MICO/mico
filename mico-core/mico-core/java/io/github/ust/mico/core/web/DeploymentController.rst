.. java:import:: io.github.ust.mico.core.exception ImageBuildException

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception NotInitializedException

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.service MicoCoreBackgroundTaskFactory

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.service.imagebuilder ImageBuilder

.. java:import:: io.github.ust.mico.core.service.imagebuilder.buildtypes Build

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation PostMapping

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent ExecutionException

.. java:import:: java.util.concurrent TimeoutException

DeploymentController
====================

.. java:package:: io.github.ust.mico.core.web
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class DeploymentController

Methods
-------
deploy
^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Void> deploy(String shortName, String version)
   :outertype: DeploymentController

