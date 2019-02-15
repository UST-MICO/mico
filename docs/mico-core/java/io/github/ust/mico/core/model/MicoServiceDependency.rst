.. java:import:: org.neo4j.ogm.annotation EndNode

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation RelationshipEntity

.. java:import:: org.neo4j.ogm.annotation StartNode

.. java:import:: com.fasterxml.jackson.annotation JsonIdentityInfo

.. java:import:: com.fasterxml.jackson.annotation JsonIgnore

.. java:import:: com.fasterxml.jackson.annotation ObjectIdGenerators

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceDependency
=====================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @RelationshipEntity public class MicoServiceDependency

   Represents a dependency of a \ :java:ref:`MicoService`\ .

Methods
-------
getMaxMicoVersion
^^^^^^^^^^^^^^^^^

.. java:method:: @JsonIgnore public MicoVersion getMaxMicoVersion() throws VersionNotSupportedException
   :outertype: MicoServiceDependency

getMinMicoVersion
^^^^^^^^^^^^^^^^^

.. java:method:: @JsonIgnore public MicoVersion getMinMicoVersion() throws VersionNotSupportedException
   :outertype: MicoServiceDependency

