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

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO;
import io.github.ust.mico.core.dto.response.internal.PrometheusResponseDTO;
import io.github.ust.mico.core.dto.response.status.*;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.PrometheusRequestFailedException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;

/**
 * Provides functionality to retrieve status information for a {@link MicoApplication} or a particular {@link
 * MicoService}.
 */
@Slf4j
@Component
public class MicoStatusService {

    private static final String POD_PHASE_RUNNING = "Running";
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
                             RestTemplate restTemplate, MicoServiceRepository serviceRepository,
                             MicoApplicationRepository micoApplicationRepository) {
        this.prometheusConfig = prometheusConfig;
        this.micoKubernetesClient = micoKubernetesClient;
        this.restTemplate = restTemplate;
        this.serviceRepository = serviceRepository;
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
        for (MicoServiceDeploymentInfo kfConnectorDeploymentInfo : micoApplication.getKafkaFaasConnectorDeploymentInfos()) {
            MicoServiceStatusResponseDTO kfConnectorServiceStatus = getServiceStatus(kfConnectorDeploymentInfo.getService());
            podCount += kfConnectorServiceStatus.getPodsInformation().size();
            requestedReplicasCount += kfConnectorServiceStatus.getRequestedReplicas();
            availableReplicasCount += kfConnectorServiceStatus.getAvailableReplicas();
            applicationStatus.getServiceStatuses().add(kfConnectorServiceStatus);
        }

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
            .setTotalNumberOfMicoServices(micoServices.size() + micoApplication.getKafkaFaasConnectorDeploymentInfos().size())
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
        MicoServiceStatusResponseDTO serviceStatus = new MicoServiceStatusResponseDTO()
            .setShortName(micoService.getShortName())
            .setVersion(micoService.getVersion())
            .setName(micoService.getName());

        String message;
        List<Deployment> deployments = micoKubernetesClient.getDeploymentsOfMicoService(micoService);
        if (deployments.size() > 1) {
            throw new IllegalStateException("Currently MICO doesn't support multiple instance deployments.");
        } else if (deployments.size() == 1) {
            Deployment deployment = deployments.get(0);
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
            int sumRunningPods = 0;
            int sumCpuLoadOnNode = 0;
            int sumMemoryUsageOnNode = 0;
            for (Pod pod : podsPerNode.get(nodeName)) {
                KubernetesPodInformationResponseDTO podInformation = getPodInformation(pod);
                podInfos.add(podInformation);
                String phase = pod.getStatus().getPhase();
                if (phase.equals(POD_PHASE_RUNNING)) {
                    sumCpuLoadOnNode += podInformation.getMetrics().getCpuLoad();
                    sumMemoryUsageOnNode += podInformation.getMetrics().getMemoryUsage();
                    sumRunningPods++;
                } else {
                    log.debug("Pod '{}' on node '{}' is not running. It is in phase '{}'.",
                        pod.getMetadata().getName(), nodeName, phase);
                }
            }
            int averageCpuLoad = 0;
            int averageMemoryUsage = 0;
            if (sumRunningPods > 0) {
                averageCpuLoad = sumCpuLoadOnNode / sumRunningPods;
                averageMemoryUsage = sumMemoryUsageOnNode / sumRunningPods;
            } else {
                message = "There are no Pods running on node '" + nodeName + "' for MICO service '"
                    + micoService.getShortName() + "' '" + micoService.getVersion() + "'.";
                log.warn(message);
                MicoMessage warning = MicoMessage.warning(message);
                serviceStatus.getErrorMessages().add(new MicoMessageResponseDTO(warning));
            }
            nodeMetrics.add(new KubernetesNodeMetricsResponseDTO()
                .setNodeName(nodeName)
                .setAverageCpuLoad(averageCpuLoad)
                .setAverageMemoryUsage(averageMemoryUsage));
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
                MicoServiceInterfaceStatusResponseDTO interfaceStatusResponseDTO = getPublicIpOfKubernetesService(micoService, serviceInterface);
                interfacesInformation.add(interfaceStatusResponseDTO);
            } catch (KubernetesResourceException e) {
                interfacesInformation.add(new MicoServiceInterfaceStatusResponseDTO().setName(serviceInterfaceName));
                errorMessages.add(new MicoMessageResponseDTO(MicoMessage.error(e.getMessage())));
            }
        }
        return interfacesInformation;
    }

    /**
     * Get the public IP of a {@link MicoServiceInterface} by providing the corresponding Kubernetes {@link Service}.
     *
     * @param micoService      is the {@link MicoService}, that has a {@link MicoServiceInterface}, which is
     *                         deployed on Kubernetes
     * @param serviceInterface the {@link MicoServiceInterface}, that is deployed as a Kubernetes service
     * @return the  public IP of the provided Kubernetes Service
     * @throws KubernetesResourceException if it's not possible to get the Kubernetes service
     */
    public MicoServiceInterfaceStatusResponseDTO getPublicIpOfKubernetesService(MicoService micoService, MicoServiceInterface serviceInterface) throws KubernetesResourceException {
        String serviceInterfaceName = serviceInterface.getServiceInterfaceName();

        Optional<Service> kubernetesServiceOptional = micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, serviceInterfaceName);
        if (!kubernetesServiceOptional.isPresent()) {
            log.warn("There is no Kubernetes service deployed for MicoServiceInterface with name '{}' of MicoService '{}' in version '{}'.",
                serviceInterfaceName, micoService.getShortName(), micoService.getVersion());
            throw new KubernetesResourceException("No deployed service interface '" + serviceInterfaceName
                + "' of MicoService '" + micoService.getShortName() + "' '" + micoService.getVersion() + "' was found!");
        }
        Service kubernetesService = kubernetesServiceOptional.get();

        Optional<String> ip = micoKubernetesClient.getPublicIpOfKubernetesService(kubernetesService.getMetadata().getName(), kubernetesService.getMetadata().getNamespace());
        List<Integer> ports = micoKubernetesClient.getPublicPortsOfKubernetesService(kubernetesService.getMetadata().getName(), kubernetesService.getMetadata().getNamespace());
        int port;
        if (ports.size() >= 1) {
            port = ports.get(0);
            if (ports.size() > 1) {
                log.warn("There are {} ports defined for interface '{}' of MicoService '{}' '{}'. Using first port {}.",
                    ports.size(), serviceInterface, micoService.getShortName(), micoService.getVersion(), port);
            }
        } else {
            port = 80;
            log.warn("There are no ports defined for interface '{}' of MicoService '{}' '{}'. Using default port {}.",
                serviceInterfaceName, micoService.getShortName(), micoService.getVersion(), port);
        }

        MicoServiceInterfaceStatusResponseDTO responseDTO = new MicoServiceInterfaceStatusResponseDTO()
            .setName(serviceInterfaceName)
            .setPort(port);
        if (ip.isPresent()) {
            responseDTO.setExternalIpIsAvailable(true);
            responseDTO.setExternalIp(ip.get());
        }
        return responseDTO;
    }

    /**
     * Get information and metrics for a {@link Pod} representing an instance of a {@link MicoService}.
     *
     * @param pod is a {@link Pod} of Kubernetes.
     * @return a {@link KubernetesPodInformationResponseDTO} which has node name, pod name, phase, host ip, memory
     * usage, and CPU load as status information.
     */
    private KubernetesPodInformationResponseDTO getPodInformation(Pod pod) {
        String nodeName = pod.getSpec().getNodeName();
        String podName = pod.getMetadata().getName();
        String phase = pod.getStatus().getPhase();
        String hostIp = pod.getStatus().getHostIP();
        int restarts = 0;
        for (ContainerStatus containerStatus : pod.getStatus().getContainerStatuses()) {
            restarts += containerStatus.getRestartCount();
        }
        String age = pod.getStatus().getStartTime();

        KubernetesPodInformationResponseDTO kubernetesPodInformationResponseDTO = new KubernetesPodInformationResponseDTO()
            .setNodeName(nodeName)
            .setPodName(podName)
            .setPhase(phase)
            .setHostIp(hostIp)
            .setRestarts(restarts)
            .setStartTime(age);

        // Request values from Prometheus only if the pod phase is "Running"
        if (phase.equals(POD_PHASE_RUNNING)) {
            int memoryUsage = 0;
            int cpuLoad = 0;
            try {
                memoryUsage = getMemoryUsageForPod(podName);
                cpuLoad = getCpuLoadForPod(podName);
            } catch (PrometheusRequestFailedException | ResourceAccessException e) {
                log.error(e.getMessage(), e);
            }
            kubernetesPodInformationResponseDTO.setMetrics(new KubernetesPodMetricsResponseDTO()
                .setMemoryUsage(memoryUsage)
                .setCpuLoad(cpuLoad));
        }
        return kubernetesPodInformationResponseDTO;
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
