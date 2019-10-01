.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: io.github.ust.mico.core.broker MicoServiceBroker

.. java:import:: io.github.ust.mico.core.dto.request CrawlingInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceRequestDTO

.. java:import:: io.github.ust.mico.core.dto.request MicoVersionRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDependencyGraphResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoYamlResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoServiceStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.service GitHubCrawler

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

.. java:import:: java.util.stream Collectors

ServiceResource
===============

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ServiceResource

Fields
------
PATH_VARIABLE_INSTANCE_ID
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: static final String PATH_VARIABLE_INSTANCE_ID
   :outertype: ServiceResource

PATH_VARIABLE_SHORT_NAME
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: static final String PATH_VARIABLE_SHORT_NAME
   :outertype: ServiceResource

PATH_VARIABLE_VERSION
^^^^^^^^^^^^^^^^^^^^^

.. java:field:: static final String PATH_VARIABLE_VERSION
   :outertype: ServiceResource

Methods
-------
createNewDependee
^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Void> createNewDependee(String shortName, String version, String dependeeShortName, String dependeeVersion)
   :outertype: ServiceResource

   Creates a new dependency edge between the Service and the depended service.

createService
^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoServiceResponseDTO>> createService(MicoServiceRequestDTO serviceDto)
   :outertype: ServiceResource

deleteAllDependees
^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllDependees(String shortName, String version)
   :outertype: ServiceResource

deleteAllVersionsOfService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllVersionsOfService(String shortName)
   :outertype: ServiceResource

deleteDependee
^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteDependee(String shortName, String version, String dependeeShortName, String dependeeVersion)
   :outertype: ServiceResource

deleteService
^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteService(String shortName, String version)
   :outertype: ServiceResource

getDependees
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getDependees(String shortName, String version)
   :outertype: ServiceResource

getDependencyGraph
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceDependencyGraphResponseDTO>> getDependencyGraph(String shortName, String version)
   :outertype: ServiceResource

getDependers
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getDependers(String shortName, String version)
   :outertype: ServiceResource

getServiceByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceResponseDTO>> getServiceByShortNameAndVersion(String shortName, String version)
   :outertype: ServiceResource

getServiceLinks
^^^^^^^^^^^^^^^

.. java:method:: static Iterable<Link> getServiceLinks(MicoService service)
   :outertype: ServiceResource

getServiceList
^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getServiceList()
   :outertype: ServiceResource

getServiceResponseDTOResource
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: static Resource<MicoServiceResponseDTO> getServiceResponseDTOResource(MicoService service)
   :outertype: ServiceResource

getServiceResponseDTOResourcesList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: static List<Resource<MicoServiceResponseDTO>> getServiceResponseDTOResourcesList(List<MicoService> services)
   :outertype: ServiceResource

getServiceYamlByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoYamlResponseDTO>> getServiceYamlByShortNameAndVersion(String shortName, String version)
   :outertype: ServiceResource

   Return yaml for a \ :java:ref:`MicoService`\  for the give shortName and version.

   :param shortName: the short name of the \ :java:ref:`MicoService`\ .
   :param version: version the version of the \ :java:ref:`MicoService`\ .
   :return: the kubernetes YAML for the \ :java:ref:`MicoService`\ .

getStatusListOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceStatusResponseDTO>>> getStatusListOfService(String shortName, String version)
   :outertype: ServiceResource

getStatusOfServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceStatusResponseDTO>> getStatusOfServiceInstance(String shortName, String version, String instanceId)
   :outertype: ServiceResource

getVersionsFromGitHub
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoVersionRequestDTO>>> getVersionsFromGitHub(String url)
   :outertype: ServiceResource

getVersionsOfService
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceResponseDTO>>> getVersionsOfService(String shortName)
   :outertype: ServiceResource

importMicoServiceFromGitHub
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoServiceResponseDTO>> importMicoServiceFromGitHub(CrawlingInfoRequestDTO crawlingInfo)
   :outertype: ServiceResource

promoteService
^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoServiceResponseDTO>> promoteService(String shortName, String version, MicoVersionRequestDTO newVersionDto)
   :outertype: ServiceResource

updateService
^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoServiceResponseDTO>> updateService(String shortName, String version, MicoServiceRequestDTO serviceDto)
   :outertype: ServiceResource

