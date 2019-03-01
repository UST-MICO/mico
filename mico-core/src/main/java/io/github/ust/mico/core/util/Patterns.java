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

/**
 * Contains regular expressions that are used for pattern matching.
 */
public class Patterns {

    /**
     * Regex for strings that MUST NOT be empty.
     */
    public static final String NOT_EMPTY_REGEX = ".*\\S.*";

    /**
     * Kubernetes resource names must be a valid DNS-1123 subdomain.
     * The original regex is: [a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*
     * We use a slightly different regex for the validation. We don't allow ot start with a digit (must be a letter).
     * Furthermore we don't allow to use dots.
     */
    public static final String KUBERNETES_NAMING_REGEX = "^[a-z]([-a-z0-9]*[a-z0-9])?$";

    /**
     * Message is used if a match with the {@link Patterns#KUBERNETES_NAMING_REGEX} fails.
     */
    public static final String KUBERNETES_NAMING_MESSAGE = "must be a valid Kubernetes name: " +
        "Consist of lower case alphanumeric characters, '-' or '.', and must start with a letter";

    /**
     * Regex for strings that MUST be a relative path.
     */
    public static final String RELATIVE_PATH_REGEX = "^(?!/.*$).*";

    /**
     * Regex for strings that MUST be a Semantic Versioning.
     */
    public static final String SEMANTIC_VERSIONING_REGEX = "(^\\w+)?(\\d+)\\.(\\d+)\\.(\\d+)(-(?:[^.\\s]+\\.)*[^.\\s]+)?";

    /**
     * Message is used if a match with the {@link Patterns#SEMANTIC_VERSIONING_REGEX} fails.
     */
    public static final String SEMANTIC_VERSIONING_MESSAGE = "must be using Semantic Versioning";
}
