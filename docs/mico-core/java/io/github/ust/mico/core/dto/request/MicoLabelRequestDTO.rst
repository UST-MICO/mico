.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoLabel

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoLabelRequestDTO
===================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoLabelRequestDTO

   DTO for a \ :java:ref:`MicoLabel`\  intended to use with requests only.

Constructors
------------
MicoLabelRequestDTO
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoLabelRequestDTO(MicoLabel label)
   :outertype: MicoLabelRequestDTO

   Creates an instance of \ ``MicoLabelRequestDTO``\  based on a \ ``MicoLabel``\ .

   :param label: the \ :java:ref:`MicoLabel`\ .

