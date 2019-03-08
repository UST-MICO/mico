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

import static io.github.ust.mico.core.TestConstants.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.service.ClusterAwarenessFabric8;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageBuilderTests {

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(false, true);

    private ImageBuilder imageBuilder;

    @Before
    public void setUp() {
        ClusterAwarenessFabric8 cluster = new ClusterAwarenessFabric8(mockServer.getClient());

        MicoKubernetesBuildBotConfig buildBotConfig = new MicoKubernetesBuildBotConfig();
        buildBotConfig.setNamespaceBuildExecution("build-execution-namespace");
        buildBotConfig.setKanikoExecutorImageUrl("kaniko-executor-image-url");
        buildBotConfig.setDockerRegistryServiceAccountName("service-account-name");
        buildBotConfig.setDockerImageRepositoryUrl("image-repository-url");

        imageBuilder = new ImageBuilder(cluster, buildBotConfig);
    }

    @After
    public void tearDown() {

    }

    @Test(expected = NotInitializedException.class)
    public void withoutInitializingAnErrorIsThrown() throws NotInitializedException {

        MicoService micoService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setName(NAME)
            .setVersion(SERVICE_VERSION)
            .setGitCloneUrl(GIT_TEST_REPO_URL)
            .setDockerfilePath(DOCKERFILE_PATH);

        imageBuilder.build(micoService);
    }
}
