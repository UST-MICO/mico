.. java:import:: lombok.experimental UtilityClass

.. java:import:: java.util List

.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent Future

FutureUtils
===========

.. java:package:: io.github.ust.mico.core.util
   :noindex:

.. java:type:: @UtilityClass public class FutureUtils

   Provides some utility functions for \ :java:ref:`Future`\  / \ :java:ref:`CompletableFuture`\ .

Methods
-------
all
^^^

.. java:method:: public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> futures)
   :outertype: FutureUtils

   Waits for *all* futures to complete and returns a list of results. If *any* future completes exceptionally then the resulting future will also complete exceptionally.

   :param futures: the \ :java:ref:`CompletableFutures <CompletableFuture>`\
   :param <T>: the generic type of the \ :java:ref:`CompletableFutures <CompletableFuture>`\
   :return: the list of \ :java:ref:`CompletableFutures <CompletableFuture>`\

