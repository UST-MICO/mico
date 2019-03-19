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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.ust.mico.core.configuration.MicoCoreBackgroundJobFactoryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

/**
 * Helper class for background job with the functionality
 * to attach callbacks for successful termination or failure.
 * for {@link CompletableFuture}.
 */
@Component
public class MicoCoreBackgroundJobFactory {
    
    // The configuration for this background job factory.
    private MicoCoreBackgroundJobFactoryConfig config;
    
    // Executor service handling all background jobs with a fixed number of threads.
    private ExecutorService executorService;
    
    @Autowired
    public MicoCoreBackgroundJobFactory(MicoCoreBackgroundJobFactoryConfig config) {
        this.config = config;
        executorService = Executors.newFixedThreadPool(this.config.getThreadPoolSize());
    }
    
    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a job running in the {@link ExecutorService} defined in this class
     * with the value obtained by calling the given supplier.
     * 
     * @param <T> the job's return type.
     * @param job a {@link Supplier} returning the value to be used
     *        to complete the returned CompletableFuture.
     * @return a new {@link CompletableFuture}.
     */
    public <T> CompletableFuture<T> runAsync(Supplier<T> job) {
        return CompletableFuture.supplyAsync(job, executorService);
    }
    
    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a job running in the {@link ExecutorService} defined in this class
     * with the value obtained by calling the given supplier. In case the job
     * succeeds, the given Consumer is executed.
     * 
     * @param <T> the job's return type.
     * @param job a {@link Supplier} returning the value to be used
     *        to complete the returned CompletableFuture.
     * @param onSuccess a {@link Consumer} in case the job succeeds.
     * @return a new {@link CompletableFuture}.
     */
    @SuppressWarnings("rawtypes")
    public <T> CompletableFuture runAsync(Supplier<T> job, Consumer<? super T> onSuccess) {
        return CompletableFuture.supplyAsync(job, executorService).thenAccept(onSuccess);
    }

    /**
     * Returns a new CompletableFuture that is asynchronously completed
     * by a job running in the {@link ExecutorService} defined in this class
     * with the value obtained by calling the given supplier. In case the job
     * succeeds, the given Consumer is executed, otherwise (on failure) the given
     * Function is executed.
     *
     * @param <T> the job's return type.
     * @param job a {@link Supplier} returning the value to be used
     *        to complete the returned CompletableFuture.
     * @param onSuccess a {@link Consumer} in case the job succeeds.
     * @param onError a {@link Function} in case the job fails.
     * @return a new {@link CompletableFuture}.
     */
    @SuppressWarnings("rawtypes")
    public <T> CompletableFuture runAsync(Supplier<T> job, Consumer<? super T> onSuccess, Function<Throwable, ? extends Void> onError) {
        return CompletableFuture.supplyAsync(job, executorService).thenAccept(onSuccess).exceptionally(onError);
    }


    /**
     * Waits for *all* futures to complete and returns a list of results.
     * If *any* future completes exceptionally then the resulting future will also complete exceptionally.
     *
     * @param futures the {@link CompletableFuture CompletableFutures}
     * @param <T> the generic type of the {@link CompletableFuture CompletableFutures}
     * @return the list of {@link CompletableFuture CompletableFutures}
     */
    public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[futures.size()]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(toList())
            );
    }

}
