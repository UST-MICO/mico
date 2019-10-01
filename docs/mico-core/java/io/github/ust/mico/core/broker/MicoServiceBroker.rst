.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.google.common.collect Iterables

.. java:import:: io.github.ust.mico.core.configuration KafkaFaasConnectorConfig

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDependencyGraphEdgeResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDependencyGraphResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

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

.. java:method:: public MicoService deleteAllDependees(MicoService service) throws MicoServiceIsDeployedException
   :outertype: MicoServiceBroker

deleteAllVersionsOfService
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteAllVersionsOfService(String shortName) throws MicoServiceIsDeployedException, MicoServiceIsUsedByMicoApplicationsException
   :outertype: MicoServiceBroker

deleteDependencyBetweenServices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService deleteDependencyBetweenServices(MicoService service, MicoService serviceToDelete) throws MicoServiceIsDeployedException
   :outertype: MicoServiceBroker

deleteService
^^^^^^^^^^^^^

.. java:method:: public void deleteService(MicoService service) throws MicoServiceHasDependersException, MicoServiceIsDeployedException, MicoServiceIsUsedByMicoApplicationsException
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

.. java:method:: public List<MicoService> getAllVersionsOfServiceFromDatabase(String shortName)
   :outertype: MicoServiceBroker

getDependeesByMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependeesByMicoService(MicoService service)
   :outertype: MicoServiceBroker

getDependencyGraph
^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDependencyGraphResponseDTO getDependencyGraph(MicoService micoServiceRoot) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getDependers
^^^^^^^^^^^^

.. java:method:: public List<MicoService> getDependers(MicoService serviceToLookFor)
   :outertype: MicoServiceBroker

getLatestKFConnectorVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public String getLatestKFConnectorVersion() throws KafkaFaasConnectorLatestVersionNotFound
   :outertype: MicoServiceBroker

   Returns the latest version of the KafkaFaaSConnector (according to the database)

   :throws KafkaFaasConnectorLatestVersionNotFound: if no KafkaFaasConnector can be found
   :return: the latest version of the KafkaFaaSConnector

getServiceById
^^^^^^^^^^^^^^

.. java:method:: public MicoService getServiceById(Long id) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getServiceFromDatabase
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService getServiceFromDatabase(String shortName, String version) throws MicoServiceNotFoundException
   :outertype: MicoServiceBroker

getServiceInstanceFromDatabase
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo getServiceInstanceFromDatabase(String shortName, String version, String instanceId) throws MicoServiceInstanceNotFoundException, MicoServiceInstanceDoesNotMatchShortNameAndVersionException
   :outertype: MicoServiceBroker

getServiceYamlByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public String getServiceYamlByShortNameAndVersion(String shortName, String version) throws MicoServiceNotFoundException, JsonProcessingException
   :outertype: MicoServiceBroker

   Return yaml for a \ :java:ref:`MicoService`\  for the give shortName and version.

   :param shortName: the short name of the \ :java:ref:`MicoService`\ .
   :param version: version the version of the \ :java:ref:`MicoService`\ .
   :return: the kubernetes YAML for the \ :java:ref:`MicoService`\ .

persistNewDependencyBetweenServices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoService persistNewDependencyBetweenServices(MicoService service, MicoService serviceDependee) throws MicoServiceIsDeployedException
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

.. java:method:: public MicoService updateExistingService(MicoService service) throws MicoServiceIsDeployedException
   :outertype: MicoServiceBroker

