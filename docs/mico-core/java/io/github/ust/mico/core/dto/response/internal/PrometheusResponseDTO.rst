.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: com.fasterxml.jackson.databind.annotation JsonDeserialize

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.util PrometheusValueDeserializer

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

PrometheusResponseDTO
=====================

.. java:package:: io.github.ust.mico.core.dto.response.internal
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class PrometheusResponseDTO

   Internal DTO for a response from Prometheus. It contains a status field and the value field for the CPU load / memory usage.

