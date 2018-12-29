package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

import static io.github.ust.mico.core.ServiceControllerTests.*;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest()
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ServiceInterfaceControllerTests {


    @Autowired
    private MockMvc mvc;

    @MockBean
    private ServiceRepository serviceRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;
    private static final String SERVICE_URL = "/services/" + ServiceControllerTests.SHORT_NAME + "/" + ServiceControllerTests.VERSION;
    private static final String INTERFACES_URL = SERVICE_URL + "/interfaces/";

    @Test
    public void postServiceInterface() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(ServiceControllerTests.SHORT_NAME, ServiceControllerTests.VERSION)).willReturn(
            Optional.of(new Service(ServiceControllerTests.SHORT_NAME, ServiceControllerTests.VERSION))
        );

        ServiceInterface serviceInterface = getTestServiceInterface();
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
        ServiceInterface serviceInterface = getTestServiceInterface();
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void postServiceInterfaceExists() throws Exception {
        ServiceInterface serviceInterface = getTestServiceInterface();
        Service service = new Service(ServiceControllerTests.SHORT_NAME, ServiceControllerTests.VERSION);
        service.getServiceInterfaces().add(serviceInterface);
        given(serviceRepository.findByShortNameAndVersion(ServiceControllerTests.SHORT_NAME, ServiceControllerTests.VERSION)).willReturn(
            Optional.of(service)
        );
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("An interface with this name is already associated with this service."))
            .andReturn();
    }


    private ServiceInterface getTestServiceInterface() {
        ServiceInterface serviceInterface = new ServiceInterface("ServiceInterface");
        serviceInterface.setProtocol("HTTP");
        serviceInterface.setPort("1024");
        serviceInterface.setDescription("This is a test interface");
        serviceInterface.setPublicDns("Test String");
        serviceInterface.setTransportProtocol("HTTP");
        return serviceInterface;
    }

    private ResultMatcher getServiceInterfaceMatcher(ServiceInterface serviceInterface, String selfBaseUrl, String serviceUrl) {
        return ResultMatcher.matchAll(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE),
            jsonPath("$.serviceInterfaceName", is(serviceInterface.getServiceInterfaceName())),
            jsonPath("$.port", is(serviceInterface.getPort())),
            jsonPath("$.protocol", is(serviceInterface.getProtocol())),
            jsonPath("$.description", is(serviceInterface.getDescription())),
            jsonPath("$.publicDns", is(serviceInterface.getPublicDns())),
            jsonPath("$.transportProtocol", is(serviceInterface.getTransportProtocol())),
            jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, endsWith(selfBaseUrl + serviceInterface.getServiceInterfaceName())),
            jsonPath(JSON_PATH_LINKS_SECTION + INTERFACES_HREF, endsWith(selfBaseUrl)),
            jsonPath(JSON_PATH_LINKS_SECTION + "service.href", endsWith(serviceUrl)));
    }


}
