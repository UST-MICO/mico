.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonSetter

.. java:import:: com.fasterxml.jackson.annotation Nulls

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoLabel

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo.ImagePullPolicy

.. java:import:: io.github.ust.mico.core.model MicoServiceDeploymentInfo.RestartPolicy

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: javax.validation Valid

.. java:import:: javax.validation.constraints Positive

.. java:import:: javax.validation.constraints PositiveOrZero

.. java:import:: java.util ArrayList

.. java:import:: java.util List

MicoServiceDeploymentInfoDTO
============================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties public class MicoServiceDeploymentInfoDTO

   DTO for \ :java:ref:`MicoServiceDeploymentInfo`\ .

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoServiceDeploymentInfoDTO valueOf(MicoServiceDeploymentInfo micoServiceDeploymentInfo)
   :outertype: MicoServiceDeploymentInfoDTO

   Creates a \ ``MicoServiceDeploymentInfoDTO``\  based on a \ :java:ref:`MicoServiceDeploymentInfo`\ .

   :param micoServiceDeploymentInfo: the \ :java:ref:`MicoServiceDeploymentInfo`\  to use.
   :return: a \ :java:ref:`MicoServiceDeploymentInfoDTO`\  with all the values of the given \ ``MicoServiceDeploymentInfo``\ .

