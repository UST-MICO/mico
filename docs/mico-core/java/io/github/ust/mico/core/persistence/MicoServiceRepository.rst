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

findAll
^^^^^^^

.. java:method:: @Override  List<MicoService> findAll(int depth)
   :outertype: MicoServiceRepository

findAllByApplication
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoService> findAllByApplication(String applicationShortName, String applicationVersion)
   :outertype: MicoServiceRepository

findByShortName
^^^^^^^^^^^^^^^

.. java:method:: @Depth  List<MicoService> findByShortName(String shortName)
   :outertype: MicoServiceRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Depth  Optional<MicoService> findByShortNameAndVersion(String shortName, String version)
   :outertype: MicoServiceRepository

findInterfaceOfServiceByName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<MicoServiceInterface> findInterfaceOfServiceByName(String serviceInterfaceName, String shortName, String version)
   :outertype: MicoServiceRepository

   Find a specific service interface. The returned interface will NOT have ports mapped by the ogm. If you want to have a interface with mapped ports use the serviceInterface list in the corresponding MicoService object returned by findByShortNameAndVersion!

   :param serviceInterfaceName:
   :param shortName:
   :param version:

getAllDependeesOfMicoService
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoService> getAllDependeesOfMicoService(String shortName, String version)
   :outertype: MicoServiceRepository

