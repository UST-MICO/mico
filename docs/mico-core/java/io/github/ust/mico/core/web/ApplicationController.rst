.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model PodList

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model ServiceList

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.fabric8.kubernetes.api.model.apps DeploymentList

.. java:import:: io.github.ust.mico.core.service ClusterAwarenessFabric8

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesConfig

.. java:import:: io.github.ust.mico.core.configuration PrometheusConfig

.. java:import:: io.github.ust.mico.core.exception PrometheusRequestFailedException

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.client ResourceAccessException

.. java:import:: org.springframework.web.client RestTemplate

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: org.springframework.web.util UriComponentsBuilder

.. java:import:: java.net URI

.. java:import:: java.util HashMap

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

ApplicationController
=====================

.. java:package:: io.github.ust.mico.core.web
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ApplicationController

Fields
------
PATH_APPLICATIONS
^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_APPLICATIONS
   :outertype: ApplicationController

PATH_SERVICES
^^^^^^^^^^^^^

.. java:field:: public static final String PATH_SERVICES
   :outertype: ApplicationController

cluster
^^^^^^^

.. java:field:: @Autowired  ClusterAwarenessFabric8 cluster
   :outertype: ApplicationController

micoKubernetesConfig
^^^^^^^^^^^^^^^^^^^^

.. java:field:: @Autowired  MicoKubernetesConfig micoKubernetesConfig
   :outertype: ApplicationController

Methods
-------
addServiceToApplication
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Void> addServiceToApplication(String applicationShortName, String applicationVersion, MicoService serviceFromBody)
   :outertype: ApplicationController

createApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoApplication>> createApplication(MicoApplication newApplication)
   :outertype: ApplicationController

deleteApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoApplication>> deleteApplication(String shortName, String version)
   :outertype: ApplicationController

getAllApplications
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoApplication>>> getAllApplications()
   :outertype: ApplicationController

getApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplication>> getApplicationByShortNameAndVersion(String shortName, String version)
   :outertype: ApplicationController

getApplicationDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<UiDeploymentInformation>> getApplicationDeploymentInformation(String shortName, String version)
   :outertype: ApplicationController

getApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoApplication>>> getApplicationsByShortName(String shortName)
   :outertype: ApplicationController

getMicoServicesFromApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getMicoServicesFromApplication(String applicationShortName, String applicationVersion)
   :outertype: ApplicationController

   Returns a list of services associated with the mico application specified by the parameters.

   :param applicationShortName: the name of the application
   :param applicationVersion: the version of the application
   :return: the list of mico services that are associated with the application

updateApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoApplication>> updateApplication(String shortName, String version, MicoApplication application)
   :outertype: ApplicationController

