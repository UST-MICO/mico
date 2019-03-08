.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent ExecutionException

.. java:import:: java.util.concurrent TimeoutException

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation PostMapping

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: io.github.ust.mico.core ImageBuildException

.. java:import:: io.github.ust.mico.core NotInitializedException

.. java:import:: io.github.ust.mico.core.concurrency MicoCoreBackgroundTaskFactory

.. java:import:: io.github.ust.mico.core.imagebuilder ImageBuilder

.. java:import:: io.github.ust.mico.core.imagebuilder.buildtypes Build

.. java:import:: io.github.ust.mico.core.mapping MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: lombok.extern.slf4j Slf4j

DeploymentController
====================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class DeploymentController

Methods
-------
deploy
^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Void> deploy(String shortName, String version)
   :outertype: DeploymentController

