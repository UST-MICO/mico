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
import org.neo4j.ogm.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Data
@RedisHash("BackgroundJob")
public class MicoBackgroundTask implements Serializable {
    public enum Type {
        IMPORT, BUILD
    }

    public enum Status {
        PENDING, RUNNING, CANCELLED, ERROR, DONE
    }

    @Id
    String id;
    CompletableFuture job;
    MicoService service;
    Status status;
    Type type;

    public MicoBackgroundTask(CompletableFuture job, MicoService service, Type type) {
        this.job = job;
        this.service = service;
        this.type = type;
        this.status = Status.PENDING;
    }

    public void cancelJob() {
        job.cancel(false);
    }

}
