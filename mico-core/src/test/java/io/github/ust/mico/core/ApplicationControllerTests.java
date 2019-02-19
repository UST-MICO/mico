package io.github.ust.mico.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInfoDTO;
import io.github.ust.mico.core.dto.MicoApplicationDeploymentInformationDTO;
import io.github.ust.mico.core.dto.MicoServiceDeploymentInformationDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.PrometheusResponse;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.web.ApplicationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.web.ApplicationController.PATH_APPLICATIONS;
import static io.github.ust.mico.core.web.ApplicationController.PATH_SERVICES;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
@SuppressWarnings({"rawtypes", "unchecked"})
public class ApplicationControllerTests {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String APPLICATION_LIST = buildPath(EMBEDDED, "micoApplicationList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_PATH = buildPath(ROOT, "version");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String ID_PATH = buildPath(ROOT, "id");

    private static final String BASE_PATH = "/applications";

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private PrometheusConfig prometheusConfig;

    @MockBean
    private MicoStatusService micoStatusService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    CorsConfig corsConfig;

    @Test
    public void getAllApplications() throws Exception {
        given(applicationRepository.findAll(3)).willReturn(
            Arrays.asList(new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1),
                new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION),
                new MicoApplication().setShortName(SHORT_NAME_1).setVersion(VERSION)));

        mvc.perform(get("/applications").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(APPLICATION_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_MATCHER + "&& @.version=='" + VERSION_1_0_1 + "')]", hasSize(1)))
            .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_1_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/applications")))
            .andReturn();
    }

    @Test
    public void getApplicationByShortNameAndVersion() throws Exception {
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get("/applications/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(VERSION_PATH, is(VERSION)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/" + SHORT_NAME + "/" + VERSION)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "applications.href", is("http://localhost/applications")))
            .andReturn();
    }

    @Test
    public void getApplicationByShortName() throws Exception {
        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(Collections.singletonList(new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get("/applications/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/" + SHORT_NAME)))
            .andReturn();
    }

    @Test
    public void getApplicationByShortNameWithTrailingSlash() throws Exception {
        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(Collections.singletonList(new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get("/applications/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void createApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);

        prettyPrint(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void updateApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription("newDesc");

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(applicationRepository.save(any(MicoApplication.class))).willReturn(updatedApplication);

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(ID_PATH, is(application.getId())))
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void deleteApplication() throws Exception {
        MicoApplication app = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(app));

        ArgumentCaptor<MicoApplication> appCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(delete("/applications/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();

        verify(applicationRepository).delete(appCaptor.capture());
        assertEquals("Wrong short name.", app.getShortName(), appCaptor.getValue().getShortName());
        assertEquals("Wrong version.", app.getVersion(), appCaptor.getValue().getVersion());
    }

    @Test
    public void getStatusOfApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setServices(CollectionUtils.listOf(
                new MicoService()
                    .setShortName(SHORT_NAME)
                    .setVersion(VERSION)
                    .setServiceInterfaces(CollectionUtils.listOf(
                        new MicoServiceInterface()
                            .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                    ))
            ));
        String testNamespace = "TestNamespace";
        String nodeName = "testNode";
        String podPhase = "Running";
        String hostIp = "192.168.0.0";
        String deploymentName = "deployment1";
        String serviceName = "service1";
        String podName1 = "pod1";
        String podName2 = "pod2";

        int availableReplicas = 0;
        int replicas = 1;

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
//        given(micoKubernetesConfig.getNamespaceMicoWorkspace()).willReturn(testNamespace);

        Optional<Deployment> deployment = Optional.of(new DeploymentBuilder()
            .withNewMetadata().withName(deploymentName).endMetadata()
            .withNewSpec().withReplicas(replicas).endSpec().withNewStatus().withAvailableReplicas(availableReplicas).endStatus()
            .build());
        Optional<Service> kubernetesService = Optional.of(new ServiceBuilder()
            .withNewMetadata().withName(serviceName).endMetadata()
            .build());
        PodList podList = new PodListBuilder()
            .addNewItem()
            .withNewMetadata().withName(podName1).endMetadata()
            .withNewSpec().withNodeName(nodeName).endSpec()
            .withNewStatus().withPhase(podPhase).withHostIP(hostIp).endStatus()
            .endItem()
            .build();
//        given(micoKubernetesClient.getDeploymentOfMicoService(any(MicoService.class))).willReturn(deployment);
//        given(micoKubernetesClient.getPodsCreatedByDeploymentOfMicoService(any(MicoService.class))).willReturn(podList.getItems());
        given(micoKubernetesClient.getInterfaceByNameOfMicoService(any(MicoService.class), anyString())).willReturn(kubernetesService);

//        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");

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
            .setRequestedReplicas(replicas)
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(serviceName)))
            .setPodInfo(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2));

        MicoApplicationDeploymentInformationDTO micoApplicationDeploymentInformation = new MicoApplicationDeploymentInformationDTO();
        micoApplicationDeploymentInformation.getServiceDeploymentInformation().add(micoServiceDeploymentInformation);

        given(micoStatusService.getApplicationStatus(any(MicoApplication.class))).willReturn(micoApplicationDeploymentInformation);

        int memoryUsage1 = 50;
//        ResponseEntity responseEntity = getPrometheusResponseEntity(memoryUsage1);
        int cpuLoad1 = 10;
//        ResponseEntity responseEntity2 = getPrometheusResponseEntity(cpuLoad1);
//        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntity).willReturn(responseEntity2);

        int memoryUsage2 = 70;
        int cpuLoad2 = 40;

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/status"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(TestConstants.REQUESTED_REPLICAS, is(replicas)))
            .andExpect(jsonPath(TestConstants.AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(TestConstants.INTERFACES_INFORMATION, hasSize(1)))
            .andExpect(jsonPath(TestConstants.INTERFACES_INFORMATION_NAME, is(serviceName)))
            .andExpect(jsonPath(TestConstants.POD_INFO, hasSize(2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_POD_NAME_1, is(podName1)))
            .andExpect(jsonPath(TestConstants.POD_INFO_PHASE_1, is(podPhase)))
            .andExpect(jsonPath(TestConstants.POD_INFO_NODE_NAME_1, is(nodeName)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_MEMORY_USAGE_1, is(memoryUsage1)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_CPU_LOAD_1, is(cpuLoad1)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_AVAILABLE_1, is(false)))
            .andExpect(jsonPath(TestConstants.POD_INFO_POD_NAME_2, is(podName2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_PHASE_2, is(podPhase)))
            .andExpect(jsonPath(TestConstants.POD_INFO_NODE_NAME_2, is(nodeName)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_MEMORY_USAGE_2, is(memoryUsage2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_CPU_LOAD_2, is(cpuLoad2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_AVAILABLE_2, is(true)));
    }

    @Test
    public void deleteApplicationCorsCheckForbidden() throws Exception {
        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).header("Origin", "http://notAllowedOrigin.com"))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(content().string(is("Invalid CORS request")));
    }

    @Test
    public void deleteApplicationCorsCheckAllowed() throws Exception {
        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).header("Origin", corsConfig.getAllowedOrigins().get(0)))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    public void getServicesFormApplicationNotFound() throws Exception {
        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(status().reason(is("There is no application with the name " + SHORT_NAME + " and the version " + VERSION)));
    }

    @Test
    public void getServicesFormApplication() throws Exception {
        MicoService micoService = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1);
        MicoApplication application = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setServices(Collections.singletonList(micoService));
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        String micoServiceListJsonPath = buildPath(EMBEDDED, "micoServiceList");
        String micoServiceListJsonPathFirstElement = buildPath(micoServiceListJsonPath, FIRST_ELEMENT);
        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(LINKS_SELF_HREF, endsWith(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES)))
            .andExpect(jsonPath(micoServiceListJsonPath, hasSize(application.getServices().size())))
            .andExpect(jsonPath(buildPath(micoServiceListJsonPathFirstElement, JsonPathBuilder.VERSION), is(VERSION_1_0_1)))
            .andExpect(jsonPath(buildPath(micoServiceListJsonPathFirstElement, JsonPathBuilder.SHORT_NAME), is(SHORT_NAME_1)))
            .andExpect(jsonPath(buildPath(micoServiceListJsonPathFirstElement, LINKS_SELF_HREF), endsWith(PATH_SERVICES + "/" + SHORT_NAME_1 + "/" + VERSION_1_0_1)));
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

    private void prettyPrint(Object object) {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            String json = mapper.writeValueAsString(object);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
