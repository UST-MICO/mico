/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.ust.mico.core.configuration.MicoCoreBackgroundJobFactoryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for background task with the functionality
 * to attach callbacks for successful termination or failure.
 * for {@link CompletableFuture}.
 */
@Component
@Deprecated
// TODO: Refactor the task factory. Will be covered by mico#600
public class MicoCoreBackgroundTaskFactory {
    
    // The configuration for this background task factory.
    private MicoCoreBackgroundJobFactoryConfig config;
    
    // Executor service handling all background tasks with a fixed number of threads.
    private ExecutorService executorService;
    
    @Autowired
    public MicoCoreBackgroundTaskFactory(MicoCoreBackgroundJobFactoryConfig config) {
        this.config = config;
        executorService = Executors.newFixedThreadPool(this.config.getThreadPoolSize());
    }
    
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
    @SuppressWarnings("rawtypes")
    public <T> CompletableFuture runAsync(Supplier<T> task, Consumer<? super T> onSuccess) {
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
    @SuppressWarnings("rawtypes")
    public <T> CompletableFuture runAsync(Supplier<T> task, Consumer<? super T> onSuccess, Function<Throwable, ? extends Void> onError) {
        return CompletableFuture.supplyAsync(task, executorService).thenAccept(onSuccess).exceptionally(onError);
    }

}
