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
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.TestConstants.IntegrationTest;
import io.github.ust.mico.core.broker.BackgroundJobBroker;
import io.github.ust.mico.core.configuration.MicoKubernetesBuildBotConfig;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceBackgroundJob.Status;
import io.github.ust.mico.core.persistence.KubernetesDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.UIDUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoKubernetesClientTests {

    private static final String LABEL_PREFIX = "ust.mico/";
    private static final String LABEL_NAME_KEY = LABEL_PREFIX + "name";
    private static final String LABEL_VERSION_KEY = LABEL_PREFIX + "version";
    private static final String LABEL_INTERFACE_KEY = LABEL_PREFIX + "interface";
    private static final String LABEL_INSTANCE_KEY = LABEL_PREFIX + "instance";

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(false, true);

    @MockBean
    private MicoKubernetesConfig micoKubernetesConfig;

    @MockBean
    private MicoKubernetesBuildBotConfig micoKubernetesBuildBotConfig;

    @MockBean
    private ImageBuilder imageBuilder;
    
    @MockBean
    private BackgroundJobBroker backgroundJobBroker;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @MockBean
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    private MicoKubernetesClient micoKubernetesClient;

    private static String testNamespace = "test-namespace";
    private static String buildTestNamespace = "test-namespace";

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
    public void creationOfMicoServiceWorks() throws KubernetesResourceException {
        // Assert that at the beginning there are no deployment
        DeploymentList deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(0, deployments.getItems().size());

        MicoService micoService = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo().setService(micoService);

        micoKubernetesClient.createMicoService(deploymentInfo);

        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment actualDeployment = deployments.getItems().get(0);
        assertNotNull(actualDeployment);
        assertTrue("Name of Kubernetes Deployment does not start with short name of MicoService",
            actualDeployment.getMetadata().getName().startsWith(micoService.getShortName()));
        assertEquals(testNamespace, actualDeployment.getMetadata().getNamespace());
        assertEquals("Expected 1 container",
            1, actualDeployment.getSpec().getTemplate().getSpec().getContainers().size());
        assertEquals("Expected 3 labels",
            3, actualDeployment.getMetadata().getLabels().size());
        assertEquals("Expected 3 labels in template",
            3, actualDeployment.getSpec().getTemplate().getMetadata().getLabels().size());
    }

    @Test
    public void creationOfMicoServiceWithDeploymentInformationWorks() throws KubernetesResourceException {
        MicoService micoService = getMicoServiceWithoutInterface();
        
        MicoLabel label = new MicoLabel().setKey("some-label-key").setValue("some-label-value");
        MicoEnvironmentVariable environmentVariable = new MicoEnvironmentVariable().setName("some-env-name").setValue("some-env-value");
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setReplicas(3)
            .setImagePullPolicy(MicoServiceDeploymentInfo.ImagePullPolicy.NEVER)
            .setLabels(CollectionUtils.listOf(label))
            .setEnvironmentVariables(CollectionUtils.listOf(environmentVariable));

        micoKubernetesClient.createMicoService(serviceDeploymentInfo);

        assertEquals(1, mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().size());
        
        Deployment actualDeployment = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().get(0);
        
        assertNotNull(actualDeployment);
        assertTrue("Custom label does not exist", actualDeployment.getMetadata().getLabels().containsKey(label.getKey()));
        assertEquals("Replicas does not match expected", serviceDeploymentInfo.getReplicas(), actualDeployment.getSpec().getReplicas().intValue());
        assertEquals("Expected 1 container", 1, actualDeployment.getSpec().getTemplate().getSpec().getContainers().size());
        assertEquals("Name of container does not match short name of the MicoService",micoService.getShortName(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getName());
        assertEquals("Image of container does not match expected", micoService.getDockerImageUri(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
        assertEquals("ImagePullPolicy does not match expected", serviceDeploymentInfo.getImagePullPolicy().toString(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImagePullPolicy());
        assertTrue("Custom label in template does not exist", actualDeployment.getSpec().getTemplate().getMetadata().getLabels().containsKey(label.getKey()));
        assertEquals("Environment variable does not exist",
            serviceDeploymentInfo.getEnvironmentVariables(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().stream().map(
                envVar -> new MicoEnvironmentVariable().setName(envVar.getName()).setValue(envVar.getValue())).collect(Collectors.toList()));
    }

    @Test
    public void creationOfMicoServiceInterfaceWorks() throws KubernetesResourceException {
        // Assert that at the beginning there are no deployment
        ServiceList services = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(0, services.getItems().size());

        MicoService micoService = getMicoService();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        String deploymentUid = UIDUtils.uidFor(micoService);

        // Arrange existing deployments
        Deployment existingDeployment = getDeploymentObject(micoService, deploymentUid);
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).create(existingDeployment);

        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoService);

        services = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(1, services.getItems().size());
        Service actualService = services.getItems().get(0);
        assertNotNull(actualService);
        assertTrue("Name of Kubernetes Service does not start with name of MicoServiceInterface",
            actualService.getMetadata().getName().startsWith(micoServiceInterface.getServiceInterfaceName()));
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
    public void creationOfMicoServiceThatAlreadyExistsDoesReplaceTheSameObject() throws KubernetesResourceException {
        MicoService micoServiceWithoutInterface = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo().setService(micoServiceWithoutInterface);

        // First creation
        micoKubernetesClient.createMicoService(deploymentInfo);

        DeploymentList deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment firstDeployment = deployments.getItems().get(0);
        assertNotNull(firstDeployment);

        // Second creation
        micoKubernetesClient.createMicoService(deploymentInfo);

        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment secondDeployment = deployments.getItems().get(0);
        assertNotNull(secondDeployment);
        assertEquals("Expected both deployments have the same name", firstDeployment.getMetadata().getName(), secondDeployment.getMetadata().getName());
        assertEquals("Expected both deployments are the same", firstDeployment, secondDeployment);
    }

    @Test
    public void creationOfMicoServiceInterfaceThatAlreadyExistsReplaceTheSameObject() throws KubernetesResourceException {
        MicoService micoService = getMicoService();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        String deploymentUid = UIDUtils.uidFor(micoService);

        // Arrange existing deployments
        Deployment existingDeployment = getDeploymentObject(micoService, deploymentUid);
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).create(existingDeployment);

        // First creation
        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoService);

        ServiceList existingServices = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(1, existingServices.getItems().size());
        Service firstService = existingServices.getItems().get(0);
        assertNotNull(firstService);

        // Second creation
        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoService);

        existingServices = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(1, existingServices.getItems().size());
        Service secondService = existingServices.getItems().get(0);
        assertNotNull(secondService);
        assertEquals("Expected both services have the same name", firstService.getMetadata().getName(), secondService.getMetadata().getName());
        assertEquals("Expected both services are the same", firstService, secondService);
    }
    
    @Test
    public void getApplicationDeploymentStatusForDeployedApplication() {
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
    public void getApplicationDeploymentStatusForPendingDeployment() {
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
    public void getApplicationDeploymentStatusForRunningDeployment() {
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
    public void getApplicationDeploymentStatusForFailedDeployment() {
    	MicoApplication micoApplication = setUpApplicationDeployment();
    	
    	MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
		    micoApplication.getVersion(), Status.ERROR, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
		    micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);
        
        assertEquals("Application is expected not to be deployed due to the deployment having failed.",
        	MicoApplicationDeploymentStatus.Value.INCOMPLETED,
        	micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }
    
    @Test
    public void getApplicationDeploymentStatusForApplicationWithoutServiceDeploymentInfos() {
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
    public void getApplicationDeploymentStatusForApplicationWithoutKubernetesDeploymentInfos() {
    	MicoApplication micoApplication = setUpApplicationDeployment();
    	
    	MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
		    micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
		    micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);
        
        given(serviceDeploymentInfoRepository.findAllByApplication(
   			micoApplication.getShortName(), micoApplication.getVersion())).willReturn(
   				micoApplication.getServiceDeploymentInfos().stream().map(sdi -> sdi.setKubernetesDeploymentInfo(null)).collect(Collectors.toList()));
        
        assertEquals("Application is expected not to be deployed since it does not have any Kubernetes deployment information.",
        	MicoApplicationDeploymentStatus.Value.UNDEPLOYED,
        	micoKubernetesClient.getApplicationDeploymentStatus(micoApplication).getValue());
    }
    
    @Test
    public void getApplicationDeploymentStatusForApplicationWithNoUpdatedKubernetesDeploymentInfos() {
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
    
    // TODO: Test for INCOMPLETED deployment (required more than one service in test application)

    @Test
    public void isApplicationDeployed() {
        MicoApplication micoApplication = setUpApplicationDeployment();
        
		MicoApplicationJobStatus jobStatus = new MicoApplicationJobStatus(micoApplication.getShortName(),
		    micoApplication.getVersion(), Status.DONE, Collections.emptyList());
        given(backgroundJobBroker.getJobStatusByApplicationShortNameAndVersion(
		    micoApplication.getShortName(), micoApplication.getVersion())).willReturn(jobStatus);
        
        assertTrue("Expected application is not deployed.", micoKubernetesClient.isApplicationDeployed(micoApplication));
    }

    @Test
    public void isApplicationDeployedWithServicesUsedConcurrentlyByOtherApplications() {
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
            .setKubernetesDeploymentInfo(null);
        micoApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        
        given(serviceDeploymentInfoRepository.findAllByApplication(SHORT_NAME_2, VERSION))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));

        boolean result = micoKubernetesClient.isApplicationDeployed(micoApplication);
        assertFalse("Application is not deployed, because MicoService was deployed by other application", result);
    }

    @Test
    public void isApplicationDeployedWithNotExistingServiceInterfaces() {
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
                    ImageBuilder.BUILD_CRD_GROUP + "/buildName",
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
            .withLabel(ImageBuilder.BUILD_CRD_GROUP + "/buildName", imageBuilder.createBuildName(micoService))
            .list().getItems();
        assertNull("Expected Kubernetes deployment is deleted", actualDeployment);
        assertNull("Expected Kubernetes service is deleted", actualService);
        assertTrue("Expected there is no Kubernetes Build pod", actualPods.isEmpty());
    }

    public MicoApplication setUpApplicationDeployment() {
        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME);
        
        MicoService micoService1 = getMicoService();
        
        String deploymentUid = UIDUtils.uidFor(micoService1);
        Deployment existingKubernetesDeployment = getDeploymentObject(micoService1, deploymentUid);
        Map<String, String> labelsOfDeployment = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService1.getShortName(),
            LABEL_VERSION_KEY, micoService1.getVersion(),
            LABEL_INSTANCE_KEY, deploymentUid);
        existingKubernetesDeployment.getMetadata().setLabels(labelsOfDeployment);
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).createOrReplace(existingKubernetesDeployment);

        List<Service> existingKubernetesServices = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService1.getServiceInterfaces()) {
            String serviceUid = UIDUtils.uidFor(serviceInterface);
            Service kubernetesService = getServiceObject(serviceInterface, micoService1, serviceUid);
            Map<String, String> labelsOfService = CollectionUtils.mapOf(
                LABEL_NAME_KEY, micoService1.getShortName(),
                LABEL_VERSION_KEY, micoService1.getVersion(),
                LABEL_INTERFACE_KEY, serviceInterface.getServiceInterfaceName(),
                LABEL_INSTANCE_KEY, serviceUid);
            existingKubernetesDeployment.getMetadata().setLabels(labelsOfService);
            existingKubernetesServices.add(kubernetesService);

            mockServer.getClient().services().inNamespace(testNamespace).createOrReplace(kubernetesService);
        }

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService1)
            .setKubernetesDeploymentInfo(new KubernetesDeploymentInfo()
                .setNamespace(testNamespace)
                .setDeploymentName(existingKubernetesDeployment.getMetadata().getName())
                .setServiceNames(existingKubernetesServices.stream().map(service -> service.getMetadata().getName()).collect(Collectors.toList()))
            );

        micoApplication.setServices(CollectionUtils.listOf(micoService1));
        micoApplication.setServiceDeploymentInfos(CollectionUtils.listOf(serviceDeploymentInfo));

        given(serviceDeploymentInfoRepository.findAllByService(micoService1.getShortName(), micoService1.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication.getShortName(), micoApplication.getVersion(), micoService1.getShortName(), micoService1.getVersion()))
            .willReturn(Optional.of(serviceDeploymentInfo));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));

        return micoApplication;
    }

    private Deployment getDeploymentObject(MicoService micoService, String deploymentUid) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_INSTANCE_KEY, deploymentUid);
        return new DeploymentBuilder()
            .withNewMetadata()
            .withName(deploymentUid)
            .withNamespace(testNamespace)
            .withLabels(labels)
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .endSpec()
            .build();
    }

    private Service getServiceObject(MicoServiceInterface micoServiceInterface, MicoService micoService, String serviceInterfaceUid) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_INTERFACE_KEY, micoServiceInterface.getServiceInterfaceName(),
            LABEL_INSTANCE_KEY, serviceInterfaceUid);
        return new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceInterfaceUid)
            .withNamespace(testNamespace)
            .withLabels(labels)
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    }

    private MicoService getMicoService() {
        MicoService micoService = getMicoServiceWithoutInterface();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        micoService.setServiceInterfaces(CollectionUtils.listOf(micoServiceInterface));
        return micoService;
    }

    private MicoService getMicoServiceWithoutInterface() {
        return new MicoService()
            .setId(ID)
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
