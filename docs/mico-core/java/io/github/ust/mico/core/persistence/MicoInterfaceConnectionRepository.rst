.. java:import:: io.github.ust.mico.core.model MicoInterfaceConnection

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

MicoInterfaceConnectionRepository
=================================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoInterfaceConnectionRepository extends Neo4jRepository<MicoInterfaceConnection, Long>

Methods
-------
cleanUp
^^^^^^^

.. java:method:: @Query  void cleanUp()
   :outertype: MicoInterfaceConnectionRepository

   Deletes all interface connections that do \ **not**\  have any relationship to another node.

