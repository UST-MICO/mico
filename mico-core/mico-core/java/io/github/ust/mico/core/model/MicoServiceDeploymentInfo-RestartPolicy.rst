.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Builder

.. java:import:: lombok Builder.Default

.. java:import:: lombok Data

.. java:import:: lombok Singular

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: java.util List

.. java:import:: java.util Map

MicoServiceDeploymentInfo.RestartPolicy
=======================================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: public enum RestartPolicy
   :outertype: MicoServiceDeploymentInfo

   Enumeration for all supported restart policies.

Enum Constants
--------------
ALWAYS
^^^^^^

.. java:field:: public static final MicoServiceDeploymentInfo.RestartPolicy ALWAYS
   :outertype: MicoServiceDeploymentInfo.RestartPolicy

NEVER
^^^^^

.. java:field:: public static final MicoServiceDeploymentInfo.RestartPolicy NEVER
   :outertype: MicoServiceDeploymentInfo.RestartPolicy

ON_FAILURE
^^^^^^^^^^

.. java:field:: public static final MicoServiceDeploymentInfo.RestartPolicy ON_FAILURE
   :outertype: MicoServiceDeploymentInfo.RestartPolicy

Fields
------
DEFAULT
^^^^^^^

.. java:field:: public static RestartPolicy DEFAULT
   :outertype: MicoServiceDeploymentInfo.RestartPolicy

   Default restart policy is \ :java:ref:`RestartPolicy.ALWAYS`\ .

