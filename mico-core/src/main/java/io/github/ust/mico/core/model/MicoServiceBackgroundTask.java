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

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import org.neo4j.ogm.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Background job for a {@link MicoService}.
 */
@Data
@NoArgsConstructor
@RedisHash("BackgroundJob")
@Accessors(chain = true)
public class MicoServiceBackgroundTask implements Serializable {

	private static final long serialVersionUID = -8247189361567566737L;
	
	// TODO: Add JavaDoc for fields.

    @Id
    private String id;
    
	private CompletableFuture<?> job;
    
    @Indexed
    private String serviceShortName;
    
    @Indexed
    private String serviceVersion;
    
    @Indexed
    private Type type;
    
    private Status status = Status.PENDING;
    
    private String errorMessage;


	// Build contains currently build and deploy.
    public enum Type {
        BUILD, UNDEPLOY
    }

    public enum Status {
        PENDING, RUNNING, ERROR, DONE, UNDEFINED
    }
    
}
