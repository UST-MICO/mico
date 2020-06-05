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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.service.imagebuilder.TektonPipelinesController;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

// Is ignored because Travis can't execute integration tests
// that requires a connection to Kubernetes.
@Ignore
// TODO: Upgrade to JUnit5
@Category(IntegrationTests.class)
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class ImageBuilderIntegrationTests {

    @Autowired
    private TektonPipelinesController imageBuilder;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    @Autowired
    private MicoKubernetesBuildBotConfig micoKubernetesBuildBotConfig;

    private String namespace;

    /**
     * Set up everything that is required to execute the integration tests for the image builder.
     */
    @Before
    public void setUp() {
        namespace = integrationTestsUtils.setUpEnvironment(true);
        integrationTestsUtils.setUpDockerRegistryConnection(namespace);
        micoKubernetesBuildBotConfig.setNamespaceBuildExecution(namespace);
        micoKubernetesBuildBotConfig.setBuildTimeout(60);
    }

    /**
     * Delete namespace cleans up everything.
     */
    @After
    public void tearDown() {
        integrationTestsUtils.cleanUpEnvironment(namespace);
    }

    // TODO refactor for tekton-based image builder
    /**
     * Test if the connected Kubernetes cluster has the required Build CRD defined.
     */
    /*@Test
    public void checkBuildCustomResourceDefinition() {
        Optional<CustomResourceDefinition> buildCRD = imageBuilder.getBuildCRD();
        log.info("Build CRD: {}" + buildCRD);
        assertNotNull("No Build CRD defined", buildCRD);
    }*/

    /**
     * Test the ImageBuilder if the build and push of an image works. It uses the provided Git repository that contains
     * a Dockerfile to build a Docker image. Afterwards it pushes it to the provided Docker registry (e.g. DockerHub).
     *
     * @throws NotInitializedException if ImageBuilder was not initialized
     * @throws InterruptedException    if the build process is interrupted unexpectedly
     * @throws TimeoutException        if the build does not finish or fail in the expected time
     * @throws ExecutionException      if the build process fails unexpectedly
     */
    @Test
    public void buildAndPushImageWorks() throws NotInitializedException, InterruptedException, TimeoutException, ExecutionException, KubernetesResourceException {

        // Manual initialization is necessary so it will use the provided namespace (see setup method).
        imageBuilder.init();

        MicoService micoService = new MicoService()
            .setShortName(TestConstants.IntegrationTest.SERVICE_SHORT_NAME)
            .setName(TestConstants.IntegrationTest.SERVICE_NAME)
            .setVersion(TestConstants.IntegrationTest.RELEASE)
            .setDescription(TestConstants.IntegrationTest.SERVICE_DESCRIPTION)
            .setGitCloneUrl(TestConstants.IntegrationTest.GIT_CLONE_URL)
            .setDockerfilePath(TestConstants.IntegrationTest.DOCKERFILE_PATH);

        CompletableFuture<String> buildJob = imageBuilder.build(micoService);

        String dockerImageURI = buildJob.get();
        assertNotNull("Build failed!", dockerImageURI);
    }
}
