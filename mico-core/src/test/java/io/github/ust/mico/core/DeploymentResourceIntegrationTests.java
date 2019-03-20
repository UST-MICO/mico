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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;

import io.github.ust.mico.core.util.EmbeddedRedisServer;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

@Ignore
// TODO Upgrade to JUnit5
@Category(IntegrationTests.class)
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class DeploymentResourceIntegrationTests extends Neo4jTestClass {

    private static final String BASE_PATH = "/applications";

    @ClassRule
    public static RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    private String namespace;
    private MicoService service;
    private MicoApplication application;

    /**
     * Set up everything that is required to execute the integration tests for the deployment.
     */
    @Before
    public void setUp() {
        namespace = integrationTestsUtils.setUpEnvironment(true);
        log.info("Integration test is running in Kubernetes namespace '{}'", namespace);

        try {
            integrationTestsUtils.setUpDockerRegistryConnection(namespace);
        } catch (RuntimeException e) {
            tearDown();
            throw e;
        }

        application = getTestApplication();
        service = getTestService();
        
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(service));
        
        applicationRepository.save(application);
    }

    /**
     * Delete namespace cleans up everything.
     */
    @After
    public void tearDown() {
        integrationTestsUtils.cleanUpEnvironment(namespace);
    }

    @Test
    public void deployApplicationWithOneService() throws Exception {

        String applicationShortName = application.getShortName();
        String applicationVersion = application.getVersion();

        mvc.perform(post(BASE_PATH + "/" + applicationShortName + "/" + applicationVersion + "/deploy"))
            .andDo(print())
            .andExpect(status().isAccepted());

        // Wait until all pods (inclusive build pod) are running or succeeded
        CompletableFuture<Boolean> allPodsInNamespaceAreRunning = integrationTestsUtils.waitUntilAllPodsInNamespaceAreRunning(
            namespace, 10, 1, 60);
        assertTrue("Deployment failed!", allPodsInNamespaceAreRunning.get());

        // Wait until the deployment is created
        CompletableFuture<Deployment> createdDeployment = integrationTestsUtils.waitUntilDeploymentIsCreated(
            service, 1, 1, 10);
        assertNotNull("Kubernetes Deployment was not created!", createdDeployment.get());
        log.debug("Created Kubernetes Deployment: {}", createdDeployment.get().toString());

        // Wait until the service is created
        CompletableFuture<Service> createdService = integrationTestsUtils.waitUntilServiceIsCreated(
            service, 1, 1, 10);
        assertNotNull("Kubernetes Service was not created!", createdService.get());
        log.debug("Created Kubernetes Service: {}", createdService.get().toString());

        // Assert deployment
        assertNotNull("Expected deployment does not exist", createdDeployment.get());
        // Assert service
        assertNotNull("Expected service does not exist", createdService.get());

        log.info("ClusterIP: {}", createdService.get().getSpec().getClusterIP());
        log.info("LoadBalancerIP: {}", createdService.get().getSpec().getLoadBalancerIP());
        log.info("ExternalIPs: {}", createdService.get().getSpec().getExternalIPs());
    }

    private MicoApplication getTestApplication() {
        return new MicoApplication()
            .setShortName(TestConstants.IntegrationTest.APPLICATION_SHORT_NAME)
            .setName(TestConstants.IntegrationTest.APPLICATION_NAME)
            .setVersion(TestConstants.IntegrationTest.APPLICATION_VERSION)
            .setDescription(TestConstants.IntegrationTest.APPLICATION_DESCRIPTION);
    }

    private MicoService getTestService() {
        MicoService service = new MicoService()
            .setShortName(TestConstants.IntegrationTest.SERVICE_SHORT_NAME)
            .setName(TestConstants.IntegrationTest.SERVICE_NAME)
            .setVersion(TestConstants.IntegrationTest.RELEASE)
            .setDescription(TestConstants.IntegrationTest.SERVICE_DESCRIPTION)
            .setGitCloneUrl(TestConstants.IntegrationTest.GIT_CLONE_URL)
            .setDockerfilePath(TestConstants.IntegrationTest.DOCKERFILE_PATH);
        MicoServiceInterface serviceInterface = new MicoServiceInterface()
            .setServiceInterfaceName(TestConstants.IntegrationTest.SERVICE_INTERFACE_NAME)
            .setPorts(CollectionUtils.listOf(new MicoServicePort()
                .setPort(TestConstants.IntegrationTest.PORT)
                .setTargetPort(TestConstants.IntegrationTest.TARGET_PORT)
            ));
        service.getServiceInterfaces().add(serviceInterface);

        return service;
    }
}
