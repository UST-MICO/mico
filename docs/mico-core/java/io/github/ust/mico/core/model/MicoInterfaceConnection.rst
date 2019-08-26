.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: io.github.ust.mico.core.dto.request MicoInterfaceConnectionRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoInterfaceConnection
=======================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties @NodeEntity public class MicoInterfaceConnection

   An interface connection contains the the information needed to connect a \ :java:ref:`MicoService`\  to an \ :java:ref:`MicoServiceInterface`\  of another \ :java:ref:`MicoService`\ .

   Instances of this class are persisted as nodes in the Neo4j database.

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoInterfaceConnection valueOf(MicoInterfaceConnectionRequestDTO interfaceConnectionDTO)
   :outertype: MicoInterfaceConnection

   Creates a new \ ``MicoInterfaceConnection``\  based on a \ ``MicoInterfaceConnectionRequestDTO``\ .

   :param interfaceConnectionDTO: the \ :java:ref:`MicoInterfaceConnectionRequestDTO`\ .
   :return: a \ :java:ref:`MicoInterfaceConnection`\ .

