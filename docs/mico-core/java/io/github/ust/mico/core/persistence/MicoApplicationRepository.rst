.. java:import:: org.springframework.data.neo4j.annotation Depth

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: java.util List

.. java:import:: java.util Optional

MicoApplicationRepository
=========================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoApplicationRepository extends Neo4jRepository<MicoApplication, Long>

Methods
-------
findAll
^^^^^^^

.. java:method:: @Override  List<MicoApplication> findAll()
   :outertype: MicoApplicationRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version, int depth)
   :outertype: MicoApplicationRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version)
   :outertype: MicoApplicationRepository

