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

package io.github.ust.mico.core.service.imagebuilder.buildtypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * BuildSpec is the spec for a Build resource.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(value = "Status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BuildSpec {

    /**
     * Generation is only required as a workaround
     * https://github.com/kubernetes/kubernetes/issues/58778
     */
    private double generation;

    /**
     * Steps are the steps of the build;
     * each step is run sequentially with the source mounted into /workspace.
     */
    private List<BuildStep> steps = new ArrayList<>();

    /**
     * Optional. TemplateInstantiationSpec, if specified, references a BuildTemplate resource to use to
     * populate fields in the build, and optional Arguments to pass to the
     * template. The default Kind of template is BuildTemplate
     */
    private TemplateInstantiationSpec template;

    /**
     * Optional. SourceSpec specifies the inputs to the build
     */
    private SourceSpec source;

    /**
     * Optional. Sources specifies the inputs to the build
     */
    private List<SourceSpec> sources = new ArrayList<>();

    /**
     * Optional. The name of the service account as which to run this build
     */
    private String serviceAccountName;

    /**
     * Optional. Volumes is a collection of volumes that are available to mount into the
     * steps of the build
     */
    private List<Volume> volumes = new ArrayList<>();

    /**
     * Optional. Time after which the build times out. Defaults to 10 minutes.
     * Specified build timeout should be less than 24h.
     * Refer Go's ParseDuration documentation for expected format: https://golang.org/pkg/time/#ParseDuration
     */
    private String timeout;
}
