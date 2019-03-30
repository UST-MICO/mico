.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: io.github.ust.mico.core.dto.request MicoInterfaceConnectionRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoInterfaceConnection

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoInterfaceConnectionResponseDTO
==================================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @AllArgsConstructor @Accessors public class MicoInterfaceConnectionResponseDTO extends MicoInterfaceConnectionRequestDTO

   DTO for a \ :java:ref:`MicoInterfaceConnection`\  intended to use with responses only.

Constructors
------------
MicoInterfaceConnectionResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoInterfaceConnectionResponseDTO(MicoInterfaceConnection interfaceConnection)
   :outertype: MicoInterfaceConnectionResponseDTO

   Creates an instance of \ ``MicoInterfaceConnectionResponseDTO``\  based on a \ ``MicoInterfaceConnection``\ .

   :param interfaceConnection: the \ :java:ref:`MicoInterfaceConnection`\ .

