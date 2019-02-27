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

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.BasicMicoApplicationDTO;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInformationDTO;
import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.MicoServiceStatusDTO;
import io.github.ust.mico.core.dto.PrometheusResponse;
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

import static io.github.ust.mico.core.TestConstants.APPLICATION_NAME;
import static io.github.ust.mico.core.TestConstants.APPLICATION_NAME_OTHER;
import static io.github.ust.mico.core.TestConstants.SERVICE_INTERFACE_NAME;
import static io.github.ust.mico.core.TestConstants.SERVICE_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_OTHER;
import static io.github.ust.mico.core.TestConstants.VERSION;
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
    private MicoApplication otherMicoApplication;
    private MicoService micoService;
    private Optional<Deployment> deployment;
    private Optional<Service> kubernetesService;
    private PodList podList;

    private String nodeName = "testNode";
    private String nodeName2 = "testNode2";
    private String podPhase = "Running";
    private String hostIp = "192.168.0.0";
    private String deploymentName = "deployment1";

    // Metrics for pod 1
    private String podName1 = "pod1";
    private int memoryUsagePod1 = 70;
    private int cpuLoadPod1 = 30;
    private String startTimePod1 = new Date().toString();
    private int restartsPod1 = 0;
    private boolean podAvailablePod1 = true;

    // Metrics for pod 2
    private String podName2 = "pod2";
    private int memoryUsagePod2 = 50;
    private int cpuLoadPod2 = 10;
    private String startTimePod2 = new Date().toString();
    private int restartsPod2 = 0;
    private boolean podAvailablePod2 = true;


    @Before
    public void setupMicoApplication() {
        micoApplication = new MicoApplication()
            .setName(APPLICATION_NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        otherMicoApplication = new MicoApplication()
            .setName(APPLICATION_NAME_OTHER)
            .setShortName(SHORT_NAME_OTHER)
            .setVersion(VERSION);

        micoService = new MicoService()
            .setName(SERVICE_NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setServiceInterfaces(CollectionUtils.listOf(
                new MicoServiceInterface()
                    .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
            ));

        micoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(micoApplication)
            .setService(micoService));

        otherMicoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(otherMicoApplication)
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
            .withNewStatus().withStartTime(startTimePod1).addNewContainerStatus().withNewRestartCount(restartsPod1).endContainerStatus().withPhase(podPhase).withHostIP(hostIp).endStatus()
            .endItem()
            .addNewItem()
            .withNewMetadata().withName(podName2).endMetadata()
            .withNewSpec().withNodeName(nodeName2).endSpec()
            .withNewStatus().withStartTime(startTimePod2).addNewContainerStatus().withNewRestartCount(restartsPod2).endContainerStatus().withPhase(podPhase).withHostIP(hostIp).endStatus()
            .endItem()
            .build();
    }

    @Test
    public void getApplicationStatus() {
        MicoApplicationStatusDTO micoApplicationStatus = new MicoApplicationStatusDTO();
        micoApplicationStatus
            .setTotalNumberOfRequestedReplicas(1)
            .setTotalNumberOfAvailableReplicas(0)
            .setTotalNumberOfPods(2)
            .setTotalNumberOfMicoServices(1)
            .setServiceStatuses(CollectionUtils.listOf(new MicoServiceStatusDTO()
                .setName(SERVICE_NAME)
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setAvailableReplicas(0)
                .setRequestedReplicas(1)
                .setUsingApplications(CollectionUtils.listOf(new BasicMicoApplicationDTO(otherMicoApplication.getShortName(), otherMicoApplication.getVersion(), otherMicoApplication.getName())))
                .setAverageMemoryUsagePerNode(ImmutableMap.of(nodeName, memoryUsagePod1, nodeName2, memoryUsagePod2))
                .setAverageCpuLoadPerNode(ImmutableMap.of(nodeName, cpuLoadPod1, nodeName2, cpuLoadPod2))
                // Add two pods
                .setPodsInformation(Arrays.asList(new KubernetesPodInformationDTO()
                        .setPodName(podName1)
                        .setHostIp(hostIp)
                        .setNodeName(nodeName)
                        .setPhase(podPhase)
                        .setStartTime(startTimePod1)
                        .setRestarts(restartsPod1)
                        .setMetrics(new KuberenetesPodMetricsDTO()
                            .setMemoryUsage(memoryUsagePod1)
                            .setCpuLoad(cpuLoadPod1)
                            .setAvailable(podAvailablePod1)),
                    new KubernetesPodInformationDTO()
                        .setPodName(podName2)
                        .setHostIp(hostIp)
                        .setNodeName(nodeName2)
                        .setPhase(podPhase)
                        .setStartTime(startTimePod2)
                        .setRestarts(restartsPod2)
                        .setMetrics(new KuberenetesPodMetricsDTO()
                            .setMemoryUsage(memoryUsagePod2)
                            .setCpuLoad(cpuLoadPod2)
                            .setAvailable(podAvailablePod2))))
                .setInterfacesInformation(CollectionUtils.listOf(new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME)))))
        ;
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedService(any(), any())).willReturn(CollectionUtils.listOf(otherMicoApplication, micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        ResponseEntity responseEntityMemoryUsagePod1 = getPrometheusResponseEntity(memoryUsagePod1);
        ResponseEntity responseEntityCpuLoadPod1 = getPrometheusResponseEntity(cpuLoadPod1);
        ResponseEntity responseEntityMemoryUsagePod2 = getPrometheusResponseEntity(memoryUsagePod2);
        ResponseEntity responseEntityCpuLoadPod2 = getPrometheusResponseEntity(cpuLoadPod2);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntityMemoryUsagePod1).willReturn(responseEntityCpuLoadPod1).willReturn(responseEntityMemoryUsagePod2).willReturn(responseEntityCpuLoadPod2);
        assertEquals(micoApplicationStatus, micoStatusService.getApplicationStatus(micoApplication));
    }

    @Test
    public void getApplicationStatusWithMissingDeployment() {
        MicoApplicationStatusDTO micoApplicationStatus = new MicoApplicationStatusDTO();
        micoApplicationStatus
            .setTotalNumberOfRequestedReplicas(0)
            .setTotalNumberOfAvailableReplicas(0)
            .setTotalNumberOfPods(0)
            .setTotalNumberOfMicoServices(1)
            .setServiceStatuses(CollectionUtils.listOf(new MicoServiceStatusDTO().setErrorMessages(CollectionUtils.listOf("No deployment of " + micoService.getShortName() + " " + micoService.getVersion() + " is available."))));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(Optional.empty());
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        assertEquals(micoApplicationStatus, micoStatusService.getApplicationStatus(micoApplication));
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
        MicoServiceStatusDTO micoServiceStatus = new MicoServiceStatusDTO();
        micoServiceStatus
            .setName(SERVICE_NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setAvailableReplicas(0)
            .setRequestedReplicas(1)
            .setUsingApplications(CollectionUtils.listOf(new BasicMicoApplicationDTO(otherMicoApplication.getShortName(), otherMicoApplication.getVersion(), otherMicoApplication.getName())))
            .setAverageCpuLoadPerNode(ImmutableMap.of(nodeName, cpuLoadPod1, nodeName2, cpuLoadPod2))
            .setAverageMemoryUsagePerNode(ImmutableMap.of(nodeName, memoryUsagePod1, nodeName2, memoryUsagePod2))
            .setErrorMessages(CollectionUtils.listOf())
            .setPodsInformation(Arrays.asList(new KubernetesPodInformationDTO()
                    .setPodName(podName1)
                    .setHostIp(hostIp)
                    .setNodeName(nodeName)
                    .setPhase(podPhase)
                    .setStartTime(startTimePod1)
                    .setRestarts(restartsPod1)
                    .setMetrics(new KuberenetesPodMetricsDTO()
                        .setMemoryUsage(memoryUsagePod1)
                        .setCpuLoad(cpuLoadPod1)
                        .setAvailable(podAvailablePod1)),
                new KubernetesPodInformationDTO()
                    .setPodName(podName2)
                    .setHostIp(hostIp)
                    .setNodeName(nodeName2)
                    .setPhase(podPhase)
                    .setStartTime(startTimePod2)
                    .setRestarts(restartsPod2)
                    .setMetrics(new KuberenetesPodMetricsDTO()
                        .setMemoryUsage(memoryUsagePod2)
                        .setCpuLoad(cpuLoadPod2)
                        .setAvailable(podAvailablePod2))))
            .setInterfacesInformation(CollectionUtils.listOf(new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME)));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedService(any(), any())).willReturn(CollectionUtils.listOf(otherMicoApplication));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        ResponseEntity responseEntityMemoryUsagePod1 = getPrometheusResponseEntity(memoryUsagePod1);
        ResponseEntity responseEntityCpuLoadPod1 = getPrometheusResponseEntity(cpuLoadPod1);
        ResponseEntity responseEntityMemoryUsagePod2 = getPrometheusResponseEntity(memoryUsagePod2);
        ResponseEntity responseEntityCpuLoadPod2 = getPrometheusResponseEntity(cpuLoadPod2);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntityMemoryUsagePod1).willReturn(responseEntityCpuLoadPod1).willReturn(responseEntityMemoryUsagePod2).willReturn(responseEntityCpuLoadPod2);
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
