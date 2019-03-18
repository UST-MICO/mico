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
import java.util.*;

import javax.validation.constraints.NotNull;

import io.github.ust.mico.core.dto.response.internal.PrometheusResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.response.*;
import io.github.ust.mico.core.dto.response.status.*;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.PrometheusRequestFailedException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides functionality to retrieve status information for a {@link MicoApplication} or a particular {@link
 * MicoService}
 */
@Slf4j
@Component
public class MicoStatusService {

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
     * @return {@link MicoApplicationStatusResponseDTO} containing a list of {@link MicoServiceStatusResponseDTO} for status information
     * of a single {@link MicoService}
     */
    public MicoApplicationStatusResponseDTO getApplicationStatus(MicoApplication micoApplication) {
        MicoApplicationStatusResponseDTO applicationStatus = new MicoApplicationStatusResponseDTO();
        List<MicoService> micoServices = serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
        int podCount = 0;
        int requestedReplicasCount = 0;
        int availableReplicasCount = 0;
        for (MicoService micoService : micoServices) {
            MicoServiceStatusResponseDTO micoServiceStatus = getServiceStatus(micoService);
            podCount += micoServiceStatus.getPodsInformation().size();
            requestedReplicasCount += micoServiceStatus.getRequestedReplicas();
            availableReplicasCount += micoServiceStatus.getAvailableReplicas();
            // Remove the current application's name to retrieve a list with only the names of other applications that are sharing a service
            micoServiceStatus.getApplicationsUsingThisService().remove(new MicoApplicationResponseDTO()
                .setName(micoApplication.getName())
                .setShortName(micoApplication.getShortName())
                .setVersion(micoApplication.getVersion())
                .setDescription(micoApplication.getDescription()));
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
     * @return {@link MicoServiceStatusResponseDTO} which contains status information for a specific {@link MicoService}
     */
    public MicoServiceStatusResponseDTO getServiceStatus(MicoService micoService) {
        MicoServiceStatusResponseDTO serviceStatus = new MicoServiceStatusResponseDTO();
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

        // Get status information for the service interfaces of this service,
        // if there are any errors, add them to the service status
        List<String> errorMessages = new ArrayList<>();
        List<MicoServiceInterfaceStatusResponseDTO> interfacesInformation = getServiceInterfaceStatus(micoService, errorMessages);
        serviceStatus.setInterfacesInformation(interfacesInformation);
        if (!errorMessages.isEmpty()) {
            serviceStatus.getErrorMessages().addAll(errorMessages);
        }

        // Return all applications that are using this service and are actually deployed
        List<MicoApplication> usingApplications = micoApplicationRepository.findAllByUsedService(micoService.getShortName(), micoService.getVersion());
        for (MicoApplication application : usingApplications) {
            if(micoKubernetesClient.isApplicationDeployed(application)) {
                serviceStatus.getApplicationsUsingThisService().add(new MicoApplicationResponseDTO(application));
            }
        }

        // Get status information for all pods of a service
        List<Pod> podList = micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(micoService);
        List<KubernetesPodInformationResponseDTO> podInfos = new ArrayList<>();
        // Get all the nodes on which the pods of a deployment of a MicoService are running
        Map<String, List<Pod>> podsPerNode = new HashMap<>();
        for (Pod pod : podList) {
            if (podsPerNode.containsKey(pod.getSpec().getNodeName())) {
                podsPerNode.computeIfPresent(pod.getSpec().getNodeName(), (s, pods) -> pods).add(pod);
            } else {
                podsPerNode.computeIfAbsent(pod.getSpec().getNodeName(), pods -> new ArrayList<>()).add(pod);
            }
        }

        List<KubernetesNodeMetricsResponseDTO> nodeMetrics = new ArrayList<>();
        // Calculate for each node the average values for all pods running on this node
        for (String nodeName : podsPerNode.keySet()) {
            int sumCpuLoadOnNode = 0;
            int sumMemoryUsageOnNode = 0;
            for (Pod pod : podsPerNode.get(nodeName)) {
                KubernetesPodInformationResponseDTO podInformation = getUiPodInfo(pod);
                sumCpuLoadOnNode += podInformation.getMetrics().getCpuLoad();
                sumMemoryUsageOnNode += podInformation.getMetrics().getMemoryUsage();
                podInfos.add(podInformation);
            }
            nodeMetrics.add(new KubernetesNodeMetricsResponseDTO()
                .setNodeName(nodeName)
                .setAverageCpuLoad(sumCpuLoadOnNode / podsPerNode.get(nodeName).size())
                .setAverageMemoryUsage(sumMemoryUsageOnNode / podsPerNode.get(nodeName).size()));
        }
        serviceStatus
            .setNodeMetrics(nodeMetrics)
            .setPodsInformation(podInfos);
        return serviceStatus;
    }

    public List<MicoServiceInterfaceStatusResponseDTO> getServiceInterfaceStatus(@NotNull MicoService micoService,
                                                                         @NotNull List<String> errorMessages) {
        List<MicoServiceInterfaceStatusResponseDTO> interfacesInformation = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            String interfaceName = serviceInterface.getServiceInterfaceName();
            List<String> publicIps = new ArrayList<>();
            try {
                Optional<Service> kubernetesServices = micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, interfaceName);
                if (kubernetesServices.isPresent()) {
                    publicIps = getPublicIpsOfKubernetesService(kubernetesServices.get());
                    if (publicIps.isEmpty()) {
                        errorMessages.add("There is no Kubernetes service for the interface '" +
                            interfaceName + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "'.");
                    }
                } else {
                    log.warn("There is no Kubernetes service for the interface '{}' of MicoService '{}' '{}'. Continue with next one.",
                        interfaceName, micoService.getShortName(), micoService.getVersion());
                    errorMessages.add("There is no Kubernetes service for the interface '" +
                        interfaceName + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "'.");
                }
            } catch (Exception e) {
                log.error("Error while retrieving the Kubernetes service for the interface '{}' of MicoService '{}' '{}'. "
                        + "Continue with next one. Caused by: {}",
                    interfaceName, micoService.getShortName(), micoService.getVersion(), e.getMessage());
                errorMessages.add(e.getMessage());
            }

            interfacesInformation.add(new MicoServiceInterfaceStatusResponseDTO(interfaceName, publicIps));
        }
        return interfacesInformation;
    }

