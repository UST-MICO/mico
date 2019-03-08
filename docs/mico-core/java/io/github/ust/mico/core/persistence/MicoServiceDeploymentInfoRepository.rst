.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfoQueryResult

MicoServiceDeploymentInfoRepository
===================================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoServiceDeploymentInfoRepository extends Neo4jRepository<MicoServiceDeploymentInfo, Long>

Methods
-------
findAllByApplication
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceDeploymentInfoQueryResult> findAllByApplication(String applicationShortName, String applicationVersion)
   :outertype: MicoServiceDeploymentInfoRepository

findByApplicationAndService
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<MicoServiceDeploymentInfoQueryResult> findByApplicationAndService(String applicationShortName, String applicationVersion, String serviceShortName)
   :outertype: MicoServiceDeploymentInfoRepository

findByApplicationAndService
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<MicoServiceDeploymentInfoQueryResult> findByApplicationAndService(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion)
   :outertype: MicoServiceDeploymentInfoRepository

