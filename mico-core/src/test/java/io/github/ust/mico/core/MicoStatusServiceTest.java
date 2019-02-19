package io.github.ust.mico.core;

import java.util.Collections;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInfoDTO;
import io.github.ust.mico.core.dto.MicoApplicationDeploymentInformationDTO;
import io.github.ust.mico.core.dto.MicoServiceDeploymentInformationDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.PrometheusResponse;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.server.rest.RESTRequestGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static io.github.ust.mico.core.TestConstants.DESCRIPTION_1;
import static io.github.ust.mico.core.TestConstants.SERVICE_INTERFACE_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_1;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoStatusServiceTest {

    @MockBean
    MicoKubernetesClient micoKubernetesClient;

    @MockBean
    PrometheusConfig prometheusConfig;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    MicoApplicationRepository applicationRepository;

    @Autowired
    MicoStatusService micoStatusService;

    private MicoApplication micoApplication;
    private MicoService micoService;
    private Optional<Deployment> deployment;
    private Optional<Service> kubernetesService;
    private PodList podList;

    private String testNamespace = "TestNamespace";
    private String nodeName = "testNode";
    private String podPhase = "Running";
    private String hostIp = "192.168.0.0";
    private String deploymentName = "deployment1";
    private String serviceName = "service1";
    private String podName1 = "pod1";

    private int memoryUsage = 70;
    private int cpuLoad = 30;
    private boolean podAvailable = true;


    @Before
    public void setupMicoApplication() {
        micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setServices(CollectionUtils.listOf(
                new MicoService()
                    .setShortName(SHORT_NAME)
                    .setVersion(VERSION)
                    .setServiceInterfaces(CollectionUtils.listOf(
                        new MicoServiceInterface()
                            .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                    ))
            ));

        int availableReplicas = 0;
        int replicas = 1;

        micoService = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1);

        deployment = Optional.of(new DeploymentBuilder()
            .withNewMetadata().withName(deploymentName).endMetadata()
            .withNewSpec().withReplicas(replicas).endSpec().withNewStatus().withAvailableReplicas(availableReplicas).endStatus()
            .build());

        kubernetesService = Optional.of(new ServiceBuilder()
            .withNewMetadata().withName(serviceName).endMetadata()
            .build());

        podList = new PodListBuilder()
            .addNewItem()
                .withNewMetadata().withName(podName1).endMetadata()
                .withNewSpec().withNodeName(nodeName).endSpec()
                .withNewStatus().withPhase(podPhase).withHostIP(hostIp).endStatus()
            .endItem()
            .build();
    }

    @Test
    public void getApplicationStatus() {
        MicoApplicationDeploymentInformationDTO micoApplicationDeploymentInformation = new MicoApplicationDeploymentInformationDTO();
        micoApplicationDeploymentInformation.setServiceDeploymentInformation(Collections.singletonList(new MicoServiceDeploymentInformationDTO()
            .setAvailableReplicas(0)
            .setRequestedReplicas(1)
            .setPodInfo(Collections.singletonList(new KubernetesPodInfoDTO()
                .setPodName(podName1)
                .setHostIp(hostIp)
                .setNodeName(nodeName)
                .setPhase(podPhase)
                .setMetrics(new KuberenetesPodMetricsDTO()
                    .setMemoryUsage(memoryUsage)
                    .setCpuLoad(cpuLoad)
                    .setAvailable(podAvailable))))
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(serviceName)))));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        ResponseEntity responseEntity = getPrometheusResponseEntity(memoryUsage);
        ResponseEntity responseEntity2 = getPrometheusResponseEntity(cpuLoad);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntity).willReturn(responseEntity2);
        assertEquals(micoApplicationDeploymentInformation, micoStatusService.getApplicationStatus(micoApplication));

    }

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
    public void getServiceStatus() {

    }

    @Test
    public void getServiceInterfaceStatus() {

    }

}
