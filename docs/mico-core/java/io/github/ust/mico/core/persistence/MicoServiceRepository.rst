.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: org.springframework.data.neo4j.annotation Depth

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

MicoServiceRepository
=====================

.. java:package:: io.github.ust.mico.core.persistence
   :noindex:

.. java:type:: public interface MicoServiceRepository extends Neo4jRepository<MicoService, Long>

Methods
-------
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

   Finds all services that are included by a given application.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :return: a list of \ :java:ref:`MicoServices <MicoService>`\ .

findAllByApplicationAndServiceShortName
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<MicoService> findAllByApplicationAndServiceShortName(String applicationShortName, String applicationVersion, String serviceShortName)
   :outertype: MicoServiceRepository

   Finds the service that is included in a given application for a given service short name.

   :param applicationShortName: the short name of the \ :java:ref:`MicoApplication`\ .
   :param applicationVersion: the version of the \ :java:ref:`MicoApplication`\ .
   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :return: a list of \ :java:ref:`MicoServices <MicoService>`\ .

findByShortName
^^^^^^^^^^^^^^^

.. java:method:: @Depth  List<MicoService> findByShortName(String shortName)
   :outertype: MicoServiceRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Depth  Optional<MicoService> findByShortNameAndVersion(String shortName, String version)
   :outertype: MicoServiceRepository

findDependees
^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoService> findDependees(String shortName, String version)
   :outertype: MicoServiceRepository

   Finds all services (dependees) the given service (depender) depends on.

   :param shortName: the short name of the \ :java:ref:`MicoService`\  (depender).
   :param version: the version of the \ :java:ref:`MicoService`\  (depender).
   :return: a list of \ :java:ref:`MicoServices <MicoService>`\ .

findDependeesIncludeDepender
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoService> findDependeesIncludeDepender(String shortName, String version)
   :outertype: MicoServiceRepository

   Finds all services (dependees) the given service (depender) depends on as well as the service (depender) itself.

   :param shortName: the short name of the \ :java:ref:`MicoService`\  (depender).
   :param version: the version of the \ :java:ref:`MicoService`\  (depender).
   :return: a list of \ :java:ref:`MicoServices <MicoService>`\  including all dependees as well as the depender.

findDependers
^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoService> findDependers(String shortName, String version)
   :outertype: MicoServiceRepository

   Finds all services (dependers) that depend on the given service (dependee).

   :param shortName: the short name of the \ :java:ref:`MicoService`\  (dependee).
   :param version: the version of the \ :java:ref:`MicoService`\  (dependee).
   :return: a list of \ :java:ref:`MicoServices <MicoService>`\ .

