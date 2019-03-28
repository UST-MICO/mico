.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: io.github.ust.mico.core.model MicoLabel

MicoLabelRepository
===================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoLabelRepository extends Neo4jRepository<MicoLabel, Long>

Methods
-------
cleanUp
^^^^^^^

.. java:method:: @Query  void cleanUp()
   :outertype: MicoLabelRepository

   Deletes all labels that do \ **not**\  have any relationship to another node.

