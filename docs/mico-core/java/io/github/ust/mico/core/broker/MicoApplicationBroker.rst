.. java:import:: io.github.ust.mico.core.configuration KafkaFaasConnectorConfig

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationDeploymentStatusResponseDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationStatusResponseDTO

.. java:import:: io.github.ust.mico.core.persistence MicoApplicationRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoServiceRepository

.. java:import:: io.github.ust.mico.core.resource ApplicationResource

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: io.github.ust.mico.core.service MicoStatusService

.. java:import:: io.github.ust.mico.core.util UIDUtils

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.hateoas Link

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

MicoApplicationBroker
=====================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class MicoApplicationBroker

Methods
-------
addKafkaFaasConnectorInstanceToMicoApplicationByVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo addKafkaFaasConnectorInstanceToMicoApplicationByVersion(String applicationShortName, String applicationVersion, String kfConnectorVersion) throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException, KafkaFaasConnectorVersionNotFoundException
   :outertype: MicoApplicationBroker

   Adds a new KafkaFaasConnector instance to the \ ``kafkaFaasConnectorDeploymentInfos``\  of the \ :java:ref:`MicoApplication`\ . An unique instance ID will be created that is returned as part of a \ :java:ref:`MicoServiceDeploymentInfo`\ .

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param kfConnectorVersion: the version of the KafkaFaasConnector (\ :java:ref:`MicoService`\
   :throws MicoApplicationNotFoundException: if the \ ``MicoApplication``\  does not exist
   :throws MicoApplicationIsNotUndeployedException: if the \ ``MicoApplication``\  is not undeployed
   :throws KafkaFaasConnectorVersionNotFoundException: if the version of the KafkaFaasConnector does not exist in MICO
   :return: the \ :java:ref:`MicoServiceDeploymentInfo`\  including the newly created instance ID

addMicoServiceToMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException, MicoServiceAddedMoreThanOnceToMicoApplicationException, MicoApplicationIsNotUndeployedException, MicoTopicRoleUsedMultipleTimesException, MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException, MicoApplicationDoesNotIncludeMicoServiceException, KafkaFaasConnectorNotAllowedHereException
   :outertype: MicoApplicationBroker

copyAndUpgradeMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication copyAndUpgradeMicoApplicationByShortNameAndVersion(String shortName, String version, String newVersion) throws MicoApplicationNotFoundException, MicoApplicationAlreadyExistsException
   :outertype: MicoApplicationBroker

createMicoApplication
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication createMicoApplication(MicoApplication micoApplication) throws MicoApplicationAlreadyExistsException
   :outertype: MicoApplicationBroker

deleteMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException
   :outertype: MicoApplicationBroker

deleteMicoApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void deleteMicoApplicationsByShortName(String shortName) throws MicoApplicationIsNotUndeployedException
   :outertype: MicoApplicationBroker

getApplicationDeploymentStatus
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationDeploymentStatus getApplicationDeploymentStatus(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

getApplicationStatus
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplicationStatusResponseDTO getApplicationStatus(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

getLinksOfMicoApplication
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Iterable<Link> getLinksOfMicoApplication(MicoApplication application)
   :outertype: MicoApplicationBroker

getMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication getMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

getMicoApplicationForMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  MicoApplication getMicoApplicationForMicoService(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException
   :outertype: MicoApplicationBroker

   Returns the \ :java:ref:`MicoApplication`\  for the provided short name and version if it exists and if it includes the \ :java:ref:`MicoService`\  with the provided short name.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\
   :throws MicoApplicationNotFoundException: if the \ ``MicoApplication``\  does not exist
   :throws MicoApplicationDoesNotIncludeMicoServiceException: if the \ ``MicoApplication``\  does not include the \ ``MicoService``\  with the provided short name
   :return: the \ :java:ref:`MicoApplication`\

getMicoApplications
^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoApplication> getMicoApplications()
   :outertype: MicoApplicationBroker

getMicoApplicationsByShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoApplication> getMicoApplicationsByShortName(String shortName)
   :outertype: MicoApplicationBroker

getMicoApplicationsUsingMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoApplication> getMicoApplicationsUsingMicoService(String serviceShortName, String serviceVersion)
   :outertype: MicoApplicationBroker

getMicoServicesOfMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException
   :outertype: MicoApplicationBroker

removeAllKafkaFaasConnectorInstancesFromMicoApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void removeAllKafkaFaasConnectorInstancesFromMicoApplication(String applicationShortName, String applicationVersion) throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException
   :outertype: MicoApplicationBroker

   Removes all KafkaFaasConnector instances from the \ ``kafkaFaasConnectorDeploymentInfos``\  of the \ :java:ref:`MicoApplication`\ .

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :throws MicoApplicationNotFoundException: if the \ ``MicoApplication``\  does not exist
   :throws MicoApplicationIsNotUndeployedException: if the \ ``MicoApplication``\  is not undeployed

removeKafkaFaasConnectorInstanceFromMicoApplicationByVersionAndInstanceId
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void removeKafkaFaasConnectorInstanceFromMicoApplicationByVersionAndInstanceId(String applicationShortName, String applicationVersion, String kfConnectorVersion, String instanceId) throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException, KafkaFaasConnectorInstanceNotFoundException, MicoApplicationDoesNotIncludeKFConnectorInstanceException
   :outertype: MicoApplicationBroker

   Removes a KafkaFaasConnector instance from the \ ``kafkaFaasConnectorDeploymentInfos``\  of the \ :java:ref:`MicoApplication`\ .

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param kfConnectorVersion: the version of the KafkaFaasConnector \ :java:ref:`MicoService`\
   :param instanceId: the instance ID of the \ :java:ref:`MicoServiceDeploymentInfo`\
   :throws MicoApplicationNotFoundException: if the \ ``MicoApplication``\  does not exist
   :throws MicoApplicationIsNotUndeployedException: if the \ ``MicoApplication``\  is not undeployed
   :throws KafkaFaasConnectorInstanceNotFoundException: if the instance of the KafkaFaasConnector does not exist in MICO
   :throws MicoApplicationDoesNotIncludeKFConnectorInstanceException: if the \ ``MicoApplication``\  does not include the KafkaFaasConnector deployment with the provided instance ID

removeKafkaFaasConnectorInstancesFromMicoApplicationByVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void removeKafkaFaasConnectorInstancesFromMicoApplicationByVersion(String applicationShortName, String applicationVersion, String kfConnectorVersion) throws MicoApplicationNotFoundException, MicoApplicationIsNotUndeployedException
   :outertype: MicoApplicationBroker

   Removes KafkaFaasConnector instances from the \ ``kafkaFaasConnectorDeploymentInfos``\  of the \ :java:ref:`MicoApplication`\  that are used in the requested version.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param kfConnectorVersion: the version of the KafkaFaasConnector \ :java:ref:`MicoService`\
   :throws MicoApplicationNotFoundException: if the \ ``MicoApplication``\  does not exist
   :throws MicoApplicationIsNotUndeployedException: if the \ ``MicoApplication``\  is not undeployed

removeMicoServiceFromMicoApplicationByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, MicoApplicationIsNotUndeployedException
   :outertype: MicoApplicationBroker

updateMicoApplication
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoApplication updateMicoApplication(String shortName, String version, MicoApplication micoApplication) throws MicoApplicationNotFoundException, ShortNameOfMicoApplicationDoesNotMatchException, VersionOfMicoApplicationDoesNotMatchException, MicoApplicationIsNotUndeployedException
   :outertype: MicoApplicationBroker

