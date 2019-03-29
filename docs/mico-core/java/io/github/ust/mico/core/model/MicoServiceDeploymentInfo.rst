.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: org.neo4j.ogm.annotation Relationship

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceDeploymentInfoRequestDTO

.. java:import:: io.github.ust.mico.core.dto.response MicoServiceDeploymentInfoResponseDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoServiceDeploymentInfo
=========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class MicoServiceDeploymentInfo

   Represents the information necessary for deploying a \ :java:ref:`MicoApplication`\ . DTO is \ :java:ref:`MicoServiceDeploymentInfoResponseDTO`\ .

Methods
-------
applyValuesFrom
^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo applyValuesFrom(MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDto)
   :outertype: MicoServiceDeploymentInfo

   Applies the values of all properties of a \ ``MicoServiceDeploymentInfoRequestDTO``\  to this \ ``MicoServiceDeploymentInfo``\ . Note that the id will not be affected.

   :param serviceDeploymentInfoDto: the \ :java:ref:`MicoServiceDeploymentInfoRequestDTO`\ .
   :return: this \ :java:ref:`MicoServiceDeploymentInfo`\  with the values of the properties of the given \ :java:ref:`MicoServiceDeploymentInfoRequestDTO`\ .

applyValuesFrom
^^^^^^^^^^^^^^^

.. java:method:: public MicoServiceDeploymentInfo applyValuesFrom(MicoServiceDeploymentInfoResponseDTO serviceDeploymentInfoDto)
   :outertype: MicoServiceDeploymentInfo

   Applies the values of all properties of a \ ``MicoServiceDeploymentInfoResponseDTO``\  to this \ ``MicoServiceDeploymentInfo``\ . Note that the id will not be affected.

   :param serviceDeploymentInfoDto: the \ :java:ref:`MicoServiceDeploymentInfoResponseDTO`\ .
   :return: this \ :java:ref:`MicoServiceDeploymentInfo`\  with the values of the properties of the given \ :java:ref:`MicoServiceDeploymentInfoResponseDTO`\ .

