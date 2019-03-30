.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints Pattern

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoInterfaceConnection

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoInterfaceConnectionRequestDTO
=================================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoInterfaceConnectionRequestDTO

   DTO for the information needed to connect a \ :java:ref:`MicoService`\  to an \ :java:ref:`MicoServiceInterface`\  of another \ :java:ref:`MicoService`\  intended to use with requests only.

Constructors
------------
MicoInterfaceConnectionRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoInterfaceConnectionRequestDTO(MicoInterfaceConnection interfaceConnection)
   :outertype: MicoInterfaceConnectionRequestDTO

   Creates an instance of \ ``MicoInterfaceConnectionRequestDTO``\  based on a \ ``MicoInterfaceConnection``\ .

   :param interfaceConnection: the \ :java:ref:`MicoInterfaceConnection`\ .

