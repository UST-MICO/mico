.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerIngress

.. java:import:: io.fabric8.kubernetes.api.model LoadBalancerStatus

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

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

.. java:import:: java.util ArrayList

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.function Predicate

.. java:import:: java.util.stream Collectors

ServiceInterfaceController
==========================

.. java:package:: io.github.ust.mico.core.web
   :noindex:

.. java:type:: @Slf4j @RestController @RequestMapping public class ServiceInterfaceController

Methods
-------
createServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoServiceInterface>> createServiceInterface(String shortName, String version, MicoServiceInterface serviceInterface)
   :outertype: ServiceInterfaceController

   This is not transactional. At the moment we have only one user. If this changes transactional support is a must. FIXME Add transactional support

   :param shortName: the name of the MICO service
   :param version: the version of the MICO service
   :param serviceInterface: the name of the MICO service interface
   :return: the created MICO service interface

deleteServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteServiceInterface(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceController

getInterfaceByName
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceInterface>> getInterfaceByName(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceController

getInterfacePublicIpByName
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<List<String>> getInterfacePublicIpByName(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceController

getInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceInterface>>> getInterfacesOfService(String shortName, String version)
   :outertype: ServiceInterfaceController

updateMicoServiceInterface
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoServiceInterface>> updateMicoServiceInterface(String shortName, String version, String serviceInterfaceName, MicoServiceInterface modifiedMicoServiceInterface)
   :outertype: ServiceInterfaceController

   Updates an existing micoServiceInterface

   :param shortName:
   :param version:
   :param serviceInterfaceName:
   :param modifiedMicoServiceInterface:

