.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: io.github.ust.mico.core.dto.request MicoLabelRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoLabel

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoLabelResponseDTO
====================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @AllArgsConstructor @Accessors public class MicoLabelResponseDTO extends MicoLabelRequestDTO

   DTO for a \ :java:ref:`MicoLabel`\  intended to use with responses only.

Constructors
------------
MicoLabelResponseDTO
^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoLabelResponseDTO(MicoLabel label)
   :outertype: MicoLabelResponseDTO

   Creates an instance of \ ``MicoLabelResponseDTO``\  based on a \ ``MicoLabel``\ .

   :param label: the \ :java:ref:`MicoLabel`\ .

