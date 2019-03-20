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

package io.github.ust.mico.core.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

/**
 * Provides some utility functions for Futures / CompletableFutures.
 */
@UtilityClass
public class FutureUtils {

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
