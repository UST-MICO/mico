.. java:import:: com.fasterxml.jackson.annotation JsonIgnoreProperties

.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: java.util List

BuildStatus
===========

.. java:package:: io.github.ust.mico.core.imagebuilder
   :noindex:

.. java:type:: @JsonInclude @JsonIgnoreProperties @Getter @Builder @NoArgsConstructor @AllArgsConstructor @ToString @EqualsAndHashCode public class BuildStatus

Fields
------
cluster
^^^^^^^

.. java:field::  ClusterSpec cluster
   :outertype: BuildStatus

   Cluster provides additional information if the builder is Cluster. +optional

stepsCompleted
^^^^^^^^^^^^^^

.. java:field::  List<String> stepsCompleted
   :outertype: BuildStatus

   StepsCompleted lists the name of build steps completed. +optional

