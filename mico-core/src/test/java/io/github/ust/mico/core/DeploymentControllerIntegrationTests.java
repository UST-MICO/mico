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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoPortType;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

@Ignore
// TODO Upgrade to JUnit5
@Category(IntegrationTests.class)
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class DeploymentControllerIntegrationTests extends Neo4jTestClass {

    private static final String BASE_PATH = "/applications";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

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

        applicationRepository.save(application);
        serviceRepository.save(service);
        serviceDeploymentInfoRepository.save(new MicoServiceDeploymentInfo()
            .setService(service)
            .setApplication(application));
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
            .andExpect(status().isOk());

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
            .setShortName("hello")
            .setName("hello-application")
            .setVersion("v1.0.0")
            .setDescription("Hello World Application");
    }

    private MicoService getTestService() {
        MicoService service = new MicoService()
            .setShortName("hello-integration-test")
            .setName("UST-MICO/hello")
            .setVersion("v1.0.0")
            .setDescription("Hello World Service for integration testing")
            .setGitCloneUrl("https://github.com/UST-MICO/hello.git")
            .setDockerfilePath("Dockerfile");
        MicoServiceInterface serviceInterface = new MicoServiceInterface()
            .setServiceInterfaceName("hello-service")
            .setPorts(CollectionUtils.listOf(new MicoServicePort()
                .setPort(80)
                .setTargetPort(80)
                .setType(MicoPortType.TCP)));
        service.getServiceInterfaces().add(serviceInterface);

        return service;
    }
}
