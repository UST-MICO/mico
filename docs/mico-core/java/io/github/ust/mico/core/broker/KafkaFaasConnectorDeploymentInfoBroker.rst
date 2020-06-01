.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: com.google.common.base Strings

.. java:import:: io.github.ust.mico.core.dto.request KFConnectorDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.exception KafkaFaasConnectorInstanceNotFoundException

.. java:import:: io.github.ust.mico.core.exception MicoApplicationNotFoundException

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoTopic

.. java:import:: io.github.ust.mico.core.model MicoTopicRole

.. java:import:: io.github.ust.mico.core.model OpenFaaSFunction

.. java:import:: io.github.ust.mico.core.persistence MicoServiceDeploymentInfoRepository

.. java:import:: io.github.ust.mico.core.persistence MicoTopicRepository

.. java:import:: io.github.ust.mico.core.persistence OpenFaaSFunctionRepository

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

KafkaFaasConnectorDeploymentInfoBroker
======================================

.. java:package:: io.github.ust.mico.core.broker
   :noindex:

.. java:type:: @Slf4j @Service public class KafkaFaasConnectorDeploymentInfoBroker

Methods
-------
getKafkaFaasConnectorDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<MicoServiceDeploymentInfo> getKafkaFaasConnectorDeploymentInformation(String micoApplicationShortName, String micoApplicationVersion) throws MicoApplicationNotFoundException
   :outertype: KafkaFaasConnectorDeploymentInfoBroker

   Fetches a list of \ :java:ref:`MicoServiceDeploymentInfos <MicoServiceDeploymentInfo>`\  of all KafkaFaasConnector instances associated with the specified \ :java:ref:`MicoApplication`\ .

   :param micoApplicationShortName: the shortName of the micoApplication
   :param micoApplicationVersion: the version of the micoApplication
   :throws MicoApplicationNotFoundException: if there is no such micoApplication
   :return: the list of \ :java:ref:`MicoServiceDeploymentInfos <MicoServiceDeploymentInfo>`\

getKafkaFaasConnectorDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<MicoServiceDeploymentInfo> getKafkaFaasConnectorDeploymentInformation(String micoApplicationShortName, String micoApplicationVersion, String instanceId) throws MicoApplicationNotFoundException
   :outertype: KafkaFaasConnectorDeploymentInfoBroker

   Filters the list of \ :java:ref:`MicoServiceDeploymentInfo`\  from \ :java:ref:`KafkaFaasConnectorDeploymentInfoBroker.getKafkaFaasConnectorDeploymentInformation(String,String)`\  for a specific \ ``instanceId``\ .

   :param micoApplicationShortName: the short name of the \ :java:ref:`MicoApplication`\
   :param micoApplicationVersion: the version of the \ :java:ref:`MicoApplication`\
   :param instanceId: the instance ID of the \ :java:ref:`MicoServiceDeploymentInfo`\
   :throws MicoApplicationNotFoundException: if the \ :java:ref:`MicoApplication`\  does not exist.
   :return: a single \ :java:ref:`MicoServiceDeploymentInfo`\  with an instance ID equal to the give one.

updateKafkaFaasConnectorDeploymentInformation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo updateKafkaFaasConnectorDeploymentInformation(String instanceId, KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO) throws KafkaFaasConnectorInstanceNotFoundException
   :outertype: KafkaFaasConnectorDeploymentInfoBroker

   Updates an existing \ :java:ref:`MicoServiceDeploymentInfo`\  in the database based on the values of a \ :java:ref:`KFConnectorDeploymentInfoRequestDTO`\  object.

   :param instanceId: the instance ID of the \ :java:ref:`MicoServiceDeploymentInfo`\
   :param kfConnectorDeploymentInfoRequestDTO: the \ :java:ref:`KFConnectorDeploymentInfoRequestDTO`\
   :throws KafkaFaasConnectorInstanceNotFoundException: if there is no \ ``MicoServiceDeploymentInfo``\  for the requested \ ``instanceId``\  stored in the database
   :return: the new \ :java:ref:`MicoServiceDeploymentInfo`\  stored in the database

