.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints Pattern

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoEnvironmentVariableRequestDTO
=================================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoEnvironmentVariableRequestDTO

   DTO for a \ :java:ref:`MicoEnvironmentVariable`\  intended to use with requests only.

Constructors
------------
MicoEnvironmentVariableRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoEnvironmentVariableRequestDTO(MicoEnvironmentVariable environmentVariable)
   :outertype: MicoEnvironmentVariableRequestDTO

   Creates an instance of \ ``MicoEnvironmentVariableRequestDTO``\  based on a \ ``MicoEnvironmentVariable``\ .

   :param environmentVariable: the \ :java:ref:`MicoEnvironmentVariable`\ .

