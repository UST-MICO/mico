.. java:import:: java.util.stream Collectors

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoServiceDeploymentInfoResponseDTO
====================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceDeploymentInfoResponseDTO extends MicoServiceDeploymentInfoRequestDTO

   DTO for \ :java:ref:`MicoServiceDeploymentInfo`\  intended to use with responses only.

Constructors
------------
MicoServiceDeploymentInfoResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceDeploymentInfoResponseDTO(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoServiceDeploymentInfoResponseDTO

   Creates an instance of \ ``MicoServiceDeploymentInfoResponseDTO``\  based on a \ ``MicoServiceDeploymentInfo``\ .

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .

