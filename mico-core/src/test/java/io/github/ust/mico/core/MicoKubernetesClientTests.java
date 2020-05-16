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

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceStatusBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.TestConstants.IntegrationTest;
import io.github.ust.mico.core.broker.BackgroundJobBroker;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.KubernetesDeploymentInfo;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoApplicationDeploymentStatus;
import io.github.ust.mico.core.model.MicoApplicationJobStatus;
import io.github.ust.mico.core.model.MicoEnvironmentVariable;
import io.github.ust.mico.core.model.MicoInterfaceConnection;
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceBackgroundJob.Status;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.model.OpenFaaSFunction;
import io.github.ust.mico.core.persistence.KubernetesDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.knativebuild.KnativeBuildController;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static io.github.ust.mico.core.TestConstants.ID_1;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID_1;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID_2;
import static io.github.ust.mico.core.TestConstants.NAME;
import static io.github.ust.mico.core.TestConstants.NAME_1;
import static io.github.ust.mico.core.TestConstants.NAME_2;
import static io.github.ust.mico.core.TestConstants.SERVICE_INTERFACE_NAME;
import static io.github.ust.mico.core.TestConstants.SERVICE_INTERFACE_NAME_1;
import static io.github.ust.mico.core.TestConstants.SERVICE_SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SERVICE_SHORT_NAME_1;
import static io.github.ust.mico.core.TestConstants.SERVICE_VERSION;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_2;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.service.MicoKubernetesClient.OPEN_FAAS_SECRET_DATA_PASSWORD_NAME;
import static io.github.ust.mico.core.service.MicoKubernetesClient.OPEN_FAAS_SECRET_DATA_USERNAME_NAME;
import static io.github.ust.mico.core.service.MicoKubernetesClient.OPEN_FAAS_SECRET_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class MicoKubernetesClientTests {

    private static final String LABEL_PREFIX = "ust.mico/";
    private static final String LABEL_NAME_KEY = LABEL_PREFIX + "name";
    private static final String LABEL_VERSION_KEY = LABEL_PREFIX + "version";
    private static final String LABEL_INTERFACE_KEY = LABEL_PREFIX + "interface";
    private static final String LABEL_INSTANCE_KEY = LABEL_PREFIX + "instance";
    private static final String testNamespace = "test-namespace";
    private static final String buildTestNamespace = "test-namespace";
    @Rule
    public KubernetesServer mockServer = new KubernetesServer(false, true);
    @MockBean
    private MicoKubernetesConfig micoKubernetesConfig;
    @MockBean
    private MicoKubernetesBuildBotConfig micoKubernetesBuildBotConfig;
    @MockBean
    private KnativeBuildController imageBuilder;
    @MockBean
    private BackgroundJobBroker backgroundJobBroker;
    @MockBean
    private MicoApplicationRepository applicationRepository;
    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;
    @MockBean
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;
    private MicoKubernetesClient micoKubernetesClient;

    @Before
    public void setUp() {
        given(micoKubernetesConfig.getNamespaceMicoWorkspace()).willReturn(testNamespace);
        given(micoKubernetesBuildBotConfig.getNamespaceBuildExecution()).willReturn(buildTestNamespace);
        given(micoKubernetesBuildBotConfig.isBuildCleanUpByUndeploy()).willReturn(true);

        micoKubernetesClient = new MicoKubernetesClient(micoKubernetesConfig, micoKubernetesBuildBotConfig,
            mockServer.getClient(), imageBuilder, backgroundJobBroker, applicationRepository,
            serviceDeploymentInfoRepository, kubernetesDeploymentInfoRepository);

        mockServer.getClient().namespaces().create(new NamespaceBuilder().withNewMetadata().withName(testNamespace).endMetadata().build());
    }

    @Test
    public void creationOfMicoServiceWorks() {
        // Assert that at the beginning there are no deployment
        DeploymentList deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(0, deployments.getItems().size());

        MicoService micoService = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setInstanceId(INSTANCE_ID);

        micoKubernetesClient.createMicoServiceInstance(deploymentInfo);

        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment actualDeployment = deployments.getItems().get(0);
        assertNotNull(actualDeployment);
        assertEquals("Name of Kubernetes Deployment does not match provided instance ID",
            INSTANCE_ID, actualDeployment.getMetadata().getName());
        assertEquals(testNamespace, actualDeployment.getMetadata().getNamespace());
        assertEquals("Expected 1 container",
            1, actualDeployment.getSpec().getTemplate().getSpec().getContainers().size());
        assertEquals("Expected 3 labels",
            3, actualDeployment.getMetadata().getLabels().size());
        assertEquals("Expected 3 labels in template",
            3, actualDeployment.getSpec().getTemplate().getMetadata().getLabels().size());
        assertEquals("Expected instance ID is used for instance ID label",
            INSTANCE_ID, actualDeployment.getSpec().getTemplate().getMetadata().getLabels().get(LABEL_INSTANCE_KEY));
    }

    @Test
    public void creationOfMicoServiceWithDeploymentInformationWorks() {
        MicoService micoService = getMicoServiceWithoutInterface();

        MicoLabel label = new MicoLabel().setKey("some-label-key").setValue("some-label-value");
        MicoEnvironmentVariable environmentVariable = new MicoEnvironmentVariable().setName("some-env-name").setValue("some-env-value");
        MicoTopicRole topicRole = new MicoTopicRole().setRole(MicoTopicRole.Role.INPUT).setTopic(new MicoTopic().setName("input-topic"));
        OpenFaaSFunction openFaaSFunction = new OpenFaaSFunction().setName("open-faas-function");
        MicoInterfaceConnection interfaceConnection = new MicoInterfaceConnection()
            .setEnvironmentVariableName("ENV_VAR")
            .setMicoServiceInterfaceName("INTERFACE_NAME")
            .setMicoServiceShortName("SERVICE_NAME");
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setInstanceId(INSTANCE_ID)
            .setReplicas(3)
            .setImagePullPolicy(MicoServiceDeploymentInfo.ImagePullPolicy.NEVER)
            .setLabels(CollectionUtils.listOf(label))
            .setEnvironmentVariables(CollectionUtils.listOf(environmentVariable))
            .setTopics(CollectionUtils.listOf(topicRole))
            .setOpenFaaSFunction(openFaaSFunction)
            .setInterfaceConnections(CollectionUtils.listOf(interfaceConnection));

        micoKubernetesClient.createMicoServiceInstance(serviceDeploymentInfo);

        assertEquals(1, mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().size());

        Deployment actualDeployment = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().get(0);

        assertNotNull(actualDeployment);
        assertTrue("Custom label does not exist", actualDeployment.getMetadata().getLabels().containsKey(label.getKey()));
        assertEquals("Replicas does not match expected", serviceDeploymentInfo.getReplicas(), actualDeployment.getSpec().getReplicas().intValue());
        assertEquals("Expected 1 container", 1, actualDeployment.getSpec().getTemplate().getSpec().getContainers().size());
        assertEquals("Name of container does not match short name of the MicoService", micoService.getShortName(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getName());
        assertEquals("Image of container does not match expected", micoService.getDockerImageUri(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
        assertEquals("ImagePullPolicy does not match expected", serviceDeploymentInfo.getImagePullPolicy().toString(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());
        assertTrue("Custom label in template does not exist", actualDeployment.getSpec().getTemplate().getMetadata().getLabels().containsKey(label.getKey()));
        List<EnvVar> actualEnvVarList = actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
        Optional<EnvVar> actualCustomEnvVar = actualEnvVarList.stream().filter(envVar -> envVar.getName().equals(environmentVariable.getName()) && envVar.getValue().equals(environmentVariable.getValue())).findFirst();
        assertTrue("Custom environment variable is not present", actualCustomEnvVar.isPresent());
        Optional<EnvVar> actualTopicEnvVar = actualEnvVarList.stream().filter(envVar -> envVar.getName().equals(MicoEnvironmentVariable.DefaultNames.KAFKA_TOPIC_INPUT.name()) && envVar.getValue().equals(topicRole.getTopic().getName())).findFirst();
        assertTrue("Topic environment variable is not present", actualTopicEnvVar.isPresent());
        Optional<EnvVar> actualOpenFaasFunctionNameEnvVar = actualEnvVarList.stream().filter(envVar -> envVar.getName().equals(MicoEnvironmentVariable.DefaultNames.OPENFAAS_FUNCTION_NAME.name())).findFirst();
        assertTrue("OpenFaaS function name environment variable is not present", actualOpenFaasFunctionNameEnvVar.isPresent());
        assertEquals("OpenFaaS function name environment variable does not match expected", serviceDeploymentInfo.getOpenFaaSFunction().getName(), actualOpenFaasFunctionNameEnvVar.get().getValue());
    }

    @Test
    public void creationOfMicoServiceInterfaceWorks() throws KubernetesResourceException {
        // Assert that at the beginning there are no deployment
        ServiceList services = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(0, services.getItems().size());

        MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceInstance();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();

        // Arrange existing deployments
        Deployment existingDeployment = getDeploymentObject(micoServiceDeploymentInfo);
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).create(existingDeployment);

        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoServiceDeploymentInfo);

        services = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(1, services.getItems().size());
        Service actualService = services.getItems().get(0);
        assertNotNull(actualService);
        assertTrue("Name of Kubernetes Service does not start with the instance ID and the name of the MicoServiceInterface",
            actualService.getMetadata().getName().startsWith(micoKubernetesClient.createServiceName(micoServiceDeploymentInfo, micoServiceInterface)));
        assertEquals(testNamespace, actualService.getMetadata().getNamespace());

        assertEquals("Expected 4 labels",
            4, actualService.getMetadata().getLabels().size());
        assertEquals("Expected 1 selector",
            1, actualService.getSpec().getSelector().size());
        assertEquals("Type does not match expected",
            "LoadBalancer", actualService.getSpec().getType());

        List<ServicePort> actualServicePorts = actualService.getSpec().getPorts();
        assertEquals("Expected one port", 1, actualServicePorts.size());
        ServicePort actualServicePort = actualServicePorts.get(0);
        assertEquals("Service port does not match expected",
            micoServiceInterface.getPorts().get(0).getPort(), actualServicePort.getPort().intValue());
        assertEquals("Service target port does not match expected",
            micoServiceInterface.getPorts().get(0).getTargetPort(), actualServicePort.getTargetPort().getIntVal().intValue());
    }

    @Test
    public void creationOfMicoServiceThatAlreadyExistsDoesReplaceTheSameObject() {
        MicoService micoServiceWithoutInterface = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoServiceWithoutInterface)
            .setInstanceId(INSTANCE_ID);

        // First creation
        micoKubernetesClient.createMicoServiceInstance(deploymentInfo);

        DeploymentList deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment firstDeployment = deployments.getItems().get(0);
        assertNotNull(firstDeployment);

        // Second creation
        micoKubernetesClient.createMicoServiceInstance(deploymentInfo);

        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment secondDeployment = deployments.getItems().get(0);
        assertNotNull(secondDeployment);
        assertEquals("Expected both deployments have the same name", firstDeployment.getMetadata().getName(), secondDeployment.getMetadata().getName());
        assertEquals("Expected both deployments are the same", firstDeployment, secondDeployment);
    }

    @Test
    public void creationOfMicoServiceInterfaceThatAlreadyExistsReplaceTheSameObject() throws KubernetesResourceException {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceInstance();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();

        // Arrange existing deployments
        Deployment existingDeployment = getDeploymentObject(micoServiceDeploymentInfo);
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).create(existingDeployment);

        // First creation
        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoServiceDeploymentInfo);

        ServiceList existingServices = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(1, existingServices.getItems().size());
        Service firstService = existingServices.getItems().get(0);
        assertNotNull(firstService);

        // Second creation
        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoServiceDeploymentInfo);

        existingServices = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(1, existingServices.getItems().size());
        Service secondService = existingServices.getItems().get(0);
        assertNotNull(secondService);
        assertEquals("Expected both services have the same name", firstService.getMetadata().getName(), secondService.getMetadata().getName());
        assertEquals("Expected both services are the same", firstService, secondService);
    }

    @Test
    public void getApplicationDeploymentStatusForDeployedApplication() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        assertEquals("Application is expected to be deployed but actually is not.",
            MicoApplicationDeploymentStatus.Value.DEPLOYED,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForPendingDeployment() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.PENDING, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        assertEquals("Application is expected not to be deployed due to the deployment not having started yet.",
            MicoApplicationDeploymentStatus.Value.PENDING,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForRunningDeployment() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.RUNNING, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        assertEquals("Application is expected not to be deployed due to the deployment currently being in progress.",
            MicoApplicationDeploymentStatus.Value.PENDING,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForFailedDeployment() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.ERROR, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        assertEquals("Application is expected not to be deployed due to the deployment having failed.",
            MicoApplicationDeploymentStatus.Value.INCOMPLETE,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithoutServiceDeploymentInfos() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        given(serviceDeploymentInfoRepository.findAllByApplication(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(Collections.emptyList());

        assertEquals("Application is expected not to be deployed since it does not provide any service deployment information.",
            MicoApplicationDeploymentStatus.Value.UNDEPLOYED,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithoutKubernetesDeploymentInfos() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        given(serviceDeploymentInfoRepository.findAllByApplication(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(
            micoApplication.getServiceDeploymentInfos().stream().map(sdi -> sdi.setKubernetesDeploymentInfo(null)).collect(Collectors.toList()));

        assertEquals("Application is expected to be undeployed since it does not have any Kubernetes deployment information.",
            MicoApplicationDeploymentStatus.Value.UNDEPLOYED,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithNoUpdatedKubernetesDeploymentInfos() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        mockServer.getClient().apps().deployments().inNamespace(testNamespace).delete();

        assertEquals("Application is expected not to be deployed since there are no Kubernetes resources deployed.",
            MicoApplicationDeploymentStatus.Value.UNDEPLOYED,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithUnknownStatus() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        // Simulate error while updating Kubernetes Deployment Info (deployment name not set) -> Status Unknown
        micoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo().setDeploymentName(null);

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.UNDEFINED, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        assertEquals("Application deployment status is expected to be unknown because the name of the Kubernetes deployment name is missing.",
            MicoApplicationDeploymentStatus.Value.UNKNOWN,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithDeploymentNotAvailableAnymore() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment(getMicoServiceInstance(), getMicoServiceInstance_2());

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.UNDEFINED, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        List<Deployment> deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems();
        assertEquals("Expected 2 deployments before deletion of one", 2, deployments.size());
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).delete(deployments.get(0));
        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems();
        assertEquals("Expected 1 deployment after deletion of one", 1, deployments.size());

        assertEquals("Application deployment status is expected to be incomplete because a Kubernetes deployment is missing.",
            MicoApplicationDeploymentStatus.Value.INCOMPLETE,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithTwoServices_FirstUnknown_SecondNotAvailable() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment(getMicoServiceInstance(), getMicoServiceInstance_2());
        MicoServiceDeploymentInfo serviceDeploymentInfo1 = micoApplication.getServiceDeploymentInfos().get(0);
        MicoServiceDeploymentInfo serviceDeploymentInfo2 = micoApplication.getServiceDeploymentInfos().get(1);

        // Simulate error while updating Kubernetes Deployment Info (deployment name not set) -> Status Unknown
        serviceDeploymentInfo1.getKubernetesDeploymentInfo().setDeploymentName(null);

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.UNDEFINED, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        List<Deployment> deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems();
        assertEquals("Expected 2 deployments before deletion of one", 2, deployments.size());
        Optional<Deployment> firstDeploymentOptional = deployments.stream()
            .filter(deployment -> deployment.getMetadata().getName().startsWith(serviceDeploymentInfo2.getInstanceId())).findFirst();
        assertTrue("There is no deployment with the expected instance ID", firstDeploymentOptional.isPresent());
        Deployment firstDeployment = firstDeploymentOptional.get();
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).delete(firstDeployment);
        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems();
        assertEquals("Expected 1 deployment after deletion of one", 1, deployments.size());
        assertTrue("Expected deployment name starts with instance ID", deployments.get(0).getMetadata().getName().startsWith(
            serviceDeploymentInfo1.getInstanceId()));

        assertEquals("Application deployment status is expected to be incomplete because a Kubernetes deployment is missing.",
            MicoApplicationDeploymentStatus.Value.UNKNOWN,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void getApplicationDeploymentStatusForApplicationWithKubernetesServiceNotAvailableAnymore() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment(getMicoServiceInstance(), getMicoServiceInstance_2());

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.UNDEFINED, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        List<Service> kubernetesServices = mockServer.getClient().services().inNamespace(testNamespace).list().getItems();
        assertEquals("Expected 2 Kubernetes Services before deletion of one", 2, kubernetesServices.size());
        mockServer.getClient().services().inNamespace(testNamespace).delete(kubernetesServices.get(0));
        kubernetesServices = mockServer.getClient().services().inNamespace(testNamespace).list().getItems();
        assertEquals("Expected 1 Kubernetes Service after deletion of one", 1, kubernetesServices.size());

        KubernetesDeploymentInfo updatedKubernetesDeploymentInfoAfterDeletion = new KubernetesDeploymentInfo()
            .setId(micoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo().getId())
            .setNamespace(micoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo().getNamespace())
            .setDeploymentName(micoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo().getDeploymentName())
            .setServiceNames(new ArrayList<>()); // There are no Kubernetes services anymore
        given(kubernetesDeploymentInfoRepository.save(any(KubernetesDeploymentInfo.class)))
            .willReturn(updatedKubernetesDeploymentInfoAfterDeletion);

        assertEquals("Application deployment status is expected to be incomplete because a Kubernetes Service is missing.",
            MicoApplicationDeploymentStatus.Value.INCOMPLETE,
            micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }

    @Test
    public void isApplicationDeployed() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        boolean result = micoKubernetesClient.isApplicationDeployed(micoApplication);
        assertTrue("Expected application is not deployed.", result);
    }

    @Test
    public void isApplicationDeployedWithServicesUsedConcurrentlyByOtherApplications() throws MicoApplicationNotFoundException {
        // Arrange already existing MicoService that was deployed by another application
        MicoApplication otherMicoApplication = setUpApplicationDeployment();
        MicoService otherMicoService = otherMicoApplication.getServices().get(0);

        // Arrange new MicoApplication and ServiceDeploymentInfo
        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION)
            .setName(NAME_2);

        // Kubernetes deployment information is null, because the service was not deployed by this application
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(otherMicoService)
            .setInstanceId(INSTANCE_ID_2)
            .setKubernetesDeploymentInfo(null);
        micoApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(serviceDeploymentInfoRepository.findAllByApplication(SHORT_NAME_2, VERSION))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(SHORT_NAME_2, VERSION))
            .willReturn(new MicoApplicationJobStatus(SHORT_NAME_2, VERSION, Status.UNDEFINED, Collections.emptyList()));

        boolean result = micoKubernetesClient.isApplicationDeployed(micoApplication);
        assertFalse("Application is not deployed, because MicoService was deployed by other application", result);
    }

    @Test
    public void isApplicationDeployedWithNotExistingServiceInterfaces() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = setUpApplicationDeployment();

        // There are no Kubernetes Services
        micoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo().setServiceNames(new ArrayList<>());
        MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
            micoApplication.getVersion(), Status.ERROR, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);

        assertFalse("Expected application is not deployed, because there are no Kubernetes Services", micoKubernetesClient.isApplicationDeployed(micoApplication));
    }

    @Test
    public void undeployApplication() {
        MicoApplication micoApplication = setUpApplicationDeployment();
        MicoService micoService = micoApplication.getServices().get(0);
        KubernetesDeploymentInfo kubernetesDeploymentInfo = micoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo();
        // Prepare build
        mockServer.getClient()
            .pods()
            .inNamespace(testNamespace)
            .create(new PodBuilder()
                .withNewMetadata()
                .withLabels(CollectionUtils.mapOf(
                    KnativeBuildController.BUILD_CRD_GROUP + "/buildName",
                    imageBuilder.createBuildName(micoService)))
                .endMetadata()
                .build());

        micoKubernetesClient.undeployApplication(micoApplication);

        Deployment actualDeployment = mockServer.getClient()
            .apps()
            .deployments()
            .inNamespace(kubernetesDeploymentInfo.getNamespace())
            .withName(kubernetesDeploymentInfo.getDeploymentName()).get();
        Service actualService = mockServer.getClient()
            .services()
            .inNamespace(kubernetesDeploymentInfo.getNamespace())
            .withName(kubernetesDeploymentInfo.getServiceNames().get(0)).get();
        List<Pod> actualPods = mockServer.getClient()
            .pods()
            .inAnyNamespace()
            .withLabel(KnativeBuildController.BUILD_CRD_GROUP + "/buildName", imageBuilder.createBuildName(micoService))
            .list().getItems();
        assertNull("Expected Kubernetes deployment is deleted", actualDeployment);
        assertNull("Expected Kubernetes service is deleted", actualService);
        assertTrue("Expected there is no Kubernetes Build pod", actualPods.isEmpty());
    }

    @Test
    public void undeployApplicationIfServiceIsUsedByMultipleApplicationsWithDifferentInstances() throws MicoApplicationNotFoundException {
        // Create a setup with 3 applications that all uses the same service but with different instances:
        // Two of them are already deployed, the third wants to use the same service want is not deployed yet.

        // Create first application (already deployed)
        MicoApplication micoApplication1 = setUpApplicationDeployment();
        MicoService micoService = micoApplication1.getServices().get(0);
        MicoServiceDeploymentInfo serviceDeploymentInfo1 = micoApplication1.getServiceDeploymentInfos().get(0);
        KubernetesDeploymentInfo kubernetesDeploymentInfo1 = serviceDeploymentInfo1.getKubernetesDeploymentInfo();

        // Create second application (already deployed)
        KubernetesDeploymentInfo kubernetesDeploymentInfo2 = new KubernetesDeploymentInfo()
            .setId(4001L)
            .setNamespace(kubernetesDeploymentInfo1.getNamespace())
            .setDeploymentName(kubernetesDeploymentInfo1.getDeploymentName())
            .setServiceNames(kubernetesDeploymentInfo1.getServiceNames());
        MicoServiceDeploymentInfo serviceDeploymentInfo2 = new MicoServiceDeploymentInfo()
            .setId(3001L)
            .setService(micoService)
            .setInstanceId(INSTANCE_ID_1) // different instance ID
            .setKubernetesDeploymentInfo(kubernetesDeploymentInfo2);
        MicoApplication micoApplication2 = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION)
            .setName(NAME_1)
            .setServices(CollectionUtils.listOf(micoService))
            .setServiceDeploymentInfos(CollectionUtils.listOf(serviceDeploymentInfo2));

        // Create third application (not deployed)
        MicoServiceDeploymentInfo serviceDeploymentInfo3 = new MicoServiceDeploymentInfo()
            .setId(3002L)
            .setInstanceId(INSTANCE_ID_2) // different instance ID
            .setService(micoService)
            .setKubernetesDeploymentInfo(null);
        MicoApplication micoApplication3 = new MicoApplication()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION)
            .setName(NAME_2)
            .setServices(CollectionUtils.listOf(micoService))
            .setServiceDeploymentInfos(CollectionUtils.listOf(serviceDeploymentInfo3));

        // Each MICO application uses a different instance of the same MICO service
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication1.getShortName(), micoApplication1.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo1));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication2.getShortName(), micoApplication2.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo2));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication3.getShortName(), micoApplication3.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo3));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication1.getShortName(), micoApplication1.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo1));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication2.getShortName(), micoApplication2.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo2));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication3.getShortName(), micoApplication3.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo3));
        given(applicationRepository.findAllByUsedServiceInstance(serviceDeploymentInfo1.getInstanceId()))
            .willReturn(CollectionUtils.listOf(micoApplication1));
        given(applicationRepository.findAllByUsedServiceInstance(serviceDeploymentInfo2.getInstanceId()))
            .willReturn(CollectionUtils.listOf(micoApplication2));
        given(applicationRepository.findAllByUsedServiceInstance(serviceDeploymentInfo3.getInstanceId()))
            .willReturn(CollectionUtils.listOf(micoApplication3));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(micoApplication1.getShortName(), micoApplication1.getVersion()))
            .willReturn(new MicoApplicationJobStatus(micoApplication1.getShortName(), micoApplication1.getVersion(), Status.DONE, new ArrayList<>()));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(micoApplication2.getShortName(), micoApplication2.getVersion()))
            .willReturn(new MicoApplicationJobStatus(micoApplication2.getShortName(), micoApplication2.getVersion(), Status.DONE, new ArrayList<>()));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(micoApplication3.getShortName(), micoApplication3.getVersion()))
            .willReturn(new MicoApplicationJobStatus(micoApplication3.getShortName(), micoApplication3.getVersion(), Status.UNDEFINED, new ArrayList<>()));

        // Prepare build
        mockServer.getClient()
            .pods()
            .inNamespace(testNamespace)
            .create(new PodBuilder()
                .withNewMetadata()
                .withLabels(CollectionUtils.mapOf(
                    KnativeBuildController.BUILD_CRD_GROUP + "/buildName",
                    imageBuilder.createBuildName(micoService)))
                .endMetadata()
                .build());

        micoKubernetesClient.undeployApplication(micoApplication2);

        Deployment actualDeployment = mockServer.getClient()
            .apps()
            .deployments()
            .inNamespace(kubernetesDeploymentInfo2.getNamespace())
            .withName(kubernetesDeploymentInfo2.getDeploymentName()).get();
        Service actualService = mockServer.getClient()
            .services()
            .inNamespace(kubernetesDeploymentInfo2.getNamespace())
            .withName(kubernetesDeploymentInfo2.getServiceNames().get(0)).get();
        List<Pod> actualPods = mockServer.getClient()
            .pods()
            .inAnyNamespace()
            .withLabel(KnativeBuildController.BUILD_CRD_GROUP + "/buildName", imageBuilder.createBuildName(micoService))
            .list().getItems();
        // It's not possible to check if the actual replicas are valid, because they are not applied immediately.
        // Therefore just test if the Kubernetes resources still exist and there are no error during the scale in.
        assertNull("Expected Kubernetes deployment is removed after undeployment", actualDeployment);
        assertNull("Expected Kubernetes service is removed after undeployment", actualService);
        assertTrue("Expected there are no Kubernetes Build pods anymore", actualPods.isEmpty());
    }

    @Test
    public void undeployApplicationIfServiceInstanceIsUsedByMultipleApplications() throws MicoApplicationNotFoundException {
        // Create a setup with 3 applications that all uses the same service:
        // Two of them are already deployed, the third wants to use the same service want is not deployed yet.

        // Create first application (already deployed)
        MicoApplication micoApplication1 = setUpApplicationDeployment();
        MicoService micoService = micoApplication1.getServices().get(0);
        MicoServiceDeploymentInfo serviceDeploymentInfo1 = micoApplication1.getServiceDeploymentInfos().get(0);
        String instanceId = serviceDeploymentInfo1.getInstanceId();
        KubernetesDeploymentInfo kubernetesDeploymentInfo1 = serviceDeploymentInfo1.getKubernetesDeploymentInfo();

        // Create second application (already deployed)
        KubernetesDeploymentInfo kubernetesDeploymentInfo2 = new KubernetesDeploymentInfo()
            .setId(4001L)
            .setNamespace(kubernetesDeploymentInfo1.getNamespace())
            .setDeploymentName(kubernetesDeploymentInfo1.getDeploymentName())
            .setServiceNames(kubernetesDeploymentInfo1.getServiceNames());
        MicoServiceDeploymentInfo serviceDeploymentInfo2 = new MicoServiceDeploymentInfo()
            .setId(3001L)
            .setService(micoService)
            .setInstanceId(instanceId) // same instance ID
            .setKubernetesDeploymentInfo(kubernetesDeploymentInfo2);
        MicoApplication micoApplication2 = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION)
            .setName(NAME_1)
            .setServices(CollectionUtils.listOf(micoService))
            .setServiceDeploymentInfos(CollectionUtils.listOf(serviceDeploymentInfo2));

        // Create third application (not deployed)
        MicoServiceDeploymentInfo serviceDeploymentInfo3 = new MicoServiceDeploymentInfo()
            .setId(3002L)
            .setInstanceId(instanceId) // same instance ID
            .setService(micoService)
            .setKubernetesDeploymentInfo(null);
        MicoApplication micoApplication3 = new MicoApplication()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION)
            .setName(NAME_2)
            .setServices(CollectionUtils.listOf(micoService))
            .setServiceDeploymentInfos(CollectionUtils.listOf(serviceDeploymentInfo3));

        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication1.getShortName(), micoApplication1.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo1));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication2.getShortName(), micoApplication2.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo2));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication3.getShortName(), micoApplication3.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo3));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication1.getShortName(), micoApplication1.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo1));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication2.getShortName(), micoApplication2.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo2));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication3.getShortName(), micoApplication3.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo3));
        given(applicationRepository.findAllByUsedServiceInstance(instanceId))
            .willReturn(CollectionUtils.listOf(micoApplication1, micoApplication2, micoApplication3));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(micoApplication1.getShortName(), micoApplication1.getVersion()))
            .willReturn(new MicoApplicationJobStatus(micoApplication1.getShortName(), micoApplication1.getVersion(), Status.DONE, new ArrayList<>()));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(micoApplication2.getShortName(), micoApplication2.getVersion()))
            .willReturn(new MicoApplicationJobStatus(micoApplication2.getShortName(), micoApplication2.getVersion(), Status.DONE, new ArrayList<>()));
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(micoApplication3.getShortName(), micoApplication3.getVersion()))
            .willReturn(new MicoApplicationJobStatus(micoApplication3.getShortName(), micoApplication3.getVersion(), Status.UNDEFINED, new ArrayList<>()));

        // Prepare build
        mockServer.getClient()
            .pods()
            .inNamespace(testNamespace)
            .create(new PodBuilder()
                .withNewMetadata()
                .withLabels(CollectionUtils.mapOf(
                    KnativeBuildController.BUILD_CRD_GROUP + "/buildName",
                    imageBuilder.createBuildName(micoService)))
                .endMetadata()
                .build());

        micoKubernetesClient.undeployApplication(micoApplication2);

        Deployment actualDeployment = mockServer.getClient()
            .apps()
            .deployments()
            .inNamespace(kubernetesDeploymentInfo2.getNamespace())
            .withName(kubernetesDeploymentInfo2.getDeploymentName()).get();
        Service actualService = mockServer.getClient()
            .services()
            .inNamespace(kubernetesDeploymentInfo2.getNamespace())
            .withName(kubernetesDeploymentInfo2.getServiceNames().get(0)).get();
        List<Pod> actualPods = mockServer.getClient()
            .pods()
            .inAnyNamespace()
            .withLabel(KnativeBuildController.BUILD_CRD_GROUP + "/buildName", imageBuilder.createBuildName(micoService))
            .list().getItems();
        // It's not possible to check if the actual replicas are valid, because they are not applied immediately.
        // Therefore just test if the Kubernetes resources still exist and there are no error during the scale in.
        assertNotNull("Expected Kubernetes deployment is still there (with less replicas)", actualDeployment);
        assertNotNull("Expected Kubernetes service is still there", actualService);
        assertFalse("Expected there are still Kubernetes Build pods", actualPods.isEmpty());
    }

    public MicoApplication setUpApplicationDeployment() {
        return setUpApplicationDeployment(getMicoServiceInstance());
    }

    public MicoApplication setUpApplicationDeployment(MicoServiceDeploymentInfo... micoServiceDeploymentInfoArray) {
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos = Arrays.asList(micoServiceDeploymentInfoArray);
        List<MicoService> micoServices = micoServiceDeploymentInfos.stream().map(MicoServiceDeploymentInfo::getService).collect(Collectors.toList());
        MicoApplication micoApplication = new MicoApplication()
            .setId(1000L)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setServices(micoServices)
            .setServiceDeploymentInfos(micoServiceDeploymentInfos);

        for (MicoServiceDeploymentInfo serviceDeploymentInfo : micoApplication.getServiceDeploymentInfos()) {
            MicoService micoService = serviceDeploymentInfo.getService();
            String instanceId = serviceDeploymentInfo.getInstanceId();
            Deployment existingKubernetesDeployment = getDeploymentObject(serviceDeploymentInfo);
            Map<String, String> labelsOfDeployment = CollectionUtils.mapOf(
                LABEL_INSTANCE_KEY, instanceId);
            existingKubernetesDeployment.getMetadata().setLabels(labelsOfDeployment);
            mockServer.getClient().apps().deployments().inNamespace(testNamespace).createOrReplace(existingKubernetesDeployment);

            List<Service> existingKubernetesServices = new ArrayList<>();
            for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
                Service kubernetesService = getServiceObject(serviceInterface, serviceDeploymentInfo);
                Map<String, String> labelsOfService = CollectionUtils.mapOf(
                    LABEL_NAME_KEY, micoService.getShortName(),
                    LABEL_VERSION_KEY, micoService.getVersion(),
                    LABEL_INTERFACE_KEY, serviceInterface.getServiceInterfaceName(),
                    LABEL_INSTANCE_KEY, instanceId);
                existingKubernetesDeployment.getMetadata().setLabels(labelsOfService);
                existingKubernetesServices.add(kubernetesService);

                mockServer.getClient().services().inNamespace(testNamespace).createOrReplace(kubernetesService);
            }

            serviceDeploymentInfo.setKubernetesDeploymentInfo(new KubernetesDeploymentInfo()
                .setNamespace(testNamespace)
                .setDeploymentName(existingKubernetesDeployment.getMetadata().getName())
                .setServiceNames(existingKubernetesServices.stream()
                    .map(service -> service.getMetadata().getName()).collect(Collectors.toList())));

            given(serviceDeploymentInfoRepository.findByApplicationAndService(
                micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName(), micoService.getVersion()))
                .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
            given(serviceDeploymentInfoRepository.findAllByService(micoService.getShortName(), micoService.getVersion()))
                .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
            given(applicationRepository.findAllByUsedService(micoService.getShortName(), micoService.getVersion()))
                .willReturn(CollectionUtils.listOf(micoApplication));
        }
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(micoApplication.getServiceDeploymentInfos());

        return micoApplication;
    }

    @Test
    public void getYaml() throws JsonProcessingException {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceInstance();
        MicoService micoService = micoServiceDeploymentInfo.getService();
        String instanceId = micoServiceDeploymentInfo.getInstanceId();
        Deployment existingDeployment = getDeploymentObject(micoServiceDeploymentInfo);
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();

        mockServer.getClient().services().inNamespace(testNamespace).createOrReplace(
            getServiceObject(micoServiceInterface, micoServiceDeploymentInfo));
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).createOrReplace(existingDeployment);
        String actualYaml = micoKubernetesClient.getYaml(micoService);
        assertEquals("---\n" +
            "apiVersion: \"apps/v1\"\n" +
            "kind: \"Deployment\"\n" +
            "metadata:\n" +
            "  labels:\n" +
            "    ust.mico/instance: \"" + instanceId + "\"\n" +
            "    ust.mico/name: \"" + SERVICE_SHORT_NAME + "\"\n" +
            "    ust.mico/version: \"" + SERVICE_VERSION + "\"\n" +
            "  name: \"" + instanceId + "\"\n" +
            "  namespace: \"" + testNamespace + "\"\n" +
            "spec:\n" +
            "  replicas: 1\n" +
            "---\n" +
            "apiVersion: \"v1\"\n" +
            "kind: \"Service\"\n" +
            "metadata:\n" +
            "  labels:\n" +
            "    ust.mico/interface: \"" + SERVICE_INTERFACE_NAME + "\"\n" +
            "    ust.mico/instance: \"" + instanceId + "\"\n" +
            "    ust.mico/name: \"" + SERVICE_SHORT_NAME + "\"\n" +
            "    ust.mico/version: \"" + SERVICE_VERSION + "\"\n" +
            "  name: \"" + micoKubernetesClient.createServiceName(micoServiceDeploymentInfo, micoServiceInterface) + "\"\n" +
            "  namespace: \"" + testNamespace + "\"\n" +
            "spec: {}\n", actualYaml);
    }

    @Test
    public void testGetOpenFaaSCredentialsTest() {
        String testPassword = "testPassword";
        String testUsername = "testUsername";

        String testPasswordBase64 = Base64.getEncoder().encodeToString(testPassword.getBytes());
        String testUsernameBase64 = Base64.getEncoder().encodeToString(testUsername.getBytes());

        given(micoKubernetesConfig.getNamespaceOpenFaasWorkspace()).willReturn("openfaas");

        mockServer.getClient().secrets().
            inNamespace(micoKubernetesConfig.getNamespaceOpenFaasWorkspace())
            .create(new SecretBuilder()
                .addToData(OPEN_FAAS_SECRET_DATA_PASSWORD_NAME, testPasswordBase64)
                .addToData(OPEN_FAAS_SECRET_DATA_USERNAME_NAME, testUsernameBase64)
                .withNewMetadata().withName(OPEN_FAAS_SECRET_NAME).endMetadata().build());

        PasswordAuthentication openFaasCredentials = micoKubernetesClient.getOpenFaasCredentials();

        assertThat(openFaasCredentials.getUserName(), is(equalTo(testUsername)));
        assertThat(new String(openFaasCredentials.getPassword()), is(equalTo(testPassword)));
    }

    @Test(expected = KubernetesResourceException.class)
    public void getExternalIpOfServiceNoStatus() throws Exception {
        String testServiceName = "testservice";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName).build());
        micoKubernetesClient.getPublicIpOfKubernetesService(testServiceName, testNamespace);
    }

    @Test(expected = KubernetesResourceException.class)
    public void getExternalIpOfServiceWithNoService() throws Exception {
        String testServiceName = "testservice";
        micoKubernetesClient.getPublicIpOfKubernetesService(testServiceName, testNamespace);
    }

    @Test
    public void getExternalIpOfService() throws Exception {
        String testServiceName = "testservice";
        String ip = "192.168.0.0";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName)
                .withStatus(
                    new ServiceStatusBuilder()
                        .withNewLoadBalancer()
                        .addNewIngress()
                        .withIp(ip)
                        .endIngress()
                        .endLoadBalancer().build())
                .build());
        assertThat(micoKubernetesClient.getPublicIpOfKubernetesService(testServiceName, testNamespace), is(optionalWithValue(is(ip))));
    }

    @Test
    public void getExternalIpOfServiceNoIngress() throws Exception {
        String testServiceName = "testservice";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName)
                .withStatus(
                    new ServiceStatusBuilder()
                        .withNewLoadBalancer()
                        .endLoadBalancer().build())
                .build());
        assertThat(micoKubernetesClient.getPublicIpOfKubernetesService(testServiceName, testNamespace), is(emptyOptional()));
    }

    @Test
    public void getExternalIpOfServiceTooManyIngresses() throws Exception {
        String testServiceName = "testservice";
        String ip = "192.168.0.0";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName)
                .withStatus(
                    new ServiceStatusBuilder()
                        .withNewLoadBalancer()
                        .addNewIngress()
                        .withIp(ip)
                        .endIngress()
                        .addNewIngress()
                        .withIp(ip)
                        .endIngress()
                        .endLoadBalancer().build())
                .build());
        assertThat(micoKubernetesClient.getPublicIpOfKubernetesService(testServiceName, testNamespace), is(optionalWithValue(is(ip))));
    }

    @Test
    public void getPublicPortsOfKubernetesService() throws Exception {
        String testServiceName = "testservice";
        int port = 8080;
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName)
                .withNewSpec()
                .addNewPort()
                .withPort(port)
                .endPort()
                .endSpec()
                .build()
        );
        List<Integer> ports = micoKubernetesClient.getPublicPortsOfKubernetesService(testServiceName, testNamespace);
        assertThat(ports, hasItem(port));
        assertThat(ports, hasSize(1));
    }

    @Test
    public void getPublicPortsOfKubernetesServiceWithTwoPorts() throws Exception {
        String testServiceName = "testservice";
        int port = 8080;
        int port2 = 8081;
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName)
                .withNewSpec()
                .addNewPort()
                .withPort(port)
                .endPort()
                .addNewPort()
                .withPort(port2)
                .endPort()
                .endSpec()
                .build()
        );
        List<Integer> ports = micoKubernetesClient.getPublicPortsOfKubernetesService(testServiceName, testNamespace);
        assertThat(ports, hasItems(port, port2));
        assertThat(ports, hasSize(2));
    }

    @Test(expected = KubernetesResourceException.class)
    public void getPublicPortsOfKubernetesServiceWithNoSpec() throws Exception {
        String testServiceName = "testservice";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName).build());
        micoKubernetesClient.getPublicPortsOfKubernetesService(testServiceName, testNamespace);
    }

    @Test
    public void getPublicPortsOfKubernetesServiceWithNoPorts() throws Exception {
        String testServiceName = "testservice";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName)
                .withNewSpec()
                .endSpec().build());
        List<Integer> ports = micoKubernetesClient.getPublicPortsOfKubernetesService(testServiceName, testNamespace);
        assertThat(ports, empty());
    }

    @Test
    public void getService() {
        String testServiceName = "testservice";
        mockServer.getClient().services().inNamespace(testNamespace).create(
            getServiceBuilderWithNameAndNamespace(testServiceName).build());
        Optional<Service> service = micoKubernetesClient.getService(testServiceName, testNamespace);
        assertThat(service, is(optionalWithValue()));
        assertTrue(service.isPresent());
        assertThat(service.get().getMetadata().getName(), is(testServiceName));
    }

    @Test
    public void getServiceNoMatchingService() {
        String testServiceName = "testservice";
        Optional<Service> service = micoKubernetesClient.getService(testServiceName, testNamespace);
        assertThat(service, is(emptyOptional()));
    }

    private ServiceBuilder getServiceBuilderWithNameAndNamespace(String testServiceName) {
        return new ServiceBuilder().withNewMetadata()
            .withNamespace(testNamespace)
            .withName(testServiceName)
            .endMetadata();
    }

    private Deployment getDeploymentObject(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, serviceDeploymentInfo.getService().getShortName(),
            LABEL_VERSION_KEY, serviceDeploymentInfo.getService().getVersion(),
            LABEL_INSTANCE_KEY, serviceDeploymentInfo.getInstanceId());
        return new DeploymentBuilder()
            .withNewMetadata()
            .withName(serviceDeploymentInfo.getInstanceId())
            .withNamespace(testNamespace)
            .withLabels(labels)
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .endSpec()
            .build();
    }

    private Service getServiceObject(MicoServiceInterface micoServiceInterface, MicoServiceDeploymentInfo serviceDeploymentInfo) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, serviceDeploymentInfo.getService().getShortName(),
            LABEL_VERSION_KEY, serviceDeploymentInfo.getService().getVersion(),
            LABEL_INTERFACE_KEY, micoServiceInterface.getServiceInterfaceName(),
            LABEL_INSTANCE_KEY, serviceDeploymentInfo.getInstanceId());
        return new ServiceBuilder()
            .withNewMetadata()
            .withName(micoKubernetesClient.createServiceName(serviceDeploymentInfo, micoServiceInterface))
            .withNamespace(testNamespace)
            .withLabels(labels)
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    }

    private MicoServiceDeploymentInfo getMicoServiceInstance() {
        MicoService micoService = getMicoServiceWithoutInterface();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        micoService.setServiceInterfaces(CollectionUtils.listOf(micoServiceInterface));
        return new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setInstanceId(INSTANCE_ID);
    }

    private MicoServiceDeploymentInfo getMicoServiceInstance_2() {
        MicoService micoService = getMicoServiceWithoutInterface()
            .setId(ID_1).setShortName(SERVICE_SHORT_NAME_1).setName(NAME_1);
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface()
            .setServiceInterfaceName(SERVICE_INTERFACE_NAME_1);
        micoService.setServiceInterfaces(CollectionUtils.listOf(micoServiceInterface));
        return new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setInstanceId(INSTANCE_ID_1);
    }

    private MicoService getMicoServiceWithoutInterface() {
        return new MicoService()
            .setId(2000L)
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setName(NAME)
            .setDockerImageUri(IntegrationTest.DOCKER_IMAGE_URI);
    }

    private MicoServiceInterface getMicoServiceInterface() {
        return new MicoServiceInterface()
            .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
            .setPorts(CollectionUtils.listOf(
                new MicoServicePort()
                    .setPort(80)
                    .setTargetPort(80)
                )
            );
    }
}
