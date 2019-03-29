.. java:import:: io.github.ust.mico.core.dto.request MicoServicePortRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoServicePort

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoServicePortResponseDTO
==========================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @AllArgsConstructor @Accessors public class MicoServicePortResponseDTO extends MicoServicePortRequestDTO

   DTO for a \ :java:ref:`MicoServicePort`\  intended to use with responses only.

Constructors
------------
MicoServicePortResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServicePortResponseDTO(MicoServicePort servicePort)
   :outertype: MicoServicePortResponseDTO

   Creates an instance of \ ``MicoServicePortResponseDTO``\  based on a \ ``MicoServicePort``\ .

   :param servicePort: the \ :java:ref:`MicoServicePort`\ .

