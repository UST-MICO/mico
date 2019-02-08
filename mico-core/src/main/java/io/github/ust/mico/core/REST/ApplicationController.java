package io.github.ust.mico.core.REST;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import io.github.ust.mico.core.MicoKubernetesConfig;
import io.github.ust.mico.core.PrometheusConfig;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

@RestController
@RequestMapping(value = ApplicationController.PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class ApplicationController {

    public static final String PATH_SERVICES = "services";
    public static final String PATH_APPLICATIONS = "/applications";
    private Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";

    /**
     * Used by deployments
     */
    public static final String LABEL_APP_KEY = "app";
    public static final String LABEL_VERSION_KEY = "version";


    private static final int MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT = 1;
    private static final String PROMETHEUS_QUERY_FOR_MEMORY_USAGE = "sum(container_memory_working_set_bytes{pod_name=\"%s\",container_name=\"\"})";
    private static final String PROMETHEUS_QUERY_FOR_CPU_USAGE = "sum(container_cpu_load_average_10s{pod_name=\"%s\"})";
    private static final String PROMETHEUS_QUERY_PARAMETER_NAME = "query";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private PrometheusConfig prometheusConfig;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    MicoKubernetesConfig micoKubernetesConfig;

    @Autowired
    ClusterAwarenessFabric8 clusterAwarenessFabric8;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping()
    public ResponseEntity<Resources<Resource<MicoApplication>>> getAllApplications() {
        List<MicoApplication> allApplications = applicationRepository.findAll(3);
        List<Resource<MicoApplication>> applicationResources = getApplicationResourceList(allApplications);

        return ResponseEntity.ok(
            new Resources<>(applicationResources,
                linkTo(methodOn(ApplicationController.class).getAllApplications()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> getApplicationByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
        return applicationOptional.map(application -> new Resource<>(application, getApplicationLinks(application)))
            .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' links not found!"));
    }

    private List<Resource<MicoApplication>> getApplicationResourceList(List<MicoApplication> applications) {
        return applications.stream().map(application -> new Resource<>(application, getApplicationLinks(application)))
            .collect(Collectors.toList());
    }

    private Iterable<Link> getApplicationLinks(MicoApplication application) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationController.class).getApplicationByShortNameAndVersion(application.getShortName(), application.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ApplicationController.class).getAllApplications()).withRel("applications"));
        return links;
    }

    @PostMapping
    public ResponseEntity<Resource<MicoApplication>> createApplication(@RequestBody MicoApplication newApplication) {
        Optional<MicoApplication> applicationOptional = applicationRepository.
            findByShortNameAndVersion(newApplication.getShortName(), newApplication.getVersion());
        if (applicationOptional.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        List<MicoService> oldServices = newApplication.getServices();
        newApplication.getServices().clear();

        // specifically load all services from db
        for (MicoService service : oldServices) {
            Optional<MicoService> dbService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion());
            if (!dbService.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the provided Services was not found!");
            }
            newApplication.getServices().add(dbService.get());
        }

        // TODO update deploy info here if neccessary

        MicoApplication savedApplication = applicationRepository.save(newApplication);

        return ResponseEntity
            .created(linkTo(methodOn(ApplicationController.class)
                .getApplicationByShortNameAndVersion(savedApplication.getShortName(), savedApplication.getVersion())).toUri())
            .body(new Resource<>(savedApplication, getApplicationLinks(savedApplication)));
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> updateApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                       @RequestBody MicoApplication application) {
        if (!application.getShortName().equals(shortName) || !application.getVersion().equals(version)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }

        application.setId(applicationOptional.get().getId());
        MicoApplication updatedApplication = applicationRepository.save(application);

        return ResponseEntity.ok(new Resource<>(updatedApplication, linkTo(methodOn(ApplicationController.class).updateApplication(shortName, version, application)).withSelfRel()));
    }

    @DeleteMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    public ResponseEntity<Resource<MicoApplication>> deleteApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!applicationOptional.isPresent()) {
            // application already deleted
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        applicationOptional.map(application -> {
            if (application.getDeploymentInfo() == null || application.getDeploymentInfo().getServiceDeploymentInfos() != null) {
                return application; // TODO better deployment detection
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application is currently deployed!");
        }).map(application -> {
            application.getServices().clear();
            return application;
        }).map(application -> {
            applicationRepository.save(application);
            return application;
        }).map(application -> {
            applicationRepository.delete(application);
            return application;
        });
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + "/deploymentInformation")
    public ResponseEntity<Resource<UiDeploymentInformation>> getApplicationDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (micoApplicationOptional.isPresent()) {
            KubernetesClient kubernetesClient = clusterAwarenessFabric8.getClient();
            HashMap<String, String> labels = new HashMap<>();
            labels.put(LABEL_APP_KEY, shortName);
            labels.put(LABEL_VERSION_KEY, version);
            String namespace = micoKubernetesConfig.getNamespaceMicoWorkspace();
            DeploymentList deploymentList = kubernetesClient.apps().deployments().inNamespace(namespace).withLabels(labels).list();
            if (deploymentList.getItems().size() == 1) {
                Deployment deployment = deploymentList.getItems().get(0);
                UiDeploymentInformation uiDeploymentInformation = new UiDeploymentInformation();
                int requestedReplicas = deployment.getSpec().getReplicas();
                int availableReplicas = deployment.getStatus().getAvailableReplicas();
                uiDeploymentInformation.setAvailableReplicas(availableReplicas);
                uiDeploymentInformation.setRequestedReplicas(requestedReplicas);

                ServiceList serviceList = kubernetesClient.services().inNamespace(namespace).withLabels(labels).list(); //MicoServiceInterface maps to Service
                List<UiExternalMicoInterfaceInformation> interfacesInformation = new LinkedList<>();
                if (serviceList.getItems().size() >= MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT) {
                    for (Service service : serviceList.getItems()) {
                        String name = service.getMetadata().getName();
                        UiExternalMicoInterfaceInformation interfaceInformation = UiExternalMicoInterfaceInformation.builder()
                            .name(name).build();
                        interfacesInformation.add(interfaceInformation);
                    }
                    uiDeploymentInformation.setInterfacesInformation(interfacesInformation);
                    PodList podList = kubernetesClient.pods().inNamespace(namespace).withLabels(labels).list();
                    List<UiPodInfo> podInfos = new LinkedList<>();
                    for (Pod pod : podList.getItems()) {
                        UiPodInfo uiPodInfo = getUiPodInfo(pod);
                        podInfos.add(uiPodInfo);
                    }
                    uiDeploymentInformation.setPodInfo(podInfos);
                    return ResponseEntity.ok(new Resource<>(uiDeploymentInformation));

                } else {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "There are not at least " + MINIMAL_EXTERNAL_MICO_INTERFACE_COUNT + " service interface");
                }
            } else {
                if (deploymentList.getItems().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "There are zero deployments of the application '" + shortName + "' in version '" + version + "'");
                } else {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "There are more than one deployments of the application '" + shortName + "' in version '" + version + "'");
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application '" + shortName + "' '" + version + "' was not found!");
        }
    }

    private UiPodInfo getUiPodInfo(Pod pod) {
        String nodeName = pod.getSpec().getNodeName();
        String podName = pod.getMetadata().getName();
        String phase = pod.getStatus().getPhase();
        String hostIp = pod.getStatus().getHostIP();
        int memoryUsage = -1;
        int cpuLoad = -1;
        UiPodMetrics uiPodMetrics = new UiPodMetrics();
        try {
            memoryUsage = getMemoryUsageForPod(podName);
            cpuLoad = getCpuLoadForPod(podName);
            uiPodMetrics.setAvailable(true);
        } catch (PrometheusRequestFailedException | ResourceAccessException e) {
            uiPodMetrics.setAvailable(false);
            logger.error(e.getMessage(), e);
        }
        uiPodMetrics.setMemoryUsage(memoryUsage);
        uiPodMetrics.setCpuLoad(cpuLoad);
        return UiPodInfo.builder().nodeName(nodeName).podName(podName)
            .phase(phase).hostIp(hostIp).metrics(uiPodMetrics).build();
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
        ResponseEntity<PrometheusResponse> response
            = restTemplate.getForEntity(prometheusUri, PrometheusResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            PrometheusResponse prometheusMemoryResponse = response.getBody();
            if (prometheusMemoryResponse.wasSuccessful()) {
                return prometheusMemoryResponse.getValue();
            } else {
                throw new PrometheusRequestFailedException("The status of the prometheus response was " + prometheusMemoryResponse.getStatus(), response.getStatusCode(), prometheusMemoryResponse.getStatus());
            }
        } else {
            throw new PrometheusRequestFailedException("The http status code was not 2xx", response.getStatusCode(), null);
        }
    }

    private URI getPrometheusUri(String query, String podName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(prometheusConfig.getUri());
        uriBuilder.queryParam(PROMETHEUS_QUERY_PARAMETER_NAME, String.format(query, podName));
        return uriBuilder.build().toUri();
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}")
    public ResponseEntity<Resources<Resource<MicoApplication>>> getApplicationsByShortName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);

        List<Resource<MicoApplication>> applicationResourceList = getApplicationResourceList(micoApplicationList);

        return ResponseEntity.ok(
            new Resources<>(applicationResourceList,
                linkTo(methodOn(ApplicationController.class).getApplicationsByShortName(shortName)).withSelfRel()));
    }

    @PostMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Void> addServiceToApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                        @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion,
                                                        @RequestBody MicoService serviceFromBody) {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(serviceFromBody.getShortName(), serviceFromBody.getVersion());
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(applicationShortName, applicationVersion);
        if (serviceOptional.isPresent() && applicationOptional.isPresent()) {
            MicoService service = serviceOptional.get();
            MicoApplication application = applicationOptional.get();
            if (!application.getServices().contains(service)) {
                application.getServices().add(service);
                applicationRepository.save(application);
            }
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no such application/service");
        }
    }

    /**
     * Returns a list of services associated with the mico application specified by the parameters.
     * @param applicationShortName
     * @param applicationVersion
     * @return
     */
    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_SERVICES)
    public ResponseEntity<Resources<Resource<MicoService>>> getMicoServicesFromApplication(@PathVariable(PATH_VARIABLE_SHORT_NAME) String applicationShortName,
                                                                                           @PathVariable(PATH_VARIABLE_VERSION) String applicationVersion) {
        Optional<MicoApplication> applicationOptional = applicationRepository.findByShortNameAndVersion(applicationShortName, applicationVersion);
        if (applicationOptional.isPresent()) {
            MicoApplication micoApplication = applicationOptional.get();
            List<MicoService> micoServices = micoApplication.getServices();
            List<Resource<MicoService>> micoServicesWithLinks = ServiceController.getServiceResourcesList(micoServices);
            return ResponseEntity.ok(
                new Resources<>(micoServicesWithLinks,
                    linkTo(methodOn(ApplicationController.class).getMicoServicesFromApplication(applicationShortName, applicationVersion)).withSelfRel()));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no application with the name " + applicationShortName + " and the version " + applicationVersion);
        }
    }
}
