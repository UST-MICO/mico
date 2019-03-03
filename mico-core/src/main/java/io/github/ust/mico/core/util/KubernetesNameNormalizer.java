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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Normalizes names to be valid Kubernetes resource names.
 */
@Slf4j
@Component
public class KubernetesNameNormalizer {

    private final static String REGEX_NOT_BASIC_LATIN_CHAR = "[\\P{InBasicLatin}]+";
    private final static String REGEX_NOT_VALID_CHAR = "[^-a-z0-9]";
    private final static String REGEX_WHITESPACE = "\\s+";
    private final static String REGEX_CHARS_REPLACED_BY_A_DASH = "[._]+";
    private final static String REGEX_MULTIPLE_DASHES = "[-]+";
    private final static String REGEX_MATCH_VALID_FIRST_CHAR = "^[a-z]+.*";

    /**
     * Normalizes a name so it is a valid Kubernetes resource name.
     *
     * @return the normalized name
     */
    public String normalizeName(String name) throws IllegalArgumentException {
        String result;

        // Unicode normalization
        String s1 = Normalizer.normalize(name, Normalizer.Form.NFKD);
        // Replacing all combined characters
        String s2 = new String(s1.replaceAll(REGEX_NOT_BASIC_LATIN_CHAR, "").getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // Convert to lower case
        String s3 = s2.toLowerCase();
        // Replace '_' and '.' and with a dash
        String s4 = new String(s3.replaceAll(REGEX_CHARS_REPLACED_BY_A_DASH, "-").getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // Replace whitespace characters to dashes
        String s5 = new String(s4.replaceAll(REGEX_WHITESPACE, "-").getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // Replace multiple subsequent dashes with one dash
        String s6 = new String(s5.replaceAll(REGEX_MULTIPLE_DASHES, "-").getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // Remove all invalid characters
        String s7 = new String(s6.replaceAll(REGEX_NOT_VALID_CHAR, "").getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
        // Check if name begins with a valid character
        if (s7.matches(REGEX_MATCH_VALID_FIRST_CHAR)) {
            result = s7;
        } else {
            result = "short-name-" + s7;
        }

        if (!result.matches(Patterns.KUBERNETES_NAMING_REGEX)) {
            throw new IllegalArgumentException("Name '" + name + "' could not be normalized correctly");
        }

        return result;
    }
}
