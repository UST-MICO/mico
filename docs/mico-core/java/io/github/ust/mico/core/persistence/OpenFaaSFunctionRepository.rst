.. java:import:: io.github.ust.mico.core.model OpenFaaSFunction

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: java.util Optional

OpenFaaSFunctionRepository
==========================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface OpenFaaSFunctionRepository extends Neo4jRepository<OpenFaaSFunction, Long>

Methods
-------
cleanUp
^^^^^^^

.. java:method:: @Query  void cleanUp()
   :outertype: OpenFaaSFunctionRepository

   Deletes all OpenFaaS functions that do \ **not**\  have any relationship to another node.

findByName
^^^^^^^^^^

.. java:method::  Optional<OpenFaaSFunction> findByName(String name)
   :outertype: OpenFaaSFunctionRepository

