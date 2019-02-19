package io.github.ust.mico.core;

import io.github.ust.mico.core.model.MicoVersion;

import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
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

    public static final String SERVICE_INTERFACE_NAME = "service-interface-name";

    public static final String FIRST_SERVICE = buildPath(ROOT, "serviceDeploymentInformation[0]");
    public static final String REQUESTED_REPLICAS = buildPath(FIRST_SERVICE, "requestedReplicas");
    public static final String AVAILABLE_REPLICAS = buildPath(FIRST_SERVICE, "availableReplicas");
    public static final String INTERFACES_INFORMATION = buildPath(FIRST_SERVICE, "interfacesInformation");
    public static final String INTERFACES_INFORMATION_NAME = buildPath(FIRST_SERVICE, "interfacesInformation[0].name");
    public static final String POD_INFO = buildPath(FIRST_SERVICE, "podInfo");
    public static final String POD_INFO_POD_NAME_1 = buildPath(FIRST_SERVICE, "podInfo[0].podName");
    public static final String POD_INFO_PHASE_1 = buildPath(FIRST_SERVICE, "podInfo[0].phase");
    public static final String POD_INFO_NODE_NAME_1 = buildPath(FIRST_SERVICE, "podInfo[0].nodeName");
    public static final String POD_INFO_METRICS_MEMORY_USAGE_1 = buildPath(FIRST_SERVICE, "podInfo[0].metrics.memoryUsage");
    public static final String POD_INFO_METRICS_CPU_LOAD_1 = buildPath(FIRST_SERVICE, "podInfo[0].metrics.cpuLoad");
    public static final String POD_INFO_METRICS_AVAILABLE_1 = buildPath(FIRST_SERVICE, "podInfo[0].metrics.available");
    public static final String POD_INFO_POD_NAME_2 = buildPath(FIRST_SERVICE, "podInfo[1].podName");
    public static final String POD_INFO_PHASE_2 = buildPath(FIRST_SERVICE, "podInfo[1].phase");
    public static final String POD_INFO_NODE_NAME_2 = buildPath(FIRST_SERVICE, "podInfo[1].nodeName");
    public static final String POD_INFO_METRICS_MEMORY_USAGE_2 = buildPath(FIRST_SERVICE, "podInfo[1].metrics.memoryUsage");
    public static final String POD_INFO_METRICS_CPU_LOAD_2 = buildPath(FIRST_SERVICE, "podInfo[1].metrics.cpuLoad");
    public static final String POD_INFO_METRICS_AVAILABLE_2 = buildPath(FIRST_SERVICE, "podInfo[1].metrics.available");

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
