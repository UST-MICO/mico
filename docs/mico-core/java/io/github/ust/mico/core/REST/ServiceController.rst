.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation DeleteMapping

.. java:import:: org.springframework.web.bind.annotation GetMapping

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation PostMapping

.. java:import:: org.springframework.web.bind.annotation PutMapping

.. java:import:: org.springframework.web.bind.annotation RequestBody

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

ServiceController
=================

.. java:package:: io.github.ust.mico.core.REST
   :noindex:

.. java:type:: @RestController @RequestMapping public class ServiceController

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

PATH_VARIABLE_ID
^^^^^^^^^^^^^^^^

.. java:field:: public static final String PATH_VARIABLE_ID
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

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoService>> createNewDependee(MicoServiceDependency newServiceDependee, String shortName, String version)
   :outertype: ServiceController

createService
^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoService>> createService(MicoService newService)
   :outertype: ServiceController

deleteAllDependees
^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoService>> deleteAllDependees(String shortName, String version)
   :outertype: ServiceController

deleteDependee
^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<MicoService>> deleteDependee(String shortName, String version, String shortNameToDelete, String versionToDelete)
   :outertype: ServiceController

deleteService
^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteService(String shortName, String version)
   :outertype: ServiceController

getDependees
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getDependees(String shortName, String version)
   :outertype: ServiceController

getDependers
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getDependers(String shortName, String version)
   :outertype: ServiceController

getDependers
^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependers(MicoService serviceToLookFor)
   :outertype: ServiceController

getService
^^^^^^^^^^

.. java:method:: public MicoService getService(MicoService newService)
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

getVersionsOfService
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoService>>> getVersionsOfService(String shortName)
   :outertype: ServiceController

setServiceDependees
^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService setServiceDependees(MicoService newService)
   :outertype: ServiceController

updateService
^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoService>> updateService(String shortName, String version, MicoService service)
   :outertype: ServiceController

