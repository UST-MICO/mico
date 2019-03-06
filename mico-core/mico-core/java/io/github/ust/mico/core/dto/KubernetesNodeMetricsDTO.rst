.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: io.fabric8.kubernetes.api.model Node

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

KubernetesNodeMetricsDTO
========================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties public class KubernetesNodeMetricsDTO

   DTO for the average CPU load and the average memory usage of all \ :java:ref:`Pods <Pod>`\  running on a Kubernetes \ :java:ref:`Node`\ .

