.. java:import:: io.github.ust.mico.core.broker MicoServiceBroker

.. java:import:: io.github.ust.mico.core.broker MicoServiceInterfaceBroker

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceInterfaceRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceInterfaceResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoServiceInterfaceStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.service MicoStatusService

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

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

ServiceInterfaceResource
========================

.. java:package:: io.github.ust.mico.core.resource
   :noindex:

.. java:type:: @RestController @RequestMapping public class ServiceInterfaceResource

Methods
-------
createServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PostMapping public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> createServiceInterface(String shortName, String version, MicoServiceInterfaceRequestDTO serviceInterfaceRequestDto)
   :outertype: ServiceInterfaceResource

   This is not transactional. At the moment we have only one user. If this changes transactional support is a must. FIXME Add transactional support

   :param shortName: the name of the MICO service
   :param version: the version of the MICO service
   :param serviceInterfaceRequestDto: the \ :java:ref:`MicoServiceInterfaceRequestDTO`\
   :return: the created MICO service interface

deleteServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @DeleteMapping public ResponseEntity<Void> deleteServiceInterface(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceResource

getInterfaceByName
^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> getInterfaceByName(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceResource

getInterfacePublicIpByName
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<MicoServiceInterfaceStatusResponseDTO> getInterfacePublicIpByName(String shortName, String version, String serviceInterfaceName)
   :outertype: ServiceInterfaceResource

getInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @GetMapping public ResponseEntity<Resources<Resource<MicoServiceInterfaceResponseDTO>>> getInterfacesOfService(String shortName, String version)
   :outertype: ServiceInterfaceResource

updateServiceInterface
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @PutMapping public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> updateServiceInterface(String shortName, String version, String serviceInterfaceName, MicoServiceInterfaceRequestDTO updatedServiceInterfaceRequestDto)
   :outertype: ServiceInterfaceResource

   Updates an existing MICO service interface.

   :param shortName: the name of a \ :java:ref:`MicoService`\
   :param version: the version a \ :java:ref:`MicoService`\
   :param serviceInterfaceName: the name of a \ :java:ref:`MicoServiceInterface`\
   :param updatedServiceInterfaceRequestDto: the \ :java:ref:`MicoServiceInterfaceRequestDTO`\
   :return: the updated \ :java:ref:`MicoServiceInterfaceResponseDTO`\

