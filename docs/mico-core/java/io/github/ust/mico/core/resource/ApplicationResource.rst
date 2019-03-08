.. java:import:: io.github.ust.mico.core.dto MicoApplicationDTO

.. java:import:: io.github.ust.mico.core.dto MicoApplicationDTO.MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.dto MicoApplicationStatusDTO

.. java:import:: io.github.ust.mico.core.dto MicoApplicationWithServicesDTO

.. java:import:: io.github.ust.mico.core.dto MicoServiceDeploymentInfoDTO

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfoQueryResult

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

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

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

ApplicationResource
===================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ApplicationResource

Fields
------
PATH_APPLICATIONS
^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_APPLICATIONS
   :outertype: ApplicationResource

PATH_DEPLOYMENT_INFORMATION
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_DEPLOYMENT_INFORMATION
   :outertype: ApplicationResource

PATH_PROMOTE
^^^^^^^^^^^^

.. java:field:: public static final String PATH_PROMOTE
   :outertype: ApplicationResource

PATH_SERVICES
^^^^^^^^^^^^^

.. java:field:: public static final String PATH_SERVICES
   :outertype: ApplicationResource

Methods
-------
addServiceToApplication
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Void> addServiceToApplication(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion)
   :outertype: ApplicationResource

createApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<?> createApplication(MicoApplicationDTO newApplicationDto)
   :outertype: ApplicationResource

deleteAllVersionsOfAnApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllVersionsOfAnApplication(String shortName) throws KubernetesResourceException
   :outertype: ApplicationResource

deleteApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteApplication(String shortName, String version) throws KubernetesResourceException
   :outertype: ApplicationResource

deleteServiceFromApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteServiceFromApplication(String shortName, String version, String serviceShortName)
   :outertype: ApplicationResource

getAllApplications
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoApplicationWithServicesDTO>>> getAllApplications()
   :outertype: ApplicationResource

getApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplicationWithServicesDTO>> getApplicationByShortNameAndVersion(String shortName, String version)
   :outertype: ApplicationResource

getApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoApplicationWithServicesDTO>>> getApplicationsByShortName(String shortName)
   :outertype: ApplicationResource

getServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceDeploymentInfoDTO>> getServiceDeploymentInformation(String shortName, String version, String serviceShortName)
   :outertype: ApplicationResource

getServicesFromApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getServicesFromApplication(String shortName, String version)
   :outertype: ApplicationResource

   Returns a list of services associated with the mico application specified by the parameters.

   :param shortName: the name of the application
   :param version: the version of the application
   :return: the list of mico services that are associated with the application

getStatusOfApplication
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplicationStatusDTO>> getStatusOfApplication(String shortName, String version)
   :outertype: ApplicationResource

promoteApplication
^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoApplicationDTO>> promoteApplication(String shortName, String version, String newVersion)
   :outertype: ApplicationResource

updateApplication
^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<?> updateApplication(String shortName, String version, MicoApplicationDTO applicationDto)
   :outertype: ApplicationResource

updateServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoServiceDeploymentInfoDTO>> updateServiceDeploymentInformation(String shortName, String version, String serviceShortName, MicoServiceDeploymentInfoDTO serviceDeploymentInfoDTO)
   :outertype: ApplicationResource

