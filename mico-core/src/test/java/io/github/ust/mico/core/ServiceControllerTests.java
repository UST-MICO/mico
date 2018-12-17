package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.REST.ServiceController;
import org.hamcrest.Matchers;
import org.junit.Ignore;
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
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
public class ServiceControllerTests {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";
    private static final String SELF_HREF = "self.href";
    private static final String INTERFACES_HREF = "interfaces.href";
    private static final String SERVICE_INTERFACE_SELF_LINK_PART = "services/ShortName/1.0/interfaces";
    //TODO: Use these variables inside the tests instead of the local variables
    private static final String SHORT_NAME = "ServiceShortName";
    private static final String VERSION = "1.0.0";
    private static final String DESCRIPTION = "Some description";
    private static final String BASE_PATH = "/services/";
    private static final String DELETE_ALL_DEPENDEES_PATH = "/services/" + SHORT_NAME + "/" + VERSION + "/dependees";
    private static final Long TEST_ID = new Long(45325345);

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ServiceRepository serviceRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getCompleteServiceList() throws Exception {
        given(serviceRepository.findAll()).willReturn(
                Arrays.asList(
                        new Service("ShortName1", "1.0.1", "Test service"),
                        new Service("ShortName1", "1.0.0", "Test service"),
                        new Service("ShortName2", "1.0.0", "Test service2")));

        mvc.perform(get("/services").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$._embedded.serviceList[*]", hasSize(3)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName1' && @.version == '1.0.0' && @.description == 'Test service' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName1' && @.version == '1.0.1' && @.description == 'Test service' )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceList[?(@.shortName =='ShortName2' && @.version == '1.0.0' && @.description == 'Test service2' )]", hasSize(1)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/services")))
                .andReturn();
    }

    @Test
    public void getServiceViaShortNameAndVersion() throws Exception {
        given(serviceRepository.findByShortNameAndVersion("ShortName1", "1.0.1")).willReturn(
                Optional.of(new Service("ShortName1", "1.0.1", "Test service")));

        mvc.perform(get("/services/ShortName1/1.0.1").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.description", is("Test service")))
                .andExpect(jsonPath("$.shortName", is("ShortName1")))
                .andExpect(jsonPath("$.version", is("1.0.1")))
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

        given(serviceRepository.findById(id)).willReturn(Optional.of(new Service(shortName, version, description)));

        mvc.perform(get(urlTemplate).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.description", is(description)))
                .andExpect(jsonPath("$.shortName", is(shortName)))
                .andExpect(jsonPath("$.version", is(version)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is(linksSelf)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "services.href", is(linksServices)))
                .andReturn();
    }

    @Test
    public void getServiceInterfaces() throws Exception {
        ServiceInterface serviceInterface1 = generateServiceInterface("ServiceInterfaceName1", "serviceInterfaceDescription1", "1024", "HTTP");
        ServiceInterface serviceInterface2 = generateServiceInterface("ServiceInterfaceName2", "serviceInterfaceDescription2", "1025", "MQTT");

        String serviceShortName = "ShortName";
        String serviceVersion = "1.0";
        List<ServiceInterface> serviceInterfaces = Arrays.asList(
                serviceInterface1,
                serviceInterface2);
        given(serviceRepository.findInterfacesOfService(serviceShortName, serviceVersion)).willReturn(
                serviceInterfaces);

        mvc.perform(get("/services/" + serviceShortName + "/" + serviceVersion + "/interfaces").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$._embedded.serviceInterfaceList[*]", hasSize(serviceInterfaces.size())))
                .andExpect(jsonPath("$._embedded.serviceInterfaceList[?(@.serviceInterfaceName =='ServiceInterfaceName1' " +
                        "&& @.description == '" + serviceInterface1.getDescription() + "' " +
                        "&& @.port == '" + serviceInterface1.getPort() + "' " +
                        "&& @.protocol == '" + serviceInterface1.getProtocol() + "' " +
                        "&& @._links.self.href =~ /[\\S]+\\/services\\/" + serviceShortName + "\\/1\\.0\\/interfaces\\/ServiceInterfaceName1/i )]", hasSize(1)))
                .andExpect(jsonPath("$._embedded.serviceInterfaceList[?(@.serviceInterfaceName =='ServiceInterfaceName2' " +
                        "&& @.description == '" + serviceInterface2.getDescription() + "' " +
                        "&& @.port == '" + serviceInterface2.getPort() + "' " +
                        "&& @.protocol == '" + serviceInterface2.getProtocol() + "' " +
                        "&& @._links.self.href =~ /[\\S]+\\/services\\/" + serviceShortName + "\\/1\\.0\\/interfaces\\/ServiceInterfaceName2/i )]", hasSize(1)))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, endsWith("/services/ShortName/1.0/interfaces")))
                .andReturn();
    }

    @Test
    public void getServiceInterface() throws Exception {
        ServiceInterface serviceInterface = generateServiceInterface("ServiceInterfaceName1", "serviceInterfaceDescription1", "1024", "HTTP");
        String serviceShortName = "ShortName";
        String serviceVersion = "1.0";
        given(serviceRepository.findInterfaceOfServiceByName(serviceInterface.getServiceInterfaceName(), serviceShortName, serviceVersion)).willReturn(
                Optional.of(serviceInterface));

        mvc.perform(get("/services/" + serviceShortName + "/" + serviceVersion + "/interfaces/" + serviceInterface.getServiceInterfaceName()).accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.description", is(serviceInterface.getDescription())))
                .andExpect(jsonPath("$.serviceInterfaceName", is(serviceInterface.getServiceInterfaceName())))
                .andExpect(jsonPath("$.protocol", is(serviceInterface.getProtocol())))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, endsWith(SERVICE_INTERFACE_SELF_LINK_PART + "/ServiceInterfaceName1")))
                .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + INTERFACES_HREF, endsWith(SERVICE_INTERFACE_SELF_LINK_PART)))
                .andReturn();
    }

    private ServiceInterface generateServiceInterface(String serviceInterfaceName1, String serviceInterfaceDescription1, String s, String http) {
        ServiceInterface serviceInterface1 = new ServiceInterface(serviceInterfaceName1);
        serviceInterface1.setDescription(serviceInterfaceDescription1);
        serviceInterface1.setPort(s);
        serviceInterface1.setProtocol(http);
        return serviceInterface1;
    }

    @Test
    public void createService() throws Exception {
        Service service = new Service(SHORT_NAME,VERSION,DESCRIPTION);

        given(serviceRepository.save(any(Service.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(BASE_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
    }

    //TODO: Should probably work with in-memory database
    @Ignore
    @Test
    public void createServiceWithDependees() throws Exception {
        Service service = new Service(SHORT_NAME,VERSION,DESCRIPTION);
        Service serviceDependee = new Service("DependsOnService","1.0.1", "Some Depends On Description");
        LinkedList<DependsOn> dependsOn = new LinkedList<DependsOn>();
        dependsOn.add(new DependsOn(service,serviceDependee));
        //TODO: Test is not working with dependsOn object
        service.setDependsOn(dependsOn);

        given(serviceRepository.save(any(Service.class))).willReturn(service);

       final ResultActions result = mvc.perform(post(BASE_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void deleteAllServiceDependees() throws Exception {
        Service service = new Service(SHORT_NAME,VERSION,DESCRIPTION);

        given(serviceRepository.save(any(Service.class))).willReturn(service);

//        ResultActions resultPost = mvc.perform(post(BASE_PATH)
//                .content(mapper.writeValueAsBytes(service))
//                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE));
//                //.andDo(print());

        System.out.println(DELETE_ALL_DEPENDEES_PATH);

        ResultActions resultDelete = mvc.perform(delete(DELETE_ALL_DEPENDEES_PATH)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        resultDelete.andExpect(status().isCreated());
    }

}
