.. java:import:: javax.validation.constraints Max

.. java:import:: javax.validation.constraints Min

.. java:import:: com.fasterxml.jackson.annotation JsonSetter

.. java:import:: com.fasterxml.jackson.annotation Nulls

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoPortType

.. java:import:: io.github.ust.mico.core.model MicoServicePort

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServicePortRequestDTO
=========================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServicePortRequestDTO

   DTO for a \ :java:ref:`MicoServicePort`\  intended to use with requests only.

Constructors
------------
MicoServicePortRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServicePortRequestDTO(MicoServicePort servicePort)
   :outertype: MicoServicePortRequestDTO

   Creates an instance of \ ``MicoServicePortRequestDTO``\  based on a \ ``MicoServicePort``\ .

   :param servicePort: the \ :java:ref:`MicoServicePort`\ .

