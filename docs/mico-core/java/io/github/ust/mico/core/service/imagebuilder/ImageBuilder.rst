.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent ExecutionException

.. java:import:: java.util.concurrent TimeoutException

.. java:import:: io.github.ust.mico.core.exception KubernetesResourceException

.. java:import:: io.github.ust.mico.core.exception NotInitializedException

.. java:import:: io.github.ust.mico.core.model MicoService

.. java:import:: org.springframework.context.event ContextRefreshedEvent

.. java:import:: org.springframework.context.event EventListener

ImageBuilder
============

.. java:package:: io.github.ust.mico.core.service.imagebuilder
   :noindex:

.. java:type:: public interface ImageBuilder

Methods
-------
build
^^^^^

.. java:method::  CompletableFuture<String> build(MicoService micoService) throws NotInitializedException, InterruptedException, ExecutionException, TimeoutException, KubernetesResourceException
   :outertype: ImageBuilder

init
^^^^

.. java:method:: @EventListener  void init(ContextRefreshedEvent cre)
   :outertype: ImageBuilder

init
^^^^

.. java:method::  void init() throws NotInitializedException
   :outertype: ImageBuilder

isInitialized
^^^^^^^^^^^^^

.. java:method::  boolean isInitialized()
   :outertype: ImageBuilder

