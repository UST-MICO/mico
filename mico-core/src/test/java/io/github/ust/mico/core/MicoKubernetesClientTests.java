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

import com.fasterxml.jackson.core.JsonProcessingException;

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
        MicoInterfaceConnection interfaceConnection = new MicoInterfaceConnection()
                .setEnvironmentVariableName("ENV_VAR")
                .setMicoServiceInterfaceName("INTERFACE_NAME")
                .setMicoServiceShortName("SERVICE_NAME");
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
                .setService(micoService)
                .setReplicas(3)
                .setImagePullPolicy(MicoServiceDeploymentInfo.ImagePullPolicy.NEVER)
                .setLabels(CollectionUtils.listOf(label))
                .setEnvironmentVariables(CollectionUtils.listOf(environmentVariable))
                .setInterfaceConnections(CollectionUtils.listOf(interfaceConnection));

        micoKubernetesClient.createMicoService(serviceDeploymentInfo);

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
        	MicoApplicationDeploymentStatus.Value.INCOMPLETE,
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

    @Test
    public void getApplicationDeploymentStatusForApplicationWithDeploymentNotAvailableAnymore() {
        MicoApplication micoApplication = setUpApplicationDeployment(getMicoService(), getMicoService_2());

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
    public void getApplicationDeploymentStatusForApplicationWithKubernetesServiceNotAvailableAnymore() {
        MicoApplication micoApplication = setUpApplicationDeployment(getMicoService(), getMicoService_2());

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

    @Test
    public void undeployApplicationIfServiceIsUsedByMultipleApplications() {
        // Create a setup with 3 applications that all uses the same service:
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
            .setService(micoService)
            .setKubernetesDeploymentInfo(null);
        MicoApplication micoApplication3 = new MicoApplication()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION)
            .setName(NAME_2)
            .setServices(CollectionUtils.listOf(micoService))
            .setServiceDeploymentInfos(CollectionUtils.listOf(serviceDeploymentInfo3));

        given(serviceDeploymentInfoRepository.findAllByService(micoService.getShortName(), micoService.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo1, serviceDeploymentInfo2, serviceDeploymentInfo3));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication1.getShortName(), micoApplication1.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(Optional.of(serviceDeploymentInfo1));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication2.getShortName(), micoApplication2.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(Optional.of(serviceDeploymentInfo2));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(
            micoApplication3.getShortName(), micoApplication3.getVersion(), micoService.getShortName(), micoService.getVersion()))
            .willReturn(Optional.of(serviceDeploymentInfo3));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication1.getShortName(), micoApplication1.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo1));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication2.getShortName(), micoApplication2.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo2));
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication3.getShortName(), micoApplication3.getVersion()))
            .willReturn(CollectionUtils.listOf(serviceDeploymentInfo3));
        given(applicationRepository.findAllByUsedService(micoService.getShortName(), micoService.getVersion()))
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
                    ImageBuilder.BUILD_CRD_GROUP + "/buildName",
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
            .withLabel(ImageBuilder.BUILD_CRD_GROUP + "/buildName", imageBuilder.createBuildName(micoService))
            .list().getItems();
        // It's not possible to check if the actual replicas are valid, because they are not applied immediately.
        // Therefore just test if the Kubernetes resources still exist and there are no error during the scale in.
        assertNotNull("Expected Kubernetes deployment is still there (with less replicas)", actualDeployment);
        assertNotNull("Expected Kubernetes service is still there", actualService);
        assertFalse("Expected there are still Kubernetes Build pods", actualPods.isEmpty());
    }

    public MicoApplication setUpApplicationDeployment() {
        return setUpApplicationDeployment(getMicoService());
    }

    public MicoApplication setUpApplicationDeployment(MicoService... micoServices) {
        MicoApplication micoApplication = new MicoApplication()
            .setId(1000L)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setServices(Arrays.asList(micoServices));

        for (MicoService micoService : micoApplication.getServices()) {
            String deploymentUid = UIDUtils.uidFor(micoService);
            Deployment existingKubernetesDeployment = getDeploymentObject(micoService, deploymentUid);
            Map<String, String> labelsOfDeployment = CollectionUtils.mapOf(
                LABEL_NAME_KEY, micoService.getShortName(),
                LABEL_VERSION_KEY, micoService.getVersion(),
                LABEL_INSTANCE_KEY, deploymentUid);
            existingKubernetesDeployment.getMetadata().setLabels(labelsOfDeployment);
            mockServer.getClient().apps().deployments().inNamespace(testNamespace).createOrReplace(existingKubernetesDeployment);

            List<Service> existingKubernetesServices = new ArrayList<>();
            for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
                String serviceUid = UIDUtils.uidFor(serviceInterface);
                Service kubernetesService = getServiceObject(serviceInterface, micoService, serviceUid);
                Map<String, String> labelsOfService = CollectionUtils.mapOf(
                    LABEL_NAME_KEY, micoService.getShortName(),
                    LABEL_VERSION_KEY, micoService.getVersion(),
                    LABEL_INTERFACE_KEY, serviceInterface.getServiceInterfaceName(),
                    LABEL_INSTANCE_KEY, serviceUid);
                existingKubernetesDeployment.getMetadata().setLabels(labelsOfService);
                existingKubernetesServices.add(kubernetesService);

                mockServer.getClient().services().inNamespace(testNamespace).createOrReplace(kubernetesService);
            }

            MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
                .setId(3000L)
                .setService(micoService)
                .setKubernetesDeploymentInfo(new KubernetesDeploymentInfo()
                    .setId(4000L)
                    .setNamespace(testNamespace)
                    .setDeploymentName(existingKubernetesDeployment.getMetadata().getName())
                    .setServiceNames(existingKubernetesServices.stream().map(service -> service.getMetadata().getName()).collect(Collectors.toList()))
                );
            micoApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo);

            given(serviceDeploymentInfoRepository.findAllByService(micoService.getShortName(), micoService.getVersion()))
                .willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
            given(serviceDeploymentInfoRepository.findByApplicationAndService(
                micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName(), micoService.getVersion()))
                .willReturn(Optional.of(serviceDeploymentInfo));
            given(applicationRepository.findAllByUsedService(micoService.getShortName(), micoService.getVersion()))
                .willReturn(CollectionUtils.listOf(micoApplication));
        }
        given(serviceDeploymentInfoRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(micoApplication.getServiceDeploymentInfos());

        return micoApplication;
    }

    @Test
    public void getYaml() throws KubernetesResourceException, JsonProcessingException {
        MicoService micoService = getMicoService();
        String deploymentUid = UIDUtils.uidFor(micoService);
        Deployment existingDeployment = getDeploymentObject(micoService, deploymentUid);
        Map<String, String> labels = CollectionUtils.mapOf(
                LABEL_NAME_KEY, micoService.getShortName(),
                LABEL_VERSION_KEY, micoService.getVersion(),
                LABEL_INSTANCE_KEY, deploymentUid);
        existingDeployment.getMetadata().setLabels(labels);
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        String serviceUid = UIDUtils.uidFor(micoServiceInterface);

        mockServer.getClient().services().inNamespace(testNamespace).createOrReplace(getServiceObject(micoServiceInterface, micoService, serviceUid));
        mockServer.getClient().apps().deployments().inNamespace(testNamespace).createOrReplace(existingDeployment);
        System.out.println(micoKubernetesClient.getYaml(micoService));
        assertEquals("---\n" +
                "apiVersion: \"apps/v1\"\n" +
                "kind: \"Deployment\"\n" +
                "metadata:\n" +
                "  annotations: {}\n" +
                "  labels:\n" +
                "    ust.mico/instance: \"" + deploymentUid + "\"\n" +
                "    ust.mico/name: \"" + SERVICE_SHORT_NAME + "\"\n" +
                "    ust.mico/version: \"" + SERVICE_VERSION + "\"\n" +
                "  name: \"" + deploymentUid + "\"\n" +
                "  namespace: \"" + testNamespace + "\"\n" +
                "spec:\n" +
                "  replicas: 1\n" +
                "---\n" +
                "apiVersion: \"v1\"\n" +
                "kind: \"Service\"\n" +
                "metadata:\n" +
                "  annotations: {}\n" +
                "  labels:\n" +
                "    ust.mico/interface: \"" + SERVICE_INTERFACE_NAME + "\"\n" +
                "    ust.mico/instance: \"" + serviceUid + "\"\n" +
                "    ust.mico/name: \"" + SERVICE_SHORT_NAME + "\"\n" +
                "    ust.mico/version: \"" + SERVICE_VERSION + "\"\n" +
                "  name: \"" + serviceUid + "\"\n" +
                "  namespace: \"" + testNamespace + "\"\n" +
                "spec:\n" +
                "  selector: {}\n", micoKubernetesClient.getYaml(micoService));
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

    private MicoService getMicoService_2() {
        MicoService micoService = getMicoServiceWithoutInterface()
            .setId(ID_1).setShortName(SERVICE_SHORT_NAME_1).setName(NAME_1);
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface()
            .setServiceInterfaceName(SERVICE_INTERFACE_NAME_1);
        micoService.setServiceInterfaces(CollectionUtils.listOf(micoServiceInterface));
        return micoService;
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
