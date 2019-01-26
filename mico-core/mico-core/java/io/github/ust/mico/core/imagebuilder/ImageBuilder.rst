.. java:import:: io.fabric8.kubernetes.api.model ObjectMeta

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model ServiceAccount

.. java:import:: io.fabric8.kubernetes.api.model.apiextensions CustomResourceDefinition

.. java:import:: io.fabric8.kubernetes.api.model.apiextensions CustomResourceDefinitionList

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.fabric8.kubernetes.client.dsl MixedOperation

.. java:import:: io.fabric8.kubernetes.client.dsl NonNamespaceOperation

.. java:import:: io.fabric8.kubernetes.client.dsl Resource

.. java:import:: io.github.ust.mico.core ClusterAwarenessFabric8

.. java:import:: io.github.ust.mico.core MicoKubernetesBuildBotConfig

.. java:import:: io.github.ust.mico.core NotInitializedException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.util StringUtils

.. java:import:: java.util List

.. java:import:: java.util Optional

ImageBuilder
============

.. java:package:: io.github.ust.mico.core.imagebuilder
   :noindex:

.. java:type:: @Slf4j @Service public class ImageBuilder

   Builds container images by using Knative Build and Kaniko

Constructors
------------
ImageBuilder
^^^^^^^^^^^^

.. java:constructor:: @Autowired public ImageBuilder(ClusterAwarenessFabric8 cluster, MicoKubernetesBuildBotConfig buildBotConfig)
   :outertype: ImageBuilder

   :param cluster: The Kubernetes cluster object
   :param buildBotConfig: The build bot configuration for the image builder

Methods
-------
build
^^^^^

.. java:method:: public Build build(MicoService micoService) throws NotInitializedException, IllegalArgumentException
   :outertype: ImageBuilder

   :param micoService: the MICO service for which the image should be build
   :throws NotInitializedException: if the image builder was not initialized
   :return: the resulting build

createImageName
^^^^^^^^^^^^^^^

.. java:method:: public String createImageName(String serviceName, String serviceVersion)
   :outertype: ImageBuilder

   Creates a image name based on the service name and the service version (used as image tag).

   :param serviceName: the name of the MICO service
   :param serviceVersion: the version of the MICO service
   :return: the image name

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(String buildName)
   :outertype: ImageBuilder

   Delete the build

   :param buildName: the name of the build

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(Build build)
   :outertype: ImageBuilder

   Delete the build

   :param build: the build object

getBuild
^^^^^^^^

.. java:method:: public Build getBuild(String buildName)
   :outertype: ImageBuilder

   Returns the build object

   :param buildName: the name of the build
   :return: the build object

getBuildCRD
^^^^^^^^^^^

.. java:method:: public Optional<CustomResourceDefinition> getBuildCRD()
   :outertype: ImageBuilder

   Returns the build CRD if exists

   :return: the build CRD

init
^^^^

.. java:method:: public void init() throws NotInitializedException
   :outertype: ImageBuilder

   Initialize the image builder.

   :throws NotInitializedException: if the image builder was not initialized

waitUntilBuildIsFinished
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public CompletableFuture<Boolean> waitUntilBuildIsFinished(String buildName) throws InterruptedException, ExecutionException, TimeoutException
   :outertype: ImageBuilder

