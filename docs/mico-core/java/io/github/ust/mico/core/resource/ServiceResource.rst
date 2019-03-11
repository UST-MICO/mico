.. java:import:: io.github.ust.mico.core.dto CrawlingInfoDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceDependencyGraphDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceDependencyGraphEdgeDTO

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

.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: java.io IOException

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

ServiceResource
===============

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ServiceResource

Fields
------
PATH_DELETE_SHORT_NAME
^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_DELETE_SHORT_NAME
   :outertype: ServiceResource

PATH_DELETE_VERSION
^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_DELETE_VERSION
   :outertype: ServiceResource

PATH_GITHUB_ENDPOINT
^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_GITHUB_ENDPOINT
   :outertype: ServiceResource

PATH_PROMOTE
^^^^^^^^^^^^

.. java:field:: public static final String PATH_PROMOTE
   :outertype: ServiceResource

PATH_VARIABLE_GITHUB
^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_GITHUB
   :outertype: ServiceResource

PATH_VARIABLE_ID
^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_ID
   :outertype: ServiceResource

PATH_VARIABLE_IMPORT
^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_IMPORT
   :outertype: ServiceResource

PATH_VARIABLE_SHORT_NAME
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_SHORT_NAME
   :outertype: ServiceResource

PATH_VARIABLE_VERSION
^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_VERSION
   :outertype: ServiceResource

Methods
-------
createNewDependee
^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoService>> createNewDependee(String shortName, String version, MicoServiceDependency newServiceDependee)
   :outertype: ServiceResource

   Create a new dependency edge between the Service and the dependee service.

createService
^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<?> createService(MicoService newService)
   :outertype: ServiceResource

deleteAllDependees
^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoService>> deleteAllDependees(String shortName, String version)
   :outertype: ServiceResource

deleteAllVersionsOfService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllVersionsOfService(String shortName) throws KubernetesResourceException
   :outertype: ServiceResource

deleteDependee
^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoService>> deleteDependee(String shortName, String version, String shortNameToDelete, String versionToDelete)
   :outertype: ServiceResource

deleteService
^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteService(String shortName, String version) throws KubernetesResourceException
   :outertype: ServiceResource

getDependees
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getDependees(String shortName, String version)
   :outertype: ServiceResource

getDependencyGraph
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceDependencyGraphDTO>> getDependencyGraph(String shortName, String version)
   :outertype: ServiceResource

getDependers
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getDependers(String shortName, String version)
   :outertype: ServiceResource

getDependers
^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependers(MicoService serviceToLookFor)
   :outertype: ServiceResource

getServiceById
^^^^^^^^^^^^^^

.. java:method:: public ResponseEntity<Resource<MicoService>> getServiceById(Long id)
   :outertype: ServiceResource

getServiceByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoService>> getServiceByShortNameAndVersion(String shortName, String version)
   :outertype: ServiceResource

getServiceList
^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getServiceList()
   :outertype: ServiceResource

getServiceResourcesList
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: static List<Resource<MicoService>> getServiceResourcesList(List<MicoService> services)
   :outertype: ServiceResource

getStatusOfService
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceStatusDTO>> getStatusOfService(String shortName, String version)
   :outertype: ServiceResource

getVersionsFromGitHub
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping @ResponseBody public LinkedList<String> getVersionsFromGitHub(String url)
   :outertype: ServiceResource

getVersionsOfService
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getVersionsOfService(String shortName)
   :outertype: ServiceResource

importMicoServiceFromGitHub
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<?> importMicoServiceFromGitHub(CrawlingInfoDTO crawlingInfo)
   :outertype: ServiceResource

promoteService
^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoService>> promoteService(String shortName, String version, String newVersion)
   :outertype: ServiceResource

setServiceDependees
^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService setServiceDependees(MicoService newService)
   :outertype: ServiceResource

updateService
^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<?> updateService(String shortName, String version, MicoService service)
   :outertype: ServiceResource

