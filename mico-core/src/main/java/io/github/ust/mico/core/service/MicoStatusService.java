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

package io.github.ust.mico.core.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.BasicMicoApplicationDTO;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInformationDTO;
import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.MicoServiceStatusDTO;
import io.github.ust.mico.core.dto.PrometheusResponse;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.PrometheusRequestFailedException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Provide functionality to retrieve status information for a {@link MicoApplication} or a particular {@link
 * MicoService}
 */
@Slf4j
@Component
public class MicoStatusService {

    private static final int MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT = 1;
    private static final String PROMETHEUS_QUERY_FOR_MEMORY_USAGE = "sum(container_memory_working_set_bytes{pod_name=\"%s\",container_name=\"\"})";
    private static final String PROMETHEUS_QUERY_FOR_CPU_USAGE = "sum(container_cpu_load_average_10s{pod_name=\"%s\"})";
    private static final String PROMETHEUS_QUERY_PARAMETER_NAME = "query";
    private final PrometheusConfig prometheusConfig;
    private final MicoKubernetesClient micoKubernetesClient;
    private final RestTemplate restTemplate;
    private final MicoServiceRepository serviceRepository;
    private final MicoApplicationRepository micoApplicationRepository;

    @Autowired
    public MicoStatusService(PrometheusConfig prometheusConfig, MicoKubernetesClient micoKubernetesClient,
                             RestTemplate restTemplate, MicoServiceRepository serviceRepository, MicoApplicationRepository micoApplicationRepository) {
        this.prometheusConfig = prometheusConfig;
        this.micoKubernetesClient = micoKubernetesClient;
        this.restTemplate = restTemplate;
        this.serviceRepository = serviceRepository;
        this.micoApplicationRepository = micoApplicationRepository;
    }

    /**
     * Get status information for a {@link MicoApplication}
     *
     * @param micoApplication the application the status is requested for
     * @return {@link MicoApplicationStatusDTO} containing a list of {@link MicoServiceStatusDTO} for status information
     * of a single {@link MicoService}
     */
    public MicoApplicationStatusDTO getApplicationStatus(MicoApplication micoApplication) {
        MicoApplicationStatusDTO applicationStatus = new MicoApplicationStatusDTO();
        List<MicoService> micoServices = serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
        int podCount = 0;
        int requestedReplicasCount = 0;
        int availableReplicasCount = 0;
        for (MicoService micoService : micoServices) {
            MicoServiceStatusDTO micoServiceStatus = getServiceStatus(micoService);
            podCount += micoServiceStatus.getPodsInformation().size();
            requestedReplicasCount += micoServiceStatus.getRequestedReplicas();
            availableReplicasCount += micoServiceStatus.getAvailableReplicas();
            // Remove the current application's name to retrieve a list with only the names of other applications that are sharing a service
            micoServiceStatus.getUsingApplications().remove(new BasicMicoApplicationDTO(micoApplication.getShortName(), micoApplication.getVersion(), micoApplication.getName()));
            applicationStatus.getServiceStatuses().add(micoServiceStatus);
        }
        applicationStatus
            .setTotalNumberOfMicoServices(micoServices.size())
            .setTotalNumberOfPods(podCount)
            .setTotalNumberOfAvailableReplicas(availableReplicasCount)
            .setTotalNumberOfRequestedReplicas(requestedReplicasCount);
        return applicationStatus;
    }

