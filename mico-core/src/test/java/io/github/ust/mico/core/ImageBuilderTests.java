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

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.TestConstants.*;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.KubernetesNameNormalizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static io.github.ust.mico.core.TestConstants.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class ImageBuilderTests {

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(false, true);

    private ImageBuilder imageBuilder;

    @Before
    public void setUp() {
        MicoKubernetesBuildBotConfig buildBotConfig = new MicoKubernetesBuildBotConfig();
        buildBotConfig.setNamespaceBuildExecution("build-execution-namespace");
        buildBotConfig.setKanikoExecutorImageUrl("kaniko-executor-image-url");
        buildBotConfig.setDockerRegistryServiceAccountName("service-account-name");
        buildBotConfig.setDockerImageRepositoryUrl("image-repository-url");

        KubernetesNameNormalizer kubernetesNameNormalizer = new KubernetesNameNormalizer();
        imageBuilder = new ImageBuilder(mockServer.getClient(), buildBotConfig, kubernetesNameNormalizer);
    }

    @After
    public void tearDown() {

    }

    @Test(expected = NotInitializedException.class)
    public void withoutInitializingAnErrorIsThrown() throws NotInitializedException, InterruptedException, ExecutionException, TimeoutException {

        MicoService micoService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setName(NAME)
            .setGitCloneUrl(IntegrationTest.GIT_CLONE_URL);

        imageBuilder.build(micoService);
    }
}
