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
     * (DNS_LABEL): An alphanumeric (a-z, and 0-9) string, with a maximum length of 63 characters, with the '-'
     * character allowed anywhere except the first or last character, suitable for use as a hostname or segment in a domain name.
     */
    private static final String DNS_SUBDOMAIN_REGEX = "^[a-z]([-a-z0-9]*[a-z0-9])?$";

    /**
     * OpenFaaS function names must be a valid DNS-1123 subdomain.
     */
    public static final String OPEN_FAAS_FUNCTION_NAME_REGEX = DNS_SUBDOMAIN_REGEX;

    /**
     * Message is used if a match with the {@link Patterns#OPEN_FAAS_FUNCTION_NAME_REGEX} fails.
     */
    public static final String OPEN_FAAS_FUNCTION_NAME_MESSAGE = "must be a valid OpenFaaS function name: " +
        "Consist of lower case alphanumeric characters and '-', and must start with a letter";

    /**
     * Kubernetes resource names must be a valid DNS-1123 subdomain.
     */
    public static final String KUBERNETES_NAMING_REGEX = DNS_SUBDOMAIN_REGEX;

    /**
     * Message is used if a match with the {@link Patterns#KUBERNETES_NAMING_REGEX} fails.
     */
    public static final String KUBERNETES_NAMING_MESSAGE = "must be a valid Kubernetes name: " +
        "Consist of lower case alphanumeric characters, '-' or '.', and must start with a letter";

    /**
     * Kubernetes label prefix (optional segment of label keys) is optional.
     * If specified, the prefix must be a DNS subdomain: a series of DNS labels separated by dots (.),
     * not longer than 253 characters in total.
     */
    private static final String KUBERNETES_LABEL_PREFIX_REGEX = "[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*";

    /**
     * Kubernetes label names (part of both key and values)  must consist of alphanumeric characters, '-', '_' or '.',
     * and must start and end with an alphanumeric character. Max length is 63 characters.
     */
    private static final String KUBERNETES_LABEL_NAME_REGEX = "[a-z0-9A-Z][a-z0-9A-Z-_.]*[a-z0-9A-Z]";

    /**
     * Valid label keys have two segments: an optional prefix and name, separated by a slash (/).
     * The name segment is required and must be 63 characters or less, beginning and ending with an alphanumeric character ([a-z0-9A-Z])
     * with dashes (-), underscores (_), dots (.), and alphanumerics between.
     * The prefix is optional. If specified, the prefix must be a DNS subdomain:
     * a series of DNS labels separated by dots (.), not longer than 253 characters in total, followed by a slash (/).
     */
    public static final String KUBERNETES_LABEL_KEY_REGEX = "^(" + KUBERNETES_LABEL_PREFIX_REGEX + "\\/)?" + KUBERNETES_LABEL_NAME_REGEX + "$";

    /**
     * Message is used if a match with the {@link Patterns#KUBERNETES_LABEL_KEY_REGEX} fails.
     */
    public static final String KUBERNETES_LABEL_KEY_MESSAGE = "must be a valid Kubernetes label key: " +
        "Valid label keys have two segments: an optional prefix and name, separated by a slash (/). " +
        "The name segment is required and must be 63 characters or less, beginning and ending with an alphanumeric character ([a-z0-9A-Z]) " +
        "with dashes (-), underscores (_), dots (.), and alphanumerics between. " +
        "The prefix is optional. If specified, the prefix must be a DNS subdomain: " +
        "a series of DNS labels separated by dots (.), not longer than 253 characters in total, followed by a slash (/).";

    /**
     * Kubernetes label values must be 63 characters or less and must be empty or begin and end
     * with an alphanumeric character ([a-z0-9A-Z]) with dashes (-), underscores (_), dots (.), and alphanumerics between.
     */
    public static final String KUBERNETES_LABEL_VALUE_REGEX = "^" + KUBERNETES_LABEL_NAME_REGEX + "$";

    /**
     * Message is used if a match with the {@link Patterns#KUBERNETES_LABEL_VALUE_REGEX} fails.
     */
    public static final String KUBERNETES_LABEL_VALUE_MESSAGE = "must be a valid Kubernetes label value: " +
        "Begin and end with an alphanumeric character ([a-z0-9A-Z]) with dashes (-), underscores (_), dots (.), and alphanumerics between.";

    /**
     * Kubernetes environment variable names must only contain letters, numbers and underscores, and must not start with a digit.
     */
    public static final String KUBERNETES_ENV_VAR_NAME_REGEX = "^[A-Za-z_][A-Za-z0-9_]*$";

    /**
     * Message is used if a match with the {@link Patterns#KUBERNETES_ENV_VAR_NAME_REGEX} fails.
     */
    public static final String KUBERNETES_ENV_VAR_NAME_MESSAGE = "must only contain letters, numbers and underscores, " +
        "and must not start with a digit.";

    /**
     * Regex for strings that MUST be a relative path.
     */
    public static final String RELATIVE_PATH_REGEX = "^(?!/.*$).*";

    /**
     * Regex to ensure to only use letters (may be empty).
     */
    public static final String ONLY_LETTERS_OR_EMPTY_REGEX = "^[a-zA-Z]*$";

    private static final String SEMANTIC_VERSION_MAJOR_MINOR_PATCH_REGEX =
        "(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)";
    private static final String SEMANTIC_VERSION_PRE_RELEASE_TAG_REGEX =
        "(((0|[1-9]\\d*|\\d*[A-Z-a-z-][\\dA-Za-z-]*))(\\.(0|[1-9]\\d*|\\d*[A-Za-z-][\\dA-Za-z-]*))*)";
    private static final String SEMANTIC_VERSION_BUILD_METADATA_TAG_REGEX =
        "([\\dA-Za-z-]+(\\.[\\dA-Za-z-]*)*)";
    private static final String SEMANTIC_VERSION_TEMP_REGEX = SEMANTIC_VERSION_MAJOR_MINOR_PATCH_REGEX +
        "(-" + SEMANTIC_VERSION_PRE_RELEASE_TAG_REGEX + ")?" +
        "(\\+" + SEMANTIC_VERSION_BUILD_METADATA_TAG_REGEX + ")?";

    /**
     * Regex for a semantic version.
     */
    public static final String SEMANTIC_VERSION_REGEX = "^" + SEMANTIC_VERSION_TEMP_REGEX + "$";

    /**
     * Regex for a semantic version with a prefix (optional) consisting of letters.
     */
    public static final String SEMANTIC_VERSION_WITH_PREFIX_REGEX = "^[a-zA-Z]*" + SEMANTIC_VERSION_TEMP_REGEX + "$";

    /**
     * Message is used if a match with the {@link Patterns#SEMANTIC_VERSION_WITH_PREFIX_REGEX} fails.
     */
    public static final String SEMANTIC_VERSIONING_MESSAGE = "must be using Semantic Versioning";

    /**
     * Kafka topic names must only contain letters, numbers, dots, underscores and minus symbols.
     */
    public static final String KAFKA_TOPIC_NAME_REGEX = "[a-zA-Z0-9\\._\\-]+";

    /**
     * Message is used if a match with the {@link Patterns#KAFKA_TOPIC_NAME_REGEX} fails.
     */
    public static final String KAFKA_TOPIC_NAME_MESSAGE = "must be a valid Kafka topic name: " +
        "Must not contain a character other than ASCII alphanumerics, '.', '_' and '-'.";

}
