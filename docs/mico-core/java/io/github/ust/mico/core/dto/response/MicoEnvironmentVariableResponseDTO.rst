.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: io.github.ust.mico.core.dto.request MicoEnvironmentVariableRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoEnvironmentVariable

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoEnvironmentVariableResponseDTO
==================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @AllArgsConstructor @Accessors public class MicoEnvironmentVariableResponseDTO extends MicoEnvironmentVariableRequestDTO

   DTO for a \ :java:ref:`MicoEnvironmentVariable`\  intended to use with responses only.

Constructors
------------
MicoEnvironmentVariableResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoEnvironmentVariableResponseDTO(MicoEnvironmentVariable environmentVariable)
   :outertype: MicoEnvironmentVariableResponseDTO

   Creates an instance of \ ``MicoEnvironmentVariableResponseDTO``\  based on a \ ``MicoEnvironmentVariable``\ .

   :param environmentVariable: the \ :java:ref:`MicoEnvironmentVariable`\ .

