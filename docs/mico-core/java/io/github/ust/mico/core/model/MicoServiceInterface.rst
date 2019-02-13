.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints Pattern

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: org.springframework.data.neo4j.annotation QueryResult

.. java:import:: com.fasterxml.jackson.annotation JsonIgnore

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceInterface
====================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity @QueryResult public class MicoServiceInterface

   Represents a interface, e.g., REST API, of a \ :java:ref:`MicoService`\ .

