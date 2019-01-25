.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Builder

.. java:import:: lombok Builder.Default

.. java:import:: lombok Value

MicoDeploymentStrategy.Type
===========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: public enum Type
   :outertype: MicoDeploymentStrategy

   Enumeration for the supported types of deployment strategies.

Enum Constants
--------------
RECREATE
^^^^^^^^

.. java:field:: public static final MicoDeploymentStrategy.Type RECREATE
   :outertype: MicoDeploymentStrategy.Type

   Delete all running instances and then create new ones.

ROLLING_UPDATE
^^^^^^^^^^^^^^

.. java:field:: public static final MicoDeploymentStrategy.Type ROLLING_UPDATE
   :outertype: MicoDeploymentStrategy.Type

   Update one after the other.

Fields
------
DEFAULT
^^^^^^^

.. java:field:: public static Type DEFAULT
   :outertype: MicoDeploymentStrategy.Type

   Default deployment strategy type is \ :java:ref:`Type.ROLLING_UPDATE`\ .

