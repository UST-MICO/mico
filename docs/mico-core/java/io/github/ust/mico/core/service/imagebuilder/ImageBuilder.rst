.. java:import:: io.fabric8.kubernetes.api.model ContainerStatus

.. java:import:: io.fabric8.kubernetes.api.model ObjectMeta

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model ServiceAccount

.. java:import:: io.fabric8.kubernetes.api.model.apiextensions CustomResourceDefinition

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.fabric8.kubernetes.client KubernetesClientException

.. java:import:: io.fabric8.kubernetes.client.dsl MixedOperation

.. java:import:: io.fabric8.kubernetes.client.dsl NonNamespaceOperation

.. java:import:: io.fabric8.kubernetes.client.dsl Resource

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesBuildBotConfig

.. java:import:: io.github.ust.mico.core.exception ImageBuildException

.. java:import:: io.github.ust.mico.core.exception NotInitializedException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.util CollectionUtils

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: lombok Getter

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.context.event ContextRefreshedEvent

.. java:import:: org.springframework.context.event EventListener

.. java:import:: org.springframework.core.env Environment

.. java:import:: org.springframework.core.env Profiles

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.util StringUtils

.. java:import:: java.util Arrays

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util.stream Collectors

ImageBuilder
============

.. java:package:: io.github.ust.mico.core.service.imagebuilder
   :noindex:

.. java:type:: @Slf4j @Service public class ImageBuilder

   Builds container images by using Knative Build and Kaniko.

Fields
------
BUILD_CRD_GROUP
^^^^^^^^^^^^^^^

.. java:field:: public static final String BUILD_CRD_GROUP
   :outertype: ImageBuilder

Constructors
------------
ImageBuilder
^^^^^^^^^^^^

.. java:constructor:: @Autowired public ImageBuilder(KubernetesClient kubernetesClient, MicoKubernetesBuildBotConfig buildBotConfig, KubernetesNameNormalizer kubernetesNameNormalizer)
   :outertype: ImageBuilder

   Create a \ ``ImageBuilder``\  to be able to build Docker images in the cluster.

   :param kubernetesClient: the \ :java:ref:`KubernetesClient`\
   :param buildBotConfig: the build bot configuration for the image builder
   :param kubernetesNameNormalizer: the \ :java:ref:`KubernetesNameNormalizer`\

Methods
-------
build
^^^^^

.. java:method:: public CompletableFuture<String> build(MicoService micoService) throws NotInitializedException, InterruptedException, ExecutionException, TimeoutException
   :outertype: ImageBuilder

   Builds an OCI image based on a Git repository provided by a \ ``MicoService``\ . The result of the returned \ ``CompletableFuture``\  is the Docker image URI.

   :param micoService: the MICO service for which the image should be build
   :throws NotInitializedException: if the image builder was not initialized
   :return: the \ :java:ref:`CompletableFuture`\  that executes the build. The result is the Docker image URI.

createBuildName
^^^^^^^^^^^^^^^

.. java:method:: public String createBuildName(String serviceName, String serviceVersion)
   :outertype: ImageBuilder

   Creates a build name based on the service name and version that is used for the build pod.

   :param serviceName: the name of the MICO service
   :param serviceVersion: the version of the MICO service
   :return: the name of the build pod

createBuildName
^^^^^^^^^^^^^^^

.. java:method:: public String createBuildName(MicoService service)
   :outertype: ImageBuilder

   Creates a build name based on the service name and version that is used for the build pod.

   :param service: the \ :java:ref:`MicoService`\ .
   :return: the image name.

createImageName
^^^^^^^^^^^^^^^

.. java:method:: public String createImageName(String serviceShortName, String serviceVersion)
   :outertype: ImageBuilder

   Creates an image name based on the short name and version of a service (used as image tag).

   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :param serviceVersion: the version of the \ :java:ref:`MicoService`\ .
   :return: the image name.

createImageName
^^^^^^^^^^^^^^^

.. java:method:: public String createImageName(MicoService service)
   :outertype: ImageBuilder

   Creates an image name based on a service (used as image tag).

   :param service: the \ :java:ref:`MicoService`\ .
   :return: the image name.

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(String buildName)
   :outertype: ImageBuilder

   Deletes the build for a given build name.

   :param buildName: the name of the build.

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(Build build)
   :outertype: ImageBuilder

   Deletes a given \ ``Build``\ .

   :param build: the \ :java:ref:`Build`\ .

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(MicoService service)
   :outertype: ImageBuilder

   Deletes the \ :java:ref:`Build`\  for a given service.

   :param service: the \ :java:ref:`MicoService`\ .

getBuildCRD
^^^^^^^^^^^

.. java:method:: public Optional<CustomResourceDefinition> getBuildCRD() throws KubernetesClientException
   :outertype: ImageBuilder

   Returns the build CRD if exists

   :throws KubernetesClientException: if operation fails
   :return: the build CRD

init
^^^^

.. java:method:: @EventListener public void init(ContextRefreshedEvent cre) throws NotInitializedException
   :outertype: ImageBuilder

   Initialize the image builder every time the application context is refreshed.

   :param cre: the \ :java:ref:`ContextRefreshedEvent`\
   :throws NotInitializedException: if there are errors during initialization

init
^^^^

.. java:method:: public void init() throws NotInitializedException
   :outertype: ImageBuilder

   Initialize the image builder. This is required to be able to use the image builder. It's not required to trigger the initialization manually, because at every application context refresh the method is called by the \ ``@EventListener init``\  method.

   :throws NotInitializedException: if there are errors during initialization

