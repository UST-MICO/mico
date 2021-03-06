.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: javax.validation.constraints NotNull

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoTopicRole

.. java:import:: io.github.ust.mico.core.model OpenFaaSFunction

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

KFConnectorDeploymentInfoRequestDTO
===================================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class KFConnectorDeploymentInfoRequestDTO

   DTO for \ :java:ref:`MicoServiceDeploymentInfo`\  specialised for a KafkaFaasConnector intended to use with requests only.

Constructors
------------
KFConnectorDeploymentInfoRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public KFConnectorDeploymentInfoRequestDTO(MicoServiceDeploymentInfo kfConnectorDeploymentInfo)
   :outertype: KFConnectorDeploymentInfoRequestDTO

   Creates an instance of \ ``KFConnectorDeploymentInfoRequestDTO``\  based on a \ ``MicoServiceDeploymentInfo``\ .

   :param kfConnectorDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .

