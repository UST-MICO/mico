.. java:import:: java.util ArrayList

.. java:import:: java.util Arrays

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.model MicoMessage.Type

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok RequiredArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoApplicationDeploymentStatus.Value
=====================================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @AllArgsConstructor public enum Value
   :outertype: MicoApplicationDeploymentStatus

   Enumeration for the different values of a \ :java:ref:`MicoApplicationDeploymentStatus`\ .

Enum Constants
--------------
DEPLOYED
^^^^^^^^

.. java:field:: @JsonProperty public static final MicoApplicationDeploymentStatus.Value DEPLOYED
   :outertype: MicoApplicationDeploymentStatus.Value

   Indicates that a \ :java:ref:`MicoApplication`\  with all its \ :java:ref:`MicoService`\  has been deployed successfully.

INCOMPLETE
^^^^^^^^^^

.. java:field:: @JsonProperty public static final MicoApplicationDeploymentStatus.Value INCOMPLETE
   :outertype: MicoApplicationDeploymentStatus.Value

   Indicates that the deployment / undeployment of a \ :java:ref:`MicoApplication`\  did not complete due to at least one \ :java:ref:`MicoService`\  of the \ ``MicoApplication``\  that couldn't be deployed / undeployed successfully.

PENDING
^^^^^^^

.. java:field:: @JsonProperty public static final MicoApplicationDeploymentStatus.Value PENDING
   :outertype: MicoApplicationDeploymentStatus.Value

   Indicates that a \ :java:ref:`MicoApplication`\  is currently being deployed / undeployed.

UNDEPLOYED
^^^^^^^^^^

.. java:field:: @JsonProperty public static final MicoApplicationDeploymentStatus.Value UNDEPLOYED
   :outertype: MicoApplicationDeploymentStatus.Value

   Indicates that a \ :java:ref:`MicoApplication`\  with all its \ :java:ref:`MicoService`\  has been undeployed successfully.

UNKNOWN
^^^^^^^

.. java:field:: @JsonProperty public static final MicoApplicationDeploymentStatus.Value UNKNOWN
   :outertype: MicoApplicationDeploymentStatus.Value

   Indicates that the current deployment status of a \ :java:ref:`MicoApplication`\  is not known.

Methods
-------
toString
^^^^^^^^

.. java:method:: @Override public String toString()
   :outertype: MicoApplicationDeploymentStatus.Value

