.. java:import:: java.io File

.. java:import:: java.io IOException

.. java:import:: java.util Arrays

.. java:import:: java.util Collections

.. java:import:: java.util List

.. java:import:: java.util Objects

.. java:import:: java.util UUID

.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent ExecutionException

.. java:import:: java.util.concurrent Executors

.. java:import:: java.util.concurrent ScheduledExecutorService

.. java:import:: java.util.concurrent ScheduledFuture

.. java:import:: java.util.concurrent TimeUnit

.. java:import:: java.util.concurrent TimeoutException

.. java:import:: io.fabric8.knative.v1 Condition

.. java:import:: io.fabric8.kubernetes.api.model PersistentVolumeClaim

.. java:import:: io.fabric8.kubernetes.api.model PersistentVolumeClaimBuilder

.. java:import:: io.fabric8.kubernetes.api.model Pod

.. java:import:: io.fabric8.kubernetes.api.model Quantity

.. java:import:: io.fabric8.kubernetes.api.model ResourceRequirements

.. java:import:: io.fabric8.kubernetes.api.model ServiceAccount

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.fabric8.tekton.client DefaultTektonClient

.. java:import:: io.fabric8.tekton.client TektonClient

.. java:import:: io.fabric8.tekton.pipeline.v1beta1 Pipeline

.. java:import:: io.fabric8.tekton.pipeline.v1beta1 PipelineRun

.. java:import:: io.fabric8.tekton.pipeline.v1beta1 PipelineRunBuilder

.. java:import:: io.fabric8.tekton.pipeline.v1beta1 Task

.. java:import:: io.github.ust.mico.core.configuration MicoKubernetesBuildBotConfig

.. java:import:: io.github.ust.mico.core.exception ImageBuildException

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception NotInitializedException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: io.github.ust.mico.core.util KubernetesNameNormalizer

.. java:import:: lombok Getter

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.context.event ContextRefreshedEvent

.. java:import:: org.springframework.context.event EventListener

.. java:import:: org.springframework.core.env Environment

.. java:import:: org.springframework.core.env Profiles

.. java:import:: org.springframework.core.io ClassPathResource

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.util StringUtils

TektonPipelinesController
=========================

.. java:package:: io.github.ust.mico.core.service.imagebuilder
   :noindex:

.. java:type:: @Slf4j @Service public class TektonPipelinesController implements ImageBuilder

   Builds container images by using Tekton Pipelines and Kaniko.

Constructors
------------
TektonPipelinesController
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public TektonPipelinesController(KubernetesClient kubernetesClient, MicoKubernetesBuildBotConfig buildBotConfig, KubernetesNameNormalizer kubernetesNameNormalizer)
   :outertype: TektonPipelinesController

   Create a \ ``ImageBuilder``\  to be able to build Docker images in the cluster.

   :param kubernetesClient: the \ :java:ref:`KubernetesClient`\
   :param buildBotConfig: the build bot configuration for the image builder
   :param kubernetesNameNormalizer: the \ :java:ref:`KubernetesNameNormalizer`\

Methods
-------
build
^^^^^

.. java:method:: @Override public CompletableFuture<String> build(MicoService micoService) throws InterruptedException, ExecutionException, TimeoutException, KubernetesResourceException, NotInitializedException
   :outertype: TektonPipelinesController

   Builds an OCI image based on a Git repository provided by a \ ``MicoService``\ . The result of the returned \ ``CompletableFuture``\  is the Docker image URI.

   :param micoService: the MICO service for which the image should be build
   :throws NotInitializedException: if the image builder was not initialized
   :return: the \ :java:ref:`CompletableFuture`\  that executes the build. The result is the Docker image URI.

createImageUrl
^^^^^^^^^^^^^^

.. java:method:: public String createImageUrl(String serviceShortName)
   :outertype: TektonPipelinesController

   Creates an image name based on the DockerHub registry name and service's short name.

   :param serviceShortName: the short name of the \ :java:ref:`MicoService`\ .
   :return: the image name.

init
^^^^

.. java:method:: @Override @EventListener public void init(ContextRefreshedEvent cre)
   :outertype: TektonPipelinesController

   Initialize the image builder every time the application context is refreshed.

   :param cre: the \ :java:ref:`ContextRefreshedEvent`\

init
^^^^

.. java:method:: @Override public void init() throws NotInitializedException
   :outertype: TektonPipelinesController

   Initialize the image builder. This is required to be able to use the image builder. It's not required to trigger the initialization manually, because the method is triggered by application context refresh events.

   :throws NotInitializedException: if there are errors during initialization

initilizeBuildPipeline
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void initilizeBuildPipeline(String namespace) throws IOException
   :outertype: TektonPipelinesController

   Initialize the Tekton build-and-push pipeline. This is required to be able to use the image builder.

   :param namespace: the namespace for Tekton pipeline resources.
   :throws IOException: if there are errors when reading Tekton definition files

