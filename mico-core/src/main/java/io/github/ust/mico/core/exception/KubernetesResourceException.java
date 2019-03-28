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

package io.github.ust.mico.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Used to indicate that there is a problem concerning
 * a Kubernetes resource, e.g., a Deployment cannot be found
 * or there are multiple results for a query for a resource
 * that is expected to be unique.
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class KubernetesResourceException extends Exception {

    private static final long serialVersionUID = 6203468717140353398L;

    public KubernetesResourceException() {
        super();
    }
    
    public KubernetesResourceException(String message) {
        super(message);
    }

    public KubernetesResourceException (Throwable cause) {
        super (cause);
    }

    public KubernetesResourceException (String message, Throwable cause) {
        super (message, cause);
    }
}
