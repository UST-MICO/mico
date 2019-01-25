.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: org.springframework.data.neo4j.annotation Depth

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

MicoServiceRepository
=====================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoServiceRepository extends Neo4jRepository<MicoService, Long>

Methods
-------
deleteInterfaceOfServiceByName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteInterfaceOfServiceByName(String serviceInterfaceName, String shortName, String version)
   :outertype: MicoServiceRepository

deleteServiceByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteServiceByShortNameAndVersion(String shortName, String version)
   :outertype: MicoServiceRepository

findAll
^^^^^^^

.. java:method:: @Override  List<MicoService> findAll()
   :outertype: MicoServiceRepository

findByShortName
^^^^^^^^^^^^^^^

.. java:method::  List<MicoService> findByShortName(String shortName)
   :outertype: MicoServiceRepository

findByShortName
^^^^^^^^^^^^^^^

.. java:method::  List<MicoService> findByShortName(String shortName, int depth)
   :outertype: MicoServiceRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<MicoService> findByShortNameAndVersion(String shortName, String version)
   :outertype: MicoServiceRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<MicoService> findByShortNameAndVersion(String shortName, String version, int depth)
   :outertype: MicoServiceRepository

findInterfaceOfServiceByName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<MicoServiceInterface> findInterfaceOfServiceByName(String serviceInterfaceName, String shortName, String version)
   :outertype: MicoServiceRepository

findInterfacesOfService
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceInterface> findInterfacesOfService(String shortName, String version)
   :outertype: MicoServiceRepository

