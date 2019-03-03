package io.github.ust.mico.core;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.*;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoStatusServiceTest {

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private PrometheusConfig prometheusConfig;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoStatusService micoStatusService;

    private MicoApplication micoApplication;
    private MicoService micoService;
    private Optional<Deployment> deployment;
    private Optional<Service> kubernetesService;
    private PodList podList;

    private String nodeName = "testNode";
    private String podPhase = "Running";
    private String hostIp = "192.168.0.0";
    private String deploymentName = "deployment1";
    private String podName1 = "pod1";

    private int memoryUsage = 70;
    private int cpuLoad = 30;
    private String startTime = new Date().toString();
    private int restarts = 0;
    private boolean podAvailable = true;

    @Before
    public void setupMicoApplication() {
        micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);

        micoService = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setServiceInterfaces(CollectionUtils.listOf(
                new MicoServiceInterface()
                    .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
            ));

        micoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(micoApplication)
            .setService(micoService));

        int availableReplicas = 0;
        int replicas = 1;

        deployment = Optional.of(new DeploymentBuilder()
            .withNewMetadata().withName(deploymentName).endMetadata()
            .withNewSpec().withReplicas(replicas).endSpec().withNewStatus().withAvailableReplicas(availableReplicas).endStatus()
            .build());

        kubernetesService = Optional.of(new ServiceBuilder()
            .withNewMetadata().withName(SERVICE_INTERFACE_NAME).endMetadata()
            .build());

        podList = new PodListBuilder()
            .addNewItem()
            .withNewMetadata().withName(podName1).endMetadata()
            .withNewSpec().withNodeName(nodeName).endSpec()
            .withNewStatus()
            .withStartTime(startTime)
            .addNewContainerStatus().withRestartCount(restarts).endContainerStatus()
            .withPhase(podPhase)
            .withHostIP(hostIp)
            .endStatus()
            .endItem()
            .build();
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getApplicationStatus() {
        MicoApplicationStatusDTO micoApplicationStatus = new MicoApplicationStatusDTO();
        micoApplicationStatus.setServiceStatus(Collections.singletonList(new MicoServiceStatusDTO()
            .setName(NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setAvailableReplicas(0)
            .setRequestedReplicas(1)
            .setPodInfo(Collections.singletonList(new KubernetesPodInfoDTO()
                .setPodName(podName1)
                .setHostIp(hostIp)
                .setNodeName(nodeName)
                .setPhase(podPhase)
                .setAge(startTime)
                .setRestarts(restarts)
                .setMetrics(new KuberenetesPodMetricsDTO()
                    .setMemoryUsage(memoryUsage)
                    .setCpuLoad(cpuLoad)
                    .setAvailable(podAvailable))))
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME)))));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        ResponseEntity responseEntityMemoryUsage = getPrometheusResponseEntity(memoryUsage);
        ResponseEntity responseEntityCpuLoad = getPrometheusResponseEntity(cpuLoad);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntityMemoryUsage).willReturn(responseEntityCpuLoad);
        assertEquals(micoApplicationStatus, micoStatusService.getApplicationStatus(micoApplication));
    }


    @SuppressWarnings("rawtypes")
    private ResponseEntity getPrometheusResponseEntity(int value) {
        PrometheusResponse prometheusResponse = new PrometheusResponse();
        prometheusResponse.setStatus(PrometheusResponse.PROMETHEUS_SUCCESSFUL_RESPONSE);
        prometheusResponse.setValue(value);
        ResponseEntity responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getStatusCode()).willReturn(HttpStatus.OK);
        given(responseEntity.getBody()).willReturn(prometheusResponse);
        return responseEntity;
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getServiceStatus() {
        MicoServiceStatusDTO micoServiceStatus = new MicoServiceStatusDTO();
        micoServiceStatus
            .setName(NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setAvailableReplicas(0)
            .setRequestedReplicas(1)
            .setPodInfo(Collections.singletonList(new KubernetesPodInfoDTO()
                .setPodName(podName1)
                .setHostIp(hostIp)
                .setNodeName(nodeName)
                .setPhase(podPhase)
                .setRestarts(restarts)
                .setAge(startTime)
                .setMetrics(new KuberenetesPodMetricsDTO()
                    .setMemoryUsage(memoryUsage)
                    .setCpuLoad(cpuLoad)
                    .setAvailable(podAvailable))))
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME)));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        ResponseEntity responseEntityMemoryUsage = getPrometheusResponseEntity(memoryUsage);
        ResponseEntity responseEntityCpuLoad = getPrometheusResponseEntity(cpuLoad);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntityMemoryUsage).willReturn(responseEntityCpuLoad);
        assertEquals(micoServiceStatus, micoStatusService.getServiceStatus(micoService));
    }

    @Test
    public void getServiceInterfaceStatus() {
        MicoServiceInterfaceDTO micoServiceInterface = new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME);
        try {
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, micoServiceInterface.getName())).willReturn(kubernetesService);
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        List<MicoServiceInterfaceDTO> micoServiceInterfaceDTOList = new LinkedList<>();
        micoServiceInterfaceDTOList.add(micoServiceInterface);
        assertEquals(micoServiceInterfaceDTOList, micoStatusService.getServiceInterfaceStatus(micoService));
    }
}
