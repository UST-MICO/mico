.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: org.springframework.data.neo4j.annotation Depth

.. java:import:: org.springframework.data.neo4j.annotation Query

.. java:import:: org.springframework.data.neo4j.repository Neo4jRepository

.. java:import:: org.springframework.data.repository.query Param

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

findAll
^^^^^^^

.. java:method:: @Override  List<MicoApplication> findAll(int depth)
   :outertype: MicoApplicationRepository

findAllByUsedService
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoApplication> findAllByUsedService(String shortName, String version)
   :outertype: MicoApplicationRepository

   Find all applications that are using the given service.

   :param shortName: the shortName of the \ :java:ref:`MicoService`\
   :param version: the version of the \ :java:ref:`MicoService`\
   :return: a list of \ :java:ref:`MicoApplication`\

findAllByUsedServiceInstance
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  List<MicoApplication> findAllByUsedServiceInstance(String instanceId)
   :outertype: MicoApplicationRepository

   Find all applications that are using the given service.

   :param instanceId: the instance ID of the \ :java:ref:`MicoServiceDeploymentInfo`\
   :return: a list of \ :java:ref:`MicoApplication`\

findByShortName
^^^^^^^^^^^^^^^

.. java:method:: @Depth  List<MicoApplication> findByShortName(String shortName)
   :outertype: MicoApplicationRepository

findByShortNameAndVersion
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Depth  Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version)
   :outertype: MicoApplicationRepository

