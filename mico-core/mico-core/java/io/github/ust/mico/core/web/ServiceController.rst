.. java:import:: io.github.ust.mico.core.dto MicoServiceDependencyGraphDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceDependencyGraphEdgeDTO

.. java:import:: io.github.ust.mico.core.dto CrawlingInfoDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceStatusDTO

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.service GitHubCrawler

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.service MicoStatusService

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

.. java:import:: java.io IOException

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

ServiceController
=================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ServiceController

Fields
------
PATH_DELETE_SHORT_NAME
^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_DELETE_SHORT_NAME
   :outertype: ServiceController

PATH_DELETE_VERSION
^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_DELETE_VERSION
   :outertype: ServiceController

PATH_GITHUB_ENDPOINT
^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_GITHUB_ENDPOINT
   :outertype: ServiceController

PATH_VARIABLE_GITHUB
^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_GITHUB
   :outertype: ServiceController

PATH_VARIABLE_ID
^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_ID
   :outertype: ServiceController

PATH_VARIABLE_IMPORT
^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_IMPORT
   :outertype: ServiceController

PATH_VARIABLE_SHORT_NAME
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_SHORT_NAME
   :outertype: ServiceController

PATH_VARIABLE_VERSION
^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_VERSION
   :outertype: ServiceController

Methods
-------
createNewDependee
^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoService>> createNewDependee(String shortName, String version, MicoServiceDependency newServiceDependee)
   :outertype: ServiceController

   Create a new dependency edge between the Service and the dependee service.

createService
^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<?> createService(MicoService newService)
   :outertype: ServiceController

deleteAllDependees
^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoService>> deleteAllDependees(String shortName, String version)
   :outertype: ServiceController

deleteAllVersionsOfService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllVersionsOfService(String shortName) throws KubernetesResourceException
   :outertype: ServiceController

deleteDependee
^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoService>> deleteDependee(String shortName, String version, String shortNameToDelete, String versionToDelete)
   :outertype: ServiceController

deleteService
^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteService(String shortName, String version) throws KubernetesResourceException
   :outertype: ServiceController

getDependees
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getDependees(String shortName, String version)
   :outertype: ServiceController

getDependencyGraph
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceDependencyGraphDTO>> getDependencyGraph(String shortName, String version)
   :outertype: ServiceController

getDependers
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getDependers(String shortName, String version)
   :outertype: ServiceController

getDependers
^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependers(MicoService serviceToLookFor)
   :outertype: ServiceController

getServiceById
^^^^^^^^^^^^^^

.. java:method:: public ResponseEntity<Resource<MicoService>> getServiceById(Long id)
   :outertype: ServiceController

getServiceByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoService>> getServiceByShortNameAndVersion(String shortName, String version)
   :outertype: ServiceController

getServiceList
^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getServiceList()
   :outertype: ServiceController

getServiceResourcesList
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: static List<Resource<MicoService>> getServiceResourcesList(List<MicoService> services)
   :outertype: ServiceController

getStatusOfService
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceStatusDTO>> getStatusOfService(String shortName, String version)
   :outertype: ServiceController

getVersionsFromGitHub
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping @ResponseBody public LinkedList<String> getVersionsFromGitHub(String url)
   :outertype: ServiceController

getVersionsOfService
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getVersionsOfService(String shortName)
   :outertype: ServiceController

importMicoServiceFromGitHub
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<?> importMicoServiceFromGitHub(CrawlingInfoDTO crawlingInfo)
   :outertype: ServiceController

setServiceDependees
^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService setServiceDependees(MicoService newService)
   :outertype: ServiceController

updateService
^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<?> updateService(String shortName, String version, MicoService service)
   :outertype: ServiceController

