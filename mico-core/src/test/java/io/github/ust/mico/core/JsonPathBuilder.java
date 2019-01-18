package io.github.ust.mico.core;

import io.github.ust.mico.core.model.MicoVersion;

public class JsonPathBuilder {

    public static final String ROOT = "$";
    public static final String LOCAL_ROOT = "@";
    public static final String LINKS = "_links";
    public static final String EMBEDDED = "_embedded";
    public static final String HREF = "href";
    public static final String SELF = "self";

    public static final String EQUALS = "==";

    public static String buildPath(String ...path) {
        StringBuilder resultPath = new StringBuilder(500);
        for (String pathSegment: path) {
            if (!pathSegment.startsWith(".") && !pathSegment.startsWith("[")) {
                if (resultPath.length() > 0) {
                    resultPath.append(".");
                }
            }
            resultPath.append(pathSegment);
        }
        return resultPath.toString();
    }

    public static String buildAttributePath(String attribute) {
        return buildAttributePath(attribute, true);
    }

    public static String buildAttributePath(String attribute, boolean useLocalRoot) {
        if (useLocalRoot) {
            return buildPath(LOCAL_ROOT, attribute);
        } else {
            return buildPath(ROOT, attribute);
        }
    }

    public static String buildSingleMatcher(String path, String target) {
        return buildSingleMatcher(path, target, JsonPathBuilder.EQUALS, true);
    }

    public static String buildSingleMatcher(String path, String target, boolean isString) {
        return buildSingleMatcher(path, target, JsonPathBuilder.EQUALS, isString);
    }

    public static String buildSingleMatcher(String path, String target, String operator, boolean isString) {
        StringBuilder resultMatcher = new StringBuilder();
        resultMatcher.append(path);
        resultMatcher.append(operator);
        if (isString) {
            resultMatcher.append("'");
        }
        resultMatcher.append(target);
        if (isString) {
            resultMatcher.append("'");
        }
        return resultMatcher.toString();
    }

    public static String buildVersionMatcher(MicoVersion version) {
        return buildVersionMatcher(
            String.valueOf(version.getMajorVersion()),
            String.valueOf(version.getMinorVersion()),
            String.valueOf(version.getPatchVersion())
        );
    }

    public static String buildVersionMatcher(String version) {
        String[] splitVersion = version.split("\\.");
        return buildVersionMatcher(
            splitVersion[0],
            splitVersion.length > 1 ? splitVersion[1] : null,
            splitVersion.length > 2 ? splitVersion[2] : null
        );
    }

    public static String buildVersionMatcher(String major, String minor, String patch) {
        StringBuilder resultMatcher = new StringBuilder(500);
        resultMatcher.append(buildSingleMatcher(buildPath(LOCAL_ROOT, "version", "majorVersion"), major, false));
        if (minor != null) {
            resultMatcher.append(" && ");
            resultMatcher.append(buildSingleMatcher(buildPath(LOCAL_ROOT, "version", "minorVersion"), minor, false));
        }
        if (patch != null) {
            resultMatcher.append(" && ");
            resultMatcher.append(buildSingleMatcher(buildPath(LOCAL_ROOT, "version", "patchVersion"), patch, false));
        }
        return resultMatcher.toString();
    }
}
