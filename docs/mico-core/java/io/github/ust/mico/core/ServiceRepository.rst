.. java:import:: org.springframework.data.neo4j.annotation Depth

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

.. java:import:: java.util List

.. java:import:: java.util Optional

ServiceRepository
=================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: public interface ServiceRepository extends Neo4jRepository<Service, Long>

Methods
-------
findAll
^^^^^^^

.. java:method:: @Override  List<Service> findAll()
   :outertype: ServiceRepository

findByName
^^^^^^^^^^

.. java:method::  List<Service> findByName(String name)
   :outertype: ServiceRepository

findByName
^^^^^^^^^^

.. java:method::  List<Service> findByName(String name, int depth)
   :outertype: ServiceRepository

findByShortName
^^^^^^^^^^^^^^^

.. java:method::  List<Service> findByShortName(String shortName)
   :outertype: ServiceRepository

findByShortName
^^^^^^^^^^^^^^^

.. java:method::  List<Service> findByShortName(String shortName, int depth)
   :outertype: ServiceRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<Service> findByShortNameAndVersion(String shortName, String version, int depth)
   :outertype: ServiceRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<Service> findByShortNameAndVersion(String shortName, String version)
   :outertype: ServiceRepository

findInterfaceOfServiceByName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<ServiceInterface> findInterfaceOfServiceByName(String serviceInterfaceName, String shortName, String version)
   :outertype: ServiceRepository

findInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<ServiceInterface> findInterfacesOfService(String shortName, String version)
   :outertype: ServiceRepository

