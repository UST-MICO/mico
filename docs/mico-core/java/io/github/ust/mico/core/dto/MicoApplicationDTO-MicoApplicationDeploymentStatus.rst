.. java:import:: com.fasterxml.jackson.annotation JsonInclude.Include

.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.model MicoApplication

.. java:import:: io.github.ust.mico.core.util Patterns

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

.. java:import:: javax.validation.constraints NotEmpty

.. java:import:: javax.validation.constraints Pattern

.. java:import:: javax.validation.constraints Size

MicoApplicationDTO.MicoApplicationDeploymentStatus
==================================================

.. java:package:: io.github.ust.mico.core.dto
   :noindex:

.. java:type:: public enum MicoApplicationDeploymentStatus
   :outertype: MicoApplicationDTO

Enum Constants
--------------
DEPLOYED
^^^^^^^^

.. java:field:: public static final MicoApplicationDTO.MicoApplicationDeploymentStatus DEPLOYED
   :outertype: MicoApplicationDTO.MicoApplicationDeploymentStatus

NOT_DEPLOYED
^^^^^^^^^^^^

.. java:field:: public static final MicoApplicationDTO.MicoApplicationDeploymentStatus NOT_DEPLOYED
   :outertype: MicoApplicationDTO.MicoApplicationDeploymentStatus

UNKNOWN
^^^^^^^

.. java:field:: public static final MicoApplicationDTO.MicoApplicationDeploymentStatus UNKNOWN
   :outertype: MicoApplicationDTO.MicoApplicationDeploymentStatus

Methods
-------
toString
^^^^^^^^

.. java:method:: @Override public String toString()
   :outertype: MicoApplicationDTO.MicoApplicationDeploymentStatus

