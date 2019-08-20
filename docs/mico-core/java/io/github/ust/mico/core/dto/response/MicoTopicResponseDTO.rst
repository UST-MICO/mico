.. java:import:: io.github.ust.mico.core.dto.request MicoTopicRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoTopic

.. java:import:: io.github.ust.mico.core.model MicoTopicRole

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoTopicResponseDTO
====================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @AllArgsConstructor @Accessors public class MicoTopicResponseDTO extends MicoTopicRequestDTO

   DTO for a \ :java:ref:`MicoTopic`\  for response only use

Constructors
------------
MicoTopicResponseDTO
^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoTopicResponseDTO(MicoTopicRole micoTopicRole)
   :outertype: MicoTopicResponseDTO

   Creates an instance of \ ``MicoTopicResponseDTO``\  based on a \ ``MicoTopicRole``\ .

   :param micoTopicRole: \ :java:ref:`MicoTopicRole`\ .

