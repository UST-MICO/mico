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

    public static final MicoVersion VERSION = MicoVersion.forIntegers(1, 0, 0);
    public static final MicoVersion VERSION_1_0_1 = MicoVersion.forIntegers(1, 0, 1);
    public static final MicoVersion VERSION_1_0_2 = MicoVersion.forIntegers(1, 0, 2);
    public static final MicoVersion VERSION_1_0_3 = MicoVersion.forIntegers(1, 0, 3);
    public static final String VERSION_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION);
    public static final String VERSION_1_0_1_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_1);
    public static final String VERSION_1_0_2_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_2);
    public static final String VERSION_1_0_3_MATCHER = JsonPathBuilder.buildVersionMatcher(VERSION_1_0_3);

    public static final String SHORT_NAME = "ShortName";
    public static final String SHORT_NAME_1 = "ShortName1";
    public static final String SHORT_NAME_2 = "ShortName2";
    public static final String SHORT_NAME_3 = "ShortName3";
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

}
