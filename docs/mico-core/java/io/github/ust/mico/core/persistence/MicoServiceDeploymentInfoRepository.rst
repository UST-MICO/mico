.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

.. java:import:: java.util List

.. java:import:: java.util Optional

MicoServiceDeploymentInfoRepository
===================================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoServiceDeploymentInfoRepository extends Neo4jRepository<MicoServiceDeploymentInfo, Long>

Methods
-------
deleteAllByApplication
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteAllByApplication(String applicationShortName)
   :outertype: MicoServiceDeploymentInfoRepository

   Deletes all deployment information for all versions of an application including the ones for the KafkaFaasConnectors. All additional properties of a \ :java:ref:`MicoServiceDeploymentInfo`\  that are stored as a separate node entity and connected to it via a \ ``[:HAS]``\  relationship will be deleted too, if they are used exclusively by this deployment information. Exclusively means that there must only be one single edge connected to the particular property (\ ``relatedNode``\ , see \ ``size``\  operator in \ ``WHERE``\  clause). If that's the case, it's possible to delete this related node safely.

   Also works with a KafkaFaasConnector instance.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .

deleteAllByApplication
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteAllByApplication(String applicationShortName, String applicationVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Deletes all deployment information for a particular application including the ones for the KafkaFaasConnectors. All additional properties of a \ :java:ref:`MicoServiceDeploymentInfo`\  that are stored as a separate node entity and connected to it via a \ ``[:HAS]``\  relationship will be deleted too, if they are used exclusively by this deployment information. Exclusively means that there must only be one single edge connected to the particular property (\ ``relatedNode``\ , see \ ``size``\  operator in \ ``WHERE``\  clause). If that's the case, it's possible to delete this related node safely.

   Also works with a KafkaFaasConnector instance.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .

deleteByApplicationAndService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteByApplicationAndService(String applicationShortName, String applicationVersion, String serviceShortName)
   :outertype: MicoServiceDeploymentInfoRepository

   Deletes the deployment information for a particular application and service. All additional properties of a \ :java:ref:`MicoServiceDeploymentInfo`\  that are stored as a separate node entity and connected to it via a \ ``[:HAS]``\  relationship will be deleted too, if they are used exclusively by this deployment information. Exclusively means that there must only be one single edge connected to the particular property (\ ``relatedNode``\ , see \ ``size``\  operator in \ ``WHERE``\  clause). If that's the case, it's possible to delete this related node safely.

   Also works with a KafkaFaasConnector instance.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .

deleteByApplicationAndService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteByApplicationAndService(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Deletes the deployment information for a particular application and service. All additional properties of a \ :java:ref:`MicoServiceDeploymentInfo`\  that are stored as a separate node entity and connected to it via a \ ``[:HAS]``\  relationship will be deleted too, if they are used exclusively by this deployment information. Exclusively means that there must only be one single edge connected to the particular property (\ ``relatedNode``\ , see \ ``size``\  operator in \ ``WHERE``\  clause). If that's the case, it's possible to delete this related node safely.

   Also works with a KafkaFaasConnector instance.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :param serviceVersion: the version of the \ :java:ref:`MicoService`\ .

findAllByApplication
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfo> findAllByApplication(String applicationShortName, String applicationVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Retrieves all service deployment information of a particular application. Also includes these which are used for the deployments of KafkaFaasConnector instances.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :return: a \ :java:ref:`List`\  of \ :java:ref:`MicoServiceDeploymentInfo`\  instances.

findAllByService
^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfo> findAllByService(String serviceShortName, String serviceVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Retrieves all service deployment information of a service. Note that one service can be used by (included in) multiple applications. Also works with a KafkaFaasConnector instance.

   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :param serviceVersion: the version of the \ :java:ref:`MicoService`\ .
   :return: a \ :java:ref:`List`\  of \ :java:ref:`MicoServiceDeploymentInfo`\  instances.

findByApplicationAndService
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfo> findByApplicationAndService(String applicationShortName, String applicationVersion, String serviceShortName)
   :outertype: MicoServiceDeploymentInfoRepository

   Retrieves the deployment information for a particular application and service. Also works with a KafkaFaasConnector instance.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :return: an \ :java:ref:`Optional`\  of \ :java:ref:`MicoServiceDeploymentInfo`\ .

findByApplicationAndService
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfo> findByApplicationAndService(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Retrieves the deployment information for a particular application and service. Also works with a KafkaFaasConnector instance.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :param serviceVersion: the version of the \ :java:ref:`MicoService`\ .
   :return: an \ :java:ref:`Optional`\  of \ :java:ref:`MicoServiceDeploymentInfo`\ .

findByInstanceId
^^^^^^^^^^^^^^^^

.. java:method::  Optional<MicoServiceDeploymentInfo> findByInstanceId(String instanceId)
   :outertype: MicoServiceDeploymentInfoRepository

findKFConnectorSDIsByApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfo> findKFConnectorSDIsByApplication(String applicationShortName, String applicationVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Retrieves all service deployment information that are used for KafkaFaasConnectors of a particular application.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :return: a \ :java:ref:`List`\  of \ :java:ref:`MicoServiceDeploymentInfo`\  instances.

findMicoServiceSDIsByApplication
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfo> findMicoServiceSDIsByApplication(String applicationShortName, String applicationVersion)
   :outertype: MicoServiceDeploymentInfoRepository

   Retrieves all service deployment information that are used for normal MicoServices of a particular application.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :return: a \ :java:ref:`List`\  of \ :java:ref:`MicoServiceDeploymentInfo`\  instances.

