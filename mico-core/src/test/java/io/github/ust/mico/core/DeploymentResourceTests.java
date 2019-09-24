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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.TestConstants.*;
import io.github.ust.mico.core.broker.BackgroundJobBroker;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.exception.NotInitializedException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.EmbeddedRedisServer;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("unit-testing")
public class DeploymentResourceTests {

    @ClassRule
    public static RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());

    private static final String DEPLOYMENT_NAME = "deployment-name";
    private static final String SERVICE_NAME = "service-name";
    private static final String NAMESPACE_NAME = "namespace-name";

    @Captor
    private ArgumentCaptor<MicoService> micoServiceArgumentCaptor;

    @Captor
    private ArgumentCaptor<MicoServiceInterface> micoServiceInterfaceArgumentCaptor;

    @Captor
    private ArgumentCaptor<MicoServiceDeploymentInfo> serviceDeploymentInfoArgumentCaptor;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @MockBean
    private BackgroundJobBroker backgroundJobBroker;

    @MockBean
    private ImageBuilder imageBuilder;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @Before
    public void setUp() throws KubernetesResourceException {
        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata().withName(DEPLOYMENT_NAME).withNamespace(NAMESPACE_NAME).endMetadata()
            .build();
        given(micoKubernetesClient.createMicoService(any(MicoServiceDeploymentInfo.class)))
            .willReturn(deployment);
        Service service = new ServiceBuilder()
            .withNewMetadata().withName(SERVICE_NAME).withNamespace(NAMESPACE_NAME).endMetadata()
            .build();
        given(micoKubernetesClient.createMicoServiceInterface(any(MicoServiceInterface.class), any(MicoService.class)))
            .willReturn(service);
    }

    @Test
    public void deployApplicationWithOneServiceAndOneServiceInterface() throws Exception {
        MicoService service = getTestService();
        service.setDockerImageUri(TestConstants.IntegrationTest.DOCKER_IMAGE_URI);

        MicoApplication application = getTestApplication();
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(INSTANCE_ID));

        setupDeploymentResources(application, service);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/deploy"))
            .andDo(print())
            .andExpect(status().isAccepted());

        // Sleep is required to wait for background job (another thread)
        Thread.sleep(500);

        verify(serviceRepository, times(1)).save(micoServiceArgumentCaptor.capture(), eq(0));

        MicoService storedMicoService = micoServiceArgumentCaptor.getValue();
        assertNotNull(storedMicoService);
        assertNotNull("DockerImageUri was not set", storedMicoService.getDockerImageUri());
        assertEquals(service.getDockerImageUri(), storedMicoService.getDockerImageUri());

        verify(micoKubernetesClient, times(1)).createMicoService(serviceDeploymentInfoArgumentCaptor.capture());

        MicoService micoServiceToCreate = micoServiceArgumentCaptor.getValue();
        assertNotNull(micoServiceToCreate);
        assertEquals("MicoService that will be created as Kubernetes resources does not match", service, micoServiceToCreate);

        MicoServiceDeploymentInfo deploymentInfo = serviceDeploymentInfoArgumentCaptor.getValue();
        assertNotNull(deploymentInfo);
        int actualReplicas = deploymentInfo.getReplicas();
        int expectedReplicas = application.getServiceDeploymentInfos().get(0).getReplicas();
        assertEquals("Replicas does not match the definition in the deployment info", expectedReplicas, actualReplicas);

        verify(micoKubernetesClient, times(1)).createMicoServiceInterface(
            micoServiceInterfaceArgumentCaptor.capture(),
            micoServiceArgumentCaptor.capture());

        MicoServiceInterface micoServiceInterfaceToCreate = micoServiceInterfaceArgumentCaptor.getValue();
        assertNotNull(micoServiceInterfaceToCreate);
        assertEquals("MicoServiceInterface that will be created as Kubernetes resources does not match",
            service.getServiceInterfaces().get(0), micoServiceInterfaceToCreate);

        MicoService micoServiceThatIsUsedForInterfaceCreation = micoServiceArgumentCaptor.getValue();
        assertNotNull(micoServiceThatIsUsedForInterfaceCreation);
        assertEquals("MicoService that will be used to create a MicoServiceInterface does not match",
            service, micoServiceThatIsUsedForInterfaceCreation);

        verify(serviceDeploymentInfoRepository, times(1)).save(serviceDeploymentInfoArgumentCaptor.capture(), eq(1));
        MicoServiceDeploymentInfo storedServiceDeploymentInfo = serviceDeploymentInfoArgumentCaptor.getValue();
        assertNotNull(storedServiceDeploymentInfo);
        KubernetesDeploymentInfo kubernetesDeploymentInfo = storedServiceDeploymentInfo.getKubernetesDeploymentInfo();
        assertEquals(DEPLOYMENT_NAME, kubernetesDeploymentInfo.getDeploymentName());
        assertEquals(1, kubernetesDeploymentInfo.getServiceNames().size());
        assertEquals(SERVICE_NAME, kubernetesDeploymentInfo.getServiceNames().get(0));
        assertEquals(NAMESPACE_NAME, kubernetesDeploymentInfo.getNamespace());
    }

    @Test
    public void deployApplicationWithServiceWithoutServiceInterface() throws Exception {
        MicoService service = getTestService();
        service.setServiceInterfaces(new ArrayList<>()); // There are no interfaces
        MicoApplication application = getTestApplication();
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(INSTANCE_ID));
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/deploy"))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andExpect(status().reason(Matchers.containsString("interfaces")));
    }

    @Test
    public void deployApplicationWithKafkaEnabledServiceWithoutServiceInterface() throws Exception {
        List<MicoTopicRole> micoTopicRoles = Arrays.asList(
            new MicoTopicRole().setTopic(new MicoTopic().setName("inputTopic")).setRole(MicoTopicRole.Role.INPUT),
            new MicoTopicRole().setRole(MicoTopicRole.Role.OUTPUT).setTopic(new MicoTopic().setName("outputTopic")));
        MicoService service = getTestService();
        service.setServiceInterfaces(new ArrayList<>()); // There are no interfaces
        service.setKafkaEnabled(true); // Service is Kafka enabled
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(INSTANCE_ID)
            .setTopics(micoTopicRoles);
        MicoApplication application = getTestApplication();
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        setupDeploymentResources(application, service);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/deploy"))
            .andDo(print())
            .andExpect(status().isAccepted());
    }

    private void setupDeploymentResources(MicoApplication application, MicoService service) throws NotInitializedException, InterruptedException, ExecutionException, TimeoutException, MicoApplicationNotFoundException {
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceDeploymentInfoRepository
            .findByApplicationAndService(application.getShortName(), application.getVersion(), service.getShortName(), service.getVersion()))
            .willReturn(CollectionUtils.listOf(new MicoServiceDeploymentInfo()
                .setService(service)
                .setInstanceId(INSTANCE_ID)));
        given(serviceRepository.save(any(MicoService.class), anyInt())).willReturn(service);
        given(serviceDeploymentInfoRepository.save(any(MicoServiceDeploymentInfo.class)))
            .willReturn(new MicoServiceDeploymentInfo()
                .setService(service)
                .setInstanceId(INSTANCE_ID)
                .setKubernetesDeploymentInfo(new KubernetesDeploymentInfo()
                    .setNamespace("namespace")
                    .setDeploymentName("deployment")
                    .setServiceNames(CollectionUtils.listOf("service"))));

        // Assume asynchronous image build operation was successful
        CompletableFuture<String> futureOfBuildJob = CompletableFuture.completedFuture(IntegrationTest.DOCKER_IMAGE_URI);
        given(imageBuilder.build(service)).willReturn(futureOfBuildJob);

        MicoServiceBackgroundJob mockJob = new MicoServiceBackgroundJob()
            .setFuture(futureOfBuildJob)
            .setServiceShortName(service.getShortName())
            .setServiceVersion(service.getVersion())
            .setType(MicoServiceBackgroundJob.Type.BUILD);

        given(backgroundJobBroker.getJobByMicoService(service.getShortName(), service.getVersion(), MicoServiceBackgroundJob.Type.BUILD))
            .willReturn(Optional.of(mockJob));
        given(backgroundJobBroker.saveJob(mockJob)).willReturn(mockJob);

        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(SHORT_NAME, VERSION))
            .willReturn(new MicoApplicationJobStatus()
                .setApplicationShortName(SHORT_NAME)
                .setApplicationVersion(VERSION)
                .setStatus(MicoServiceBackgroundJob.Status.PENDING)
                .setJobs(Collections.singletonList(mockJob)));
    }

    private MicoApplication getTestApplication() {
        return new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);
    }

    private MicoService getTestService() {
        MicoService service = new MicoService()
            .setId(ID_1)
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
