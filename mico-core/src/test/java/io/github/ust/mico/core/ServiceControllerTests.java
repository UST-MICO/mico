package io.github.ust.mico.core;

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
@ContextConfiguration(classes = {MicoCoreApplication.class,WebConfig.class})
@WebAppConfiguration
public class ServiceControllerTests {

    public static final String JSON_PATH_LINKS_SECTION = "$._links.";
    public static final String SELF_HREF = "self.href";
    public static final String INTERFACES_HREF = "interfaces.href";
    public static final String SERVICES_HREF = "services.href";
    public static final String SERVICE_INTERFACE_SELF_LINK_PART = "services/ShortName/1.0/interfaces";
    //TODO: Use these variables inside the tests instead of the local variables
    public static final String SHORT_NAME = "ServiceShortName";
    public static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "Some description";
    private static final String BASE_PATH = "/services/";
    private static final String DEPENDEES_BASE_PATH = "/services/" + SHORT_NAME + "/" + VERSION + "/dependees";
    private static final String SHORT_NAME_TO_DELETE = "shortNameToDelete";
    private static final String VERSION_TO_DELETE = "1.0.1";
    private static final String DELETE_SPECIFIC_DEPENDEES_PATH = "/services/" + SHORT_NAME + "/" + VERSION + "/dependees/" + SHORT_NAME_TO_DELETE + "/" + VERSION_TO_DELETE;
    private static final String DEPENDERS_PATH = "/services/" + SHORT_NAME + "/" + VERSION + "/dependers";

