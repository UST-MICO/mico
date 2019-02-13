.. java:import:: java.util ArrayList

.. java:import:: java.util HashMap

.. java:import:: java.util List

.. java:import:: java.util Map

.. java:import:: org.neo4j.ogm.annotation GeneratedValue

.. java:import:: org.neo4j.ogm.annotation Id

.. java:import:: org.neo4j.ogm.annotation NodeEntity

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok Singular

.. java:import:: lombok.experimental Accessors

MicoServiceDeploymentInfo.ImagePullPolicy
=========================================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: public enum ImagePullPolicy
   :outertype: MicoServiceDeploymentInfo

   Enumeration for the different policies specifying when to pull an image.

Enum Constants
--------------
ALWAYS
^^^^^^

.. java:field:: public static final MicoServiceDeploymentInfo.ImagePullPolicy ALWAYS
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

IF_NOT_PRESENT
^^^^^^^^^^^^^^

.. java:field:: public static final MicoServiceDeploymentInfo.ImagePullPolicy IF_NOT_PRESENT
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

NEVER
^^^^^

.. java:field:: public static final MicoServiceDeploymentInfo.ImagePullPolicy NEVER
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

Fields
------
DEFAULT
^^^^^^^

.. java:field:: public static ImagePullPolicy DEFAULT
   :outertype: MicoServiceDeploymentInfo.ImagePullPolicy

   Default image pull policy is \ :java:ref:`ImagePullPolicy.ALWAYS`\ .

