.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: java.util List

BuildStatus
===========

.. java:package:: io.github.ust.mico.core.imagebuilder.buildtypes
   :noindex:

.. java:type:: @JsonInclude @JsonIgnoreProperties @Getter @Builder @NoArgsConstructor @AllArgsConstructor @ToString @EqualsAndHashCode public class BuildStatus

   BuildStatus is the status for a Build resource

Fields
------
cluster
^^^^^^^

.. java:field::  ClusterSpec cluster
   :outertype: BuildStatus

   Optional. Cluster provides additional information if the builder is Cluster.

stepsCompleted
^^^^^^^^^^^^^^

.. java:field::  List<String> stepsCompleted
   :outertype: BuildStatus

   Optional. StepsCompleted lists the name of build steps completed.

