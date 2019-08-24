.. java:import:: com.fasterxml.jackson.annotation JsonBackReference

.. java:import:: io.github.ust.mico.core.dto.request KFConnectorDeploymentInfoRequestDTO

.. java:import:: lombok.experimental Accessors

KFConnectorDeploymentInfo
=========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @RelationshipEntity public class KFConnectorDeploymentInfo

   Represents an instance of a \ :java:ref:`MicoServiceDeploymentInfo`\  for the deployment of a KafkaFaasConnector.

   An instance of this class is persisted as a relationship between between a \ :java:ref:`MicoApplication`\  and a \ :java:ref:`MicoServiceDeploymentInfo`\  node in the Neo4j database.

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static KFConnectorDeploymentInfo valueOf(KFConnectorDeploymentInfoRequestDTO sdiDto, MicoApplication application)
   :outertype: KFConnectorDeploymentInfo

   Creates a new \ :java:ref:`KFConnectorDeploymentInfo`\  based on a \ :java:ref:`KFConnectorDeploymentInfoRequestDTO`\ .

