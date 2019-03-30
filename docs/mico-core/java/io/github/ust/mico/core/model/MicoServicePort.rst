.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: io.github.ust.mico.core.dto.request MicoServicePortRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServicePort
===============

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoServicePort

   Represents a basic port with a port number and port type (protocol).

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoServicePort valueOf(MicoServicePortRequestDTO servicePortDto)
   :outertype: MicoServicePort

   Creates a new \ ``MicoServicePort``\  based on a \ ``MicoServicePortRequestDTO``\ . Note that the id will be set to \ ``null``\ .

   :param servicePortDto: the \ :java:ref:`MicoServicePortRequestDTO`\ .
   :return: a \ :java:ref:`MicoServicePort`\ .

