package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.service.ClusterAwarenessFabric8;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.UIDUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoKubernetesClientTests {

    private static final String LABEL_APP_KEY = "app";
    private static final String LABEL_VERSION_KEY = "version";
    private static final String LABEL_INTERFACE_KEY = "interface";
    private static final String LABEL_RUN_KEY = "run";

    @MockBean
    MicoKubernetesConfig micoKubernetesConfig;

    @MockBean
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    MicoKubernetesClient micoKubernetesClient;

    private static String testNamespace = "test-namespace";

    @Before
    public void setUp() {
        given(micoKubernetesConfig.getNamespaceMicoWorkspace()).willReturn(testNamespace);
    }

    @Test
    public void creationOfMicoServiceWorks() throws KubernetesResourceException {

        DeploymentList emptyDeploymentList = new DeploymentList(); // at the beginning there are no existing deployments
        given(cluster.getDeploymentsByLabels(anyMap(), anyString())).willReturn(emptyDeploymentList);

        MicoService micoServiceWithoutInterface = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo();
        micoKubernetesClient.createMicoService(micoServiceWithoutInterface, deploymentInfo);

        ArgumentCaptor<Deployment> deploymentArgumentCaptor = ArgumentCaptor.forClass(Deployment.class);
        ArgumentCaptor<String> namespaceArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(cluster, times(1)).createDeployment(deploymentArgumentCaptor.capture(), namespaceArgumentCaptor.capture());
        assertEquals(testNamespace, namespaceArgumentCaptor.getValue());

        Deployment actualDeployment = deploymentArgumentCaptor.getValue();
        assertNotNull(actualDeployment);
        assertTrue("Name of Kubernetes Deployment does not start with short name of MicoService",
            actualDeployment.getMetadata().getName().startsWith(micoServiceWithoutInterface.getShortName()));
        assertEquals(testNamespace, actualDeployment.getMetadata().getNamespace());

        verify(cluster, never()).createService(any(), any());
    }

    @Test
    public void creationOfMicoServiceInterfaceWorks() throws KubernetesResourceException {

        ServiceList existingServices = new ServiceList(); // at the beginning there are no existing services
        given(cluster.getServicesByLabels(anyMap(), anyString())).willReturn(existingServices);

        MicoService micoService = getMicoServiceWithInterface();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        String deploymentUid = UIDUtils.uidFor(micoService);

        Deployment existingDeployment = getDeploymentObject(micoService, deploymentUid);

        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_APP_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion());
        DeploymentList deploymentList = new DeploymentList();
        deploymentList.setItems(CollectionUtils.listOf(existingDeployment));
        given(cluster.getDeploymentsByLabels(labels, testNamespace)).willReturn(deploymentList);

        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoService);

        ArgumentCaptor<Service> serviceArgumentCaptor = ArgumentCaptor.forClass(Service.class);
        ArgumentCaptor<String> namespaceArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(cluster, times(1)).createService(serviceArgumentCaptor.capture(), namespaceArgumentCaptor.capture());
        assertEquals(testNamespace, namespaceArgumentCaptor.getValue());

        Service actualService = serviceArgumentCaptor.getValue();
        assertNotNull(actualService);
        assertTrue("Name of Kubernetes Service does not start with name of MicoServiceInterface",
            actualService.getMetadata().getName().startsWith(micoServiceInterface.getServiceInterfaceName()));

        String expectedRunLabelValue = existingDeployment.getMetadata().getLabels().getOrDefault(LABEL_RUN_KEY, "UNKNOWN RUN LABEL KEY");
        assertEquals("Expected RUN label value in metadata is same than associated deployment",
            expectedRunLabelValue, actualService.getMetadata().getLabels().getOrDefault(LABEL_RUN_KEY, "UNKNOWN RUN LABEL KEY"));
        assertEquals("Expected RUN label value in spec used as selector is same than associated deployment",
            expectedRunLabelValue, actualService.getSpec().getSelector().getOrDefault(LABEL_RUN_KEY, "UNKNOWN RUN LABEL KEY"));
        assertEquals(testNamespace, actualService.getMetadata().getNamespace());

        verify(cluster, never()).createDeployment(any(), any());
    }

    @Test
    public void creationOfMicoServiceThatAlreadyExistsDoesReplaceTheSameObject() throws KubernetesResourceException {

        // Arrange existing deployments
        DeploymentList existingDeployments = new DeploymentList(); // at the beginning there are no existing deployments
        given(cluster.getDeploymentsByLabels(anyMap(), anyString())).willReturn(existingDeployments);

        MicoService micoServiceWithoutInterface = getMicoServiceWithoutInterface();
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo();

        ArgumentCaptor<Deployment> deploymentArgumentCaptor = ArgumentCaptor.forClass(Deployment.class);
        ArgumentCaptor<String> namespaceArgumentCaptor = ArgumentCaptor.forClass(String.class);

        // First creation
        micoKubernetesClient.createMicoService(micoServiceWithoutInterface, deploymentInfo);

        verify(cluster, times(1)).createDeployment(deploymentArgumentCaptor.capture(), namespaceArgumentCaptor.capture());
        Deployment firstDeployment = deploymentArgumentCaptor.getValue();
        assertNotNull(firstDeployment);
        String firstDeploymentName = firstDeployment.getMetadata().getName();
        Deployment deployment = getDeploymentObject(micoServiceWithoutInterface, firstDeploymentName);
        existingDeployments.setItems(CollectionUtils.listOf(deployment));  // after the first deployment there is one deployment
        given(cluster.getDeploymentsByLabels(anyMap(), anyString())).willReturn(existingDeployments);

        // Second creation
        micoKubernetesClient.createMicoService(micoServiceWithoutInterface, deploymentInfo);

        verify(cluster, times(2)).createDeployment(deploymentArgumentCaptor.capture(), namespaceArgumentCaptor.capture());
        Deployment secondDeployment = deploymentArgumentCaptor.getValue();
        assertNotNull(secondDeployment);
        assertEquals("Expected both deployments have the same name", firstDeployment.getMetadata().getName(), secondDeployment.getMetadata().getName());
        assertEquals("Expected both deployments are the same", firstDeployment, secondDeployment);
    }

    @Test
    public void creationOfMicoServiceInterfaceThatAlreadyExistsDoesNotReplaceTheSameObject() throws KubernetesResourceException {

        MicoService micoService = getMicoServiceWithInterface();
        MicoServiceInterface micoServiceInterface = getMicoServiceInterface();
        String deploymentUid = UIDUtils.uidFor(micoService);

        // Arrange existing deployments
        Deployment existingDeployment = getDeploymentObject(micoService, deploymentUid);
        Map<String, String> labels = CollectionUtils.mapOf(LABEL_APP_KEY, micoService.getShortName(), LABEL_VERSION_KEY, micoService.getVersion());
        DeploymentList deploymentList = new DeploymentList();
        deploymentList.setItems(CollectionUtils.listOf(existingDeployment));
        given(cluster.getDeploymentsByLabels(labels, testNamespace)).willReturn(deploymentList);

        // Arrange existing services
        ServiceList existingServices = new ServiceList(); // at the beginning there are no existing services
        given(cluster.getServicesByLabels(anyMap(), anyString())).willReturn(existingServices);

        ArgumentCaptor<Service> serviceArgumentCaptor = ArgumentCaptor.forClass(Service.class);
        ArgumentCaptor<String> namespaceArgumentCaptor = ArgumentCaptor.forClass(String.class);

        // First creation
        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoService);

        verify(cluster, times(1)).createService(serviceArgumentCaptor.capture(), namespaceArgumentCaptor.capture());
        Service firstService = serviceArgumentCaptor.getValue();
        assertNotNull(firstService);
        String firstServiceName = firstService.getMetadata().getName();
        Service service = getServiceObject(micoServiceInterface, micoService, firstServiceName);
        existingServices.setItems(CollectionUtils.listOf(service)); // after the first creation there is one service
        given(cluster.getServicesByLabels(anyMap(), anyString())).willReturn(existingServices);

        // Second creation
        micoKubernetesClient.createMicoServiceInterface(micoServiceInterface, micoService);

        verify(cluster, times(2)).createService(serviceArgumentCaptor.capture(), namespaceArgumentCaptor.capture());
        Service secondService = serviceArgumentCaptor.getValue();
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
            LABEL_APP_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion());
        DeploymentList deploymentList = new DeploymentList();
        deploymentList.setItems(CollectionUtils.listOf(existingDeployment));
        given(cluster.getDeploymentsByLabels(labels, testNamespace)).willReturn(deploymentList);

        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setServices(CollectionUtils.listOf(micoService));

        boolean result = micoKubernetesClient.isApplicationDeployed(micoApplication);

        assertTrue("Expected application is deployed", result);
    }

    private Deployment getDeploymentObject(MicoService micoService, String deploymentUid) {
        Map<String, String> labels = CollectionUtils.mapOf(
            LABEL_APP_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_RUN_KEY, deploymentUid);
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
            LABEL_APP_KEY, micoService.getShortName(),
            LABEL_VERSION_KEY, micoService.getVersion(),
            LABEL_INTERFACE_KEY, micoServiceInterface.getServiceInterfaceName(),
            LABEL_RUN_KEY, serviceInterfaceUid);
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
            .setShortName(SHORT_NAME)
            .setVersion(RELEASE)
            .setGitCloneUrl(GIT_TEST_REPO_URL)
            .setDockerfilePath(DOCKERFILE);
    }

    private MicoServiceInterface getMicoServiceInterface() {
        return new MicoServiceInterface()
            .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
            .setPorts(CollectionUtils.listOf(
                new MicoServicePort()
                    .setNumber(80)
                    .setTargetPort(80)
                )
            );
    }
}
