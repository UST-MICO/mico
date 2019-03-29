.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: javax.validation Valid

.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints NotNull

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceInterfaceRequestDTO
==============================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceInterfaceRequestDTO

   DTO for a \ :java:ref:`MicoServiceInterface`\  intended to use with requests only.

Constructors
------------
MicoServiceInterfaceRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceInterfaceRequestDTO(MicoServiceInterface serviceInterface)
   :outertype: MicoServiceInterfaceRequestDTO

   Creates an instance of \ ``MicoServiceInterfaceRequestDTO``\  based on a \ ``MicoServiceInterface``\ .

   :param serviceInterface: the \ :java:ref:`MicoServiceInterface`\ .

