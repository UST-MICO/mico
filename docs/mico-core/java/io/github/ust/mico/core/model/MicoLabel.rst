.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: io.github.ust.mico.core.dto.request MicoLabelRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoLabel
=========

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoLabel

   A label represented as a simple key-value pair. Necessary since Neo4j does not allow to persist properties of composite types.

   Instances of this class are persisted as nodes in the Neo4j database.

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoLabel valueOf(MicoLabelRequestDTO labelDto)
   :outertype: MicoLabel

   Creates a new \ ``MicoLabel``\  based on a \ ``MicoLabelRequestDTO``\ . Note that the id will be set to \ ``null``\ .

   :param labelDto: the \ :java:ref:`MicoLabelRequestDTO`\ .
   :return: a \ :java:ref:`MicoLabel`\ .

