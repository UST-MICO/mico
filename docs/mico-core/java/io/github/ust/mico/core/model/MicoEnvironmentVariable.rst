.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: io.github.ust.mico.core.dto.request MicoEnvironmentVariableRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

MicoEnvironmentVariable
=======================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties @NodeEntity public class MicoEnvironmentVariable

   An environment variable represented as a simple key-value pair. Necessary since Neo4j does not allow to persist properties of composite types.

   Instances of this class are persisted as nodes in the Neo4j database.

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoEnvironmentVariable valueOf(MicoEnvironmentVariableRequestDTO environmentVariableDto)
   :outertype: MicoEnvironmentVariable

   Creates a new \ ``MicoEnvironmentVariable``\  based on a \ ``MicoEnvironmentVariableRequestDTO``\ . Note that the id will be set to \ ``null``\ .

   :param environmentVariableDto: the \ :java:ref:`MicoEnvironmentVariableRequestDTO`\ .
   :return: a \ :java:ref:`MicoEnvironmentVariable`\ .

