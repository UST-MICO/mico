.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: io.github.ust.mico.core.model KubernetesDeploymentInfo

KubernetesDeploymentInfoRepository
==================================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface KubernetesDeploymentInfoRepository extends Neo4jRepository<KubernetesDeploymentInfo, Long>

Methods
-------
cleanUp
^^^^^^^

.. java:method:: @Query  void cleanUp()
   :outertype: KubernetesDeploymentInfoRepository

   Deletes all \ :java:ref:`KubernetesDeploymentInfo`\  nodes that do \ **not**\  have any relationship to another node.

