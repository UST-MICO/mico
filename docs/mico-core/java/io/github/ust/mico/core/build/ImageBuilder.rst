.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: com.fasterxml.jackson.databind SerializationFeature

.. java:import:: com.fasterxml.jackson.dataformat.yaml YAMLMapper

.. java:import:: io.fabric8.kubernetes.api.model ObjectMeta

.. java:import:: io.fabric8.kubernetes.api.model.apiextensions CustomResourceDefinition

.. java:import:: io.fabric8.kubernetes.api.model.apiextensions CustomResourceDefinitionList

.. java:import:: io.fabric8.kubernetes.client KubernetesClient

.. java:import:: io.fabric8.kubernetes.client KubernetesClientException

.. java:import:: io.fabric8.kubernetes.client Watcher

.. java:import:: io.fabric8.kubernetes.client.dsl MixedOperation

.. java:import:: io.fabric8.kubernetes.client.dsl NonNamespaceOperation

.. java:import:: io.fabric8.kubernetes.client.dsl Resource

.. java:import:: io.github.ust.mico.core ClusterAwarenessFabric8

.. java:import:: io.github.ust.mico.core NotInitializedException

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: javax.annotation PostConstruct

.. java:import:: java.io IOException

.. java:import:: java.io StringWriter

.. java:import:: java.util List

.. java:import:: java.util Optional

ImageBuilder
============

.. java:package:: io.github.ust.mico.core.build
   :noindex:

.. java:type:: @Slf4j @Component public class ImageBuilder

Constructors
------------
ImageBuilder
^^^^^^^^^^^^

.. java:constructor:: @Autowired public ImageBuilder(ClusterAwarenessFabric8 cluster, ImageBuilderConfig config)
   :outertype: ImageBuilder

Methods
-------
build
^^^^^

.. java:method:: public Build build(String serviceName, String serviceVersion, String dockerfile, String gitUrl, String gitRevision) throws NotInitializedException
   :outertype: ImageBuilder

   :param serviceName: the name of the MICO service
   :param serviceVersion: the version of the MICO service
   :param dockerfile: the relative path to the dockerfile
   :param gitUrl: the URL to the remote git repository
   :param gitRevision: the
   :throws NotInitializedException:

createImageName
^^^^^^^^^^^^^^^

.. java:method:: public String createImageName(String serviceNameNormalized, String serviceVersion)
   :outertype: ImageBuilder

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(String buildName)
   :outertype: ImageBuilder

deleteBuild
^^^^^^^^^^^

.. java:method:: public void deleteBuild(Build build)
   :outertype: ImageBuilder

getBuild
^^^^^^^^

.. java:method:: public Build getBuild(String buildName)
   :outertype: ImageBuilder

getBuildCRD
^^^^^^^^^^^

.. java:method:: public Optional<CustomResourceDefinition> getBuildCRD()
   :outertype: ImageBuilder

getCustomResourceDefinitions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<CustomResourceDefinition> getCustomResourceDefinitions()
   :outertype: ImageBuilder

init
^^^^

.. java:method:: public void init() throws NotInitializedException
   :outertype: ImageBuilder

