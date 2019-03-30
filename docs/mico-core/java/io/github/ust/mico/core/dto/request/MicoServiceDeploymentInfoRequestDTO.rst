.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: javax.validation Valid

.. java:import:: javax.validation.constraints Positive

.. java:import:: com.fasterxml.jackson.annotation JsonSetter

.. java:import:: com.fasterxml.jackson.annotation Nulls

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo.ImagePullPolicy

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceDeploymentInfoRequestDTO
===================================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceDeploymentInfoRequestDTO

   DTO for \ :java:ref:`MicoServiceDeploymentInfo`\  intended to use with requests only.

Constructors
------------
MicoServiceDeploymentInfoRequestDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceDeploymentInfoRequestDTO(MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoServiceDeploymentInfoRequestDTO

   Creates an instance of \ ``MicoServiceDeploymentInfoRequestDTO``\  based on a \ ``MicoServiceDeploymentInfo``\ .

   :param serviceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\ .

