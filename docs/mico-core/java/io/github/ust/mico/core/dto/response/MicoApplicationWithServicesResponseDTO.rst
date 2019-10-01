.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.dto.response.status MicoApplicationDeploymentStatusResponseDTO

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoApplicationDeploymentStatus

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok.experimental Accessors

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

MicoApplicationWithServicesResponseDTO
======================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoApplicationWithServicesResponseDTO extends MicoApplicationResponseDTO

   DTO for a \ :java:ref:`MicoApplication`\  intended to use with responses only. Additionally includes all of services of the application.

Constructors
------------
MicoApplicationWithServicesResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationWithServicesResponseDTO(MicoApplication application)
   :outertype: MicoApplicationWithServicesResponseDTO

   Creates an instance of \ ``MicoApplicationWithServicesResponseDTO``\  based on a \ ``MicoApplication``\ . Note that the deployment status is not set since it cannot be inferred from the \ ``MicoApplication``\  itself

   :param application: the \ :java:ref:`MicoApplication`\ .

MicoApplicationWithServicesResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoApplicationWithServicesResponseDTO(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus)
   :outertype: MicoApplicationWithServicesResponseDTO

   Creates an instance of \ ``MicoApplicationWithServicesResponseDTO``\  based on a \ ``MicoApplication``\  and a \ ``MicoApplicationDeploymentStatus``\ .

   :param application: the \ :java:ref:`MicoApplication`\ .
   :param deploymentStatus: the \ :java:ref:`MicoApplicationDeploymentStatus`\ .

