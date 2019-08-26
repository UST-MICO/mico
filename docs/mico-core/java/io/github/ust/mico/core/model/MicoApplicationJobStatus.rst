.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: io.github.ust.mico.core.model MicoServiceBackgroundJob.Status

.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.experimental Accessors

MicoApplicationJobStatus
========================

.. java:package:: io.github.ust.mico.core.model
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Accessors public class MicoApplicationJobStatus

   Represents the job status for a \ :java:ref:`MicoApplication`\ . Contains a list of jobs.

   Note that this class is only used for business logic purposes and instances are not persisted.

