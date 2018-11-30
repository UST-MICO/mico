package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    @Autowired
    private DependsOnRepository dependsOnRepository;
    @Autowired
    private ServiceInterfaceRepository serviceInterfaceRepository;

    @Test
    public void testServicesWithMultipleDescriptions() throws Exception {
        //TODO: evaluate json-path-assert,json-path from com.jayway.jsonpath
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
        Service service = generateService("1");

        RestTemplate restTemplate = new RestTemplate();
        JsonNode body = postService(service);

        JsonNode links = body.get("_links");
        JsonNode linkServiceInterfaces = links.get("serviceInterfaces").get("href");
        URI serviceInterfacesURI = new URI(linkServiceInterfaces.asText());

        String serviceInterfaceValue = "Some description";
        String serviceInterfacePortVariable = "<PORT>";
        String serviceInterfaceProtocol = "HTTP";
        String serviceName = "Some Service Name";

        ServiceInterface serviceInterface1 = generateServiceInterface("1");
        ServiceInterface serviceInterface2 = generateServiceInterface("2");

        URI serviceInterfaceLinkSelf1 = postServiceInterface(serviceInterface1);
        URI serviceInterfaceLinkSelf2 = postServiceInterface(serviceInterface2);

        putServiceInterface(serviceInterfacesURI, serviceInterfaceLinkSelf1);
        putServiceInterface(serviceInterfacesURI, serviceInterfaceLinkSelf2);

        ResponseEntity<JsonNode> getResultServiceToServiceInterface = restTemplate.getForEntity(serviceInterfacesURI, JsonNode.class);

        assertThat(getResultServiceToServiceInterface.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode serviceInterfacesList = getResultServiceToServiceInterface.getBody().get("_embedded").get("serviceInterfaces");
        assertThat(serviceInterfacesList.size()).isEqualTo(2);
    }

    @Test(expected = HttpClientErrorException.NotFound.class)
    public void testDeleteService() throws Exception {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();

        Service service = generateService("2");
        JsonNode serviceResult = postService(service);
        JsonNode links = serviceResult.get("_links");
        JsonNode linkServiceInterfaces = links.get("serviceInterfaces").get("href");
        JsonNode linkServiceSelf = links.get("self").get("href");
        URI serviceInterfacesURI = new URI(linkServiceInterfaces.asText());
        URI linkServiceSelfURI = new URI(linkServiceSelf.asText());

        ServiceInterface serviceInterface1 = generateServiceInterface("2");
        URI serviceInterfaceLinkSelf1 = postServiceInterface(serviceInterface1);
        putServiceInterface(serviceInterfacesURI, serviceInterfaceLinkSelf1);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(linkServiceSelfURI);

        ResponseEntity<JsonNode> getServiceInterfaceResult = restTemplate.getForEntity(serviceInterfaceLinkSelf1, JsonNode.class);
        assertThat(getServiceInterfaceResult.getStatusCode()).isEqualTo(HttpStatus.OK);
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


    private ServiceInterface generateServiceInterface(String variance) {
        String serviceInterfaceValue = "Some description" + variance;
        String serviceInterfacePortVariable = "<PORT>" + variance;
        String serviceInterfaceProtocol = "HTTP" + variance;
        String serviceName = "Some Service Name" + variance;
        ServiceInterface serviceInterface1 = new ServiceInterface();
        serviceInterface1.setDescription(serviceInterfaceValue);
        serviceInterface1.setPort(serviceInterfacePortVariable);
        serviceInterface1.setProtocol(serviceInterfaceProtocol);
        serviceInterface1.setServiceName(serviceName);
        return serviceInterface1;
    }

    private void putServiceInterface(URI serviceInterfacesURI, URI serviceInterfaceLinkSelf1) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add(HttpHeaders.CONTENT_TYPE, new MediaType("text", "uri-list").toString());
        HttpEntity<String> reqEntity = new HttpEntity<String>(serviceInterfaceLinkSelf1.toString(), reqHeaders);
        ResponseEntity<String> putServiceInterfaceResult = restTemplate.exchange(serviceInterfacesURI, HttpMethod.PUT, reqEntity, String.class);
        assertThat(putServiceInterfaceResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private URI postServiceInterface(ServiceInterface serviceInterface) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> resultPostServiceInterface = restTemplate.postForEntity(getLocalHostWithPort() + "serviceInterfaces", serviceInterface, JsonNode.class);
        JsonNode serviceInterfaceBody = resultPostServiceInterface.getBody();

        assertThat(serviceInterfaceBody.get("port").asText()).isEqualTo(serviceInterface.getPort());
        assertThat(serviceInterfaceBody.get("description").asText()).isEqualTo(serviceInterface.getDescription());
        assertThat(serviceInterfaceBody.get("protocol").asText()).isEqualTo(serviceInterface.getProtocol());
        assertThat(serviceInterfaceBody.get("serviceName").asText()).isEqualTo(serviceInterface.getServiceName());

        JsonNode serviceInterfaceLinks = serviceInterfaceBody.get("_links");
        return new URI(serviceInterfaceLinks.get("self").get("href").asText());
    }

    private String getLocalHostWithPort() {
        return HTTP_LOCALHOST + port + "/";
    }

    @Test
    public void cleanupDatabase() {
        serviceRepository.deleteAll();
        dependsOnRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
    }
}
