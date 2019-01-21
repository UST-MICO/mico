package io.github.ust.mico.core;

import static io.github.ust.mico.core.ServiceControllerTests.INTERFACES_HREF;
import static io.github.ust.mico.core.ServiceControllerTests.SELF_HREF;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.JsonPathBuilder.LINKS;
import static io.github.ust.mico.core.JsonPathBuilder.HREF;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ust.mico.core.model.MicoPortType;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.model.MicoVersion;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.springframework.web.util.UriComponentsBuilder;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest()
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ServiceInterfaceControllerTests {

    private static final String INTERFACE_NAME = "Interface Test";
    private static final int INTERFACE_PORT = 1024;
    private static final MicoPortType INTERFACE_PORT_TYPE = MicoPortType.TCP;
    private static final int INTERFACE_TARGET_PORT = 1025;
    private static final String INTERFACE_DESCRIPTION = "This is a service interface.";
    private static final String INTERFACE_PUBLIC_DNS = "DNS";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ObjectMapper mapper;

    public static final String SERVICES_HREF = buildPath(ROOT, LINKS, "service", HREF);
    private static final String SERVICE_URL = "/services/" + SHORT_NAME + "/" + VERSION;
    private static final String INTERFACES_URL = SERVICE_URL + "/interfaces/";

    @Test
    public void postServiceInterface() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION.toString())).willReturn(
            Optional.of(MicoService.builder().shortName(SHORT_NAME).version(VERSION).build())
        );

        MicoServiceInterface serviceInterface = getTestServiceInterface();
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(getServiceInterfaceMatcher(serviceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();
    }

    @Test
    public void postServiceNotFound() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(any(), any())).willReturn(
            Optional.empty()
        );
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void postServiceInterfaceExists() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        MicoService service = MicoService.builder().shortName(SHORT_NAME).version(VERSION).build();
        service = service.toBuilder().serviceInterface(serviceInterface).build();
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION.toString())).willReturn(
            Optional.of(service)
        );
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("An interface with this name is already associated with this service."))
            .andReturn();
    }

    @Test
    public void getSpecificServiceInterface() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        given(serviceRepository.findInterfaceOfServiceByName(serviceInterface.getServiceInterfaceName(), SHORT_NAME, VERSION.toString())).willReturn(
            Optional.of(serviceInterface));

        mvc.perform(get(INTERFACES_URL + serviceInterface.getServiceInterfaceName()).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(getServiceInterfaceMatcher(serviceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();

    }

    @Test
    public void getSpecificServiceInterfaceNotFound() throws Exception {
        given(serviceRepository.findInterfaceOfServiceByName(any(), any(), any())).willReturn(
            Optional.empty());

        mvc.perform(get(INTERFACES_URL + "NotThereInterface").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

    }

    @Test
    public void getAllServiceInterfacesOfService() throws Exception {
        MicoServiceInterface serviceInterface0 = MicoServiceInterface.builder().serviceInterfaceName("ServiceInterface0").build();
        MicoServiceInterface serviceInterface1 = MicoServiceInterface.builder().serviceInterfaceName("ServiceInterface1").build();
        List<MicoServiceInterface> serviceInterfaces = Arrays.asList(serviceInterface0, serviceInterface1);
        given(serviceRepository.findInterfacesOfService(SHORT_NAME, VERSION.toString())).willReturn(
            serviceInterfaces);
        mvc.perform(get(INTERFACES_URL).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceList[*]", hasSize(serviceInterfaces.size())))
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceList[?(@.serviceInterfaceName =='" + serviceInterface0.getServiceInterfaceName() + "')]", hasSize(1)))
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceList[?(@.serviceInterfaceName =='" + serviceInterface1.getServiceInterfaceName() + "')]", hasSize(1)))
            .andReturn();
    }


    private MicoServiceInterface getTestServiceInterface() {
        return MicoServiceInterface.builder()
            .serviceInterfaceName(INTERFACE_NAME)
            .port(MicoServicePort.builder()
                .number(INTERFACE_PORT)
                .type(INTERFACE_PORT_TYPE)
                .targetPort(INTERFACE_TARGET_PORT)
                .build())
            .description(INTERFACE_DESCRIPTION)
            .publicDns(INTERFACE_PUBLIC_DNS)
            .build();
    }

    private ResultMatcher getServiceInterfaceMatcher(MicoServiceInterface serviceInterface, String selfBaseUrl, String serviceUrl) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        URI selfHrefEnding = UriComponentsBuilder.fromUriString(selfBaseUrl + serviceInterface.getServiceInterfaceName()).build().encode().toUri();
        return ResultMatcher.matchAll(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE),
            jsonPath("$.serviceInterfaceName", is(serviceInterface.getServiceInterfaceName())),
            jsonPath("$.ports", hasSize(serviceInterface.getPorts().size())),
            jsonPath("$.ports", not(empty())),
            jsonPath("$.protocol", is(serviceInterface.getProtocol())),
            jsonPath("$.description", is(serviceInterface.getDescription())),
            jsonPath("$.publicDns", is(serviceInterface.getPublicDns())),
            jsonPath("$.transportProtocol", is(serviceInterface.getTransportProtocol())),
            jsonPath(SELF_HREF, endsWith(selfHrefEnding.toString())),
            jsonPath(INTERFACES_HREF, endsWith(selfBaseUrl)),
            jsonPath(SERVICES_HREF, endsWith(serviceUrl)));
    }


}
