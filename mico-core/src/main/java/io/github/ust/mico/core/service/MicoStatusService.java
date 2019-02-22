package io.github.ust.mico.core.service;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInfoDTO;
import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.dto.MicoServiceStatusDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.PrometheusResponse;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.PrometheusRequestFailedException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
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
 * Provide functionality to retrieve status information for a {@link MicoApplication} or a particular {@link MicoService}
 */
@Slf4j
@Component
public class MicoStatusService {

    private final PrometheusConfig prometheusConfig;
    private final MicoKubernetesClient micoKubernetesClient;
    private final RestTemplate restTemplate;
    private final MicoServiceRepository serviceRepository;

    private static final int MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT = 1;
    private static final String PROMETHEUS_QUERY_FOR_MEMORY_USAGE = "sum(container_memory_working_set_bytes{pod_name=\"%s\",container_name=\"\"})";
    private static final String PROMETHEUS_QUERY_FOR_CPU_USAGE = "sum(container_cpu_load_average_10s{pod_name=\"%s\"})";
    private static final String PROMETHEUS_QUERY_PARAMETER_NAME = "query";

    @Autowired
    public MicoStatusService(PrometheusConfig prometheusConfig, MicoKubernetesClient micoKubernetesClient,
            RestTemplate restTemplate, MicoServiceRepository serviceRepository) {
        this.prometheusConfig = prometheusConfig;
        this.micoKubernetesClient = micoKubernetesClient;
        this.restTemplate = restTemplate;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Get status information for a {@link MicoApplication}
     * @param micoApplication
     * @return {@link MicoApplicationStatusDTO} containing a list of {@link MicoServiceStatusDTO} for status information of a single {@link MicoService}
     */
    public MicoApplicationStatusDTO getApplicationStatus(MicoApplication micoApplication) {
        MicoApplicationStatusDTO applicationStatus = new MicoApplicationStatusDTO();
        List<MicoService> micoServices = serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
        for (MicoService micoService: micoServices) {
            MicoServiceStatusDTO micoServiceStatus = getServiceStatus(micoService);
            applicationStatus.getServiceStatus().add(micoServiceStatus);
        }
        return applicationStatus;
    }

    /**
     * Get status information for a single {@link MicoService}: # available replicas, # requested replicas, pod metrics (cpu load, memory load)
     * @param micoService is a {@link MicoService}
     * @return {@link MicoServiceStatusDTO} which contains status information for a specific {@link MicoService}
     */
    public MicoServiceStatusDTO getServiceStatus(MicoService micoService) {
        Optional<Deployment> deploymentOptional = null;
        try {
            deploymentOptional = micoKubernetesClient.getDeploymentOfMicoService(micoService);
        } catch (KubernetesResourceException e) {
            log.error("Error while retrieving Kubernetes deployment of MicoService '{}' '{}'. Continue with next one. Caused by: {}",
                micoService.getShortName(), micoService.getVersion(), e.getMessage());
        }
        if (!deploymentOptional.isPresent()) {
            log.warn("There is no deployment of the MicoService '{}' '{}'. Continue with next one.",
                micoService.getShortName(), micoService.getVersion());
        }

        Deployment deployment = deploymentOptional.get();
        MicoServiceStatusDTO serviceStatus = new MicoServiceStatusDTO();
        serviceStatus.setName(micoService.getName());
        serviceStatus.setShortName(micoService.getShortName());
        serviceStatus.setVersion(micoService.getVersion());
        serviceStatus.setAvailableReplicas(deployment.getStatus().getAvailableReplicas());
        serviceStatus.setRequestedReplicas(deployment.getSpec().getReplicas());

        // Get status information for service interfaces of a serivce
        List<MicoServiceInterfaceDTO> micoServiceInterfaceDTOList = getServiceInterfaceStatus(micoService);
        serviceStatus.setInterfacesInformation(micoServiceInterfaceDTOList);

        // Get status information for all pods of a service
        List<Pod> podList = micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(micoService);
        List<KubernetesPodInfoDTO> podInfos = new LinkedList<>();
        for (Pod pod : podList) {
            KubernetesPodInfoDTO podInfo = getUiPodInfo(pod);
            podInfos.add(podInfo);
        }
        serviceStatus.setPodInfo(podInfos);
        return serviceStatus;
    }

    /**
     * Get status information for all {@link MicoServiceInterface} of a {@link MicoService}: service name,
     * @param micoService is a {@link MicoService}
     * @return a list of {@link MicoServiceInterfaceDTO}, each item contains status information for a {@link MicoServiceInterface}
     * TODO add externalIP information
     */
    public List<MicoServiceInterfaceDTO> getServiceInterfaceStatus(MicoService micoService) {
        List<MicoServiceInterfaceDTO> interfacesInformation = new LinkedList<>();
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
     * Get information and metrics for a {@link Pod} representing an instance of a {@link MicoService}
     * @param pod is a {@link Pod} of Kubernetes
     * @return a {@link KubernetesPodInfoDTO} which has node name, pod name, phase, host ip, memory usage, and cpu load as status information
     */
    private KubernetesPodInfoDTO getUiPodInfo(Pod pod) {
        String nodeName = pod.getSpec().getNodeName();
        String podName = pod.getMetadata().getName();
        String phase = pod.getStatus().getPhase();
        String hostIp = pod.getStatus().getHostIP();
        int restarts = 0;
        for (ContainerStatus containterStatus: pod.getStatus().getContainerStatuses()) {{
            restarts += containterStatus.getRestartCount();
        }}
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
        return new KubernetesPodInfoDTO(podName, phase, hostIp, nodeName, restarts, age, podMetrics);
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
