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

import static io.github.ust.mico.core.JsonPathBuilder.*;

class TestConstants {

    public static final String STRING_ID = "2b4fd36e-42fe-4e42-be6f-dd1ef743b69e";
    static final String BASE_URL = "http://localhost";
    static final String SERVICES_PATH = "/services";
    static final String DEPENDEES_SUBPATH = "/dependees";
    static final String DEPENDERS_SUBPATH = "/dependers";

    static final Long ID = 1000L;
    static final Long ID_1 = 1001L;
    static final Long ID_2 = 1002L;
    static final Long ID_3 = 1003L;
    static final Long ID_4 = 1004L;

    static final String VERSION = MicoVersion.forIntegers(1, 0, 0).toString();
    static final String VERSION_1_0_1 = MicoVersion.forIntegers(1, 0, 1).toString();
    static final String VERSION_1_0_2 = MicoVersion.forIntegers(1, 0, 2).toString();
    static final String VERSION_1_0_3 = MicoVersion.forIntegers(1, 0, 3).toString();
    static final String VERSION_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION);
    static final String VERSION_1_0_1_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_1);
    static final String VERSION_1_0_2_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_2);
    static final String VERSION_1_0_3_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_3);

    static final String SHORT_NAME = "short-name";
    static final String SHORT_NAME_OTHER = "other-short-name";
    static final String SHORT_NAME_1 = "short-name-1";
    static final String SHORT_NAME_2 = "short-name-2";
    static final String SHORT_NAME_3 = "short-name-3";
    static final String SHORT_NAME_INVALID = "short_NAME";
    static final String SHORT_NAME_ATTRIBUTE = buildAttributePath("shortName");
    static final String SHORT_NAME_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME);
    static final String SHORT_NAME_1_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_1);
    static final String SHORT_NAME_2_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_2);
    static final String SHORT_NAME_3_MATCHER = JsonPathBuilder.buildSingleMatcher(SHORT_NAME_ATTRIBUTE, SHORT_NAME_3);

    static final String NAME = "test-name";
    static final String NAME_1 = "test-name-1";
    static final String NAME_2 = "test-name-2";
    static final String NAME_3 = "test-name-3";
    static final String NAME_ATTRIBUTE = JsonPathBuilder.buildAttributePath("name");
    static final String NAME_MATCHER = JsonPathBuilder.buildSingleMatcher(NAME_ATTRIBUTE, NAME);
    static final String NAME_1_MATCHER = JsonPathBuilder.buildSingleMatcher(NAME_ATTRIBUTE, NAME_1);
    static final String NAME_2_MATCHER = JsonPathBuilder.buildSingleMatcher(NAME_ATTRIBUTE, NAME_2);
    static final String NAME_3_MATCHER = JsonPathBuilder.buildSingleMatcher(NAME_ATTRIBUTE, NAME_3);

    static final String DESCRIPTION = "Description";
    static final String DESCRIPTION_1 = "Description 1";
    static final String DESCRIPTION_2 = "Description 2";
    static final String DESCRIPTION_3 = "Description 3";
    static final String DESCRIPTION_ATTRIBUTE = JsonPathBuilder.buildAttributePath("description");
    static final String DESCRIPTION_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION);
    static final String DESCRIPTION_1_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_1);
    static final String DESCRIPTION_2_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_2);
    static final String DESCRIPTION_3_MATCHER = JsonPathBuilder.buildSingleMatcher(DESCRIPTION_ATTRIBUTE, DESCRIPTION_3);

    static final String SERVICE_SHORT_NAME = "service-short-name";
    static final String SERVICE_SHORT_NAME_1 = "service-short-name-1";

    static final String SERVICE_VERSION = "1.0.0";

    static final String SERVICE_INTERFACE_NAME = "service-interface-name";
    static final String SERVICE_INTERFACE_NAME_1 = "service-interface-name-1";

    static final String OWNER = "owner";

    static final String INSTANCE_ID = "instance-id";
    static final String INSTANCE_ID_1 = "instance-id-1";
    static final String INSTANCE_ID_2 = "instance-id-2";

    static final String INPUT_TOPIC = "input-topic";
    static final String INPUT_TOPIC_1 = "input-topic-1";
    static final String OUTPUT_TOPIC = "output-topic";
    static final String OUTPUT_TOPIC_1 = "output-topic-1";

    static final String OPEN_FAAS_FUNCTION_NAME = "open-faas-function-name";
    static final String OPEN_FAAS_FUNCTION_NAME_1 = "open-faas-function-name-1";

    /*
     * For tests in ApplicationResourceIntegrationTests, one service is added to the list of MicoServiceStatusDTOs in MicoApplicationStatusDTO.
     * All paths are build on the path for the status of this service.
     */
    static final String SERVICE_STATUS_PATH = buildPath(ROOT, "serviceStatuses[0]");
    static final String SERVICE_INFORMATION_NAME = buildPath(SERVICE_STATUS_PATH, "name");
    static final String REQUESTED_REPLICAS = buildPath(SERVICE_STATUS_PATH, "requestedReplicas");
    static final String AVAILABLE_REPLICAS = buildPath(SERVICE_STATUS_PATH, "availableReplicas");
    static final String NODE_METRICS_PATH_FIRST_ELEMENT = buildPath(SERVICE_STATUS_PATH, "nodeMetrics[0]");
    static final String NODE_METRICS_NAME = buildPath(NODE_METRICS_PATH_FIRST_ELEMENT, "nodeName");
    static final String NODE_METRICS_AVERAGE_CPU_LOAD = buildPath(NODE_METRICS_PATH_FIRST_ELEMENT, "averageCpuLoad");
    static final String NODE_METRICS_AVERAGE_MEMORY_USAGE = buildPath(NODE_METRICS_PATH_FIRST_ELEMENT, "averageMemoryUsage");
    static final String ERROR_MESSAGES = buildPath(SERVICE_STATUS_PATH, "errorMessages");
    static final String INTERFACES_INFORMATION = buildPath(SERVICE_STATUS_PATH, "interfacesInformation");
    static final String INTERFACES_INFORMATION_NAME = buildPath(SERVICE_STATUS_PATH, "interfacesInformation[0].name");
    static final String POD_INFO = buildPath(SERVICE_STATUS_PATH, "podsInformation");
    static final String POD_INFO_POD_NAME_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].podName");
    static final String POD_INFO_PHASE_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].phase");
    static final String POD_INFO_NODE_NAME_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].nodeName");
    static final String POD_INFO_METRICS_MEMORY_USAGE_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].metrics.memoryUsage");
    static final String POD_INFO_METRICS_CPU_LOAD_1 = buildPath(SERVICE_STATUS_PATH, "podsInformation[0].metrics.cpuLoad");
    static final String POD_INFO_POD_NAME_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].podName");
    static final String POD_INFO_PHASE_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].phase");
    static final String POD_INFO_NODE_NAME_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].nodeName");
    static final String POD_INFO_METRICS_MEMORY_USAGE_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].metrics.memoryUsage");
    static final String POD_INFO_METRICS_CPU_LOAD_2 = buildPath(SERVICE_STATUS_PATH, "podsInformation[1].metrics.cpuLoad");
    static final String TOTAL_NUMBER_OF_MICO_SERVICES = buildPath(ROOT, "totalNumberOfMicoServices");
    static final String TOTAL_NUMBER_OF_AVAILABLE_REPLICAS = buildPath(ROOT, "totalNumberOfAvailableReplicas");
    static final String TOTAL_NUMBER_OF_REQUESTED_REPLICAS = buildPath(ROOT, "totalNumberOfRequestedReplicas");
    static final String TOTAL_NUMBER_OF_PODS = buildPath(ROOT, "totalNumberOfPods");
    static final String APPLICATION_DEPLOYMENT_STATUS_RESPONSE_DTO = buildPath(ROOT, "applicationDeploymentStatusResponseDTO");


    /*
     * For tests in ServiceResourceIntegrationTests, a MicoServiceStatusDTO is used.
     * All paths are build on the base path of this object.
     */
    static final String SERVICE_DTO_SERVICE_NAME = buildAttributePath("name");
    static final String SERVICE_DTO_REQUESTED_REPLICAS = buildPath(ROOT, "requestedReplicas");
    static final String SERVICE_DTO_AVAILABLE_REPLICAS = buildPath(ROOT, "availableReplicas");
    static final String SERVICE_DTO_NODE_METRICS = buildPath(ROOT, "nodeMetrics[0]");
    static final String SERVICE_DTO_NODE_NAME = buildPath(SERVICE_DTO_NODE_METRICS, "nodeName");
    static final String SERVICE_DTO_NODE_METRICS_AVERAGE_CPU_LOAD = buildPath(SERVICE_DTO_NODE_METRICS, "averageCpuLoad");
    static final String SERVICE_DTO_NODE_METRICS_AVERAGE_MEMORY_USAGE = buildPath(SERVICE_DTO_NODE_METRICS, "averageMemoryUsage");
    static final String SERVICE_DTO_ERROR_MESSAGES = buildPath(ROOT, "errorMessages");
    static final String SERVICE_DTO_INTERFACES_INFORMATION = buildPath(ROOT, "interfacesInformation");
    static final String SERVICE_DTO_INTERFACES_INFORMATION_NAME = buildPath(ROOT, "interfacesInformation[0].name");
    static final String SERVICE_DTO_POD_INFO = buildPath(ROOT, "podsInformation");
    static final String SERVICE_DTO_POD_INFO_POD_NAME_1 = buildPath(ROOT, "podsInformation[0].podName");
    static final String SERVICE_DTO_POD_INFO_PHASE_1 = buildPath(ROOT, "podsInformation[0].phase");
    static final String SERVICE_DTO_POD_INFO_NODE_NAME_1 = buildPath(ROOT, "podsInformation[0].nodeName");
    static final String SERVICE_DTO_POD_INFO_METRICS_MEMORY_USAGE_1 = buildPath(ROOT, "podsInformation[0].metrics.memoryUsage");
    static final String SERVICE_DTO_POD_INFO_METRICS_CPU_LOAD_1 = buildPath(ROOT, "podsInformation[0].metrics.cpuLoad");
    static final String SERVICE_DTO_POD_INFO_POD_NAME_2 = buildPath(ROOT, "podsInformation[1].podName");
    static final String SERVICE_DTO_POD_INFO_PHASE_2 = buildPath(ROOT, "podsInformation[1].phase");
    static final String SERVICE_DTO_POD_INFO_NODE_NAME_2 = buildPath(ROOT, "podsInformation[1].nodeName");
    static final String SERVICE_DTO_POD_INFO_METRICS_MEMORY_USAGE_2 = buildPath(ROOT, "podsInformation[1].metrics.memoryUsage");
    static final String SERVICE_DTO_POD_INFO_METRICS_CPU_LOAD_2 = buildPath(ROOT, "podsInformation[1].metrics.cpuLoad");

    static final String SDI_REPLICAS_PATH = buildPath(ROOT, "replicas");
    static final String SDI_LABELS_PATH = buildPath(ROOT, "labels");
    static final String SDI_TOPICS_PATH = buildPath(ROOT, "topics");
    static final String SDI_IMAGE_PULLPOLICY_PATH = buildPath(ROOT, "imagePullPolicy");

    static class IntegrationTest {
        static final String APPLICATION_SHORT_NAME = "hello";
        static final String APPLICATION_NAME = "hello-application";
        static final String APPLICATION_VERSION = "v0.0.1";
        static final String APPLICATION_DESCRIPTION = "Hello World Application";

        static final String SERVICE_SHORT_NAME = "hello-integration-test";
        static final String SERVICE_NAME = "UST-MICO/hello";
        static final String SERVICE_DESCRIPTION = "Hello World Service for integration testing";

        static final String SERVICE_INTERFACE_NAME = "hello-interface";

        /**
         * Path to the Dockerfile. It must be relative to the root of the Git repository.
         */
        static final String DOCKERFILE_PATH = "Dockerfile";

        /**
         * Git repository that is used for testing. It must contain a Dockerfile and at least one release.
         */
        static final String GIT_CLONE_URL = "https://github.com/UST-MICO/hello.git";

        /**
         * Release tag of the release that should be used for testing. Must be in in supported version format (semantic
         * versioning with a prefix that only consists of letters).
         */
        static final String RELEASE = "v1.0.0";

        /**
         * Resulting docker image URI that is created based on the short name and the version of a service.
         */
        static final String DOCKER_IMAGE_URI = "ustmico/" + SERVICE_SHORT_NAME + ":" + RELEASE;

        /**
         * Port of the externally exposed port.
         */
        static final int PORT = 80;

        /**
         * Port inside the container.
         */
        static final int TARGET_PORT = 80;
    }
}
