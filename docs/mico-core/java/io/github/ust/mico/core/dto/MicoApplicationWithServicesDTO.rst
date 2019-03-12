.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.dto MicoApplicationDTO.MicoApplicationDeploymentStatus

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

MicoApplicationWithServicesDTO
==============================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: @Data @ToString @EqualsAndHashCode @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties @JsonInclude public class MicoApplicationWithServicesDTO extends MicoApplicationDTO

   DTO for a \ :java:ref:`MicoApplication`\  including all of its associated \ :java:ref:`MicoServices <MicoService>`\ .

Methods
-------
valueOf
^^^^^^^

.. java:method:: public static MicoApplicationWithServicesDTO valueOf(MicoApplication application)
   :outertype: MicoApplicationWithServicesDTO

   Creates a \ ``MicoApplicationWithServicesDTO``\  based on a \ :java:ref:`MicoApplication`\ . Note that the deployment status of the application needs to be set explicitly since it cannot be inferred from the given \ :java:ref:`MicoApplication`\  itself.

   :param application: the \ :java:ref:`MicoApplication`\ .
   :return: a \ :java:ref:`MicoApplicationWithServicesDTO`\  with all the values of the given \ ``MicoApplication``\ .

valueOf
^^^^^^^

.. java:method:: public static MicoApplicationWithServicesDTO valueOf(MicoApplication application, MicoApplicationDeploymentStatus deploymentStatus)
   :outertype: MicoApplicationWithServicesDTO

   Creates a \ ``MicoApplicationWithServicesDTO``\  based on a \ :java:ref:`MicoApplication`\ .

   :param application: the \ :java:ref:`MicoApplication`\ .
   :param deploymentStatus: indicates the current \ :java:ref:`MicoApplicationDeploymentStatus`\ .
   :return: a \ :java:ref:`MicoApplicationWithServicesDTO`\  with all the values of the given \ ``MicoApplication``\ .