    /**
     * Get status information for a single {@link MicoService}: # available replicas, # requested replicas, pod metrics
     * (cpu load, memory load)
     *
     * @param micoService is a {@link MicoService}
     * @return {@link MicoServiceStatusDTO} which contains status information for a specific {@link MicoService}
     */
    public MicoServiceStatusDTO getServiceStatus(MicoService micoService) {
        MicoServiceStatusDTO serviceStatus = new MicoServiceStatusDTO();
        try {
            Optional<Deployment> deploymentOptional = micoKubernetesClient.getDeploymentOfMicoService(micoService);
            if (deploymentOptional.isPresent()) {
                Deployment deployment = deploymentOptional.get();
                serviceStatus.setAvailableReplicas(deployment.getStatus().getAvailableReplicas());
                serviceStatus.setRequestedReplicas(deployment.getSpec().getReplicas());
            } else {
                log.warn("There is no deployment of the MicoService '{}' '{}'. Continue with next one.",
                    micoService.getShortName(), micoService.getVersion());
                return serviceStatus.setErrorMessages(CollectionUtils.listOf("No deployment of " + micoService.getShortName() + " " + micoService.getVersion() + " is available."));
            }
        } catch (KubernetesResourceException e) {
            log.error("Error while retrieving Kubernetes deployment of MicoService '{}' '{}'. Continue with next one. Caused by: {}",
                micoService.getShortName(), micoService.getVersion(), e.getMessage());
            return serviceStatus.setErrorMessages(CollectionUtils.listOf("Error while retrieving Kubernetes deployment of MicoService " + micoService.getShortName() + " "
                + micoService.getVersion() + " .  Caused by: " + e.getMessage()));
        }
        serviceStatus
            .setName(micoService.getName())
            .setShortName(micoService.getShortName())
            .setVersion(micoService.getVersion());

        // Get status information for service interfaces of a service
        serviceStatus.setInterfacesInformation(getServiceInterfaceStatus(micoService));

        // Return the names of all applications that are using this service
        List<MicoApplication> usingApplications = micoApplicationRepository.findAllByUsedService(micoService.getShortName(), micoService.getVersion());
        for (MicoApplication micoApplication : usingApplications) {
            BasicMicoApplicationDTO usingApplication = new BasicMicoApplicationDTO(micoApplication.getShortName(), micoApplication.getVersion(), micoApplication.getName());
            serviceStatus.getUsingApplications().add(usingApplication);
        }

        // Get status information for all pods of a service
        List<Pod> podList = micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(micoService);
        List<KubernetesPodInformationDTO> podInfos = new ArrayList<>();
        // Get all the nodes on which the pods of a deployment of a MicoService are running
        Map<String, List<Pod>> podsPerNodeMap = new HashMap<>();
        for (Pod pod : podList) {
            if (podsPerNodeMap.containsKey(pod.getSpec().getNodeName())) {
                podsPerNodeMap.computeIfPresent(pod.getSpec().getNodeName(), (s, pods) -> {
                    pods.add(pod);
                    return (pods);
                });
            } else {
                podsPerNodeMap.computeIfAbsent(pod.getSpec().getNodeName(), pods -> CollectionUtils.listOf(pod));
            }
        }

        Map<String, Integer> averageCpuUsagePerNode = new HashMap<>();
        Map<String, Integer> averageMemoryUsagePerNode = new HashMap<>();
        for (String nodeName : podsPerNodeMap.keySet()) {
            int sumCpuLoadOnNode = 0;
            int sumMemoryUsageOnNode = 0;
            for (Pod pod : podsPerNodeMap.get(nodeName)) {
                KubernetesPodInformationDTO podInformation = getUiPodInfo(pod);
                sumCpuLoadOnNode += podInformation.getMetrics().getCpuLoad();
                sumMemoryUsageOnNode += podInformation.getMetrics().getMemoryUsage();
                podInfos.add(podInformation);
            }
            averageCpuUsagePerNode.put(nodeName, sumCpuLoadOnNode / podsPerNodeMap.get(nodeName).size());
            averageMemoryUsagePerNode.put(nodeName, sumMemoryUsageOnNode / podsPerNodeMap.get(nodeName).size());
        }
        serviceStatus.setAverageCpuLoadPerNode(averageCpuUsagePerNode);
        serviceStatus.setAverageMemoryUsagePerNode(averageMemoryUsagePerNode);
        serviceStatus.setPodsInformation(podInfos);
        return serviceStatus;
    }

    /**
     * Get status information for all {@link MicoServiceInterface} of a {@link MicoService}: service name,
     *
     * @param micoService is a {@link MicoService}
     * @return a list of {@link MicoServiceInterfaceDTO}, each item contains status information for a {@link
     * MicoServiceInterface}
     */
    public List<MicoServiceInterfaceDTO> getServiceInterfaceStatus(MicoService micoService) {
        List<MicoServiceInterfaceDTO> interfacesInformation = new ArrayList<>();
        List<MicoServiceInterface> serviceInterfaces = micoService.getServiceInterfaces();
        if (serviceInterfaces.size() < MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "There are " + serviceInterfaces.size() + " service interfaces of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "'. That is less than the required " +
                    MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT + " service interfaces.");
        }

