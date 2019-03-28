.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceDependencyGraphEdgeResponseDTO
=========================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceDependencyGraphEdgeResponseDTO

   DTO for the edge of the dependency graph of a \ :java:ref:`MicoService`\ .

Constructors
------------
MicoServiceDependencyGraphEdgeResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceDependencyGraphEdgeResponseDTO(MicoService source, MicoService target)
   :outertype: MicoServiceDependencyGraphEdgeResponseDTO

   Creates an instance of \ ``MicoServiceDependencyGraphEdgeResponseDTO``\  based on a source \ ``MicoService``\  and a target \ ``MicoService``\ .

   :param source: the source \ :java:ref:`MicoService`\ .
   :param target: the target \ :java:ref:`MicoService`\ .

