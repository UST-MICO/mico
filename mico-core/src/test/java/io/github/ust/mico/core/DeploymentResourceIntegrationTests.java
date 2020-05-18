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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.broker.MicoApplicationBroker;
import io.github.ust.mico.core.broker.MicoServiceBroker;
import io.github.ust.mico.core.broker.MicoServiceDeploymentInfoBroker;
import io.github.ust.mico.core.configuration.KafkaFaasConnectorConfig;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoBackgroundJobRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.persistence.MicoTopicRepository;
import io.github.ust.mico.core.service.imagebuilder.TektonPipelinesController;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.EmbeddedRedisServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Is ignored because Jenkins currently can't connect to Kubernetes.
@Ignore
@Category(IntegrationTests.class)
@Slf4j
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
// Only works with local Neo4j database (embedded database has threading problems)
public class DeploymentResourceIntegrationTests {

    // Deployment timeout in seconds.
    private static final int TIMEOUT_DEPLOYMENT = 10;
    @ClassRule
    public static RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());
    // Build timeout in seconds.
    private static int TIMEOUT_BUILD = 60;
    @Autowired
    KafkaFaasConnectorConfig kafkaFaasConnectorConfig;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;
    @Autowired
    private MicoKubernetesBuildBotConfig micoKubernetesBuildBotConfig;
    @Autowired
    private MicoApplicationRepository applicationRepository;
    @Autowired
    private MicoServiceRepository serviceRepository;
    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;
    @Autowired
    private MicoTopicRepository micoTopicRepository;
    @Autowired
    private MicoBackgroundJobRepository jobRepository;
    @Autowired
    private MicoApplicationBroker micoApplicationBroker;
    @Autowired
    private MicoServiceBroker serviceBroker;
    @Autowired
    private MicoServiceDeploymentInfoBroker serviceDeploymentInfoBroker;
    @Autowired
    private TektonPipelinesController imageBuilder;

    private String namespace;
    private MicoService service;
    private MicoApplication application;
    private MicoServiceDeploymentInfo serviceDeploymentInfo;

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

        micoKubernetesBuildBotConfig.setBuildTimeout(TIMEOUT_BUILD);
    }

    /**
     * Deletion of namespace cleans up everything in Kubernetes. However not in the local Neo4j database. To delete
     * everything in the database use Cypher: {@code MATCH (n) DETACH DELETE n;}
     */
    @After
    public void tearDown() {
        integrationTestsUtils.cleanUpEnvironment(namespace);

        // Delete all jobs in Redis.
        jobRepository.deleteAll();
        // Delete all entities that were added during the test execution
        serviceDeploymentInfoRepository.deleteAllByApplication(application.getShortName(), application.getVersion());
        serviceDeploymentInfoBroker.cleanUpTanglingNodes();
        serviceRepository.deleteServiceByShortNameAndVersion(service.getShortName(), service.getVersion());
        applicationRepository.delete(application);
    }

    @Test
    public void deployApplicationWithOneService() throws Exception {
        createApplicationWithOneServiceInDatabase();

        // Manual initialization is necessary so it will use the provided namespace (see setup method).
        imageBuilder.init();

        String applicationShortName = application.getShortName();
        String applicationVersion = application.getVersion();

        mvc.perform(post(PATH_APPLICATIONS + "/" + applicationShortName + "/" + applicationVersion + "/deploy"))
            .andDo(print())
            .andExpect(status().isAccepted());

        waitForAllPodsInNamespace();
        CompletableFuture<Deployment> createdDeployment = waitForDeploymentCreation(serviceDeploymentInfo);

        // Wait until the service is created
        CompletableFuture<Service> createdService = waitForServiceCreation();

        // Assert deployment
        assertNotNull("Expected deployment does not exist", createdDeployment.get());
        // Assert service
        assertNotNull("Expected service does not exist", createdService.get());

        log.info("ClusterIP: {}", createdService.get().getSpec().getClusterIP());
        log.info("LoadBalancerIP: {}", createdService.get().getSpec().getLoadBalancerIP());
        log.info("ExternalIPs: {}", createdService.get().getSpec().getExternalIPs());
    }

    @Test
    public void deployApplicationWithMultipleKafkaFaasConnectorInstances() throws Exception {
        TIMEOUT_BUILD = 600;
        micoKubernetesBuildBotConfig.setBuildTimeout(TIMEOUT_BUILD);
        createApplicationWithOneServiceInDatabase();

        String kfConnectorVersion = serviceBroker.getLatestKFConnectorVersion();
        MicoServiceDeploymentInfo kafkaFaasConnectorMicoServiceDeploymentInfo1 = micoApplicationBroker
            .addKafkaFaasConnectorInstanceToMicoApplicationByVersion(application.getShortName(), application.getVersion(), kfConnectorVersion);
        MicoServiceDeploymentInfo kafkaFaasConnectorMicoServiceDeploymentInfo2 = micoApplicationBroker
            .addKafkaFaasConnectorInstanceToMicoApplicationByVersion(application.getShortName(), application.getVersion(), kfConnectorVersion);

        MicoTopic micoInputTopic = new MicoTopic().setName("TestInputTopic");
        micoInputTopic.setId(micoTopicRepository.save(micoInputTopic).getId());

        MicoTopic micoOutputTopic = new MicoTopic().setName("TestOutputTopic");
        micoOutputTopic.setId(micoTopicRepository.save(micoOutputTopic).getId());

        MicoTopicRole micoTopicInputRole = new MicoTopicRole().setServiceDeploymentInfo(
            kafkaFaasConnectorMicoServiceDeploymentInfo1).setTopic(micoInputTopic).setRole(MicoTopicRole.Role.INPUT);
        MicoTopicRole micoTopicOutputRole = new MicoTopicRole().setServiceDeploymentInfo(
            kafkaFaasConnectorMicoServiceDeploymentInfo1).setTopic(micoOutputTopic).setRole(MicoTopicRole.Role.OUTPUT);

        MicoTopicRole micoTopicInputRole2 = new MicoTopicRole().setServiceDeploymentInfo(
            kafkaFaasConnectorMicoServiceDeploymentInfo2).setTopic(micoInputTopic).setRole(MicoTopicRole.Role.INPUT);
        MicoTopicRole micoTopicOutputRole2 = new MicoTopicRole().setServiceDeploymentInfo(
            kafkaFaasConnectorMicoServiceDeploymentInfo2).setTopic(micoOutputTopic).setRole(MicoTopicRole.Role.OUTPUT);

        List<MicoTopicRole> micoTopicRoles1 = new LinkedList<>();
        micoTopicRoles1.add(micoTopicInputRole);
        micoTopicRoles1.add(micoTopicOutputRole);

        List<MicoTopicRole> micoTopicRoles2 = new LinkedList<>();
        micoTopicRoles2.add(micoTopicInputRole2);
        micoTopicRoles2.add(micoTopicOutputRole2);

        kafkaFaasConnectorMicoServiceDeploymentInfo1.setTopics(micoTopicRoles1);
        kafkaFaasConnectorMicoServiceDeploymentInfo2.setTopics(micoTopicRoles2);

        MicoServiceDeploymentInfo savedServiceDeploymentInfo1 = serviceDeploymentInfoRepository.save(kafkaFaasConnectorMicoServiceDeploymentInfo1);
        serviceDeploymentInfoRepository.save(savedServiceDeploymentInfo1);
        MicoServiceDeploymentInfo savedServiceDeploymentInfo2 = serviceDeploymentInfoRepository.save(kafkaFaasConnectorMicoServiceDeploymentInfo2);
        serviceDeploymentInfoRepository.save(savedServiceDeploymentInfo2);

        imageBuilder.init();

        String applicationShortName = application.getShortName();
        String applicationVersion = application.getVersion();

        mvc.perform(post(PATH_APPLICATIONS + "/" + applicationShortName + "/" + applicationVersion + "/deploy"))
            .andDo(print())
            .andExpect(status().isAccepted());

        waitForAllPodsInNamespace();
        CompletableFuture<Deployment> createdDeployment1 = waitForDeploymentCreation(kafkaFaasConnectorMicoServiceDeploymentInfo1);
        CompletableFuture<Deployment> createdDeployment2 = waitForDeploymentCreation(kafkaFaasConnectorMicoServiceDeploymentInfo2);
        CompletableFuture<Service> createdService = waitForServiceCreation();

        // Assert deployment
        assertNotNull("Expected deployment of KafkaFaasConnector 1 does not exist", createdDeployment1.get());
        assertNotNull("Expected deployment of KafkaFaasConnector 2 does not exist", createdDeployment2.get());
        assertEquals(kafkaFaasConnectorMicoServiceDeploymentInfo1.getInstanceId(), createdDeployment1.get().getMetadata().getName());
        assertEquals(kafkaFaasConnectorMicoServiceDeploymentInfo2.getInstanceId(), createdDeployment2.get().getMetadata().getName());

        // Assert service
        assertNotNull("Expected service does not exist", createdService.get());
    }

    /**
     * Wait until all pods (inclusive build pod) are running or succeeded
     *
     * @throws InterruptedException if the background task is aborted
     * @throws ExecutionException   if the background task has thrown an exception
     * @throws TimeoutException     if the timeout is reached
     */
    private void waitForAllPodsInNamespace() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> allPodsInNamespaceAreRunning = integrationTestsUtils.waitUntilAllPodsInNamespaceAreRunning(
            namespace, 10, 1, TIMEOUT_BUILD);
        assertTrue("Deployment failed!", allPodsInNamespaceAreRunning.get());
    }

    private CompletableFuture<Service> waitForServiceCreation() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Service> createdService = integrationTestsUtils.waitUntilServiceIsCreated(
            serviceDeploymentInfo, 1, 1, 10);
        assertNotNull("Kubernetes Service was not created!", createdService.get());
        log.debug("Created Kubernetes Service: {}", createdService.get().toString());
        return createdService;
    }

    private CompletableFuture<Deployment> waitForDeploymentCreation(MicoServiceDeploymentInfo micoServiceDeploymentInfo) throws InterruptedException, ExecutionException, TimeoutException {
        // Wait until the deployment is created
        CompletableFuture<Deployment> createdDeployment = integrationTestsUtils.waitUntilDeploymentIsCreated(
            micoServiceDeploymentInfo, 1, 1, TIMEOUT_DEPLOYMENT);
        assertNotNull("Kubernetes Deployment was not created!", createdDeployment.get());
        log.debug("Created Kubernetes Deployment: {}", createdDeployment.get().toString());
        return createdDeployment;
    }

    private void createApplicationWithOneServiceInDatabase() {
        application = getTestApplication();
        service = getTestService();
        serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(TestConstants.IntegrationTest.INSTANCE_ID);

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        applicationRepository.save(application);
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
