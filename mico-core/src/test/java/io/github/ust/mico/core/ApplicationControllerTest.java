package io.github.ust.mico.core;

import static io.github.ust.mico.core.JsonPathBuilder.EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;
import static io.github.ust.mico.core.REST.ApplicationController.LABEL_APP_KEY;
import static io.github.ust.mico.core.REST.ApplicationController.LABEL_VERSION_KEY;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1_MATCHER;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_MATCHER;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_1;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.github.ust.mico.core.REST.ApplicationController;
import io.github.ust.mico.core.REST.PrometheusResponse;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ApplicationControllerTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(true, true);

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";
    private static final String SELF_HREF = "self.href";

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

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PrometheusConfig prometheusConfig;

    @MockBean
    private MicoKubernetesConfig micoKubernetesConfig;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ClusterAwarenessFabric8 clusterAwarenessFabric8;

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private ObjectMapper mapper;


    @Test
    public void getAllApplications() throws Exception {
        given(applicationRepository.findAll()).willReturn(
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

        mvc.perform(get("/applications/" + SHORT_NAME + "/" + VERSION.toString()).accept(MediaTypes.HAL_JSON_VALUE))
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
        int availableReplicas = 1;
        int replicas = 1;
        server.getClient().apps().deployments().inNamespace(testNamespace).create(new DeploymentBuilder()
            .withNewMetadata().withName(deploymentName).withLabels(labels).endMetadata()
            .withNewSpec().withReplicas(replicas).endSpec().withNewStatus().withAvailableReplicas(availableReplicas).endStatus()
            .build());
        server.getClient().services().inNamespace(testNamespace).create(new ServiceBuilder()
            .withNewMetadata().withName(serviceName).withLabels(labels).endMetadata()
            .build());
        server.getClient().pods().inNamespace(testNamespace).create(new PodBuilder()
            .withNewMetadata().withName(podName).withLabels(labels).endMetadata()
            .withNewSpec().withNodeName(nodeName).endSpec()
            .withNewStatus().withPhase(podPhase).withHostIP(hostIp).endStatus().build());

        given(clusterAwarenessFabric8.getClient()).willReturn(server.getClient());
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
