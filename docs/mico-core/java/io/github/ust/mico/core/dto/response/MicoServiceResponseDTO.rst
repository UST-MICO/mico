.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.dto.request MicoServiceRequestDTO

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceCrawlingOrigin

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoServiceResponseDTO
======================

.. java:package:: io.github.ust.mico.core.dto.response
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceResponseDTO extends MicoServiceRequestDTO

   DTO for a \ :java:ref:`MicoService`\  intended for use with responses only. Note that the \ :java:ref:`MicoServiceDependencies <MicoServiceDependency>`\  and \ :java:ref:`MicoServiceInterfaces <MicoServiceInterface>`\  are not included.

Constructors
------------
MicoServiceResponseDTO
^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceResponseDTO(MicoService service)
   :outertype: MicoServiceResponseDTO

   Creates an instance of \ ``MicoServiceResponseDTO``\  based on a \ ``MicoService``\ .

   :param service: the \ :java:ref:`MicoService`\ .

