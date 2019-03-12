.. java:import:: io.github.ust.mico.core.configuration.extension CustomOpenApiExtentionsPlugin

.. java:import:: io.github.ust.mico.core.exception VersionNotSupportedException

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: lombok.experimental Accessors

.. java:import:: javax.validation Valid

.. java:import:: javax.validation.constraints NotNull

MicoServiceDependency
=====================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors @JsonIgnoreProperties @RelationshipEntity public class MicoServiceDependency

   Represents a dependency of a \ :java:ref:`MicoService`\ .

Methods
-------
getMaxMicoVersion
^^^^^^^^^^^^^^^^^

.. java:method:: @JsonIgnore public MicoVersion getMaxMicoVersion() throws VersionNotSupportedException
   :outertype: MicoServiceDependency

getMinMicoVersion
^^^^^^^^^^^^^^^^^

.. java:method:: @JsonIgnore public MicoVersion getMinMicoVersion() throws VersionNotSupportedException
   :outertype: MicoServiceDependency

