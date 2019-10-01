.. java:import:: io.github.ust.mico.core.model MicoTopic

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: java.util List

.. java:import:: java.util Optional

MicoTopicRepository
===================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoTopicRepository extends Neo4jRepository<MicoTopic, Long>

Methods
-------
cleanUp
^^^^^^^

.. java:method:: @Query  void cleanUp()
   :outertype: MicoTopicRepository

   Deletes all topics that do \ **not**\  have any relationship to another node.

findAllByName
^^^^^^^^^^^^^

.. java:method::  List<MicoTopic> findAllByName(String name)
   :outertype: MicoTopicRepository

findByName
^^^^^^^^^^

.. java:method::  Optional<MicoTopic> findByName(String name)
   :outertype: MicoTopicRepository