    @Value("${cors-policy.allowed-origins}")
    String[] allowedOrigins;


    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).dispatchOptions(true).build();
    }

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getCompleteServiceList() throws Exception {
        given(serviceRepository.findAll()).willReturn(
                Arrays.asList(
                        MicoService.builder().shortName("ShortName1").version(MicoVersion.forIntegers(1, 0, 1)).description("Service 1").build(),
                        MicoService.builder().shortName("ShortName2").version(MicoVersion.forIntegers(1, 0, 2)).description("Service 2").build(),
                        MicoService.builder().shortName("ShortName3").version(MicoVersion.forIntegers(1, 0, 3)).description("Service 3").build()));

        mvc.perform(get("/services").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$._embedded.serviceList[*]", hasSize(3)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName1' && @.version == '1.0.1' && @.description == 'Service 1' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName2' && @.version == '1.0.2' && @.description == 'Service 2' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName3' && @.version == '1.0.3' && @.description == 'Service 3' )]", hasSize(1)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/services")))
                .andReturn();
    }

    @Test
    public void getServiceViaShortNameAndVersion() throws Exception {
        given(serviceRepository.findByShortNameAndVersion("ShortName1", "1.0.1")).willReturn(
                Optional.of(MicoService.builder().shortName("ShortName1").version(MicoVersion.forIntegers(1, 0, 1)).description("Service 1").build()));

        mvc.perform(get("/services/ShortName1/1.0.1").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.shortName", is("ShortName1")))
                .andExpect(jsonPath("$.version", is("1.0.1")))
                .andExpect(jsonPath("$.description", is("Service 1")))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/services/ShortName1/1.0.1")))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "services.href", is("http://localhost/services")))
                .andReturn();
    }

    //TODO: Verfiy how to test an autogenerated id
    @Ignore
    @Test
    public void getServiceById() throws Exception {
        Long id = new Long(45325345);
        String shortName = "ServiceShortName";
        String version = "1.0.0";
        String description = "Some description";
        String urlTemplate = BASE_PATH + id;
        String linksSelf = "http://localhost" + urlTemplate;
        String linksServices = "http://localhost/services";

        given(serviceRepository.findById(id)).willReturn(Optional.of(MicoService.builder().shortName(shortName).version(MicoVersion.valueOf(version)).description(description).build()));

        mvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.description", is(description)))
                .andExpect(jsonPath("$.shortName", is(shortName)))
                .andExpect(jsonPath("$.version", is(version)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is(linksSelf)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SERVICES_HREF, is(linksServices)))
                .andReturn();
    }

    @Test
    public void createService() throws Exception {
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description(DESCRIPTION).build();

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(BASE_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void deleteAllServiceDependees() throws Exception {
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description(DESCRIPTION).build();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        ResultActions resultDelete = mvc.perform(delete(DEPENDEES_BASE_PATH)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        resultDelete.andExpect(status().isCreated());
    }

    @Test
    public void deleteSpecificServiceDependee() throws Exception {
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description(DESCRIPTION).build();
        MicoService serviceToDelete = MicoService.builder().shortName(SHORT_NAME_TO_DELETE).version(MicoVersion.valueOf(VERSION_TO_DELETE)).build();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_TO_DELETE, VERSION_TO_DELETE)).willReturn(Optional.of(serviceToDelete));

        System.out.println(DELETE_SPECIFIC_DEPENDEES_PATH);

        ResultActions resultDelete = mvc.perform(delete(DELETE_SPECIFIC_DEPENDEES_PATH)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        resultDelete.andExpect(status().isCreated());
    }


    @Test
    public void corsPolicy() throws Exception {
        mvc.perform(get("/services/").accept(MediaTypes.HAL_JSON_VALUE)
            .header("Origin", allowedOrigins))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, endsWith("/services"))).andReturn();
    }

    @Test
    public void corsPolicyNotAllowedOrigin() throws Exception {
        mvc.perform(get("/services/").accept(MediaTypes.HAL_JSON_VALUE)
            .header("Origin", "http://notAllowedOrigin.com"))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(content().string(is("Invalid CORS request")))
            .andReturn();
    }

    @Test
    public void getServiceDependers() throws Exception {
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description(DESCRIPTION).build();
        
        MicoService service1 = MicoService.builder()
                .shortName("ShortName1")
                .version(MicoVersion.forIntegers(1, 0, 1))
                .description("Service 1")
                .build();
        MicoService service2 = MicoService.builder()
                .shortName("ShortName2")
                .version(MicoVersion.forIntegers(1, 0, 2))
                .description("Service 2")
                .build();
        MicoService service3 = MicoService.builder()
                .shortName("ShortName3")
                .version(MicoVersion.forIntegers(1, 0, 3))
                .description("Service 3")
                .build();
        
        MicoServiceDependency dependency1 = MicoServiceDependency.builder().service(service1).dependedService(service).build();
        MicoServiceDependency dependency2 = MicoServiceDependency.builder().service(service2).dependedService(service).build();
        MicoServiceDependency dependency3 = MicoServiceDependency.builder().service(service3).dependedService(service).build();

        service1.setDependencies(Collections.singletonList(dependency1));
        service2.setDependencies(Collections.singletonList(dependency2));
        service3.setDependencies(Collections.singletonList(dependency3));

        given(serviceRepository.findAll()).willReturn(Arrays.asList(service, service1, service2, service3));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        ResultActions result = mvc.perform(get(DEPENDERS_PATH)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(jsonPath("$._embedded.serviceList[*]", hasSize(3)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName1' && @.version == '1.0.1' && @.description == 'Service 1' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName2' && @.version == '1.0.2' && @.description == 'Service 2' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName3' && @.version == '1.0.3' && @.description == 'Service 3' )]", hasSize(1)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost" + DEPENDERS_PATH)));


        result.andExpect(status().isOk());

    }

    @Test
    public void updateService() throws Exception {
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description(DESCRIPTION).build();
        MicoService updatedService = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description("newDesc").build();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(updatedService);

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + SHORT_NAME + "/" + VERSION)
                .content(mapper.writeValueAsBytes(updatedService))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(jsonPath("$.id", is(service.getId())))
                .andExpect(jsonPath("$.description", is(updatedService.getDescription())))
                .andExpect(jsonPath("$.shortName", is(updatedService.getShortName())))
                .andExpect(jsonPath("$.version", is(updatedService.getVersion())));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void deleteService() throws Exception {
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(MicoVersion.valueOf(VERSION)).description(DESCRIPTION).build();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        ResultActions resultDelete = mvc.perform(delete(BASE_PATH + SHORT_NAME + "/" + VERSION)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        resultDelete.andExpect(status().isOk());
    }

}
