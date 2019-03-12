.. java:import:: com.fasterxml.jackson.annotation JsonBackReference

.. java:import:: com.fasterxml.jackson.annotation JsonProperty

.. java:import:: io.github.ust.mico.core.dto MicoServiceDeploymentInfoDTO

.. java:import:: lombok.experimental Accessors

.. java:import:: java.util ArrayList

.. java:import:: java.util List

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

