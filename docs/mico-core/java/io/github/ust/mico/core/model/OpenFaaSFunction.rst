.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: io.github.ust.mico.core.dto.request MicoEnvironmentVariableRequestDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

OpenFaaSFunction
================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class OpenFaaSFunction

   Represents an OpenFaaS function as used by the KafkaFaaSConnector

   Instances of this class are persisted as nodes in the Neo4j database.

