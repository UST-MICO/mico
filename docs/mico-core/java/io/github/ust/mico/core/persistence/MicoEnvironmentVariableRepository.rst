.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable

MicoEnvironmentVariableRepository
=================================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoEnvironmentVariableRepository extends Neo4jRepository<MicoEnvironmentVariable, Long>

Methods
-------
cleanUp
^^^^^^^

.. java:method:: @Query  void cleanUp()
   :outertype: MicoEnvironmentVariableRepository

   Deletes all environment variables that do \ **not**\  have any relationship to another node.

