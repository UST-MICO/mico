.. java:import:: java.util.stream Collectors

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceInterfaceRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoServiceInterfaceResponseDTO
===============================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @AllArgsConstructor @Accessors public class MicoServiceInterfaceResponseDTO extends MicoServiceInterfaceRequestDTO

   DTO for a \ :java:ref:`MicoServiceInterface`\  intended to use with responses only.

Constructors
------------
MicoServiceInterfaceResponseDTO
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceInterfaceResponseDTO(MicoServiceInterface serviceInterface)
   :outertype: MicoServiceInterfaceResponseDTO

   Creates an instance of \ ``MicoServiceInterfaceResponseDTO``\  based on a \ ``MicoServiceInterface``\ .

   :param serviceInterface: the \ :java:ref:`MicoServiceInterface`\ .

