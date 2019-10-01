.. java:import:: java.net URI

.. java:import:: java.net URISyntaxException

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: io.github.ust.mico.core.exception MicoApplicationNotFoundException

.. java:import:: io.github.ust.mico.core.model MicoApplicationJobStatus

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http HttpHeaders

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: io.github.ust.mico.core.broker BackgroundJobBroker

.. java:import:: io.github.ust.mico.core.dto.response MicoApplicationJobStatusResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceBackgroundJobResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob

BackgroundJobResource
=====================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @RestController @RequestMapping public class BackgroundJobResource

Methods
-------
deleteAllJobs
^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteAllJobs()
   :outertype: BackgroundJobResource

deleteJob
^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteJob(String id)
   :outertype: BackgroundJobResource

getAllJobs
^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceBackgroundJobResponseDTO>>> getAllJobs()
   :outertype: BackgroundJobResource

getJobById
^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceBackgroundJobResponseDTO>> getJobById(String id)
   :outertype: BackgroundJobResource

getJobStatusByApplicationShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoApplicationJobStatusResponseDTO>> getJobStatusByApplicationShortNameAndVersion(String shortName, String version)
   :outertype: BackgroundJobResource

