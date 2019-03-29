.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: io.fabric8.kubernetes.client CustomResource

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok ToString

.. java:import:: lombok.experimental Accessors

Build
=====

.. java:package:: io.github.ust.mico.core.service.imagebuilder.buildtypes
   :noindex:

.. java:type:: @JsonInclude @Data @Accessors @EqualsAndHashCode @ToString public class Build extends CustomResource

   Build represents a build of a container image. A Build is made up of a source, and a set of steps. Steps can mount volumes to share data between themselves. A build may be created by instantiating a BuildTemplate. Implementation of the Build types: https://github.com/knative/build/blob/release-0.4/pkg/apis/build/v1alpha1/build_types.go

Constructors
------------
Build
^^^^^

.. java:constructor:: public Build()
   :outertype: Build

Build
^^^^^

.. java:constructor:: public Build(BuildSpec spec)
   :outertype: Build

