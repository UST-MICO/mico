package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringDataRestTests {

    public static final String HTTP_LOCALHOST = "http://localhost:";

    @Test
    public void contextLoads() {
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    public void testServicesWithMultipleDescriptions() throws Exception {
        //todo evaluate json-path-assert,json-path from com.jayway.jsonpath
        serviceRepository.deleteAll();
        Service service = generateService("1");

        RestTemplate restTemplate = new RestTemplate();
        JsonNode body = postService(service);

        JsonNode links = body.get("_links");
        JsonNode linkServiceDescriptions = links.get("serviceDescriptions").get("href");
        URI serviceDescriptionsURI = new URI(linkServiceDescriptions.asText());

        String serviceDescriptionValue = "Some description";
        String serviceDescriptionPortVariable = "<PORT>";
        String serviceDescriptionProtocol = "HTTP";
        String serviceName = "Some Service Name";

        ServiceDescription serviceDescription1 = generateServiceDescription("1");
        ServiceDescription serviceDescription2 = generateServiceDescription("2");

        URI serviceDescriptionLinkSelf1 = postServiceDescription(serviceDescription1);
        URI serviceDescriptionLinkSelf2 = postServiceDescription(serviceDescription2);

        putServiceDescription(serviceDescriptionsURI, serviceDescriptionLinkSelf1);
        putServiceDescription(serviceDescriptionsURI, serviceDescriptionLinkSelf2);

        ResponseEntity<JsonNode> getResultServiceToServiceDescription = restTemplate.getForEntity(serviceDescriptionsURI, JsonNode.class);

        assertThat(getResultServiceToServiceDescription.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode serviceDescriptionsList = getResultServiceToServiceDescription.getBody().get("_embedded").get("serviceDescribtions");
        assertThat(serviceDescriptionsList.size()).isEqualTo(2);
    }

    @Test(expected = HttpClientErrorException.NotFound.class)
    public void testDeleteService() throws Exception {
        serviceRepository.deleteAll();
        Service service = generateService("2");
        JsonNode serviceResult = postService(service);
        JsonNode links = serviceResult.get("_links");
        JsonNode linkServiceDescriptions = links.get("serviceDescriptions").get("href");
        JsonNode linkServiceSelf = links.get("self").get("href");
        URI serviceDescriptionsURI = new URI(linkServiceDescriptions.asText());
        URI linkServiceSelfURI = new URI(linkServiceSelf.asText());

        ServiceDescription serviceDescription1 = generateServiceDescription("2");
        URI serviceDescriptionLinkSelf1 = postServiceDescription(serviceDescription1);
        putServiceDescription(serviceDescriptionsURI, serviceDescriptionLinkSelf1);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(linkServiceSelfURI);

        ResponseEntity<JsonNode> getServiceDescriptionResult = restTemplate.getForEntity(serviceDescriptionLinkSelf1, JsonNode.class);
        assertThat(getServiceDescriptionResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<JsonNode> getServiceResult = restTemplate.getForEntity(linkServiceSelfURI, JsonNode.class);
    }

    private Service generateService(String variance) {
        String serviceShortName = "Test Short Name" + variance;
        String serviceTestDescription = "Test Description" + variance;
        String serviceVersion = "1.0" + variance;
        String longName = "Longer Name" + variance;
        Service service = new Service();
        service.setShortName(serviceShortName);
        service.setDescription(serviceTestDescription);
        service.setVersion(serviceVersion);
        service.setName(longName);
        return service;
    }


    private JsonNode postService(Service service) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> resultPostService = restTemplate.postForEntity(getLocalHostWithPort() + "services", service, JsonNode.class);
        assertThat(resultPostService).isNotNull();
        JsonNode body = resultPostService.getBody();
        assertThat(body.get("shortName").asText()).isEqualTo(service.getShortName());
        assertThat(body.get("description").asText()).isEqualTo(service.getDescription());
        assertThat(body.get("version").asText()).isEqualTo(service.getVersion());
        assertThat(body.get("name").asText()).isEqualTo(service.getName());
        return body;
    }


    private ServiceDescription generateServiceDescription(String variance) {
        String serviceDescriptionValue = "Some description" + variance;
        String serviceDescriptionPortVariable = "<PORT>" + variance;
        String serviceDescriptionProtocol = "HTTP" + variance;
        String serviceName = "Some Service Name" + variance;
        ServiceDescription serviceDescription1 = new ServiceDescription();
        serviceDescription1.setDescription(serviceDescriptionValue);
        serviceDescription1.setPort(serviceDescriptionPortVariable);
        serviceDescription1.setProtocol(serviceDescriptionProtocol);
        serviceDescription1.setService_name(serviceName);
        return serviceDescription1;
    }

    private void putServiceDescription(URI serviceDescriptionsURI, URI serviceDescriptionLinkSelf1) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add(HttpHeaders.CONTENT_TYPE, new MediaType("text", "uri-list").toString());
        HttpEntity<String> reqEntity = new HttpEntity<String>(serviceDescriptionLinkSelf1.toString(), reqHeaders);
        ResponseEntity<String> putServiceDescriptionResult = restTemplate.exchange(serviceDescriptionsURI, HttpMethod.PUT, reqEntity, String.class);
        assertThat(putServiceDescriptionResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private URI postServiceDescription(ServiceDescription serviceDescription) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> resultPostServiceDescription = restTemplate.postForEntity(getLocalHostWithPort() + "serviceDescribtions", serviceDescription, JsonNode.class);
        JsonNode serviceDescriptionBody = resultPostServiceDescription.getBody();

        assertThat(serviceDescriptionBody.get("port").asText()).isEqualTo(serviceDescription.getPort());
        assertThat(serviceDescriptionBody.get("description").asText()).isEqualTo(serviceDescription.getDescription());
        assertThat(serviceDescriptionBody.get("protocol").asText()).isEqualTo(serviceDescription.getProtocol());
        assertThat(serviceDescriptionBody.get("service_name").asText()).isEqualTo(serviceDescription.getService_name());

        JsonNode serviceDescriptionLinks = serviceDescriptionBody.get("_links");
        return new URI(serviceDescriptionLinks.get("self").get("href").asText());
    }

    private String getLocalHostWithPort() {
        return HTTP_LOCALHOST + port + "/";
    }
}