    /**
     * Get the public IPs of a {@link MicoServiceInterface} by providing the corresponding Kubernetes {@link Service}.
     *
     * @param kubernetesService the Kubernetes {@link Service}
     * @return a list with public IPs of the provided Kubernetes Service
     */
    // TODO: Duplicate exists in ServiceInterfaceResource. Will be covered by mico#491
    private List<String> getPublicIpsOfKubernetesService(Service kubernetesService) {
        LoadBalancerStatus loadBalancerStatus = kubernetesService.getStatus().getLoadBalancer();
        List<String> publicIps = new ArrayList<>();
        if (loadBalancerStatus != null) {
            List<LoadBalancerIngress> ingressList = loadBalancerStatus.getIngress();
            if (ingressList != null && !ingressList.isEmpty()) {
                for (LoadBalancerIngress ingress : ingressList) {
                    publicIps.add(ingress.getIp());
                }
            }
        }
        return publicIps;
    }

    /**
     * Get information and metrics for a {@link Pod} representing an instance of a {@link MicoService}.
     *
     * @param pod is a {@link Pod} of Kubernetes
     * @return a {@link KubernetesPodInformationResponseDTO} which has node name, pod name, phase, host ip, memory usage, and
     * cpu load as status information
     */
    private KubernetesPodInformationResponseDTO getUiPodInfo(Pod pod) {
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
        KubernetesPodMetricsResponseDTO podMetrics = new KubernetesPodMetricsResponseDTO();
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
        return new KubernetesPodInformationResponseDTO(podName, phase, hostIp, nodeName, restarts, age, podMetrics);
    }

    private int getMemoryUsageForPod(String podName) throws PrometheusRequestFailedException {
        URI prometheusUri = getPrometheusUri(PROMETHEUS_QUERY_FOR_MEMORY_USAGE, podName);
        return requestValueFromPrometheus(prometheusUri);
    }

    private int getCpuLoadForPod(String podName) throws PrometheusRequestFailedException {
        URI prometheusUri = getPrometheusUri(PROMETHEUS_QUERY_FOR_CPU_USAGE, podName);
        return requestValueFromPrometheus(prometheusUri);
    }

    /**
     * Requests the CPU load / memory usage value from Prometheus.
     *
     * @param prometheusUri is the adapted URI with the query for Prometheus, either CPU load or memory usage.
     * @return a single Integer value of the current CPU load or the memory usage of a {@link Pod}.
     * @throws PrometheusRequestFailedException is thrown if Prometheus returns an error, if there is no response body,
     *                                          or if the HTTP request was not successful.
     */
    private int requestValueFromPrometheus(URI prometheusUri) throws PrometheusRequestFailedException {
        ResponseEntity<PrometheusResponseDTO> response = restTemplate.getForEntity(prometheusUri, PrometheusResponseDTO.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PrometheusResponseDTO prometheusResponse = response.getBody();
            if (prometheusResponse != null) {
                if (prometheusResponse.isSuccess()) {
                    return prometheusResponse.getValue();
                } else {
                    throw new PrometheusRequestFailedException("Prometheus returned a response with status " + prometheusResponse.isSuccess());
                }
            } else {
                throw new PrometheusRequestFailedException("There is no body in the response with status code " + response.getStatusCode());
            }
        } else {
            throw new PrometheusRequestFailedException("The http request was not successful and returned a " + response.getStatusCode());
        }
    }

    /**
     * Builds the correct Prometheus URI to request the correct value.
     *
     * @param query   is the query for Prometheus in PromQL (either the query for the CPU load, or for the memory
     *                usage).
     * @param podName is the name of the {@link Pod}, for which the CPU load / memory usage query is build.
     * @return the URI to send the request to.
     */
    private URI getPrometheusUri(String query, String podName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(prometheusConfig.getUri());
        uriBuilder.queryParam(PROMETHEUS_QUERY_PARAMETER_NAME, String.format(query, podName));
        URI prometheusUri = uriBuilder.build().toUri();
        log.debug("Using Prometheus URI '{}'", prometheusUri);
        return prometheusUri;
    }
}
