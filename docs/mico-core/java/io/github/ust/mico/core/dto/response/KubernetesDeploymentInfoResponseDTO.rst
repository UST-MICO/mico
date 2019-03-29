.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model KubernetesDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

KubernetesDeploymentInfoResponseDTO
===================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class KubernetesDeploymentInfoResponseDTO

   DTO for \ :java:ref:`KubernetesDeploymentInfo`\  intended to use with responses only.

Constructors
------------
KubernetesDeploymentInfoResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public KubernetesDeploymentInfoResponseDTO(KubernetesDeploymentInfo kubernetesDeploymentInfo)
   :outertype: KubernetesDeploymentInfoResponseDTO

   Creates an instance of \ ``KubernetesDeploymentInfoResponseDTO``\  based on a \ ``KubernetesDeploymentInfo``\ .

   :param kubernetesDeploymentInfo: the \ :java:ref:`KubernetesDeploymentInfo`\ .

