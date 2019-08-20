.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoTopicRole

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: javax.validation.constraints NotNull

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

MicoTopicRequestDTO
===================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoTopicRequestDTO

Constructors
------------
MicoTopicRequestDTO
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoTopicRequestDTO(MicoTopicRole micoTopicRole)
   :outertype: MicoTopicRequestDTO

   Creates an instance of \ ``MicoTopicRequestDTO``\  based on a \ ``MicoTopicRole``\  that includes the \ ``MicoTopic``\  and a role.

   :param micoTopicRole: the \ :java:ref:`MicoTopicRole`\ .

