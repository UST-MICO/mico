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
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * BackgroundJob for one {@link MicoService}.
 */
@Data
@RedisHash("BackgroundJob")
@Accessors(chain = true)
public class MicoBackgroundTask implements Serializable {
    // Build contains currently build and deploy.
    public enum Type {
        BUILD, UNDEPLOY
    }

    public enum Status {
        PENDING, RUNNING, ERROR, DONE, UNDEFINED
    }

    @Id
    String id;
    CompletableFuture job;
    @Indexed
    String micoServiceShortName;
    @Indexed
    String micoServiceVersion;
    Status status;
    String errorMessage;
    @Indexed
    Type type;

    /**
     * Constructor which sets the status to {@link Status#PENDING}
     */
    public MicoBackgroundTask() {
        this.status = Status.PENDING;
    }
}
