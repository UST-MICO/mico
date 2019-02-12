.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: javax.validation.constraints Pattern

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: com.fasterxml.jackson.annotation JsonIgnore

.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoService
===========

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties @NodeEntity public class MicoService

   Represents a service in the context of MICO.

Methods
-------
getMicoVersion
^^^^^^^^^^^^^^

.. java:method:: @JsonIgnore public MicoVersion getMicoVersion() throws VersionNotSupportedException
   :outertype: MicoService

