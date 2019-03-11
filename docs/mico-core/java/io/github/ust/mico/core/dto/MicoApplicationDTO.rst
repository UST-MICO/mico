.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

MicoApplicationDTO
==================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties @JsonInclude public class MicoApplicationDTO

   DTO for a \ :java:ref:`MicoApplication`\  without services and their deployment information. Contains the current deployment status of this application (may be unknown).

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoApplicationDTO valueOf(MicoApplication application)
   :outertype: MicoApplicationDTO

   Creates a \ ``MicoApplicationDTO``\  based on a \ :java:ref:`MicoApplication`\ . Note that the deployment status needs to be set explicitly since it cannot be inferred from the given \ :java:ref:`MicoApplication`\  itself.

   :param application: the \ :java:ref:`MicoApplication`\ .
   :return: a \ :java:ref:`MicoApplicationDTO`\  with all the values of the given \ ``MicoApplication``\ .

