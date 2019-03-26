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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO;
import io.github.ust.mico.core.dto.response.internal.PrometheusResponseDTO;
import io.github.ust.mico.core.dto.response.status.*;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.PrometheusRequestFailedException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoMessage;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides functionality to retrieve status information for a {@link MicoApplication} or a particular {@link
 * MicoService}.
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
    private final MicoServiceInterfaceRepository serviceInterfaceRepository;
    private final MicoApplicationRepository micoApplicationRepository;

    @Autowired
    public MicoStatusService(PrometheusConfig prometheusConfig, MicoKubernetesClient micoKubernetesClient,
                             RestTemplate restTemplate, MicoServiceRepository serviceRepository,
                             MicoServiceInterfaceRepository serviceInterfaceRepository,
                             MicoApplicationRepository micoApplicationRepository) {
        this.prometheusConfig = prometheusConfig;
        this.micoKubernetesClient = micoKubernetesClient;
        this.restTemplate = restTemplate;
        this.serviceRepository = serviceRepository;
        this.serviceInterfaceRepository = serviceInterfaceRepository;
        this.micoApplicationRepository = micoApplicationRepository;
    }

    /**
     * Get status information for a {@link MicoApplication}.
     *
     * @param micoApplication the application the status is requested for
     * @return {@link MicoApplicationStatusResponseDTO} containing a list of {@link MicoServiceStatusResponseDTO} for
     * status information of a single {@link MicoService}.
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
            micoServiceStatus.getApplicationsUsingThisService().removeIf(a ->
                a.getShortName().equals(micoApplication.getShortName()) &&
                    a.getVersion().equals(micoApplication.getVersion()));
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
     * (CPU load, memory usage).
     *
     * @param micoService is a {@link MicoService}.
     * @return {@link MicoServiceStatusResponseDTO} which contains status information for a specific {@link
     * MicoService}.
     */
    public MicoServiceStatusResponseDTO getServiceStatus(MicoService micoService) {
        MicoServiceStatusResponseDTO serviceStatus = new MicoServiceStatusResponseDTO();
        String message;
        try {
            Optional<Deployment> deploymentOptional = micoKubernetesClient.getDeploymentOfMicoService(micoService);
            if (deploymentOptional.isPresent()) {
                Deployment deployment = deploymentOptional.get();
                serviceStatus.setRequestedReplicas(deployment.getSpec().getReplicas());
                // Check if there are no replicas available of the deployment of a MicoService.
                if (deployment.getStatus().getUnavailableReplicas() == null) {
                    log.info("The MicoService '{}' with version '{}' has '{}' available replicas.",
                        micoService.getShortName(), micoService.getVersion(), deployment.getStatus().getAvailableReplicas());
                    serviceStatus.setAvailableReplicas(deployment.getStatus().getAvailableReplicas());
                } else if ((deployment.getStatus().getUnavailableReplicas() != null) &&
                    deployment.getStatus().getUnavailableReplicas() < deployment.getSpec().getReplicas()) {
                    log.info("The MicoService '{}' with version '{}' has '{}' available replicas.",
                        micoService.getShortName(), micoService.getVersion(), deployment.getStatus().getAvailableReplicas());
                    serviceStatus.setAvailableReplicas(deployment.getStatus().getAvailableReplicas());
                } else {
                    log.info("The MicoService '{}' with version '{}' has no available replicas.", micoService.getShortName(), micoService.getVersion());
                    serviceStatus.setAvailableReplicas(0);
                }
            } else {
                message = "No deployment of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' is available.";
                log.warn(message);
                MicoMessage errorMessage = MicoMessage.error(message);
                return serviceStatus.setErrorMessages(CollectionUtils.listOf(new MicoMessageResponseDTO(errorMessage)));
            }
        } catch (KubernetesResourceException e) {
            message = "Error while retrieving Kubernetes deployment of MicoService '" + micoService.getShortName() + "' '"
                + micoService.getVersion() + "'. Caused by: " + e.getMessage();
            log.error(message);
            MicoMessage errorMessage = MicoMessage.error(message);
            return serviceStatus.setErrorMessages(CollectionUtils.listOf(new MicoMessageResponseDTO(errorMessage)));
        }
        serviceStatus
            .setName(micoService.getName())
            .setShortName(micoService.getShortName())
            .setVersion(micoService.getVersion());

        // Get status information for the service interfaces of this service,
        // if there are any errors, add them to the service status
        List<MicoMessageResponseDTO> errorMessages = new ArrayList<>();
        List<MicoServiceInterfaceStatusResponseDTO> interfacesInformation = getServiceInterfaceStatus(micoService, errorMessages);
        serviceStatus.setInterfacesInformation(interfacesInformation);
        if (!errorMessages.isEmpty()) {
            serviceStatus.getErrorMessages().addAll(errorMessages);
        }

        // Return all applications that are using this service and are actually deployed
        List<MicoApplication> usingApplications = micoApplicationRepository.findAllByUsedService(micoService.getShortName(), micoService.getVersion());
        for (MicoApplication application : usingApplications) {
            if (micoKubernetesClient.isApplicationDeployed(application)) {
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

    /**
     * Get the status information for all {@link MicoServiceInterface MicoServiceInterfaces} of the {@link
     * MicoService}.
     *
     * @param micoService   is the {@link MicoService} for which the status information of the MicoServiceInterfaces is
     *                      requested.
     * @param errorMessages is the list of error messages, which is empty if no error occurs.
     * @return a list of {@link MicoServiceInterfaceStatusResponseDTO}, one DTO per MicoServiceInterface.
     */
    public List<MicoServiceInterfaceStatusResponseDTO> getServiceInterfaceStatus(@NotNull MicoService micoService,
                                                                                 @NotNull List<MicoMessageResponseDTO> errorMessages) {
        List<MicoServiceInterfaceStatusResponseDTO> interfacesInformation = new ArrayList<>();
        for (MicoServiceInterface serviceInterface : micoService.getServiceInterfaces()) {
            String serviceInterfaceName = serviceInterface.getServiceInterfaceName();
            try {
                MicoServiceInterfaceStatusResponseDTO interfaceStatusResponseDTO = getPublicIpOfKubernetesService(micoService, serviceInterfaceName);
                interfacesInformation.add(interfaceStatusResponseDTO);
            } catch (ResponseStatusException e) {
                interfacesInformation.add(new MicoServiceInterfaceStatusResponseDTO().setName(serviceInterfaceName));
                errorMessages.add(new MicoMessageResponseDTO(MicoMessage.error(e.getMessage())));
            }
        }
        return interfacesInformation;
    }
    
    /**
     * Get the public IP of a {@link MicoServiceInterface} by providing the corresponding Kubernetes {@link Service}.
     *
     * @param micoService          is the {@link MicoService}, that has a {@link MicoServiceInterface}, which is
     *                             deployed on Kubernetes.
     * @param serviceInterfaceName is the MicoServiceInterface, that is deployed as Kubernetes service .
     * @return the  public IP of the provided Kubernetes Service
     */
    public MicoServiceInterfaceStatusResponseDTO getPublicIpOfKubernetesService(MicoService micoService, String serviceInterfaceName) {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), serviceInterfaceName);
        if (!serviceInterfaceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Service interface '" + serviceInterfaceName + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' was not found!");
        }
        Optional<Service> kubernetesServiceOptional;
        try {
            kubernetesServiceOptional = micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, serviceInterfaceName);
        } catch (KubernetesResourceException e) {
            log.error("Error occur while retrieving Kubernetes service of MicoServiceInterface '{}' of MicoService '{}' in version '{}'. Caused by: {}",
                serviceInterfaceName, micoService.getShortName(), micoService.getVersion(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error occur while retrieving Kubernetes service of MicoServiceInterface '" + serviceInterfaceName +
                    "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "'!");
        }
        if (!kubernetesServiceOptional.isPresent()) {
            log.warn("There is no Kubernetes service deployed for MicoServiceInterface with name '{}' of MicoService '{}' in version '{}'.",
                serviceInterfaceName, micoService.getShortName(), micoService.getVersion());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No deployed service interface '" + serviceInterfaceName + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' was found!");
        }
        Service kubernetesService = kubernetesServiceOptional.get();
        LoadBalancerStatus loadBalancerStatus = kubernetesService.getStatus().getLoadBalancer();
        if (loadBalancerStatus != null) {
            List<LoadBalancerIngress> ingressList = loadBalancerStatus.getIngress();
            if (ingressList != null && ingressList.size() == 1) {
                log.info("Service interface with name '{}' of MicoService '{}' in version '{}' has external IP: {}",
                    serviceInterfaceName, micoService.getShortName(), micoService.getVersion(), ingressList.get(0).getIp());
                return new MicoServiceInterfaceStatusResponseDTO().setName(serviceInterfaceName).setExternalIp(ingressList.get(0).getIp());
            } else if (ingressList != null && ingressList.size() > 1) {
                log.warn("There are " + ingressList.size() + " IP addresses for the MicoServiceInterface " + serviceInterfaceName + ". Only one IP address is returned.");
                return new MicoServiceInterfaceStatusResponseDTO().setName(serviceInterfaceName).setExternalIp(ingressList.get(0).getIp());
            } else {
                log.error("There is no Kubernetes service for the interface '{}' of MicoService '{}' in version '{}'.",
                    serviceInterfaceName, micoService.getShortName(), micoService.getVersion());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no Kubernetes service for the interface '" +
                    serviceInterfaceName + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "'.");
            }
        } else {
            log.error("There is no Load Balancer service for the Kubernetes service of the MicoServiceInterface '{}'.", serviceInterfaceName);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no Load Balancer service for the Kubernetes service of the MicoServiceInterface '" +
                serviceInterfaceName + "'.");
        }
    }

    /**
     * Get information and metrics for a {@link Pod} representing an instance of a {@link MicoService}.
     *
     * @param pod is a {@link Pod} of Kubernetes.
     * @return a {@link KubernetesPodInformationResponseDTO} which has node name, pod name, phase, host ip, memory
     * usage, and CPU load as status information.
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
