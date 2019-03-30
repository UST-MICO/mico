.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoYamlResponseDTO
===================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoYamlResponseDTO

Fields
------
yaml
^^^^

.. java:field:: @ApiModelProperty @JsonProperty  String yaml
   :outertype: MicoYamlResponseDTO

   Yaml from Kubernetes resource

