.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonPropertyOrder

.. java:import:: io.fabric8.kubernetes.api.model EnvVar

.. java:import:: java.util List

BuildStep
=========

.. java:package:: io.github.ust.mico.core.build
   :noindex:

.. java:type:: @JsonInclude @JsonIgnoreProperties @JsonPropertyOrder @Getter @Builder @NoArgsConstructor @AllArgsConstructor @ToString @EqualsAndHashCode public class BuildStep

   A single application container that you want to run within a pod.

