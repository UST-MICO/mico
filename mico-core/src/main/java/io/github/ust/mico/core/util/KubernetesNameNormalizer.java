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

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import org.springframework.stereotype.Component;

/**
 * Normalizes names to be valid Kubernetes resource names.
 */
@Component
public class KubernetesNameNormalizer {

    /**
     * A max limit of the MICO names ({@link MicoApplication}, {@link MicoService} and {@link MicoServiceInterface}) is
     * required because they are used as values of Kubernetes labels that have a limit of 63. Furthermore the name is
     * used to create a UID that adds 9 characters to it. Therefore the limit have to be set to 54.
     */
    public final static int MICO_NAME_MAX_SIZE = 54;
    private final static String REGEX_NOT_BASIC_LATIN_CHAR = "[\\P{InBasicLatin}]+";
    private final static String REGEX_NOT_VALID_CHAR = "[^-a-z0-9]";
    private final static String REGEX_WHITESPACE = "\\s+";
    private final static String REGEX_CHARS_REPLACED_BY_A_DASH = "[._]+";
    private final static String REGEX_MULTIPLE_DASHES = "[-]+";
    private final static String REGEX_FIRST_OR_LAST_CHAR_IS_A_DASH = "^-|-$";
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
        String s4 = s3.replaceAll(REGEX_CHARS_REPLACED_BY_A_DASH, "-");
        // Replace whitespace characters to dashes
        String s5 = s4.replaceAll(REGEX_WHITESPACE, "-");
        // Remove all invalid characters
        String s6 = s5.replaceAll(REGEX_NOT_VALID_CHAR, "");
        // Replace multiple subsequent dashes with one dash
        String s7 = s6.replaceAll(REGEX_MULTIPLE_DASHES, "-");
        // Remove a dash if it is the first or last character
        String s8 = s7.replaceAll(REGEX_FIRST_OR_LAST_CHAR_IS_A_DASH, "");
        // Check if name begins with a valid character
        if (s8.matches(REGEX_MATCH_VALID_FIRST_CHAR)) {
            result = s8;
        } else {
            result = "short-name-" + s8;
        }

        if (!result.matches(Patterns.KUBERNETES_NAMING_REGEX) || result.length() > MICO_NAME_MAX_SIZE) {
            throw new IllegalArgumentException("Name '" + name + "' could not be normalized correctly");
        }

        return result;
    }

    /**
     * Creates a build name based on the short name and version of a service.
     *
     * @param serviceShortName the short name of the {@link MicoService}.
     * @param serviceVersion   the version of the {@link MicoService}.
     * @return the name of the //@link Build.
     */
    public String createBuildName(String serviceShortName, String serviceVersion) {
        return normalizeName("build-" + serviceShortName + "-" + serviceVersion);
    }

    /**
     * Creates a build name based on a service.
     *
     * @param service the {@link MicoService}.
     * @return the name of the //@link Build.
     */
    public String createBuildName(MicoService service) {
        return createBuildName(service.getShortName(), service.getVersion());
    }
}
