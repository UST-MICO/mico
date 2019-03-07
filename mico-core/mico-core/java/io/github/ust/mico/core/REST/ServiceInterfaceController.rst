.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.hateoas MediaTypes

.. java:import:: org.springframework.hateoas Resource

.. java:import:: org.springframework.hateoas Resources

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.http ResponseEntity

.. java:import:: org.springframework.web.bind.annotation DeleteMapping

.. java:import:: org.springframework.web.bind.annotation GetMapping

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation PostMapping

.. java:import:: org.springframework.web.bind.annotation RequestBody

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RestController

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

ServiceInterfaceController
==========================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @RestController @RequestMapping public class ServiceInterfaceController

Fields
------
SERVICE_INTERFACE_PATH
^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String SERVICE_INTERFACE_PATH
   :outertype: ServiceInterfaceController

Methods
-------
createServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoServiceInterface>> createServiceInterface(String shortName, String version, MicoServiceInterface serviceInterface)
   :outertype: ServiceInterfaceController

   This is not transactional. At the moment we have only one user. If this changes transactional support is a must. FIXME Add transactional support

   :param shortName:
   :param version:
   :param serviceInterface:

deleteServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping  void deleteServiceInterface(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceController

getInterfaceByName
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceInterface>> getInterfaceByName(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceController

getInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceInterface>>> getInterfacesOfService(String shortName, String version)
   :outertype: ServiceInterfaceController

