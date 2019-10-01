.. java:import:: com.fasterxml.jackson.annotation JsonBackReference

.. java:import:: io.github.ust.mico.core.dto.request MicoTopicRequestDTO

.. java:import:: lombok.experimental Accessors

MicoTopicRole
=============

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @RelationshipEntity public class MicoTopicRole

   Represents a role of a \ :java:ref:`MicoTopic`\ .

   An instance of this class is persisted as a relationship between a \ :java:ref:`MicoServiceDeploymentInfo`\  and a \ :java:ref:`MicoTopic`\  node in the Neo4j database.

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoTopicRole valueOf(MicoTopicRequestDTO topicDto, MicoServiceDeploymentInfo serviceDeploymentInfo)
   :outertype: MicoTopicRole

   Creates a new \ ``MicoTopicRole``\  based on a \ ``MicoTopicRequestDTO``\ .

