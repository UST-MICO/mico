.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: io.fabric8.kubernetes.api.model EnvVar

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: java.util ArrayList

.. java:import:: java.util List

BuildStep
=========

.. java:package:: io.github.ust.mico.core.service.imagebuilder.knativebuild.buildtypes
   :noindex:

.. java:type:: @JsonInclude @JsonIgnoreProperties @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class BuildStep

   A single application container that you want to run within a pod.

