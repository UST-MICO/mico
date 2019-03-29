.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints NotNull

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: org.hibernate.validator.constraints URL

.. java:import:: com.fasterxml.jackson.annotation JsonSetter

.. java:import:: com.fasterxml.jackson.annotation Nulls

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.model MicoServiceDependency

.. java:import:: io.github.ust.mico.core.model MicoServiceInterface

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoServiceRequestDTO
=====================

.. java:package:: io.github.ust.mico.core.dto.request
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoServiceRequestDTO

   DTO for a \ :java:ref:`MicoService`\  intended to use with requests only. Note that the \ :java:ref:`MicoServiceDependencies <MicoServiceDependency>`\  and \ :java:ref:`MicoServiceInterfaces <MicoServiceInterface>`\  are not included.

Constructors
------------
MicoServiceRequestDTO
^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public MicoServiceRequestDTO(MicoService service)
   :outertype: MicoServiceRequestDTO

   Creates an instance of \ ``MicoServiceRequestDTO``\  based on a \ ``MicoService``\ .

   :param service: the \ :java:ref:`MicoService`\ .

