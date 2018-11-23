.. java:import:: org.springframework.data.neo4j.annotation Depth

.. java:import:: org.springframework.data.repository CrudRepository

ServiceRepository
=================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: public interface ServiceRepository extends CrudRepository<Service, Long>

Methods
-------
findByName
^^^^^^^^^^

.. java:method::  Service findByName(String name)
   :outertype: ServiceRepository

findByName
^^^^^^^^^^

.. java:method::  Service findByName(String name, int depth)
   :outertype: ServiceRepository

