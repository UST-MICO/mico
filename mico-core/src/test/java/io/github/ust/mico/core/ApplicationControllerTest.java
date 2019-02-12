package io.github.ust.mico.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServiceListBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.DeploymentListBuilder;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.configuration.PrometheusConfig;
import io.github.ust.mico.core.service.ClusterAwarenessFabric8;
import io.github.ust.mico.core.web.ApplicationController;
import io.github.ust.mico.core.dto.PrometheusResponse;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import java.util.HashMap;
import java.util.Optional;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.web.ApplicationController.PATH_APPLICATIONS;
import static io.github.ust.mico.core.web.ApplicationController.PATH_SERVICES;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.service.MicoKubernetesClient.LABEL_APP_KEY;
import static io.github.ust.mico.core.service.MicoKubernetesClient.LABEL_VERSION_KEY;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
public class ApplicationControllerTest {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String APPLICATION_LIST = buildPath(EMBEDDED, "micoApplicationList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_PATH = buildPath(ROOT, "version");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String ID_PATH = buildPath(ROOT, "id");

    private static final String REQUESTED_REPLICAS = buildPath(ROOT, "requestedReplicas");
    private static final String AVAILABLE_REPLICAS = buildPath(ROOT, "availableReplicas");
    private static final String INTERFACES_INFORMATION = buildPath(ROOT, "interfacesInformation");
    private static final String INTERFACES_INFORMATION_NAME = buildPath(ROOT, "interfacesInformation[0].name");
    private static final String POD_INFO = buildPath(ROOT, "podInfo");
    private static final String POD_INFO_POD_NAME = buildPath(ROOT, "podInfo[0].podName");
    private static final String POD_INFO_PHASE = buildPath(ROOT, "podInfo[0].phase");
    private static final String POD_INFO_NODE_NAME = buildPath(ROOT, "podInfo[0].nodeName");
    private static final String POD_INFO_METRICS_MEMORY_USAGE = buildPath(ROOT, "podInfo[0].metrics.memoryUsage");
    private static final String POD_INFO_METRICS_CPU_LOAD = buildPath(ROOT, "podInfo[0].metrics.cpuLoad");
    private static final String POD_INFO_METRICS_AVAILABLE = buildPath(ROOT, "podInfo[0].metrics.available");

    public static final String VERSION_MAJOR_PATH = buildPath(ROOT, "version", "majorVersion");
    public static final String VERSION_MINOR_PATH = buildPath(ROOT, "version", "minorVersion");
    public static final String VERSION_PATCH_PATH = buildPath(ROOT, "version", "patchVersion");

    private static final String BASE_PATH = "/applications";

    @MockBean
    private PrometheusConfig prometheusConfig;

    @MockBean
    private MicoKubernetesConfig micoKubernetesConfig;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ClusterAwarenessFabric8 cluster;

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
    public void getDeploymentInformation() throws Exception {
        MicoApplication application = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        String testNamespace = "TestNamespace";
        String nodeName = "testNode";
        String podPhase = "Running";
        String hostIp = "192.168.0.0";
        String deploymentName = "deployment1";
        String serviceName = "service1";
        String podName = "pod1";
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(micoKubernetesConfig.getNamespaceMicoWorkspace()).willReturn(testNamespace);

        HashMap<String, String> labels = new HashMap<>();
        labels.put(LABEL_APP_KEY, application.getShortName());
        labels.put(LABEL_VERSION_KEY, application.getVersion());
        final int availableReplicas = 1;
        final int replicas = 1;

        DeploymentList deploymentList = new DeploymentListBuilder()
            .addNewItem()
            .withNewMetadata().withName(deploymentName).withLabels(labels).endMetadata()
            .withNewSpec().withReplicas(replicas).endSpec().withNewStatus().withAvailableReplicas(availableReplicas).endStatus()
            .endItem()
            .build();
        ServiceList serviceList = new ServiceListBuilder()
            .addNewItem()
            .withNewMetadata().withName(serviceName).withLabels(labels).endMetadata()
            .endItem()
            .build();
        PodList podList = new PodListBuilder()
            .addNewItem()
            .withNewMetadata().withName(podName).withLabels(labels).endMetadata()
            .withNewSpec().withNodeName(nodeName).endSpec()
            .withNewStatus().withPhase(podPhase).withHostIP(hostIp).endStatus()
            .endItem()
            .build();
        given(cluster.getDeploymentsByLabels(labels, testNamespace)).willReturn(deploymentList);
        given(cluster.getServicesByLabels(labels, testNamespace)).willReturn(serviceList);
        given(cluster.getPodsByLabels(labels, testNamespace)).willReturn(podList);

        given(prometheusConfig.getUri()).willReturn("http://localhost:9090/api/v1/query");

        int memoryUsage = 1;
        ResponseEntity responseEntity = getPrometheusResponseEntity(memoryUsage);
        int cpuLoad = 2;
        ResponseEntity responseEntity2 = getPrometheusResponseEntity(cpuLoad);
        given(restTemplate.getForEntity(any(), eq(PrometheusResponse.class))).willReturn(responseEntity).willReturn(responseEntity2);

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/deploymentInformation"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(REQUESTED_REPLICAS, is(replicas)))
            .andExpect(jsonPath(AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(INTERFACES_INFORMATION, hasSize(1)))
            .andExpect(jsonPath(INTERFACES_INFORMATION_NAME, is(serviceName)))
            .andExpect(jsonPath(POD_INFO, hasSize(1)))
            .andExpect(jsonPath(POD_INFO_POD_NAME, is(podName)))
            .andExpect(jsonPath(POD_INFO_PHASE, is(podPhase)))
            .andExpect(jsonPath(POD_INFO_NODE_NAME, is(nodeName)))
            .andExpect(jsonPath(POD_INFO_METRICS_MEMORY_USAGE, is(memoryUsage)))
            .andExpect(jsonPath(POD_INFO_METRICS_CPU_LOAD, is(cpuLoad)))
            .andExpect(jsonPath(POD_INFO_METRICS_AVAILABLE, is(true)));
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
