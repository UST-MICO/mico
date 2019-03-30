.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoApplicationJobStatus

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoApplicationJobStatusResponseDTO
===================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoApplicationJobStatusResponseDTO

   DTO for a \ :java:ref:`MicoApplicationJobStatus`\  intended to use with responses only.

Constructors
------------
MicoApplicationJobStatusResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationJobStatusResponseDTO(MicoApplicationJobStatus applicationJobStatus)
   :outertype: MicoApplicationJobStatusResponseDTO

   Creates a \ ``MicoApplicationJobStatusDTO``\  based on a \ :java:ref:`MicoApplicationJobStatus`\ .

   :param applicationJobStatus: the \ :java:ref:`MicoApplicationJobStatus`\ .

