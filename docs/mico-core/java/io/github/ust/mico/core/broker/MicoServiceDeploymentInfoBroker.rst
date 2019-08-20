.. java:import:: io.github.ust.mico.core.configuration KafkaConfig

.. java:import:: io.github.ust.mico.core.configuration OpenFaaSConfig

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.request MicoTopicRequestDTO

.. java:import:: io.github.ust.mico.core.service MicoKubernetesClient

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util.stream Collectors

MicoServiceDeploymentInfoBroker
===============================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class MicoServiceDeploymentInfoBroker

Methods
-------
getMicoServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo getMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException, MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException
   :outertype: MicoServiceDeploymentInfoBroker

   Returns the \ :java:ref:`MicoServiceDeploymentInfo`\  stored in the database.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\
   :throws MicoServiceDeploymentInformationNotFoundException: if there is no \ ``MicoServiceDeploymentInfo``\  stored in the database
   :throws MicoApplicationNotFoundException: if there is no \ ``MicoApplication``\  with the specified short name and version
   :throws MicoApplicationDoesNotIncludeMicoServiceException: if there is no service included in the specified \ ``MicoApplication``\  with the particular short name
   :return: the \ :java:ref:`MicoServiceDeploymentInfo`\  stored in the database

setDefaultDeploymentInformationForKafkaEnabledService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  void setDefaultDeploymentInformationForKafkaEnabledService(MicoServiceDeploymentInfo micoServiceDeploymentInfo)
   :outertype: MicoServiceDeploymentInfoBroker

   Sets the default environment variables for Kafka-enabled MicoServices. See \ :java:ref:`MicoEnvironmentVariable.DefaultNames`\  for a complete list.

   :param micoServiceDeploymentInfo: The \ :java:ref:`MicoServiceDeploymentInfo`\  with an corresponding MicoService

updateMicoServiceDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName, MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException, MicoTopicRoleUsedMultipleTimesException
   :outertype: MicoServiceDeploymentInfoBroker

   Updates an existing \ :java:ref:`MicoServiceDeploymentInfo`\  in the database based on the values of a \ :java:ref:`MicoServiceDeploymentInfoRequestDTO`\  object.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\
   :param serviceDeploymentInfoDTO: the \ :java:ref:`MicoServiceDeploymentInfoRequestDTO`\
   :throws MicoApplicationNotFoundException: if there is no \ ``MicoApplication``\  with the specified short name and version
   :throws MicoApplicationDoesNotIncludeMicoServiceException: if there is no service included in the specified \ ``MicoApplication``\  with the particular short name
   :throws MicoServiceDeploymentInformationNotFoundException: if there is no \ ``MicoServiceDeploymentInfo``\  stored in the database
   :throws KubernetesResourceException: if there are problems with retrieving Kubernetes resource information
   :throws MicoTopicRoleUsedMultipleTimesException: if a \ :java:ref:`MicoTopicRole`\  is used multiple times
   :return: the new \ :java:ref:`MicoServiceDeploymentInfo`\  stored in the database

