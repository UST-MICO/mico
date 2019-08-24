.. java:import:: io.github.ust.mico.core.model KFConnectorDeploymentInfo

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: java.util Optional

KFConnectorDeploymentInfoRepository
===================================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface KFConnectorDeploymentInfoRepository extends Neo4jRepository<KFConnectorDeploymentInfo, Long>

Methods
-------
findByInstanceId
^^^^^^^^^^^^^^^^

.. java:method::  Optional<KFConnectorDeploymentInfo> findByInstanceId(String instanceId)
   :outertype: KFConnectorDeploymentInfoRepository

