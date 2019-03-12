.. java:import:: com.fasterxml.jackson.annotation JsonBackReference

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.dto MicoServiceDeploymentInfoDTO

.. java:import:: lombok.experimental Accessors

.. java:import:: java.util ArrayList

.. java:import:: java.util List

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

