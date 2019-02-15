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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.*;

/**
 * TemplateInstantiationSpec specifies how a BuildTemplate is instantiated into a Build.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TemplateInstantiationSpec {

    /**
     * Optional. The Kind of the template to be used, possible values are BuildTemplate
     * or ClusterBuildTemplate. If nothing is specified, the default if is BuildTemplate
     */
    private String name;

    /**
     * Optional. The Kind of the template to be used, possible values are BuildTemplate
     * or ClusterBuildTemplate. If nothing is specified, the default if is BuildTemplate
     */
    private String kind;

    /**
     * Optional. Arguments, if specified, lists values that should be applied to the
     * parameters specified by the template.
     */
    private ArgumentSpec arguments;

    /**
     * Optional. Env, if specified will provide variables to all build template steps.
     * This will override any of the template's steps environment variables.
     */
    private EnvVar env;
}
