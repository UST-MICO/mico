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

package io.github.ust.mico.core.service.imagebuilder.knativebuild.buildtypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.client.CustomResource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Build represents a build of a container image.
 * A Build is made up of a source, and a set of steps. Steps can mount volumes to share data between themselves.
 * A build may be created by instantiating a BuildTemplate.
 * Implementation of the Build types:
 * https://github.com/knative/build/blob/release-0.4/pkg/apis/build/v1alpha1/build_types.go
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Build extends CustomResource {

    private static final long serialVersionUID = 4619424348941454158L;

    private static final String BUILD_API_VERSION = "build.knative.dev/v1alpha1";
    private static final String BUILD_KIND_NAME = "Build";

    /**
     * BuildSpec is the spec for a Build resource
     */
    private BuildSpec spec;

    /**
     * BuildStatus is the status for a Build resource
     */
    private BuildStatus status;

    public Build() {
        super.setKind(BUILD_KIND_NAME);
        super.setApiVersion(BUILD_API_VERSION);
    }

    public Build(BuildSpec spec) {
        super();
        this.spec = spec;
    }
}
