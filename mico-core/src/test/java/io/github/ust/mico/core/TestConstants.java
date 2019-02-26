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

import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.JsonPathBuilder.buildAttributePath;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;

public class TestConstants {

    public static final String BASE_URL = "http://localhost";
    public static final String SERVICES_PATH = "/services";
    public static final String DEPENDEES_SUBPATH = "/dependees";
    public static final String DEPENDERS_SUBPATH = "/dependers";

    public static final Long ID = Long.valueOf(1000);
    public static final Long ID_1 = Long.valueOf(1001);
    public static final Long ID_2 = Long.valueOf(1002);
    public static final Long ID_3 = Long.valueOf(1003);

    public static final String APPLICATION_NAME= "application-test-name";
    public static final String APPLICATION_NAME_OTHER= "other-application-test-name";



    public static final String VERSION = MicoVersion.forIntegers(1, 0, 0).toString();
    public static final String VERSION_1_0_1 = MicoVersion.forIntegers(1, 0, 1).toString();
    public static final String VERSION_1_0_2 = MicoVersion.forIntegers(1, 0, 2).toString();
    public static final String VERSION_1_0_3 = MicoVersion.forIntegers(1, 0, 3).toString();
    public static final String VERSION_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION);
    public static final String VERSION_1_0_1_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_1);
    public static final String VERSION_1_0_2_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_2);
    public static final String VERSION_1_0_3_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_3);

    public static final String SHORT_NAME = "short-name";
    public static final String SHORT_NAME_OTHER = "other-short-name";
    public static final String SHORT_NAME_1 = "short-name-1";
    public static final String SHORT_NAME_2 = "short-name-2";
    public static final String SHORT_NAME_3 = "short-name-3";
    public static final String SHORT_NAME_INVALID = "short_NAME";
    public static final String SHORT_NAME_ATTRIBUTE = buildAttributePath("shortName");
    public static final String SHORT_NAME_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME);
    public static final String SHORT_NAME_1_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_1);
    public static final String SHORT_NAME_2_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_2);
    public static final String SHORT_NAME_3_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_3);

    public static final String DESCRIPTION = "Description Service";
    public static final String DESCRIPTION_1 = "Description Service 1";
    public static final String DESCRIPTION_2 = "Description Service 2";
    public static final String DESCRIPTION_3 = "Description Service 3";
    public static final String DESCRIPTION_ATTRIBUTE = buildAttributePath("description");
    public static final String DESCRIPTION_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION);
    public static final String DESCRIPTION_1_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_1);
    public static final String DESCRIPTION_2_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_2);
    public static final String DESCRIPTION_3_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_3);

    public static final String SERVICE_SHORT_NAME = "serviceShortName";
    public static final String SERVICE_VERSION = "1.0.0";

    public static final String SERVICE_INTERFACE_NAME = "service-interface-name";
    public static final String SERVICE_INTERFACE_NAME_1 = "service-interface-name-1";
    public static final String SERVICE_NAME = "test-service";

    /*
     * For tests in ApplicationControllerTests, one service is added to the list of MicoServiceStatusDTOs in MicoApplicationStatusDTO.
     * All paths are build on the path for the status of this service.
     */
    /**
     * Path of a single {@link io.github.ust.mico.core.dto.MicoServiceStatusDTO} in a {@link io.github.ust.mico.core.dto.MicoApplicationStatusDTO}.
     * Contains status information for this service.
     */
    public static final String SERVICE_STATUS_PATH = buildPath(ROOT, "serviceStatus[0]");
    public static final String SERVICE_INFORMATION_NAME = buildPath(SERVICE_STATUS_PATH, "name");
    public static final String REQUESTED_REPLICAS = buildPath(SERVICE_STATUS_PATH, "requestedReplicas");
    public static final String AVAILABLE_REPLICAS = buildPath(SERVICE_STATUS_PATH, "availableReplicas");
    public static final String INTERFACES_INFORMATION = buildPath(SERVICE_STATUS_PATH, "interfacesInformation");
    public static final String INTERFACES_INFORMATION_NAME = buildPath(SERVICE_STATUS_PATH, "interfacesInformation[0].name");
    public static final String POD_INFO = buildPath(SERVICE_STATUS_PATH, "podsInformation");
    public static final String POD_INFO_POD_NAME_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].podName");
    public static final String POD_INFO_PHASE_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].phase");
    public static final String POD_INFO_NODE_NAME_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].nodeName");
    public static final String POD_INFO_METRICS_MEMORY_USAGE_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].metrics.memoryUsage");
    public static final String POD_INFO_METRICS_CPU_LOAD_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].metrics.cpuLoad");
    public static final String POD_INFO_METRICS_AVAILABLE_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].metrics.available");
    public static final String POD_INFO_POD_NAME_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].podName");
    public static final String POD_INFO_PHASE_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].phase");
    public static final String POD_INFO_NODE_NAME_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].nodeName");
    public static final String POD_INFO_METRICS_MEMORY_USAGE_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].metrics.memoryUsage");
    public static final String POD_INFO_METRICS_CPU_LOAD_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].metrics.cpuLoad");
    public static final String POD_INFO_METRICS_AVAILABLE_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].metrics.available");

    /*
     * For tests in ServiceControllerTests, a MicoServiceStatusDTO is used.
     * All paths are build on the base path of this object.
     */
    public static final String SERVICE_DTO_SERVICE_NAME = buildAttributePath("name");
    public static final String SERVICE_DTO_REQUESTED_REPLICAS = buildPath(ROOT, "requestedReplicas");
    public static final String SERVICE_DTO_AVAILABLE_REPLICAS = buildPath(ROOT, "availableReplicas");
    public static final String SERVICE_DTO_INTERFACES_INFORMATION = buildPath(ROOT, "interfacesInformation");
    public static final String SERVICE_DTO_INTERFACES_INFORMATION_NAME = buildPath(ROOT, "interfacesInformation[0].name");
    public static final String SERVICE_DTO_POD_INFO = buildPath(ROOT, "podsInformation");
    public static final String SERVICE_DTO_POD_INFO_POD_NAME_1 = buildPath(ROOT, "podsInformation[0].podName");
    public static final String SERVICE_DTO_POD_INFO_PHASE_1 = buildPath(ROOT, "podsInformation[0].phase");
    public static final String SERVICE_DTO_POD_INFO_NODE_NAME_1 = buildPath(ROOT, "podsInformation[0].nodeName");
    public static final String SERVICE_DTO_POD_INFO_METRICS_MEMORY_USAGE_1 = buildPath(ROOT, "podsInformation[0].metrics.memoryUsage");
    public static final String SERVICE_DTO_POD_INFO_METRICS_CPU_LOAD_1 = buildPath(ROOT, "podsInformation[0].metrics.cpuLoad");
    public static final String SERVICE_DTO_POD_INFO_METRICS_AVAILABLE_1 = buildPath(ROOT, "podsInformation[0].metrics.available");
    public static final String SERVICE_DTO_POD_INFO_POD_NAME_2 = buildPath(ROOT, "podsInformation[1].podName");
    public static final String SERVICE_DTO_POD_INFO_PHASE_2 = buildPath(ROOT, "podsInformation[1].phase");
    public static final String SERVICE_DTO_POD_INFO_NODE_NAME_2 = buildPath(ROOT, "podsInformation[1].nodeName");
    public static final String SERVICE_DTO_POD_INFO_METRICS_MEMORY_USAGE_2 = buildPath(ROOT, "podsInformation[1].metrics.memoryUsage");
    public static final String SERVICE_DTO_POD_INFO_METRICS_CPU_LOAD_2 = buildPath(ROOT, "podsInformation[1].metrics.cpuLoad");
    public static final String SERVICE_DTO_POD_INFO_METRICS_AVAILABLE_2 = buildPath(ROOT, "podsInformation[1].metrics.available");

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
