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

package io.github.ust.mico.core.configuration;

import io.github.ust.mico.core.service.imagebuilder.knativebuild.KnativeBuildController;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Configuration of the build bot ({@link KnativeBuildController})
 */
@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "kubernetes.build-bot")
public class MicoKubernetesBuildBotConfig {

    /**
     * The name of the namespace in which the {@link KnativeBuildController}
     * builds the images.
     */
    @NotBlank
    private String namespaceBuildExecution;

    /**
     * The Docker image repository that is used by MICO to store the images
     * that are build by {@link KnativeBuildController}.
     */
    @NotBlank
    private String dockerImageRepositoryUrl;

    /**
     * The service account name to have write access to the specified docker image repository.
     */
    @NotBlank
    private String dockerRegistryServiceAccountName;

    /**
     * The url to the kaniko executor image that is used by
     * {@link KnativeBuildController}
     */
    @NotBlank
    private String kanikoExecutorImageUrl;

    /**
     * The timeout in seconds after which the build is stopped.
     * Minimum is set to 30 seconds because that is the minimum time for a build.
     * Defaults to 10 minutes (600 seconds).
     */
    @NotNull
    @Min(value = 30, message = "must be at least set to 30 seconds")
    private int buildTimeout = 600;

    /**
     * Boolean value to set whether an undeployment of a MicoApplication
     * should also clean up all build resources associated with the MicoServices
     * included by the MicoApplication.
     */
    @NotNull
    private boolean buildCleanUpByUndeploy = false;
}
