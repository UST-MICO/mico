.. java:import:: com.fasterxml.jackson.annotation JsonInclude

.. java:import:: com.fasterxml.jackson.annotation JsonPropertyOrder

.. java:import:: io.fabric8.kubernetes.client CustomResource

Build
=====

.. java:package:: io.github.ust.mico.core.imagebuilder
   :noindex:

.. java:type:: @JsonInclude @JsonPropertyOrder @Getter @NoArgsConstructor @AllArgsConstructor @ToString public class Build extends CustomResource

   Build represents a build of a container image. A Build is made up of a source, and a set of steps. Steps can mount volumes to share data between themselves. A build may be created by instantiating a BuildTemplate.

   Implemenation of the Build types: https://github.com/knative/build/blob/9127bb7ec158b60da08dda6aa9081af98951f3bb/pkg/apis/build/v1alpha1/build_types.go#L107

Constructors
------------
Build
^^^^^

.. java:constructor:: @Builder public Build(BuildSpec spec)
   :outertype: Build

