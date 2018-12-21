.. java:import:: com.fasterxml.jackson.annotation JsonInclude

ClusterSpec
===========

.. java:package:: io.github.ust.mico.core.build
   :noindex:

.. java:type:: @JsonInclude @Getter @Builder @NoArgsConstructor @AllArgsConstructor @ToString @EqualsAndHashCode public class ClusterSpec

Fields
------
namespace
^^^^^^^^^

.. java:field::  String namespace
   :outertype: ClusterSpec

   Namespace is the namespace in which the pod is running.

podName
^^^^^^^

.. java:field::  String podName
   :outertype: ClusterSpec

   PodName is the name of the pod responsible for executing this build's steps.

