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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.*;

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
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoMessage.Type;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.*;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
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

    @MockBean
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoStatusService micoStatusService;

    private MicoApplication micoApplication;
    private MicoApplication otherMicoApplication;
    private MicoService micoService;
    private MicoServiceInterface micoServiceInterface;
    private Optional<Deployment> deployment;
    private Optional<Service> kubernetesService;
    private PodList podList;
    private PodList podListWithOnePod;

    private String nodeName1 = "testNode";
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

    // Metrics for pod 3
    private String podName3 = "pod3";
    private int memoryUsagePod3 = 50;
    private int cpuLoadPod3 = 10;
    private String startTimePod3 = new Date().toString();
    private int restartsPod3 = 0;
    private boolean podAvailablePod3 = true;

    // Metrics for pod 4
    private String podName4 = "pod4";
    private int memoryUsagePod4 = 65;
    private int cpuLoadPod4 = 5;
    private String startTimePod4 = new Date().toString();
    private int restartsPod4 = 0;
    private boolean podAvailablePod4 = true;


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
                .setVersion(VERSION)
                .setServiceInterfaces(CollectionUtils.listOf(
                        new MicoServiceInterface()
                                .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                ));

        micoServiceInterface = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                .setPorts(CollectionUtils.listOf(new MicoServicePort()
                        .setPort(80)
                        .setTargetPort(80)
                        .setType(MicoPortType.TCP)));

        micoApplication.getServices().add(micoService);
        micoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(micoService));

        otherMicoApplication.getServices().add(micoService);
        otherMicoApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(micoService));

        int availableReplicas = 1;
        int replicas = 1;

        deployment = Optional.of(new DeploymentBuilder()
                .withNewMetadata().withName(deploymentName).endMetadata()
                .withNewSpec().withReplicas(replicas).endSpec().withNewStatus().withAvailableReplicas(availableReplicas).endStatus()
                .build());

        kubernetesService = Optional.of(new ServiceBuilder()
                .withNewMetadata().withName(SERVICE_INTERFACE_NAME).endMetadata()
                .withNewSpec().endSpec()
                .withNewStatus()
                .withNewLoadBalancer()
                .addNewIngress().withIp("192.168.2.112").endIngress()
                .addNewIngress().withIp("192.168.2.113").endIngress()
                .endLoadBalancer()
                .endStatus()
                .build());

        podList = new PodListBuilder()
                .addNewItem()
                .withNewMetadata().withName(podName1).endMetadata()
                .withNewSpec().withNodeName(nodeName1).endSpec()
                .withNewStatus().withStartTime(startTimePod1).
                        addNewContainerStatus().withRestartCount(restartsPod1).endContainerStatus().
                        withPhase(podPhase).
                        withHostIP(hostIp)
                .endStatus()
                .endItem()
                .addNewItem()
                .withNewMetadata().withName(podName2).endMetadata()
                .withNewSpec().withNodeName(nodeName1).endSpec()
                .withNewStatus().withStartTime(startTimePod2)
                .addNewContainerStatus().withRestartCount(restartsPod2).endContainerStatus()
                .withPhase(podPhase)
                .withHostIP(hostIp)
                .endStatus()
                .endItem()
                .addNewItem()
                .withNewMetadata().withName(podName3).endMetadata()
                .withNewSpec().withNodeName(nodeName2).endSpec()
                .withNewStatus().withStartTime(startTimePod3)
                .addNewContainerStatus().withRestartCount(restartsPod3).endContainerStatus()
                .withPhase(podPhase)
                .withHostIP(hostIp)
                .endStatus()
                .endItem()
                .addNewItem()
                .withNewMetadata().withName(podName4).endMetadata()
                .withNewSpec().withNodeName(nodeName2).endSpec()
                .withNewStatus().withStartTime(startTimePod4)
                .addNewContainerStatus().withRestartCount(restartsPod4).endContainerStatus()
                .withPhase(podPhase)
                .withHostIP(hostIp)
                .endStatus()
                .endItem()
                .build();

        podListWithOnePod = new PodListBuilder()
                .addNewItem()
                .withNewMetadata().withName(podName1).endMetadata()
                .withNewSpec().withNodeName(nodeName1).endSpec()
                .withNewStatus().withStartTime(startTimePod1).addNewContainerStatus().withRestartCount(restartsPod1).endContainerStatus().withPhase(podPhase).withHostIP(hostIp).endStatus()
                .endItem()
                .build();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getApplicationStatus() {
        MicoApplicationStatusResponseDTO micoApplicationStatus = new MicoApplicationStatusResponseDTO();
        micoApplicationStatus
                .setTotalNumberOfRequestedReplicas(1)
                .setTotalNumberOfAvailableReplicas(1)
                .setTotalNumberOfPods(4)
                .setTotalNumberOfMicoServices(1)
                .setServiceStatuses(CollectionUtils.listOf(new MicoServiceStatusResponseDTO()
                        .setName(NAME)
                        .setShortName(SHORT_NAME)
                        .setVersion(VERSION)
                        .setAvailableReplicas(1)
                        .setRequestedReplicas(1)
                        .setApplicationsUsingThisService(CollectionUtils.listOf(new MicoApplicationResponseDTO(otherMicoApplication)))
                        .setNodeMetrics(CollectionUtils.listOf(
                                new KubernetesNodeMetricsResponseDTO()
                                        .setNodeName(nodeName1)
                                        .setAverageCpuLoad(20)
                                        .setAverageMemoryUsage(60),
                                new KubernetesNodeMetricsResponseDTO()
                                        .setNodeName(nodeName2)
                                        .setAverageCpuLoad(7)
                                        .setAverageMemoryUsage(57)
                        ))
                        // Add four pods (on two different nodes)
                        .setPodsInformation(Arrays.asList(
                                new KubernetesPodInformationResponseDTO()
                                        .setPodName(podName1)
                                        .setHostIp(hostIp)
                                        .setNodeName(nodeName1)
                                        .setPhase(podPhase)
                                        .setStartTime(startTimePod1)
                                        .setRestarts(restartsPod1)
                                        .setMetrics(new KubernetesPodMetricsResponseDTO()
                                                .setMemoryUsage(memoryUsagePod1)
                                                .setCpuLoad(cpuLoadPod1)
                                                .setAvailable(podAvailablePod1)),
                                new KubernetesPodInformationResponseDTO()
                                        .setPodName(podName2)
                                        .setHostIp(hostIp)
                                        .setNodeName(nodeName1)
                                        .setPhase(podPhase)
                                        .setStartTime(startTimePod2)
                                        .setRestarts(restartsPod2)
                                        .setMetrics(new KubernetesPodMetricsResponseDTO()
                                                .setMemoryUsage(memoryUsagePod2)
                                                .setCpuLoad(cpuLoadPod2)
                                                .setAvailable(podAvailablePod2)),
                                new KubernetesPodInformationResponseDTO()
                                        .setPodName(podName3)
                                        .setHostIp(hostIp)
                                        .setNodeName(nodeName2)
                                        .setPhase(podPhase)
                                        .setStartTime(startTimePod3)
                                        .setRestarts(restartsPod3)
                                        .setMetrics(new KubernetesPodMetricsResponseDTO()
                                                .setMemoryUsage(memoryUsagePod3)
                                                .setCpuLoad(cpuLoadPod3)
                                                .setAvailable(podAvailablePod3)),
                                new KubernetesPodInformationResponseDTO()
                                        .setPodName(podName4)
                                        .setHostIp(hostIp)
                                        .setNodeName(nodeName2)
                                        .setPhase(podPhase)
                                        .setStartTime(startTimePod4)
                                        .setRestarts(restartsPod4)
                                        .setMetrics(new KubernetesPodMetricsResponseDTO()
                                                .setMemoryUsage(memoryUsagePod4)
                                                .setCpuLoad(cpuLoadPod4)
                                                .setAvailable(podAvailablePod4))))
                        .setErrorMessages(CollectionUtils.listOf())
                        .setInterfacesInformation(CollectionUtils.listOf(
                                new MicoServiceInterfaceStatusResponseDTO()
                                        .setName(SERVICE_INTERFACE_NAME)
                                        .setExternalIp("192.168.2.112")))))
        ;
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
            given(micoKubernetesClient.isApplicationDeployed(otherMicoApplication)).willReturn(true);
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedService(any(), any())).willReturn(CollectionUtils.listOf(otherMicoApplication, micoApplication));
        given(serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(CollectionUtils.listOf(micoService));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));
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
            .setServiceStatuses(CollectionUtils.listOf(new MicoServiceStatusResponseDTO()
                .setName(NAME)
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setAvailableReplicas(1)
                .setRequestedReplicas(1)
                .setApplicationsUsingThisService(CollectionUtils.listOf(new MicoApplicationResponseDTO(otherMicoApplication)))
                .setNodeMetrics(CollectionUtils.listOf(
                    new KubernetesNodeMetricsResponseDTO()
                        .setNodeName(nodeName1)
                        .setAverageCpuLoad(30)
                        .setAverageMemoryUsage(70)
                ))
                // Add four pods (on two different nodes)
                .setPodsInformation(CollectionUtils.listOf(
                    new KubernetesPodInformationResponseDTO()
                        .setPodName(podName1)
                        .setHostIp(hostIp)
                        .setNodeName(nodeName1)
                        .setPhase(podPhase)
                        .setStartTime(startTimePod1)
                        .setRestarts(restartsPod1)
                        .setMetrics(new KubernetesPodMetricsResponseDTO()
                            .setMemoryUsage(memoryUsagePod1)
                            .setCpuLoad(cpuLoadPod1)
                            .setAvailable(podAvailablePod1))))
                .setErrorMessages(CollectionUtils.listOf(
                    new MicoMessageResponseDTO().setContent("There are no public IP addresses available yet for the interface '" +
                    SERVICE_INTERFACE_NAME + "' of MicoService '" +
                    micoService.getShortName() + "' '" + micoService.getVersion() + "'.").setType(Type.ERROR)))
                .setInterfacesInformation(CollectionUtils.listOf(
                    new MicoServiceInterfaceStatusResponseDTO()
                        .setName(SERVICE_INTERFACE_NAME)
                        // Empty list of external IP addresses
                        .setExternalIps(CollectionUtils.listOf())))));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(Optional.empty());
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podListWithOnePod.getItems());
            given(micoKubernetesClient.isApplicationDeployed(otherMicoApplication)).willReturn(true);
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedService(any(), any())).willReturn(CollectionUtils.listOf(otherMicoApplication, micoApplication));
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
            .setServiceStatuses(CollectionUtils.listOf(new MicoServiceStatusResponseDTO()
		        .setErrorMessages(CollectionUtils
		            .listOf(new MicoMessageResponseDTO().setContent("No deployment of MicoService '" + micoService.getShortName()
		                + "' '" + micoService.getVersion() + "' is available.").setType(Type.ERROR)))));
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
    public void getServiceStatus() {
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
                                .setAverageCpuLoad(20)
                                .setAverageMemoryUsage(60),
                        new KubernetesNodeMetricsResponseDTO()
                                .setNodeName(nodeName2)
                                .setAverageCpuLoad(7)
                                .setAverageMemoryUsage(57)
                ))
                // Add four pods (on two different nodes)
                .setPodsInformation(Arrays.asList(
                        new KubernetesPodInformationResponseDTO()
                                .setPodName(podName1)
                                .setHostIp(hostIp)
                                .setNodeName(nodeName1)
                                .setPhase(podPhase)
                                .setStartTime(startTimePod1)
                                .setRestarts(restartsPod1)
                                .setMetrics(new KubernetesPodMetricsResponseDTO()
                                        .setMemoryUsage(memoryUsagePod1)
                                        .setCpuLoad(cpuLoadPod1)
                                        .setAvailable(podAvailablePod1)),
                        new KubernetesPodInformationResponseDTO()
                                .setPodName(podName2)
                                .setHostIp(hostIp)
                                .setNodeName(nodeName1)
                                .setPhase(podPhase)
                                .setStartTime(startTimePod2)
                                .setRestarts(restartsPod2)
                                .setMetrics(new KubernetesPodMetricsResponseDTO()
                                        .setMemoryUsage(memoryUsagePod2)
                                        .setCpuLoad(cpuLoadPod2)
                                        .setAvailable(podAvailablePod2)),
                        new KubernetesPodInformationResponseDTO()
                                .setPodName(podName3)
                                .setHostIp(hostIp)
                                .setNodeName(nodeName2)
                                .setPhase(podPhase)
                                .setStartTime(startTimePod3)
                                .setRestarts(restartsPod3)
                                .setMetrics(new KubernetesPodMetricsResponseDTO()
                                        .setMemoryUsage(memoryUsagePod3)
                                        .setCpuLoad(cpuLoadPod3)
                                        .setAvailable(podAvailablePod3)),
                        new KubernetesPodInformationResponseDTO()
                                .setPodName(podName4)
                                .setHostIp(hostIp)
                                .setNodeName(nodeName2)
                                .setPhase(podPhase)
                                .setStartTime(startTimePod4)
                                .setRestarts(restartsPod4)
                                .setMetrics(new KubernetesPodMetricsResponseDTO()
                                        .setMemoryUsage(memoryUsagePod4)
                                        .setCpuLoad(cpuLoadPod4)
                                        .setAvailable(podAvailablePod4))))
                .setErrorMessages(CollectionUtils.listOf())
                .setInterfacesInformation(CollectionUtils.listOf(new MicoServiceInterfaceStatusResponseDTO()
                        .setName(SERVICE_INTERFACE_NAME)
                        .setExternalIp("192.168.2.112")));
        try {
            given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
            given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);
            given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
            given(micoKubernetesClient.isApplicationDeployed(otherMicoApplication)).willReturn(true);
        } catch (KubernetesResourceException e) {
            e.printStackTrace();
        }
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(applicationRepository.findAllByUsedService(any(), any())).willReturn(CollectionUtils.listOf(otherMicoApplication));
        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");
        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));
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
        assertEquals(micoServiceStatus, micoStatusService.getServiceStatus(micoService));
    }

    @Test
    public void getServiceInterfaceStatus() throws KubernetesResourceException {

        given(micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, SERVICE_INTERFACE_NAME))
                .willReturn(kubernetesService);
        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.of(micoServiceInterface));

        MicoServiceInterfaceStatusResponseDTO expectedServiceInterface = new MicoServiceInterfaceStatusResponseDTO()
                .setName(SERVICE_INTERFACE_NAME)
                .setExternalIp("192.168.2.112");
        List<MicoServiceInterfaceStatusResponseDTO> expectedInterfaceStatusDTO = new LinkedList<>();
        expectedInterfaceStatusDTO.add(expectedServiceInterface);
        List<MicoMessageResponseDTO> errorMessages = new ArrayList<>();

        List<MicoServiceInterfaceStatusResponseDTO> actualInterfaceStatusDTO = micoStatusService.getServiceInterfaceStatus(micoService, errorMessages);

        assertTrue("Expected there are no errors", errorMessages.isEmpty());
        assertEquals(expectedInterfaceStatusDTO, actualInterfaceStatusDTO);
    }

    @Test
    public void getServiceInterfaceStatusWithErrors() throws KubernetesResourceException {

        given(serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), SERVICE_INTERFACE_NAME)).willReturn(Optional.empty());
        given(micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, SERVICE_INTERFACE_NAME))
                .willReturn(Optional.empty());

        MicoServiceInterfaceStatusResponseDTO expectedServiceInterface = new MicoServiceInterfaceStatusResponseDTO()
                .setName(SERVICE_INTERFACE_NAME); // Expect that there are no IPs
        List<MicoServiceInterfaceStatusResponseDTO> expectedInterfaceStatusDTO = new LinkedList<>();
        expectedInterfaceStatusDTO.add(expectedServiceInterface);

        List<MicoMessageResponseDTO> errorMessages = new ArrayList<>();

        List<MicoServiceInterfaceStatusResponseDTO> actualInterfaceStatusDTO = micoStatusService.getServiceInterfaceStatus(micoService, errorMessages);

        assertEquals("Expected one error", 1, errorMessages.size());
        assertEquals(expectedInterfaceStatusDTO, actualInterfaceStatusDTO);
    }
}
