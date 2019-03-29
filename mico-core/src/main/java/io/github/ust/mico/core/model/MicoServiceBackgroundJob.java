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
package io.github.ust.mico.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * Background job for a {@link MicoService}.
 * <p>
 * Instances of this class are persisted in the Redis database.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@RedisHash("BackgroundJob")
public class MicoServiceBackgroundJob implements Serializable {

    private static final long serialVersionUID = -8247189361567566737L;

    /**
     * The generated job id.
     */
    @Id
    private String id;

    /**
     * The actual job future.
     */
    private CompletableFuture<?> future;

    /**
     * The short name of the corresponding {@link MicoService}.
     */
    @Indexed
    private String serviceShortName;

    /**
     * The version of the corresponding {@link MicoService}.
     */
    @Indexed
    private String serviceVersion;

    /**
     * The {@link Type} of this job.
     */
    @Indexed
    private Type type;

    /**
     * The current {@link Status} of this job.
     */
    private Status status = Status.PENDING;

    /**
     * An error message in case the job has failed.
     */
    private String errorMessage;


    // Build contains currently build and deploy.
    public enum Type {
        BUILD, UNDEPLOY
    }

    public enum Status {
        PENDING, RUNNING, ERROR, DONE, UNDEFINED
    }

}
