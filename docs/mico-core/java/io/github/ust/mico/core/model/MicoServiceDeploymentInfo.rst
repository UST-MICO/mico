.. java:import:: com.fasterxml.jackson.annotation JsonBackReference

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.dto MicoServiceDeploymentInfoDTO

.. java:import:: lombok.experimental Accessors

.. java:import:: java.util ArrayList

.. java:import:: java.util List

MicoServiceDeploymentInfo
=========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @RelationshipEntity public class MicoServiceDeploymentInfo

   Represents the information necessary for deploying a \ :java:ref:`MicoApplication`\ . DTO is \ :java:ref:`MicoServiceDeploymentInfoDTO`\ .

Methods
-------
applyValuesFrom
^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo applyValuesFrom(MicoServiceDeploymentInfoDTO serviceDeploymentInfoDTO)
   :outertype: MicoServiceDeploymentInfo

   Applies the values of all properties of a \ :java:ref:`MicoServiceDeploymentInfoDTO`\  to this \ ``MicoServiceDeploymentInfo``\ .

   :param serviceDeploymentInfoDTO: the \ :java:ref:`MicoServiceDeploymentInfoDTO`\ .
   :return: this \ :java:ref:`MicoServiceDeploymentInfo`\  with the values of the properties of the given \ :java:ref:`MicoServiceDeploymentInfoDTO`\ .

