.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: io.fabric8.kubernetes.api.model Service

.. java:import:: io.fabric8.kubernetes.api.model.apps Deployment

.. java:import:: io.github.ust.mico.core.dto.response KubernetesDeploymentInfoResponseDTO

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

KubernetesDeploymentInfo
========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @NodeEntity public class KubernetesDeploymentInfo

   Information about the Kubernetes resources that are created through an actual deployment of a \ :java:ref:`MicoService`\ .

   Instances of this class are persisted as nodes in the Neo4j database.

Methods
-------
applyValuesFrom
^^^^^^^^^^^^^^^

.. java:method:: public KubernetesDeploymentInfo applyValuesFrom(KubernetesDeploymentInfoResponseDTO kubernetesDeploymentInfoDto)
   :outertype: KubernetesDeploymentInfo

   Applies the values of all properties of a \ ``KubernetesDeploymentInfoResponseDTO``\  to this \ ``KubernetesDeploymentInfo``\ . Note that the id will not be affected.

   :param kubernetesDeploymentInfoDto: the \ :java:ref:`KubernetesDeploymentInfoResponseDTO`\ .
   :return: this \ :java:ref:`KubernetesDeploymentInfo`\  with the values of the properties of the given \ :java:ref:`KubernetesDeploymentInfoResponseDTO`\ .

valueOf
^^^^^^^

.. java:method:: public static KubernetesDeploymentInfo valueOf(KubernetesDeploymentInfoResponseDTO kubernetesDeploymentInfoDto)
   :outertype: KubernetesDeploymentInfo

   Creates a new \ ``KubernetesDeploymentInfo``\  based on a \ ``KubernetesDeploymentInfoResponseDTO``\ . Note that the id will be set to \ ``null``\ .

   :param kubernetesDeploymentInfoDto: the \ :java:ref:`KubernetesDeploymentInfoResponseDTO`\ .
   :return: a \ :java:ref:`KubernetesDeploymentInfo`\ .

