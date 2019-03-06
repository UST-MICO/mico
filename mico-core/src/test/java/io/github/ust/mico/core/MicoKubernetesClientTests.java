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
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.UIDUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoKubernetesClientTests {

    private static final String LABEL_PREFIX = "ust.mico/";
    private static final String LABEL_NAME_KEY = LABEL_PREFIX + "name";
    private static final String LABEL_VERSION_KEY = LABEL_PREFIX + "version";
    private static final String LABEL_INTERFACE_KEY = LABEL_PREFIX + "interface";
    private static final String LABEL_INSTANCE_KEY = LABEL_PREFIX + "instance";

    @Captor
    ArgumentCaptor<Service> serviceArgumentCaptor;

    @Captor
    ArgumentCaptor<Deployment> deploymentArgumentCaptor;

    @Captor
    ArgumentCaptor<String> namespaceArgumentCaptor;

    @Rule
    public KubernetesServer mockServer = new KubernetesServer(false, true);

    @MockBean
    private MicoKubernetesConfig micoKubernetesConfig;

    @MockBean
    private MicoServiceRepository serviceRepository;

    private MicoKubernetesClient micoKubernetesClient;

    private static String testNamespace = "test-namespace";

    @Before
    public void setUp() {
        given(micoKubernetesConfig.getNamespaceMicoWorkspace()).willReturn(testNamespace);

        micoKubernetesClient = new MicoKubernetesClient(micoKubernetesConfig, serviceRepository, mockServer.getClient());
    }

    @Test
    public void creationOfMicoServiceWorks() throws KubernetesResourceException {
        // Assert that at the beginning there are no deployment
        DeploymentList deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(0, deployments.getItems().size());

        MicoService micoService = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService);

        micoKubernetesClient.createMicoService(deploymentInfo);

        deployments = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list();
        assertEquals(1, deployments.getItems().size());
        Deployment actualDeployment = deployments.getItems().get(0);
        assertNotNull(actualDeployment);
        assertTrue("Name of Kubernetes Deployment does not start with short name of MicoService",
            actualDeployment.getMetadata().getName().startsWith(micoService.getShortName()));
        assertEquals(testNamespace, actualDeployment.getMetadata().getNamespace());
    }

    @Test
    public void creationOfMicoServiceWithEnvironmentVariablesWorks() throws KubernetesResourceException {
        assertEquals(0, mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().size());

        MicoService micoService = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setEnvironmentVariables(CollectionUtils.listOf(new MicoEnvironmentVariable().setName("NAME").setValue("VALUE")));

        micoKubernetesClient.createMicoService(deploymentInfo);

        assertEquals(1, mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().size());

        Deployment actualDeployment = mockServer.getClient().apps().deployments().inNamespace(testNamespace).list().getItems().get(0);
        assertNotNull(actualDeployment);
        assertEquals("Environment variables do not match expected",
            deploymentInfo.getEnvironmentVariables(), actualDeployment.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().stream().map(
                envVar -> new MicoEnvironmentVariable().setName(envVar.getName()).setValue(envVar.getValue())).collect(Collectors.toList()));
    }

    @Test
    public void creationOfMicoServiceInterfaceWorks() throws KubernetesResourceException {
        // Assert that at the beginning there are no deployment
        ServiceList services = mockServer.getClient().services().inNamespace(testNamespace).list();
        assertEquals(0, services.getItems().size());

        MicoService micoService = getMicoServiceWithInterface();
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

        String expectedInstanceLabel = existingDeployment.getMetadata().getLabels().getOrDefault(LABEL_INSTANCE_KEY, "UNKNOWN INSTANCE LABEL KEY");
        assertEquals("Expected INSTANCE label value in metadata is same than associated deployment",
            expectedInstanceLabel, actualService.getMetadata().getLabels().getOrDefault(LABEL_INSTANCE_KEY, "UNKNOWN INSTANCE LABEL KEY"));
        assertEquals("Expected INSTANCE label value in spec used as selector is same than associated deployment",
            expectedInstanceLabel, actualService.getSpec().getSelector().getOrDefault(LABEL_INSTANCE_KEY, "UNKNOWN INSTANCE LABEL KEY"));
        assertEquals(testNamespace, actualService.getMetadata().getNamespace());
    }

    @Test
    public void creationOfMicoServiceThatAlreadyExistsDoesReplaceTheSameObject() throws KubernetesResourceException {
        MicoService micoServiceWithoutInterface = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoServiceWithoutInterface);

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
        MicoService micoService = getMicoServiceWithInterface();
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
    public void isApplicationDeployed() throws KubernetesResourceException {
        MicoService micoService = getMicoServiceWithInterface();
        String deploymentUid = UIDUtils.uidFor(micoService);
        Deployment existingDeployment = getDeploymentObject(micoService, deploymentUid);
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_NAME_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion());
        existingDeployment.getMetadata().setLabels(labels);

        mockServer.getClient().apps().deployments().inNamespace(testNamespace).createOrReplace(existingDeployment);
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION))
            .willReturn(CollectionUtils.listOf(micoService));

        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);

        boolean result = micoKubernetesClient.isApplicationDeployed(micoApplication);

        assertTrue("Expected application is deployed", result);
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
            .build();
    }

    private MicoService getMicoServiceWithInterface() {
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
            .setName(NAME);
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
