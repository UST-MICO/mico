.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceRequestDTO

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoService
===========

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoService

   Represents a service in the context of MICO.

Methods
-------
getMicoVersion
^^^^^^^^^^^^^^

.. java:method:: public MicoVersion getMicoVersion() throws VersionNotSupportedException
   :outertype: MicoService

valueOf
^^^^^^^

.. java:method:: public static MicoService valueOf(MicoServiceRequestDTO serviceDto)
   :outertype: MicoService

   Creates a new \ ``MicoService``\  based on a \ ``MicoServiceRequestDTO``\ . Note that the id will be set to \ ``null``\ .

   :param serviceDto: the \ :java:ref:`MicoServiceRequestDTO`\ .
   :return: a \ :java:ref:`MicoService`\ .

