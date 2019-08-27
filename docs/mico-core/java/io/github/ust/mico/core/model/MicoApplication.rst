.. java:import:: io.github.ust.mico.core.dto.request MicoApplicationRequestDTO

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: java.util ArrayList

.. java:import:: java.util List

MicoApplication
===============

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoApplication

   Represents an application as a set of \ :java:ref:`MicoService`\ s in the context of MICO.

   Instances of this class are persisted as nodes in the Neo4j database.

Methods
-------
getMicoVersion
^^^^^^^^^^^^^^

.. java:method:: public MicoVersion getMicoVersion() throws VersionNotSupportedException
   :outertype: MicoApplication

valueOf
^^^^^^^

.. java:method:: public static MicoApplication valueOf(MicoApplicationRequestDTO applicationDto)
   :outertype: MicoApplication

   Creates a new \ ``MicoApplication``\  based on a \ ``MicoApplicationRequestDTO``\ . Note that the id will be set to \ ``null``\ .

   :param applicationDto: the \ :java:ref:`MicoApplicationRequestDTO`\ .
   :return: a \ :java:ref:`MicoApplication`\ .

