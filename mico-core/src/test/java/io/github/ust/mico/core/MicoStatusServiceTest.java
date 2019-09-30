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

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO;
import io.github.ust.mico.core.dto.response.internal.PrometheusResponseDTO;
import io.github.ust.mico.core.dto.response.status.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoMessage.Type;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class MicoStatusServiceTest {

    private static final String testNamespace = "test-namespace";

    private static final String POD_PHASE_RUNNING = "Running";
    private static final String POD_PHASE_PENDING = "Pending";
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
    @MockBean
    private MicoServiceInterfaceRepository serviceInterfaceRepository;
    @Autowired
    private MicoStatusService micoStatusService;

    private MicoApplication micoApplication;
    private MicoApplication otherMicoApplication;
    private MicoService micoService;
    private MicoServiceDeploymentInfo micoServiceDeploymentInfo;
    private MicoServiceInterface micoServiceInterface;
    private Service kubernetesService;
    private PodList podList;
    private PodList podListWithOnePod;
    private String nodeName1 = "testNode";
    private String nodeName2 = "testNode2";
    private String hostIp = "192.168.0.0";
    // Metrics for pod 1
    private String podName1 = "pod1";
    private int memoryUsagePod1 = 5;
    private int cpuLoadPod1 = 5;
    private String startTimePod1 = new Date().toString();
    private int restartsPod1 = 0;

    // Metrics for pod 2
    private String podName2 = "pod2";
    private int memoryUsagePod2 = 5;
    private int cpuLoadPod2 = 5;
    private String startTimePod2 = new Date().toString();
    private int restartsPod2 = 0;

    // Metrics for pod 3
    private String podName3 = "pod3";
    private int memoryUsagePod3 = 5;
    private int cpuLoadPod3 = 5;
    private String startTimePod3 = new Date().toString();
    private int restartsPod3 = 0;

    // Metrics for pod 4
    private String podName4 = "pod4";
    private int memoryUsagePod4 = 5;
    private int cpuLoadPod4 = 5;
    private String startTimePod4 = new Date().toString();
    private int restartsPod4 = 0;

    // KafkaFaasConnector
    private MicoService kfConnectorService;
    private MicoServiceDeploymentInfo kfConnectorDeploymentInfo;
    private PodList kfConnectorPodList;
    private String podName5 = "pod5";
    private String startTimePod5 = new Date().toString();
    private int restartsPod5 = 0;
    private int memoryUsagePod5 = 5;
    private int cpuLoadPod5 = 5;

    @Before
    public void setupMicoApplication() {
        micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        otherMicoApplication = new MicoApplication()
            .setName(NAME)
            .setShortName(SHORT_NAME_OTHER)
            .setVersion(VERSION);

        micoService = new MicoService()
            .setName(NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        micoServiceInterface = new MicoServiceInterface()
            .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
            .setPorts(CollectionUtils.listOf(new MicoServicePort()
                .setPort(8080)
                .setTargetPort(8080)
                .setType(MicoPortType.TCP)));
        micoService.setServiceInterfaces(CollectionUtils.listOf(micoServiceInterface));
        micoApplication.getServices().add(micoService);
        micoServiceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setInstanceId(INSTANCE_ID)
            .setReplicas(4);
        micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);

        otherMicoApplication.getServices().add(micoService);
        otherMicoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);

        kubernetesService = new ServiceBuilder()
            .withNewMetadata().withName(SERVICE_INTERFACE_NAME).withNamespace(testNamespace).endMetadata()
            .withNewSpec().endSpec()
            .withNewStatus()
            .withNewLoadBalancer()
            .addNewIngress().withIp("192.168.2.112").endIngress()
            .addNewIngress().withIp("192.168.2.113").endIngress()
            .endLoadBalancer()
            .endStatus()
            .build();

        podList = new PodListBuilder()
            .addNewItem()
            .withNewMetadata().withName(podName1).endMetadata()
            .withNewSpec().withNodeName(nodeName1).endSpec()
            .withNewStatus().withStartTime(startTimePod1)
            .addNewContainerStatus().withRestartCount(restartsPod1).endContainerStatus()
            .withPhase(POD_PHASE_RUNNING)
            .withHostIP(hostIp)
            .endStatus()
            .endItem()
            .addNewItem()
            .withNewMetadata().withName(podName2).endMetadata()
            .withNewSpec().withNodeName(nodeName1).endSpec()
            .withNewStatus().withStartTime(startTimePod2)
            .addNewContainerStatus().withRestartCount(restartsPod2).endContainerStatus()
            .withPhase(POD_PHASE_RUNNING)
            .withHostIP(hostIp)
            .endStatus()
            .endItem()
            .addNewItem()
            .withNewMetadata().withName(podName3).endMetadata()
            .withNewSpec().withNodeName(nodeName2).endSpec()
            .withNewStatus().withStartTime(startTimePod3)
            .addNewContainerStatus().withRestartCount(restartsPod3).endContainerStatus()
            .withPhase(POD_PHASE_RUNNING)
            .withHostIP(hostIp)
            .endStatus()
            .endItem()
            .addNewItem()
            .withNewMetadata().withName(podName4).endMetadata()
            .withNewSpec().withNodeName(nodeName2).endSpec()
            .withNewStatus().withStartTime(startTimePod4)
            .addNewContainerStatus().withRestartCount(restartsPod4).endContainerStatus()
            .withPhase(POD_PHASE_PENDING)
            .withHostIP(hostIp)
            .endStatus()
            .endItem()
            .build();

        podListWithOnePod = new PodListBuilder()
            .addNewItem()
            .withNewMetadata().withName(podName1).endMetadata()
            .withNewSpec().withNodeName(nodeName1).endSpec()
            .withNewStatus().withStartTime(startTimePod1).addNewContainerStatus().withRestartCount(restartsPod1).endContainerStatus().withPhase(POD_PHASE_RUNNING).withHostIP(hostIp).endStatus()
            .endItem()
            .build();

        String kfConnectorName = "kafka-faas-connector";
        kfConnectorService = new MicoService()
            .setName(kfConnectorName)
            .setShortName(kfConnectorName)
            .setVersion(VERSION);
        kfConnectorDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(kfConnectorService)
            .setInstanceId(INSTANCE_ID_1)
            .setReplicas(1);
        kfConnectorPodList = new PodListBuilder()
            .addNewItem()
            .withNewMetadata().withName(podName5).endMetadata()
            .withNewSpec().withNodeName(nodeName1).endSpec()
            .withNewStatus().withStartTime(startTimePod5)
            .addNewContainerStatus().withRestartCount(restartsPod5).endContainerStatus()
            .withPhase(POD_PHASE_RUNNING)
            .withHostIP(hostIp)
            .endStatus()
            .endItem()
            .build();

    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getApplicationStatus() throws Exception {

        // With KafkaFaasConnector
        micoApplication.getKafkaFaasConnectorDeploymentInfos().add(kfConnectorDeploymentInfo);

        MicoApplicationStatusResponseDTO micoApplicationStatus = new MicoApplicationStatusResponseDTO()
            .setTotalNumberOfRequestedReplicas(5)
            .setTotalNumberOfAvailableReplicas(5)
            .setTotalNumberOfPods(5)
            .setTotalNumberOfMicoServices(1)
            .setTotalNumberOfKafkaFaasConnectors(1)
            .setServiceStatuses(CollectionUtils.listOf(
                new MicoServiceStatusResponseDTO()
                    .setName(NAME)
                    .setShortName(SHORT_NAME)
                    .setVersion(VERSION)
                    .setAvailableReplicas(1)
                    .setRequestedReplicas(1)
                    .setApplicationsUsingThisService(CollectionUtils.listOf(new MicoApplicationResponseDTO(otherMicoApplication)))
                    .setNodeMetrics(CollectionUtils.listOf(
                        new KubernetesNodeMetricsResponseDTO()
                            .setNodeName(nodeName1)
                            .setAverageCpuLoad(5)
                            .setAverageMemoryUsage(5),
                        new KubernetesNodeMetricsResponseDTO()
                            .setNodeName(nodeName2)
                            .setAverageCpuLoad(5)
                            .setAverageMemoryUsage(5)
                    ))
                    // Add four pods (on two different nodes)
                    .setPodsInformation(Arrays.asList(
                        new KubernetesPodInformationResponseDTO()
                            .setPodName(podName1)
                            .setHostIp(hostIp)
                            .setNodeName(nodeName1)
                            .setPhase(POD_PHASE_RUNNING)
                            .setStartTime(startTimePod1)
                            .setRestarts(restartsPod1)
                            .setMetrics(new KubernetesPodMetricsResponseDTO()
                                .setMemoryUsage(memoryUsagePod1)
                                .setCpuLoad(cpuLoadPod1)),
                        new KubernetesPodInformationResponseDTO()
                            .setPodName(podName2)
                            .setHostIp(hostIp)
                            .setNodeName(nodeName1)
                            .setPhase(POD_PHASE_RUNNING)
                            .setStartTime(startTimePod2)
                            .setRestarts(restartsPod2)
                            .setMetrics(new KubernetesPodMetricsResponseDTO()
                                .setMemoryUsage(memoryUsagePod2)
                                .setCpuLoad(cpuLoadPod2)),
                        new KubernetesPodInformationResponseDTO()
                            .setPodName(podName3)
                            .setHostIp(hostIp)
                            .setNodeName(nodeName2)
                            .setPhase(POD_PHASE_RUNNING)
                            .setStartTime(startTimePod3)
                            .setRestarts(restartsPod3)
                            .setMetrics(new KubernetesPodMetricsResponseDTO()
                                .setMemoryUsage(memoryUsagePod3)
                                .setCpuLoad(cpuLoadPod3)),
                        new KubernetesPodInformationResponseDTO()
                            .setPodName(podName4)
                            .setHostIp(hostIp)
                            .setNodeName(nodeName2)
                            .setPhase(POD_PHASE_PENDING)
                            .setStartTime(startTimePod4)
                            .setRestarts(restartsPod4)))
                    .setErrorMessages(CollectionUtils.listOf())
                    .setInterfacesInformation(CollectionUtils.listOf(
                        new MicoServiceInterfaceStatusResponseDTO()
                            .setName(SERVICE_INTERFACE_NAME)
                            .setExternalIpIsAvailable(true)
                            .setExternalIp("192.168.2.112")
                            .setPort(8080))),
                new MicoServiceStatusResponseDTO()
                    .setName(kfConnectorService.getName())
                    .setShortName(kfConnectorService.getShortName())
                    .setVersion(kfConnectorService.getVersion())
                    .setInstanceId(kfConnectorDeploymentInfo.getInstanceId())
                    .setAvailableReplicas(1)
                    .setRequestedReplicas(1)
                    .setApplicationsUsingThisService(new ArrayList<>())
                    .setNodeMetrics(CollectionUtils.listOf(
                        new KubernetesNodeMetricsResponseDTO()
                            .setNodeName(nodeName1)
                            .setAverageCpuLoad(5)
                            .setAverageMemoryUsage(5)
                    ))
                    // Add four pods (on two different nodes)
                    .setPodsInformation(Collections.singletonList(
                        new KubernetesPodInformationResponseDTO()
                            .setPodName(podName5)
                            .setHostIp(hostIp)
                            .setNodeName(nodeName1)
                            .setPhase(POD_PHASE_RUNNING)
                            .setStartTime(startTimePod5)
                            .setRestarts(restartsPod5)
                            .setMetrics(new KubernetesPodMetricsResponseDTO()
                                .setMemoryUsage(memoryUsagePod5)
                                .setCpuLoad(cpuLoadPod5))))
                    .setErrorMessages(CollectionUtils.listOf())
                    .setInterfacesInformation(new ArrayList<>())));
        given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoServiceInstance(eq(micoServiceDeploymentInfo))).willReturn(podList.getItems());
        given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoServiceInstance(eq(kfConnectorDeploymentInfo))).willReturn(kfConnectorPodList.getItems());
        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata().withName("deployment1").endMetadata()
            .withNewSpec().withReplicas(podList.getItems().size()).endSpec()
            .withNewStatus().withAvailableReplicas(podList.getItems().size()).endStatus()
            .build();
        Deployment deploymentKfConnector = new DeploymentBuilder()
            .withNewMetadata().withName("kf-connector-deployment").endMetadata()
            .withNewSpec().withReplicas(kfConnectorPodList.getItems().size()).endSpec()
            .withNewStatus().withAvailableReplicas(kfConnectorPodList.getItems().size()).endStatus()
            .build();
        given(micoKubernetesClient.getDeploymentOfMicoServiceInstance(eq(micoServiceDeploymentInfo))).willReturn(Optional.of(deployment));
        given(micoKubernetesClient.getDeploymentOfMicoServiceInstance(eq(kfConnectorDeploymentInfo))).willReturn(Optional.of(deploymentKfConnector));
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(eq(micoServiceDeploymentInfo), anyString())).willReturn(Optional.ofNullable(kubernetesService));
        given(micoKubernetesClient.isApplicationDeployed(otherMicoApplication)).willReturn(true);
        given(micoKubernetesClient.getApplicationDeploymentStatus(otherMicoApplication)).willReturn(new MicoApplicationDeploymentStatus(MicoApplicationDeploymentStatus.Value.DEPLOYED));
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedServiceInstance(INSTANCE_ID)).willReturn(CollectionUtils.listOf(otherMicoApplication, micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));

        given(micoKubernetesClient.getPublicIpOfKubernetesService(kubernetesService.getMetadata().getName(),
            kubernetesService.getMetadata().getNamespace())).willReturn(Optional.of("192.168.2.112"));
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(kubernetesService.getMetadata().getName(),
            kubernetesService.getMetadata().getNamespace())).willReturn(Collections.singletonList(8080));


        ResponseEntity responseEntityMemoryUsagePod1 = getPrometheusResponseEntity(memoryUsagePod1);
        ResponseEntity responseEntityCpuLoadPod1 = getPrometheusResponseEntity(cpuLoadPod1);
        ResponseEntity responseEntityMemoryUsagePod2 = getPrometheusResponseEntity(memoryUsagePod2);
        ResponseEntity responseEntityCpuLoadPod2 = getPrometheusResponseEntity(cpuLoadPod2);
        ResponseEntity responseEntityMemoryUsagePod3 = getPrometheusResponseEntity(memoryUsagePod3);
        ResponseEntity responseEntityCpuLoadPod3 = getPrometheusResponseEntity(cpuLoadPod3);
        ResponseEntity responseEntityMemoryUsagePod4 = getPrometheusResponseEntity(memoryUsagePod4);
        ResponseEntity responseEntityCpuLoadPod4 = getPrometheusResponseEntity(cpuLoadPod4);
        ResponseEntity responseEntityMemoryUsagePod5 = getPrometheusResponseEntity(memoryUsagePod5);
        ResponseEntity responseEntityCpuLoadPod5 = getPrometheusResponseEntity(cpuLoadPod5);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponseDTO.class))).
            willReturn(responseEntityMemoryUsagePod1)
            .willReturn(responseEntityCpuLoadPod1)
            .willReturn(responseEntityMemoryUsagePod2)
            .willReturn(responseEntityCpuLoadPod2)
            .willReturn(responseEntityMemoryUsagePod3)
            .willReturn(responseEntityCpuLoadPod3)
            .willReturn(responseEntityMemoryUsagePod4)
            .willReturn(responseEntityCpuLoadPod4)
            .willReturn(responseEntityMemoryUsagePod5)
            .willReturn(responseEntityCpuLoadPod5);
        assertEquals(micoApplicationStatus, micoStatusService.getApplicationStatus(micoApplication));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void getApplicationStatusWithMissingKubernetesService() {
        MicoApplicationStatusResponseDTO micoApplicationStatus = new MicoApplicationStatusResponseDTO();
        micoApplicationStatus
            .setTotalNumberOfRequestedReplicas(1)
            .setTotalNumberOfAvailableReplicas(1)
            .setTotalNumberOfPods(1)
            .setTotalNumberOfMicoServices(1)
            .setTotalNumberOfKafkaFaasConnectors(0)
            .setServiceStatuses(CollectionUtils.listOf(
                new MicoServiceStatusResponseDTO()
                    .setName(NAME)
                    .setShortName(SHORT_NAME)
                    .setVersion(VERSION)
                    .setInstanceId(INSTANCE_ID)
                    .setAvailableReplicas(1)
                    .setRequestedReplicas(1)
                    .setApplicationsUsingThisService(CollectionUtils.listOf(new MicoApplicationResponseDTO(otherMicoApplication,
                        new MicoApplicationDeploymentStatus(MicoApplicationDeploymentStatus.Value.DEPLOYED))))
                    .setNodeMetrics(CollectionUtils.listOf(
                        new KubernetesNodeMetricsResponseDTO()
                            .setNodeName(nodeName1)
                            .setAverageCpuLoad(5)
                            .setAverageMemoryUsage(5)
                    ))
                    // Add four pods (on two different nodes)
                    .setPodsInformation(CollectionUtils.listOf(
                        new KubernetesPodInformationResponseDTO()
                            .setPodName(podName1)
                            .setHostIp(hostIp)
                            .setNodeName(nodeName1)
                            .setPhase(POD_PHASE_RUNNING)
                            .setStartTime(startTimePod1)
                            .setRestarts(restartsPod1)
                            .setMetrics(new KubernetesPodMetricsResponseDTO()
                                .setMemoryUsage(memoryUsagePod1)
                                .setCpuLoad(cpuLoadPod1))))
                    .setErrorMessages(CollectionUtils.listOf(
                        new MicoMessageResponseDTO().setContent("No deployed service interface 'service-interface-name' of MicoService 'short-name' '1.0.0' was found!").setType(Type.ERROR)))
                    .setInterfacesInformation(CollectionUtils.listOf(
                        new MicoServiceInterfaceStatusResponseDTO()
                            .setName(SERVICE_INTERFACE_NAME))))); // No IPs
        given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class))).willReturn(podListWithOnePod.getItems());
        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata().withName("deployment1").endMetadata()
            .withNewSpec().withReplicas(podListWithOnePod.getItems().size()).endSpec()
            .withNewStatus().withAvailableReplicas(podListWithOnePod.getItems().size()).endStatus()
            .build();
        given(micoKubernetesClient.getDeploymentOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class))).willReturn(Optional.of(deployment));
        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class), anyString())).willReturn(Optional.empty());
        given(micoKubernetesClient.isApplicationDeployed(otherMicoApplication)).willReturn(true);
        given(micoKubernetesClient.getApplicationDeploymentStatus(otherMicoApplication)).willReturn(new MicoApplicationDeploymentStatus(MicoApplicationDeploymentStatus.Value.DEPLOYED));
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedServiceInstance(INSTANCE_ID)).willReturn(CollectionUtils.listOf(otherMicoApplication, micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        ResponseEntity responseEntityMemoryUsagePod1 = getPrometheusResponseEntity(memoryUsagePod1);
        ResponseEntity responseEntityCpuLoadPod1 = getPrometheusResponseEntity(cpuLoadPod1);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponseDTO.class))).
            willReturn(responseEntityMemoryUsagePod1)
            .willReturn(responseEntityCpuLoadPod1);
        assertEquals(micoApplicationStatus, micoStatusService.getApplicationStatus(micoApplication));
    }

    @Test
    public void getApplicationStatusWithMissingDeployment() {
        MicoApplicationStatusResponseDTO micoApplicationStatus = new MicoApplicationStatusResponseDTO();
        micoApplicationStatus
            .setTotalNumberOfRequestedReplicas(0)
            .setTotalNumberOfAvailableReplicas(0)
            .setTotalNumberOfPods(0)
            .setTotalNumberOfMicoServices(1)
            .setTotalNumberOfKafkaFaasConnectors(0)
            .setServiceStatuses(CollectionUtils.listOf(
                new MicoServiceStatusResponseDTO()
                    .setShortName(micoApplication.getServices().get(0).getShortName())
                    .setVersion(micoApplication.getServices().get(0).getVersion())
                    .setName(micoApplication.getServices().get(0).getName())
                    .setInstanceId(micoApplication.getServiceDeploymentInfos().get(0).getInstanceId())
                    .setErrorMessages(CollectionUtils
                        .listOf(new MicoMessageResponseDTO().setContent("No deployment of MicoService '" + micoService.getShortName()
                            + "' '" + micoService.getVersion() + "' with instance ID '" + micoServiceDeploymentInfo.getInstanceId()
                            + "' is available.").setType(Type.ERROR)))));
        given(micoKubernetesClient.getDeploymentOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class))).willReturn(Optional.empty());
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class), anyString())).willReturn(Optional.ofNullable(kubernetesService));
        given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class))).willReturn(podListWithOnePod.getItems());
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        assertEquals(micoApplicationStatus, micoStatusService.getApplicationStatus(micoApplication));
    }


    @SuppressWarnings("rawtypes")
    private ResponseEntity getPrometheusResponseEntity(int value) {
        PrometheusResponseDTO prometheusResponse = new PrometheusResponseDTO();
        prometheusResponse.setSuccess(true);
        prometheusResponse.setValue(value);
        ResponseEntity responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getStatusCode()).willReturn(HttpStatus.OK);
        given(responseEntity.getBody()).willReturn(prometheusResponse);
        return responseEntity;
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getServiceStatus() throws Exception {
        MicoServiceStatusResponseDTO micoServiceStatus = new MicoServiceStatusResponseDTO();
        micoServiceStatus
            .setName(NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setAvailableReplicas(1)
            .setRequestedReplicas(1)
            .setApplicationsUsingThisService(CollectionUtils.listOf(new MicoApplicationResponseDTO(otherMicoApplication)))
            .setNodeMetrics(CollectionUtils.listOf(
                new KubernetesNodeMetricsResponseDTO()
                    .setNodeName(nodeName1)
                    .setAverageCpuLoad(5)
                    .setAverageMemoryUsage(5),
                new KubernetesNodeMetricsResponseDTO()
                    .setNodeName(nodeName2)
                    .setAverageCpuLoad(5)
                    .setAverageMemoryUsage(5)
            ))
            // Add four pods (on two different nodes)
            .setPodsInformation(Arrays.asList(
                new KubernetesPodInformationResponseDTO()
                    .setPodName(podName1)
                    .setHostIp(hostIp)
                    .setNodeName(nodeName1)
                    .setPhase(POD_PHASE_RUNNING)
                    .setStartTime(startTimePod1)
                    .setRestarts(restartsPod1)
                    .setMetrics(new KubernetesPodMetricsResponseDTO()
                        .setMemoryUsage(memoryUsagePod1)
                        .setCpuLoad(cpuLoadPod1)),
                new KubernetesPodInformationResponseDTO()
                    .setPodName(podName2)
                    .setHostIp(hostIp)
                    .setNodeName(nodeName1)
                    .setPhase(POD_PHASE_RUNNING)
                    .setStartTime(startTimePod2)
                    .setRestarts(restartsPod2)
                    .setMetrics(new KubernetesPodMetricsResponseDTO()
                        .setMemoryUsage(memoryUsagePod2)
                        .setCpuLoad(cpuLoadPod2)),
                new KubernetesPodInformationResponseDTO()
                    .setPodName(podName3)
                    .setHostIp(hostIp)
                    .setNodeName(nodeName2)
                    .setPhase(POD_PHASE_RUNNING)
                    .setStartTime(startTimePod3)
                    .setRestarts(restartsPod3)
                    .setMetrics(new KubernetesPodMetricsResponseDTO()
                        .setMemoryUsage(memoryUsagePod3)
                        .setCpuLoad(cpuLoadPod3)),
                new KubernetesPodInformationResponseDTO()
                    .setPodName(podName4)
                    .setHostIp(hostIp)
                    .setNodeName(nodeName2)
                    .setPhase(POD_PHASE_PENDING)
                    .setStartTime(startTimePod4)
                    .setRestarts(restartsPod4)))
            .setErrorMessages(CollectionUtils.listOf())
            .setInterfacesInformation(CollectionUtils.listOf(new MicoServiceInterfaceStatusResponseDTO()
                .setName(SERVICE_INTERFACE_NAME)
                .setExternalIpIsAvailable(true)
                .setExternalIp("192.168.2.112")
                .setPort(8080)));

        given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class))).willReturn(podList.getItems());
        Deployment deployment = new DeploymentBuilder()
            .withNewMetadata().withName("deployment1").endMetadata()
            .withNewSpec().withReplicas(podList.getItems().size()).endSpec()
            .withNewStatus().withAvailableReplicas(podList.getItems().size()).endStatus()
            .build();
        given(micoKubernetesClient.getDeploymentOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class))).willReturn(Optional.of(deployment));
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(any(MicoServiceDeploymentInfo.class), anyString())).willReturn(Optional.ofNullable(kubernetesService));
        given(micoKubernetesClient.isApplicationDeployed(otherMicoApplication)).willReturn(true);
        given(micoKubernetesClient.getApplicationDeploymentStatus(otherMicoApplication)).willReturn(new MicoApplicationDeploymentStatus(MicoApplicationDeploymentStatus.Value.DEPLOYED));
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedServiceInstance(INSTANCE_ID)).willReturn(CollectionUtils.listOf(otherMicoApplication));

        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));

        given(micoKubernetesClient.getPublicIpOfKubernetesService(kubernetesService.getMetadata().getName(),
            kubernetesService.getMetadata().getNamespace())).willReturn(Optional.of("192.168.2.112"));
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(kubernetesService.getMetadata().getName(),
            kubernetesService.getMetadata().getNamespace())).willReturn(Collections.singletonList(8080));

        ResponseEntity responseEntityMemoryUsagePod1 = getPrometheusResponseEntity(memoryUsagePod1);
        ResponseEntity responseEntityCpuLoadPod1 = getPrometheusResponseEntity(cpuLoadPod1);
        ResponseEntity responseEntityMemoryUsagePod2 = getPrometheusResponseEntity(memoryUsagePod2);
        ResponseEntity responseEntityCpuLoadPod2 = getPrometheusResponseEntity(cpuLoadPod2);
        ResponseEntity responseEntityMemoryUsagePod3 = getPrometheusResponseEntity(memoryUsagePod3);
        ResponseEntity responseEntityCpuLoadPod3 = getPrometheusResponseEntity(cpuLoadPod3);
        ResponseEntity responseEntityMemoryUsagePod4 = getPrometheusResponseEntity(memoryUsagePod4);
        ResponseEntity responseEntityCpuLoadPod4 = getPrometheusResponseEntity(cpuLoadPod4);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponseDTO.class))).
            willReturn(responseEntityMemoryUsagePod1)
            .willReturn(responseEntityCpuLoadPod1)
            .willReturn(responseEntityMemoryUsagePod2)
            .willReturn(responseEntityCpuLoadPod2)
            .willReturn(responseEntityMemoryUsagePod3)
            .willReturn(responseEntityCpuLoadPod3)
            .willReturn(responseEntityMemoryUsagePod4)
            .willReturn(responseEntityCpuLoadPod4);
        assertEquals(micoServiceStatus, micoStatusService.getServiceInstanceStatus(micoServiceDeploymentInfo));
    }

    @Test
    public void getServiceInterfaceStatus() throws Exception {

        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(micoServiceDeploymentInfo, SERVICE_INTERFACE_NAME))
            .willReturn(Optional.ofNullable(kubernetesService));

        given(micoKubernetesClient.getPublicIpOfKubernetesService(kubernetesService.getMetadata().getName(),
            kubernetesService.getMetadata().getNamespace())).willReturn(Optional.of("192.168.2.112"));
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(kubernetesService.getMetadata().getName(),
            kubernetesService.getMetadata().getNamespace())).willReturn(Collections.singletonList(8080));

        MicoServiceInterfaceStatusResponseDTO expectedServiceInterface = new MicoServiceInterfaceStatusResponseDTO()
            .setName(SERVICE_INTERFACE_NAME)
            .setExternalIpIsAvailable(true)
            .setExternalIp("192.168.2.112")
            .setPort(8080);
        List<MicoServiceInterfaceStatusResponseDTO> expectedInterfaceStatusDTO = new ArrayList<>();
        expectedInterfaceStatusDTO.add(expectedServiceInterface);
        List<MicoMessageResponseDTO> errorMessages = new ArrayList<>();

        List<MicoServiceInterfaceStatusResponseDTO> actualInterfaceStatusDTO = micoStatusService.getServiceInterfaceStatus(micoServiceDeploymentInfo, errorMessages);

        assertTrue("Expected there are no errors", errorMessages.isEmpty());
        assertEquals(expectedInterfaceStatusDTO, actualInterfaceStatusDTO);
    }

    @Test
    public void getServiceInterfaceStatusWithErrors() {

        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.empty());
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(micoServiceDeploymentInfo, SERVICE_INTERFACE_NAME))
            .willReturn(Optional.empty());

        MicoServiceInterfaceStatusResponseDTO expectedServiceInterface = new MicoServiceInterfaceStatusResponseDTO()
            .setName(SERVICE_INTERFACE_NAME); // Expect that there are no IPs
        List<MicoServiceInterfaceStatusResponseDTO> expectedInterfaceStatusDTO = new ArrayList<>();
        expectedInterfaceStatusDTO.add(expectedServiceInterface);

        List<MicoMessageResponseDTO> errorMessages = new ArrayList<>();

        List<MicoServiceInterfaceStatusResponseDTO> actualInterfaceStatusDTO = micoStatusService.getServiceInterfaceStatus(micoServiceDeploymentInfo, errorMessages);

        assertEquals("Expected one error", 1, errorMessages.size());
        assertEquals(expectedInterfaceStatusDTO, actualInterfaceStatusDTO);
    }
}
