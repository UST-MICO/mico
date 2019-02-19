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

package io.github.ust.mico.core;

import io.github.ust.mico.core.model.MicoVersion;

public class TestConstants {

    public static final String BASE_URL = "http://localhost";
    public static final String SERVICES_PATH = "/services";
    public static final String DEPENDEES_SUBPATH = "/dependees";
    public static final String DEPENDERS_SUBPATH = "/dependers";

    public static final Long ID = Long.valueOf(1000);
    public static final Long ID_1 = Long.valueOf(1001);
    public static final Long ID_2 = Long.valueOf(1002);
    public static final Long ID_3 = Long.valueOf(1003);

    public static final String VERSION = MicoVersion.forIntegers(1, 0, 0).toString();
    public static final String VERSION_1_0_1 = MicoVersion.forIntegers(1, 0, 1).toString();
    public static final String VERSION_1_0_2 = MicoVersion.forIntegers(1, 0, 2).toString();
    public static final String VERSION_1_0_3 = MicoVersion.forIntegers(1, 0, 3).toString();
    public static final String VERSION_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION);
    public static final String VERSION_1_0_1_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_1);
    public static final String VERSION_1_0_2_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_2);
    public static final String VERSION_1_0_3_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_3);

    public static final String SHORT_NAME = "short-name";
    public static final String SHORT_NAME_1 = "short-name-1";
    public static final String SHORT_NAME_2 = "short-name-2";
    public static final String SHORT_NAME_3 = "short-name-3";
    public static final String SHORT_NAME_INVALID = "short_NAME";
    public static final String SHORT_NAME_ATTRIBUTE = JsonPathBuilder.buildAttributePath("shortName");
    public static final String SHORT_NAME_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME);
    public static final String SHORT_NAME_1_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_1);
    public static final String SHORT_NAME_2_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_2);
    public static final String SHORT_NAME_3_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_3);

    public static final String DESCRIPTION = "Description Service";
    public static final String DESCRIPTION_1 = "Description Service 1";
    public static final String DESCRIPTION_2 = "Description Service 2";
    public static final String DESCRIPTION_3 = "Description Service 3";
    public static final String DESCRIPTION_ATTRIBUTE = JsonPathBuilder.buildAttributePath("description");
    public static final String DESCRIPTION_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION);
    public static final String DESCRIPTION_1_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_1);
    public static final String DESCRIPTION_2_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_2);
    public static final String DESCRIPTION_3_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_3);

    public static final String SERVICE_SHORT_NAME = "serviceShortName";
    public static final String SERVICE_VERSION = "1.0.0";
    
    public static final String SERVICE_INTERFACE_NAME = "service-interface-name";


    /**
     * Git repository that is used for testing.
     * It must contain a Dockerfile and at least one release.
     */
    public static final String GIT_TEST_REPO_URL = "https://github.com/UST-MICO/hello.git";
    /**
     * Path to the Dockerfile.
     * It must be relative to the root of the Git repository.
     */
    public static final String DOCKERFILE = "Dockerfile";
    /**
     * Release tag of the release that should be used for testing.
     * Must be in in supported version format (semantic version).
     */
    public static final String RELEASE = "v1.0.0";

    /**
     * Docker image URI that is created based on the short name and the version of a service.
     */
    public static final String DOCKER_IMAGE_URI = "ustmico/" + SHORT_NAME + ":" + RELEASE;
}
