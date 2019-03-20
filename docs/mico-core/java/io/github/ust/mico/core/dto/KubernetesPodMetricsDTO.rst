.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

KubernetesPodMetricsDTO
=======================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Data @AllArgsConstructor @NoArgsConstructor @Accessors @JsonIgnoreProperties public class KubernetesPodMetricsDTO

   Contains information about CPU/memory load of a \ :java:ref:`Pod`\ .

