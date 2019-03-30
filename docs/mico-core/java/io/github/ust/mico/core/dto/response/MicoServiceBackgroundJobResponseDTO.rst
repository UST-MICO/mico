.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob.Status

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob.Type

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceBackgroundJobResponseDTO
===================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceBackgroundJobResponseDTO

   DTO for a \ :java:ref:`MicoServiceBackgroundJob`\  intended to use with responses only.

Constructors
------------
MicoServiceBackgroundJobResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceBackgroundJobResponseDTO(MicoServiceBackgroundJob serviceBackgroundJob)
   :outertype: MicoServiceBackgroundJobResponseDTO

   Creates a \ ``MicoBackgroundJobResponseDTO``\  based on a \ ``MicoServiceBackgroundJob``\ .

   :param serviceBackgroundJob: the \ :java:ref:`MicoServiceBackgroundJob`\ .
   :return: a \ :java:ref:`MicoServiceBackgroundJobResponseDTO`\  with all the values of the given \ ``MicoServiceBackgroundJob``\ .

