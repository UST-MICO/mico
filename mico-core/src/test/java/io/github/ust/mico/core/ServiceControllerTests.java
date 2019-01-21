package io.github.ust.mico.core;
import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.JsonPathBuilder.*;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ust.mico.core.REST.ServiceController;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoVersion;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ServiceControllerTests {

    public static final String JSON_PATH_LINKS_SECTION = buildPath(ROOT, LINKS);
    public static final String SELF_HREF = buildPath(JSON_PATH_LINKS_SECTION, SELF, HREF);
    public static final String INTERFACES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "interfaces", HREF);
    public static final String SERVICES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "services", HREF);
    public static final String EMBEDDED = buildPath(ROOT, JsonPathBuilder.EMBEDDED);
    public static final String SERVICE_LIST = buildPath(EMBEDDED, "micoServiceList");
    public static final String ID_PATH = buildPath(ROOT, "id");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String VERSION_PATH = buildPath(ROOT, "version");


    //TODO: Use these variables inside the tests instead of the local variables

    @Value("${cors-policy.allowed-origins}")
    String[] allowedOrigins;


    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getCompleteServiceList() throws Exception {
        given(serviceRepository.findAll()).willReturn(
                Arrays.asList(
                        MicoService.builder().shortName(SHORT_NAME_1).version(VERSION_1_0_1).description(DESCRIPTION_1).build(),
                        MicoService.builder().shortName(SHORT_NAME_2).version(VERSION_1_0_2).description(DESCRIPTION_2).build(),
                        MicoService.builder().shortName(SHORT_NAME_3).version(VERSION_1_0_3).description(DESCRIPTION_3).build()));

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
                Optional.of(MicoService.builder().shortName(SHORT_NAME).version(VERSION).description(DESCRIPTION).build()));

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

    //TODO: Verfiy how to test an autogenerated id
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
                .willReturn(Optional.of(MicoService.builder().shortName(SHORT_NAME).version(VERSION).description(DESCRIPTION).build()));

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
        MicoService service = MicoService.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(DESCRIPTION).build();

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void deleteAllServiceDependees() throws Exception {
        MicoService service = MicoService.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(DESCRIPTION).build();

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
        MicoService service = MicoService.builder().shortName(shortName).version(version).description(description).build();
        MicoService serviceToDelete = MicoService.builder().shortName(shortNameToDelete).version(versionToDelete).build();

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
        MicoService service = MicoService.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(DESCRIPTION).build();

        MicoService service1 = MicoService.builder()
                .shortName(SHORT_NAME_1)
                .version(VERSION_1_0_1)
                .description(DESCRIPTION_1)
                .build();
        MicoService service2 = MicoService.builder()
                .shortName(SHORT_NAME_2)
                .version(VERSION_1_0_2)
                .description(DESCRIPTION_2)
                .build();
        MicoService service3 = MicoService.builder()
                .shortName(SHORT_NAME_3)
                .version(VERSION_1_0_3)
                .description(DESCRIPTION_3)
                .build();

        MicoServiceDependency dependency1 = MicoServiceDependency.builder().service(service1).dependedService(service).build();
        MicoServiceDependency dependency2 = MicoServiceDependency.builder().service(service2).dependedService(service).build();
        MicoServiceDependency dependency3 = MicoServiceDependency.builder().service(service3).dependedService(service).build();

        service1.setDependencies(Collections.singletonList(dependency1));
        service2.setDependencies(Collections.singletonList(dependency2));
        service3.setDependencies(Collections.singletonList(dependency3));

        given(serviceRepository.findAll()).willReturn(Arrays.asList(service, service1, service2, service3));
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
        MicoService service = MicoService.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(DESCRIPTION).build();
        MicoService updatedService = MicoService.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(updatedDescription).build();

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
        MicoService service = MicoService.builder()
            .shortName(SHORT_NAME)
            .version(VERSION)
            .description(DESCRIPTION).build();

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

        resultDelete.andExpect(status().isOk());
    }

}
