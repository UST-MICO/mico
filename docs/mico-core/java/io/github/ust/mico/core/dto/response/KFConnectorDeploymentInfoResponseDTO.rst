.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.dto.request KFConnectorDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

KFConnectorDeploymentInfoResponseDTO
====================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @Accessors public class KFConnectorDeploymentInfoResponseDTO extends KFConnectorDeploymentInfoRequestDTO

   DTO for \ :java:ref:`MicoServiceDeploymentInfo`\  intended to use with responses only.

Constructors
------------
KFConnectorDeploymentInfoResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public KFConnectorDeploymentInfoResponseDTO(MicoServiceDeploymentInfo kfConnectorDeploymentInfo)
   :outertype: KFConnectorDeploymentInfoResponseDTO

   Creates an instance of \ ``KFConnectorDeploymentInfoResponseDTO``\  based on a \ ``MicoServiceDeploymentInfo``\ .

   :param kfConnectorDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .

