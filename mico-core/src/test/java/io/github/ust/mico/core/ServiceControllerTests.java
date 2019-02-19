package io.github.ust.mico.core;

import static io.github.ust.mico.core.JsonPathBuilder.HREF;
import static io.github.ust.mico.core.JsonPathBuilder.LINKS;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.JsonPathBuilder.SELF;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;
import static io.github.ust.mico.core.TestConstants.BASE_URL;
import static io.github.ust.mico.core.TestConstants.DEPENDEES_SUBPATH;
import static io.github.ust.mico.core.TestConstants.DEPENDERS_SUBPATH;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION_1;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION_1_MATCHER;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION_2;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION_2_MATCHER;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION_3;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION_3_MATCHER;
import static io.github.ust.mico.core.TestConstants.ID;
import static io.github.ust.mico.core.TestConstants.ID_1;
import static io.github.ust.mico.core.TestConstants.SERVICES_PATH;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1_MATCHER;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_2;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_2_MATCHER;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_3;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_3_MATCHER;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_MATCHER;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_1;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_1_MATCHER;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_2;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_2_MATCHER;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_3;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_3_MATCHER;
import static io.github.ust.mico.core.TestConstants.VERSION_MATCHER;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInfoDTO;
import io.github.ust.mico.core.dto.MicoApplicationDeploymentInformationDTO;
import io.github.ust.mico.core.dto.MicoServiceDeploymentInformationDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.PrometheusResponse;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ust.mico.core.web.ServiceController;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ServiceControllerTests {

    private static final String BASE_PATH = "/services";

    private static final String JSON_PATH_LINKS_SECTION = buildPath(ROOT, LINKS);
    private static final String SELF_HREF = buildPath(JSON_PATH_LINKS_SECTION, SELF, HREF);
    private static final String SERVICES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "services", HREF);
    private static final String EMBEDDED = buildPath(ROOT, JsonPathBuilder.EMBEDDED);
    private static final String SERVICE_LIST = buildPath(EMBEDDED, "micoServiceList");
    private static final String ID_PATH = buildPath(ROOT, "id");
    private static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    private static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    private static final String VERSION_PATH = buildPath(ROOT, "version");

    private static final String REQUESTED_REPLICAS = buildPath(ROOT, "requestedReplicas");
    private static final String AVAILABLE_REPLICAS = buildPath(ROOT, "availableReplicas");
    private static final String INTERFACES_INFORMATION = buildPath(ROOT, "interfacesInformation");
    private static final String INTERFACES_INFORMATION_NAME = buildPath(ROOT, "interfacesInformation[0].name");
    private static final String POD_INFO = buildPath(ROOT, "podInfo");
    private static final String POD_INFO_POD_NAME_1 = buildPath(ROOT, "podInfo[0].podName");
    private static final String POD_INFO_PHASE_1 = buildPath(ROOT, "podInfo[0].phase");
    private static final String POD_INFO_NODE_NAME_1 = buildPath(ROOT, "podInfo[0].nodeName");
    private static final String POD_INFO_METRICS_MEMORY_USAGE_1 = buildPath(ROOT, "podInfo[0].metrics.memoryUsage");
    private static final String POD_INFO_METRICS_CPU_LOAD_1 = buildPath(ROOT, "podInfo[0].metrics.cpuLoad");
    private static final String POD_INFO_METRICS_AVAILABLE_1 = buildPath(ROOT, "podInfo[0].metrics.available");
    private static final String POD_INFO_POD_NAME_2 = buildPath(ROOT, "podInfo[1].podName");
    private static final String POD_INFO_PHASE_2 = buildPath(ROOT, "podInfo[1].phase");
    private static final String POD_INFO_NODE_NAME_2 = buildPath(ROOT, "podInfo[1].nodeName");
    private static final String POD_INFO_METRICS_MEMORY_USAGE_2 = buildPath(ROOT, "podInfo[1].metrics.memoryUsage");
    private static final String POD_INFO_METRICS_CPU_LOAD_2 = buildPath(ROOT, "podInfo[1].metrics.cpuLoad");
    private static final String POD_INFO_METRICS_AVAILABLE_2 = buildPath(ROOT, "podInfo[1].metrics.available");

    //TODO: Use these variables inside the tests instead of the local variables

    @Value("${cors-policy.allowed-origins}")
    String[] allowedOrigins;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    MicoStatusService micoStatusService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getStatusOfService() throws Exception {
        MicoService micoService = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1);

        given(serviceRepository.findByShortNameAndVersion(ArgumentMatchers.anyString(), ArgumentMatchers.any())).willReturn(Optional.of(micoService));

        String nodeName = "testNode";
        String podPhase = "Running";
        String hostIp = "192.168.0.0";
        String serviceName = "service1";
        String podName1 = "pod1";
        String podName2 = "pod2";
        int availableReplicas = 1;
        int requestedReplicas = 2;
        int memoryUsagePod1 = 50;
        int cpuLoadPod1 = 10;
        int memoryUsagePod2 = 70;
        int cpuLoadPod2 = 40;

        MicoServiceDeploymentInformationDTO micoServiceDeploymentInformation = new MicoServiceDeploymentInformationDTO();

        KubernetesPodInfoDTO kubernetesPodInfo1 = new KubernetesPodInfoDTO();
        kubernetesPodInfo1
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName1)
            .setMetrics(new KuberenetesPodMetricsDTO()
                .setAvailable(false)
                .setCpuLoad(10)
                .setMemoryUsage(50));
        KubernetesPodInfoDTO kubernetesPodInfo2 = new KubernetesPodInfoDTO();
        kubernetesPodInfo2
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName2)
            .setMetrics(new KuberenetesPodMetricsDTO()
                .setAvailable(true)
                .setCpuLoad(40)
                .setMemoryUsage(70));

        micoServiceDeploymentInformation
            .setAvailableReplicas(availableReplicas)
            .setRequestedReplicas(requestedReplicas)
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(serviceName)))
            .setPodInfo(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2));

        given(micoStatusService.getServiceStatus(any(MicoService.class))).willReturn(micoServiceDeploymentInformation);

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/status"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(REQUESTED_REPLICAS, is(requestedReplicas)))
            .andExpect(jsonPath(AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(INTERFACES_INFORMATION, hasSize(1)))
            .andExpect(jsonPath(INTERFACES_INFORMATION_NAME, is(serviceName)))
            .andExpect(jsonPath(POD_INFO, hasSize(2)))
            .andExpect(jsonPath(POD_INFO_POD_NAME_1, is(podName1)))
            .andExpect(jsonPath(POD_INFO_PHASE_1, is(podPhase)))
            .andExpect(jsonPath(POD_INFO_NODE_NAME_1, is(nodeName)))
            .andExpect(jsonPath(POD_INFO_METRICS_MEMORY_USAGE_1, is(memoryUsagePod1)))
            .andExpect(jsonPath(POD_INFO_METRICS_CPU_LOAD_1, is(cpuLoadPod1)))
            .andExpect(jsonPath(POD_INFO_METRICS_AVAILABLE_1, is(false)))
            .andExpect(jsonPath(POD_INFO_POD_NAME_2, is(podName2)))
            .andExpect(jsonPath(POD_INFO_PHASE_2, is(podPhase)))
            .andExpect(jsonPath(POD_INFO_NODE_NAME_2, is(nodeName)))
            .andExpect(jsonPath(POD_INFO_METRICS_MEMORY_USAGE_2, is(memoryUsagePod2)))
            .andExpect(jsonPath(POD_INFO_METRICS_CPU_LOAD_2, is(cpuLoadPod2)))
            .andExpect(jsonPath(POD_INFO_METRICS_AVAILABLE_2, is(true)));
    }

    @Test
    public void getCompleteServiceList() throws Exception {
        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(
            Arrays.asList(
                new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setDescription(DESCRIPTION_1),
                new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_2).setDescription(DESCRIPTION_2),
                new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_3).setDescription(DESCRIPTION_3)));

        mvc.perform(get("/services").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_1_MATCHER + " && " + VERSION_1_0_1_MATCHER + " && " + DESCRIPTION_1_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_2_MATCHER + " && " + VERSION_1_0_2_MATCHER + " && " + DESCRIPTION_2_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_3_MATCHER + " && " + VERSION_1_0_3_MATCHER + " && " + DESCRIPTION_3_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + SERVICES_PATH)))
            .andReturn();
    }

    @Test
    public void getServiceViaShortNameAndVersion() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION).setDescription(DESCRIPTION)));

        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(SHORT_NAME);
        urlPathBuilder.append("/");
        urlPathBuilder.append(VERSION);
        String urlPath = urlPathBuilder.toString();

        mvc.perform(get(urlPath).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(VERSION_PATH, is(VERSION)))
            .andExpect(jsonPath(DESCRIPTION_PATH, is(DESCRIPTION)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + urlPath)))
            .andExpect(jsonPath(SERVICES_HREF, is(BASE_URL + SERVICES_PATH)))
            .andReturn();
    }

    //TODO: Verify how to test an autogenerated id
    @Ignore
    @Test
    public void getServiceById() throws Exception {
        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(SHORT_NAME);
        urlPathBuilder.append("/");
        urlPathBuilder.append(VERSION);
        urlPathBuilder.append("/");
        urlPathBuilder.append(ID);
        String urlPath = urlPathBuilder.toString();

        given(serviceRepository.findById(ID_1))
            .willReturn(Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION).setDescription(DESCRIPTION)));

        mvc.perform(get(urlPath).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(ID_PATH, is(ID)))
            .andExpect(jsonPath(DESCRIPTION_PATH, is(DESCRIPTION)))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(VERSION_PATH, is(VERSION)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + urlPath)))
            .andExpect(jsonPath(SERVICES_HREF, is(BASE_URL + SERVICES_PATH)))
            .andReturn();
    }

    @Test
    public void createService() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(service))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void deleteAllServiceDependees() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(SHORT_NAME);
        urlPathBuilder.append("/");
        urlPathBuilder.append(VERSION);
        urlPathBuilder.append(DEPENDEES_SUBPATH);

        String urlPath = urlPathBuilder.toString();

        ResultActions resultDelete = mvc.perform(delete(urlPath)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultDelete.andExpect(status().isCreated());
    }

    @Test
    public void deleteSpecificServiceDependee() throws Exception {
        String shortName = SHORT_NAME_1;
        String version = VERSION_1_0_1;
        String description = DESCRIPTION_1;
        String shortNameToDelete = SHORT_NAME_2;
        String versionToDelete = VERSION_1_0_2;
        MicoService service = new MicoService().setShortName(shortName).setVersion(version).setDescription(description);
        MicoService serviceToDelete = new MicoService().setShortName(shortNameToDelete).setVersion(versionToDelete);

        given(serviceRepository.findByShortNameAndVersion(shortName, version)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);
        given(serviceRepository.findByShortNameAndVersion(shortNameToDelete, versionToDelete)).willReturn(Optional.of(serviceToDelete));

        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(shortName);
        urlPathBuilder.append("/");
        urlPathBuilder.append(version);
        urlPathBuilder.append(DEPENDEES_SUBPATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(shortNameToDelete);
        urlPathBuilder.append("/");
        urlPathBuilder.append(versionToDelete);

        String urlPath = urlPathBuilder.toString();

        ResultActions resultDelete = mvc.perform(delete(urlPath)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultDelete.andExpect(status().isCreated());
    }


    @Test
    public void corsPolicy() throws Exception {
        mvc.perform(get(SERVICES_PATH).accept(MediaTypes.HAL_JSON_VALUE)
            .header("Origin", (Object[]) allowedOrigins))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SELF_HREF, endsWith(SERVICES_PATH))).andReturn();
    }

    @Test
    public void corsPolicyNotAllowedOrigin() throws Exception {
        mvc.perform(get(SERVICES_PATH).accept(MediaTypes.HAL_JSON_VALUE)
            .header("Origin", "http://notAllowedOrigin.com"))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(content().string(is("Invalid CORS request")))
            .andReturn();
    }

    @Test
    public void getServiceDependers() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setDescription(DESCRIPTION_2);
        MicoService service3 = new MicoService()
            .setShortName(SHORT_NAME_3)
            .setVersion(VERSION_1_0_3)
            .setDescription(DESCRIPTION_3);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service1).setDependedService(service);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service2).setDependedService(service);
        MicoServiceDependency dependency3 = new MicoServiceDependency().setService(service3).setDependedService(service);

        service1.setDependencies(Collections.singletonList(dependency1));
        service2.setDependencies(Collections.singletonList(dependency2));
        service3.setDependencies(Collections.singletonList(dependency3));

        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(Arrays.asList(service, service1, service2, service3));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(SHORT_NAME);
        urlPathBuilder.append("/");
        urlPathBuilder.append(VERSION);
        urlPathBuilder.append(DEPENDERS_SUBPATH);

        String urlPath = urlPathBuilder.toString();

        ResultActions result = mvc.perform(get(urlPath)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_1_MATCHER + " && " + VERSION_1_0_1_MATCHER + " && " + DESCRIPTION_1_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_2_MATCHER + " && " + VERSION_1_0_2_MATCHER + " && " + DESCRIPTION_2_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_3_MATCHER + " && " + VERSION_1_0_3_MATCHER + " && " + DESCRIPTION_3_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + urlPath)));


        result.andExpect(status().isOk());

    }

    @Test
    public void updateService() throws Exception {
        String updatedDescription = "updated description.";
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);
        MicoService updatedService = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(updatedDescription);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(updatedService);

        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(SHORT_NAME);
        urlPathBuilder.append("/");
        urlPathBuilder.append(VERSION);

        String urlPath = urlPathBuilder.toString();

        ResultActions resultUpdate = mvc.perform(put(urlPath)
            .content(mapper.writeValueAsBytes(updatedService))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(ID_PATH, is(service.getId())))
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedDescription)))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(VERSION_PATH, is(VERSION)));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void deleteService() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        StringBuilder urlPathBuilder = new StringBuilder(300);
        urlPathBuilder.append(SERVICES_PATH);
        urlPathBuilder.append("/");
        urlPathBuilder.append(SHORT_NAME);
        urlPathBuilder.append("/");
        urlPathBuilder.append(VERSION);

        String urlPath = urlPathBuilder.toString();

        ResultActions resultDelete = mvc.perform(delete(urlPath)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultDelete.andExpect(status().isNoContent());
    }

    @Test
    public void getVersionsOfService() throws Exception {
        given(serviceRepository.findByShortName(SHORT_NAME)).willReturn(
            Arrays.asList(
                new MicoService().setShortName(SHORT_NAME).setVersion(VERSION),
                new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1),
                new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_2)));

        mvc.perform(get("/services/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_MATCHER + " && " + VERSION_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_MATCHER + " && " + VERSION_1_0_1_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_MATCHER + " && " + VERSION_1_0_2_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + SERVICES_PATH + "/" + SHORT_NAME)))
            .andReturn();
    }

    @Test
    public void createNewDependee() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);

        service.setDependencies(Collections.singletonList(dependency1));

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1)).willReturn(Optional.of(service1));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH)
            .content(mapper.writeValueAsBytes(dependency1))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void getDependees() throws Exception {
        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setDescription(DESCRIPTION_2);
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service).setDependedService(service2);
        service.setDependencies(Arrays.asList(dependency1, dependency2));

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1)).willReturn(Optional.of(service1));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_2, VERSION_1_0_2)).willReturn(Optional.of(service2));


        mvc.perform(get("/services/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(2)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_1_MATCHER + " && " + VERSION_1_0_1_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_2_MATCHER + " && " + VERSION_1_0_2_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH)))
            .andReturn();
    }

    @Test
    public void createServiceViaGitHubCrawler() {
        //TODO: Implementation
    }

}
