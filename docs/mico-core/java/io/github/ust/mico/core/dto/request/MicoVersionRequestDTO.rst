.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints Pattern

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoVersionRequestDTO
=====================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @Accessors public class MicoVersionRequestDTO

   DTO for a version intended to use with requests only, e.g., with a request to promote a new version of a \ :java:ref:`MicoApplication`\ .

Constructors
------------
MicoVersionRequestDTO
^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoVersionRequestDTO(String version)
   :outertype: MicoVersionRequestDTO

   Creates an instance of \ ``MicoVersionRequestDTO``\  based on the String value \ ``version``\ .

   :param version: the version.

