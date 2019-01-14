package io.github.ust.mico.core.concurrency;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import lombok.experimental.UtilityClass;

/**
 * Utility class for background jobs providing factory methods
 * for {@link CompletableFuture}.
 */
@UtilityClass
@Component
public class MicoCoreBackgroundTaskFactory {
    
    // The configuration for this background task factory.
    private MicoCoreBackgroundTaskFactoryConfig config = new MicoCoreBackgroundTaskFactoryConfig();
    
    // Executor service handling all background tasks with a fixed number of threads.
    private ExecutorService executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
    
    
    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the {@link ExecutorService} defined in this class
     * with the value obtained by calling the given supplier.
     * 
     * @param <T> the task's return type.
     * @param task a {@link Supplier} returning the value to be used
     *        to complete the returned CompletableFuture.
     * @return a new {@link CompletableFuture}.
     */
    public <T> CompletableFuture<T> runAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executorService);
    }
    
    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the {@link ExecutorService} defined in this class
     * with the value obtained by calling the given supplier. In case the task
     * succeeds, the given Consumer is executed.
     * 
     * @param <T> the task's return type.
     * @param task a {@link Supplier} returning the value to be used
     *        to complete the returned CompletableFuture.
     * @param onSuccess a {@link Consumer} in case the task succeeds.
     * @return a new {@link CompletableFuture}.
     */
    public <T> CompletableFuture<Void> runAsync(Supplier<T> task, Consumer<? super T> onSuccess) {
        return CompletableFuture.supplyAsync(task, executorService).thenAccept(onSuccess);
    }
    
    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a task running in the {@link ExecutorService} defined in this class
     * with the value obtained by calling the given supplier. In case the task
     * succeeds, the given Consumer is executed, otherwise (on failure) the given
     * Function is executed.
     * 
     * @param <T> the task's return type.
     * @param task a {@link Supplier} returning the value to be used
     *        to complete the returned CompletableFuture.
     * @param onSuccess a {@link Consumer} in case the task succeeds.
     * @param onError a {@link Function} in case the task fails.
     * @return a new {@link CompletableFuture}.
     */
    public <T> CompletableFuture<Void> runAsync(Supplier<T> task, Consumer<? super T> onSuccess, Function<Throwable, ? extends Void> onError) {
//        return CompletableFuture.supplyAsync(task, executorService).exceptionally(onError).thenAccept(onSuccess);
        return CompletableFuture.supplyAsync(task, executorService).thenAccept(onSuccess).exceptionally(onError);
    }
    
}
