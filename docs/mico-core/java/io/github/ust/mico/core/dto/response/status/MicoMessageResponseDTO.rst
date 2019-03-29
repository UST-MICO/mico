.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoMessage

.. java:import:: io.github.ust.mico.core.model MicoMessage.Type

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoMessageResponseDTO
======================

.. java:package:: io.github.ust.mico.core.dto.response.status
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonInclude public class MicoMessageResponseDTO

   DTO for a \ :java:ref:`MicoMessage`\  intended to use with responses only.

Constructors
------------
MicoMessageResponseDTO
^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoMessageResponseDTO(MicoMessage message)
   :outertype: MicoMessageResponseDTO

   Creates an instance of \ ``MicoMessageResponseDTO``\  based on a \ ``MicoMessage``\ .

   :param message: the \ :java:ref:`message <MicoMessage>`\ .

