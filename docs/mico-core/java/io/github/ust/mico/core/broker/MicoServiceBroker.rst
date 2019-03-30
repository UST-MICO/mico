.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.web.server ResponseStatusException

.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDependencyGraphEdgeResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDependencyGraphResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoServiceStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.service MicoStatusService

.. java:import:: lombok.extern.slf4j Slf4j

MicoServiceBroker
=================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class MicoServiceBroker

Methods
-------
checkIfDependencyAlreadyExists
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean checkIfDependencyAlreadyExists(MicoService service, MicoService serviceDependee)
   :outertype: MicoServiceBroker

deleteAllDependees
^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService deleteAllDependees(MicoService service)
   :outertype: MicoServiceBroker

deleteAllVersionsOfService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteAllVersionsOfService(String shortName) throws MicoServiceNotFoundException, KubernetesResourceException, MicoServiceIsDeployedException
   :outertype: MicoServiceBroker

deleteDependencyBetweenServices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService deleteDependencyBetweenServices(MicoService service, MicoService serviceToDelete)
   :outertype: MicoServiceBroker

deleteService
^^^^^^^^^^^^^

.. java:method:: public void deleteService(MicoService service) throws KubernetesResourceException, MicoServiceHasDependersException, MicoServiceIsDeployedException
   :outertype: MicoServiceBroker

findDependers
^^^^^^^^^^^^^

.. java:method:: public List<MicoService> findDependers(MicoService service)
   :outertype: MicoServiceBroker

getAllServicesAsList
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoService> getAllServicesAsList()
   :outertype: MicoServiceBroker

getAllVersionsOfServiceFromDatabase
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName) throws ResponseStatusException, MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getDependeesByMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependeesByMicoService(MicoService service)
   :outertype: MicoServiceBroker

getDependencyGraph
^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDependencyGraphResponseDTO getDependencyGraph(MicoService micoServiceRoot) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getDependentServices
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public LinkedList<MicoService> getDependentServices(List<MicoServiceDependency> dependees)
   :outertype: MicoServiceBroker

getDependers
^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependers(MicoService serviceToLookFor)
   :outertype: MicoServiceBroker

getServiceById
^^^^^^^^^^^^^^

.. java:method:: public MicoService getServiceById(Long id) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getServiceFromDatabase
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService getServiceFromDatabase(String shortName, String version) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getServiceYamlByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public String getServiceYamlByShortNameAndVersion(String shortName, String version) throws MicoServiceNotFoundException, JsonProcessingException, KubernetesResourceException
   :outertype: MicoServiceBroker

   Return yaml for a \ :java:ref:`MicoService`\  for the give shortName and version.

   :param shortName: the short name of the \ :java:ref:`MicoService`\ .
   :param version: version the version of the \ :java:ref:`MicoService`\ .
   :return: the kubernetes YAML for the \ :java:ref:`MicoService`\ .

getStatusOfService
^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceStatusResponseDTO getStatusOfService(String shortName, String version) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

persistNewDependencyBetweenServices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService persistNewDependencyBetweenServices(MicoService service, MicoService serviceDependee)
   :outertype: MicoServiceBroker

persistService
^^^^^^^^^^^^^^

.. java:method:: public MicoService persistService(MicoService newService) throws MicoServiceAlreadyExistsException
   :outertype: MicoServiceBroker

promoteService
^^^^^^^^^^^^^^

.. java:method:: public MicoService promoteService(MicoService service, String newVersion) throws MicoServiceAlreadyExistsException
   :outertype: MicoServiceBroker

updateExistingService
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService updateExistingService(MicoService service)
   :outertype: MicoServiceBroker

