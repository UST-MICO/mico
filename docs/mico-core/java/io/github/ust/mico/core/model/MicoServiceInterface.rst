.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceInterfaceRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceInterface
====================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoServiceInterface

   Represents a interface, e.g., REST API, of a \ :java:ref:`MicoService`\ .

   Instances of this class are persisted as nodes in the Neo4j database.

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoServiceInterface valueOf(MicoServiceInterfaceRequestDTO serviceInterfaceDto)
   :outertype: MicoServiceInterface

   Creates a new \ ``MicoServiceInterface``\  based on a \ ``MicoServiceInterfaceRequestDTO``\ . Note that the id will be set to \ ``null``\ .

   :param serviceInterfaceDto: the \ :java:ref:`MicoServiceInterfaceRequestDTO`\ .
   :return: a \ :java:ref:`MicoServiceInterface`\ .

