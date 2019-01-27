.. java:import:: java.util List

.. java:import:: com.fasterxml.jackson.annotation JsonIgnore

.. java:import:: io.github.ust.mico.core VersionNotSupportedException

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: io.swagger.annotations ApiModelProperty

MicoApplication
===============

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Builder @NodeEntity public class MicoApplication

   Represents an application as a set of \ :java:ref:`MicoService`\ s in the context of MICO.

Methods
-------
getMicoVersion
^^^^^^^^^^^^^^

.. java:method:: @JsonIgnore public MicoVersion getMicoVersion() throws VersionNotSupportedException
   :outertype: MicoApplication

