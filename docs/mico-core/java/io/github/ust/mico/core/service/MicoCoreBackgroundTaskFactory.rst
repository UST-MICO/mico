.. java:import:: java.util.concurrent CompletableFuture

.. java:import:: java.util.concurrent ExecutorService

.. java:import:: java.util.concurrent Executors

.. java:import:: java.util.function Consumer

.. java:import:: java.util.function Function

.. java:import:: java.util.function Supplier

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: io.github.ust.mico.core.configuration MicoCoreBackgroundTaskFactoryConfig

MicoCoreBackgroundTaskFactory
=============================

.. java:package:: io.github.ust.mico.core.service
   :noindex:

.. java:type:: @Component public class MicoCoreBackgroundTaskFactory

   Helper class for background task with the functionality to attach callbacks for successful termination or failure. for \ :java:ref:`CompletableFuture`\ .

Constructors
------------
MicoCoreBackgroundTaskFactory
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MicoCoreBackgroundTaskFactory(MicoCoreBackgroundTaskFactoryConfig config)
   :outertype: MicoCoreBackgroundTaskFactory

Methods
-------
runAsync
^^^^^^^^

.. java:method:: public <T> CompletableFuture<T> runAsync(Supplier<T> task)
   :outertype: MicoCoreBackgroundTaskFactory

   Returns a new CompletableFuture that is asynchronously completed by a task running in the \ :java:ref:`ExecutorService`\  defined in this class with the value obtained by calling the given supplier.

   :param <T>: the task's return type.
   :param task: a \ :java:ref:`Supplier`\  returning the value to be used to complete the returned CompletableFuture.
   :return: a new \ :java:ref:`CompletableFuture`\ .

runAsync
^^^^^^^^

.. java:method:: @SuppressWarnings public <T> CompletableFuture runAsync(Supplier<T> task, Consumer<? super T> onSuccess)
   :outertype: MicoCoreBackgroundTaskFactory

   Returns a new CompletableFuture that is asynchronously completed by a task running in the \ :java:ref:`ExecutorService`\  defined in this class with the value obtained by calling the given supplier. In case the task succeeds, the given Consumer is executed.

   :param <T>: the task's return type.
   :param task: a \ :java:ref:`Supplier`\  returning the value to be used to complete the returned CompletableFuture.
   :param onSuccess: a \ :java:ref:`Consumer`\  in case the task succeeds.
   :return: a new \ :java:ref:`CompletableFuture`\ .

runAsync
^^^^^^^^

.. java:method:: @SuppressWarnings public <T> CompletableFuture runAsync(Supplier<T> task, Consumer<? super T> onSuccess, Function<Throwable, ? extends Void> onError)
   :outertype: MicoCoreBackgroundTaskFactory

   Returns a new CompletableFuture that is asynchronously completed by a task running in the \ :java:ref:`ExecutorService`\  defined in this class with the value obtained by calling the given supplier. In case the task succeeds, the given Consumer is executed, otherwise (on failure) the given Function is executed.

   :param <T>: the task's return type.
   :param task: a \ :java:ref:`Supplier`\  returning the value to be used to complete the returned CompletableFuture.
   :param onSuccess: a \ :java:ref:`Consumer`\  in case the task succeeds.
   :param onError: a \ :java:ref:`Function`\  in case the task fails.
   :return: a new \ :java:ref:`CompletableFuture`\ .

