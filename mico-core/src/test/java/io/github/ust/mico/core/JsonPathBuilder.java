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

public class JsonPathBuilder {

    public static final String ROOT = "$";
    public static final String LOCAL_ROOT = "@";
    public static final String LINKS = "_links";
    public static final String EMBEDDED = "_embedded";
    public static final String HREF = "href";
    public static final String SELF = "self";

    public static final String EQUALS = "==";
    public static final String SELF_HREF = buildPath(SELF,HREF);
    public static final String LINKS_SELF_HREF = buildPath(LINKS,SELF_HREF);
    public static final String FIRST_ELEMENT = "[0]";
    public static final String VERSION = "version";
    public static final String SHORT_NAME = "shortName";

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
        return buildVersionMatcher(version.toString());
    }

    public static String buildVersionMatcher(String version) {
        return buildSingleMatcher(buildPath(LOCAL_ROOT, "version"), version);
    }
}
