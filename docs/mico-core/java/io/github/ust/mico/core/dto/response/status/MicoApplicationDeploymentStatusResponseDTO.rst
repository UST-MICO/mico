.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus.Value

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoApplicationDeploymentStatusResponseDTO
==========================================

.. java:package:: io.github.ust.mico.core.dto.response.status
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonInclude public class MicoApplicationDeploymentStatusResponseDTO

   DTO for a \ :java:ref:`MicoApplicationDeploymentStatus`\  intended to use with responses only.

Constructors
------------
MicoApplicationDeploymentStatusResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationDeploymentStatusResponseDTO(MicoApplicationDeploymentStatus applicationDeploymentStatus)
   :outertype: MicoApplicationDeploymentStatusResponseDTO

   Creates an instance of \ ``MicoApplicationDeploymentStatusResponseDTO``\  based on a \ ``MicoApplicationDeploymentStatus``\ .

   :param applicationDeploymentStatus: the \ :java:ref:`applicationDeploymentStatus <MicoApplicationDeploymentStatus>`\ .

