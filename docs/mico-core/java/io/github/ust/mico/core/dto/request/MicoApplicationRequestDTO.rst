.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints NotNull

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

MicoApplicationRequestDTO
=========================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoApplicationRequestDTO

   DTO for a \ :java:ref:`MicoApplication`\  intended to use with requests only. Note that the services are not included.

Constructors
------------
MicoApplicationRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationRequestDTO(MicoApplication application)
   :outertype: MicoApplicationRequestDTO

   Creates an instance of \ ``MicoApplicationRequestDTO``\  based on a \ ``MicoApplication``\ .

   :param application: the \ :java:ref:`MicoApplication`\ .