        for (MicoServiceInterface micoServiceInterface : serviceInterfaces) {
            String serviceInterfaceName = micoServiceInterface.getServiceInterfaceName();
            Optional<Service> kubernetesServiceOptional = Optional.empty();
            try {
                kubernetesServiceOptional = micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, serviceInterfaceName);
            } catch (KubernetesResourceException e) {
                log.error("Error while retrieving Kubernetes services of MicoServiceInterface '{}' of MicoService '{}' '{}'. " + "Continue with next one. Caused by: {}",
                    serviceInterfaceName, micoService.getShortName(), micoService.getVersion(), e.getMessage());
            }
            if (!kubernetesServiceOptional.isPresent()) {
                log.warn("There is no service of the MicoServiceInterface '{}' of MicoService '{}' '{}'.",
                    micoService.getShortName(), micoService.getVersion());
                continue;
            }
            Service kubernetesService = kubernetesServiceOptional.get();
            String serviceName = kubernetesService.getMetadata().getName();
            MicoServiceInterfaceDTO interfaceInformation = new MicoServiceInterfaceDTO(serviceName);
            interfacesInformation.add(interfaceInformation);
        }
        return interfacesInformation;
    }

    /**
     * Get information and metrics for a {@link Pod} representing an instance of a {@link MicoService}.
     *
     * @param pod is a {@link Pod} of Kubernetes
     * @return a {@link KubernetesPodInformationDTO} which has node name, pod name, phase, host ip, memory usage, and
     * cpu load as status information
     */
    private KubernetesPodInformationDTO getUiPodInfo(Pod pod) {
        String nodeName = pod.getSpec().getNodeName();
        String podName = pod.getMetadata().getName();
        String phase = pod.getStatus().getPhase();
        String hostIp = pod.getStatus().getHostIP();
        int restarts = 0;
        for (ContainerStatus containerStatus : pod.getStatus().getContainerStatuses()) {
            restarts += containerStatus.getRestartCount();
        }
        String age = pod.getStatus().getStartTime();
        int memoryUsage = -1;
        int cpuLoad = -1;
        KuberenetesPodMetricsDTO podMetrics = new KuberenetesPodMetricsDTO();
        try {
            memoryUsage = getMemoryUsageForPod(podName);
            cpuLoad = getCpuLoadForPod(podName);
            podMetrics.setAvailable(true);
        } catch (PrometheusRequestFailedException | ResourceAccessException e) {
            podMetrics.setAvailable(false);
            log.error(e.getMessage(), e);
        }
        podMetrics.setMemoryUsage(memoryUsage);
        podMetrics.setCpuLoad(cpuLoad);
        return new KubernetesPodInformationDTO(podName, phase, hostIp, nodeName, restarts, age, podMetrics);
    }

    private int getMemoryUsageForPod(String podName) throws PrometheusRequestFailedException {
        URI prometheusUri = getPrometheusUri(PROMETHEUS_QUERY_FOR_MEMORY_USAGE, podName);
        return requestValueFromPrometheus(prometheusUri);
    }

    private int getCpuLoadForPod(String podName) throws PrometheusRequestFailedException {
        URI prometheusUri = getPrometheusUri(PROMETHEUS_QUERY_FOR_CPU_USAGE, podName);
        return requestValueFromPrometheus(prometheusUri);
    }

    private int requestValueFromPrometheus(URI prometheusUri) throws PrometheusRequestFailedException {
        ResponseEntity<PrometheusResponse> response = restTemplate.getForEntity(prometheusUri, PrometheusResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PrometheusResponse prometheusMemoryResponse = response.getBody();
            if (prometheusMemoryResponse != null) {
                if (prometheusMemoryResponse.wasSuccessful()) {
                    return prometheusMemoryResponse.getValue();
                } else {
                    throw new PrometheusRequestFailedException("The status of the prometheus response was " + prometheusMemoryResponse.getStatus(), response.getStatusCode(), prometheusMemoryResponse.getStatus());
                }
            } else {
                throw new PrometheusRequestFailedException("There was no response body", response.getStatusCode(), null);
            }
        } else {
            throw new PrometheusRequestFailedException("The http status code was not 2xx", response.getStatusCode(), null);
        }
    }

    private URI getPrometheusUri(String query, String podName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(prometheusConfig.getUri());
        uriBuilder.queryParam(PROMETHEUS_QUERY_PARAMETER_NAME, String.format(query, podName));
        URI prometheusUri = uriBuilder.build().toUri();
        log.debug("Using Prometheus URI '{}'", prometheusUri);
        return prometheusUri;
    }
}
