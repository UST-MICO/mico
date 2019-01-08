.. java:import:: io.github.ust.mico.core DependsOn

.. java:import:: io.github.ust.mico.core Service

.. java:import:: io.github.ust.mico.core ServiceInterface

.. java:import:: io.github.ust.mico.core ServiceRepository

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

ServiceController
=================

.. java:package:: io.github.ust.mico.core.REST
   :noindex:

.. java:type:: @CrossOrigin @RestController @RequestMapping public class ServiceController

Fields
------
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

.. java:method:: @PostMapping public ResponseEntity<Resource<Service>> createNewDependee(Service newServiceDependee, String shortName, String version)
   :outertype: ServiceController

createService
^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<Service>> createService(Service newService)
   :outertype: ServiceController

deleteAllDependees
^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<Service>> deleteAllDependees(String shortName, String version)
   :outertype: ServiceController

deleteDependee
^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Resource<Service>> deleteDependee(String shortName, String version, String shortNameToDelete, String versionToDelete)
   :outertype: ServiceController

getDependees
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<Service>>> getDependees(String shortName, String version)
   :outertype: ServiceController

getDependers
^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<Service>>> getDependers(String shortName, String version)
   :outertype: ServiceController

getInterfaceByName
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<ServiceInterface>> getInterfaceByName(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceController

getInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<ServiceInterface>>> getInterfacesOfService(String shortName, String version)
   :outertype: ServiceController

getService
^^^^^^^^^^

.. java:method:: public Service getService(Service newService)
   :outertype: ServiceController

getServiceById
^^^^^^^^^^^^^^

.. java:method:: public ResponseEntity<Resource<Service>> getServiceById(Long id)
   :outertype: ServiceController

getServiceByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<Service>> getServiceByShortNameAndVersion(String shortName, String version)
   :outertype: ServiceController

getServiceList
^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<Service>>> getServiceList()
   :outertype: ServiceController

getVersionsOfService
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<Service>>> getVersionsOfService(String shortName)
   :outertype: ServiceController

setServiceDependees
^^^^^^^^^^^^^^^^^^^

.. java:method:: public Service setServiceDependees(Service newService)
   :outertype: ServiceController

