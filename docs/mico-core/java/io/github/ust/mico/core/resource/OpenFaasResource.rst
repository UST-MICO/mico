.. java:import:: io.github.ust.mico.core.broker OpenFaasBroker

.. java:import:: io.github.ust.mico.core.configuration OpenFaaSConfig

.. java:import:: io.github.ust.mico.core.dto.response ExternalUrlDTO

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.util RestTemplates

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.beans.factory.annotation Qualifier

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation GetMapping

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: org.springframework.web.client ResourceAccessException

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: java.net MalformedURLException

.. java:import:: java.net URL

.. java:import:: java.util Optional

OpenFaasResource
================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class OpenFaasResource

Fields
------
FUNCTIONS_PATH
^^^^^^^^^^^^^^

.. java:field:: public static final String FUNCTIONS_PATH
   :outertype: OpenFaasResource

OPEN_FAAS_BASE_PATH
^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_BASE_PATH
   :outertype: OpenFaasResource

OPEN_FAAS_FUNCTION_LIST_PATH
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String OPEN_FAAS_FUNCTION_LIST_PATH
   :outertype: OpenFaasResource

openFaaSConfig
^^^^^^^^^^^^^^

.. java:field:: @Autowired  OpenFaaSConfig openFaaSConfig
   :outertype: OpenFaasResource

openFaasBroker
^^^^^^^^^^^^^^

.. java:field:: @Autowired  OpenFaasBroker openFaasBroker
   :outertype: OpenFaasResource

restTemplate
^^^^^^^^^^^^

.. java:field:: @Autowired @Qualifier  RestTemplate restTemplate
   :outertype: OpenFaasResource

Methods
-------
getOpenFaasFunctions
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<String> getOpenFaasFunctions()
   :outertype: OpenFaasResource

getOpenFaasURL
^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<ExternalUrlDTO> getOpenFaasURL()
   :outertype: OpenFaasResource

