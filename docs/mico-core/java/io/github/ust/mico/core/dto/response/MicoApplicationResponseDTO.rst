.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.dto.request MicoApplicationRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationDeploymentStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus.Value

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok.experimental Accessors

MicoApplicationResponseDTO
==========================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoApplicationResponseDTO extends MicoApplicationRequestDTO

   DTO for a \ :java:ref:`MicoApplication`\  intended to use with responses only. Note that neither the services nor their deployment information is included. Contains the current deployment status of this application (may be unknown).

Constructors
------------
MicoApplicationResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationResponseDTO(MicoApplication application)
   :outertype: MicoApplicationResponseDTO

   Creates an instance of \ ``MicoApplicationResponseDTO``\  based on a \ ``MicoApplication``\ . Note that the deployment status is not set since it cannot be inferred from the \ ``MicoApplication``\  itself

   :param application: the \ :java:ref:`MicoApplication`\ .

MicoApplicationResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationResponseDTO(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus)
   :outertype: MicoApplicationResponseDTO

   Creates an instance of \ ``MicoApplicationResponseDTO``\  based on a \ ``MicoApplication``\  and a \ ``MicoApplicationDeploymentStatus``\ .

   :param application: the \ :java:ref:`MicoApplication`\ .
   :param deploymentStatus: the \ :java:ref:`MicoApplicationDeploymentStatus`\ .

