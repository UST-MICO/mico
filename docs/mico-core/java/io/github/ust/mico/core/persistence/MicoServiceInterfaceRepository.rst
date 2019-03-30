.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

MicoServiceInterfaceRepository
==============================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoServiceInterfaceRepository extends Neo4jRepository<MicoServiceInterface, Long>

Methods
-------
deleteByServiceAndName
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  void deleteByServiceAndName(String shortName, String version, String serviceInterfaceName)
   :outertype: MicoServiceInterfaceRepository

findByService
^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoServiceInterface> findByService(String shortName, String version)
   :outertype: MicoServiceInterfaceRepository

findByServiceAndName
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<MicoServiceInterface> findByServiceAndName(String shortName, String version, String serviceInterfaceName)
   :outertype: MicoServiceInterfaceRepository

