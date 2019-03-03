/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInfoDTO;
import io.github.ust.mico.core.dto.MicoServiceStatusDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.web.ServiceController;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static io.github.ust.mico.core.ApplicationControllerTests.INTERFACES_LIST_PATH;
import static io.github.ust.mico.core.JsonPathBuilder.HREF;
import static io.github.ust.mico.core.JsonPathBuilder.LINKS;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.JsonPathBuilder.SELF;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT_EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ServiceControllerTests {

    private static final String BASE_PATH = "/services";

    private static final String JSON_PATH_LINKS_SECTION = buildPath(ROOT, LINKS);
    private static final String SELF_HREF = buildPath(JSON_PATH_LINKS_SECTION, SELF, HREF);
    private static final String SERVICES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "services", HREF);
    public static final String SERVICE_LIST = buildPath(ROOT_EMBEDDED, "micoServiceList");
    private static final String ID_PATH = buildPath(ROOT, "id");
    private static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    private static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    private static final String VERSION_PATH = buildPath(ROOT, "version");

    //TODO: Use these variables inside the tests instead of the local variables

    @Value("${cors-policy.allowed-origins}")
    String[] allowedOrigins;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    MicoStatusService micoStatusService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getStatusOfService() throws Exception {
        MicoService micoService = new MicoService()
                .setName(SERVICE_NAME)
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION_1);

        String nodeName = "testNode";
        String podPhase = "Running";
        String hostIp = "192.168.0.0";
        String podName1 = "pod1";
        String podName2 = "pod2";
        int availableReplicas = 1;
        int requestedReplicas = 2;
        int memoryUsagePod1 = 50;
        int cpuLoadPod1 = 10;
        int memoryUsagePod2 = 70;
        int cpuLoadPod2 = 40;

        MicoServiceStatusDTO micoServiceStatus = new MicoServiceStatusDTO();

        KubernetesPodInfoDTO kubernetesPodInfo1 = new KubernetesPodInfoDTO();
        kubernetesPodInfo1
                .setHostIp(hostIp)
                .setNodeName(nodeName)
                .setPhase(podPhase)
                .setPodName(podName1)
                .setMetrics(new KuberenetesPodMetricsDTO()
                        .setAvailable(false)
                        .setCpuLoad(cpuLoadPod1)
                        .setMemoryUsage(memoryUsagePod1));
        KubernetesPodInfoDTO kubernetesPodInfo2 = new KubernetesPodInfoDTO();
        kubernetesPodInfo2
                .setHostIp(hostIp)
                .setNodeName(nodeName)
                .setPhase(podPhase)
                .setPodName(podName2)
                .setMetrics(new KuberenetesPodMetricsDTO()
                        .setAvailable(true)
                        .setCpuLoad(cpuLoadPod2)
                        .setMemoryUsage(memoryUsagePod2));

        micoServiceStatus
                .setVersion(VERSION)
                .setName(SERVICE_NAME)
                .setShortName(SHORT_NAME)
                .setAvailableReplicas(availableReplicas)
                .setRequestedReplicas(requestedReplicas)
                .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME)))
                .setPodInfo(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2));

        given(micoStatusService.getServiceStatus(any(MicoService.class))).willReturn(micoServiceStatus);
        given(serviceRepository.findByShortNameAndVersion(ArgumentMatchers.anyString(), ArgumentMatchers.any())).willReturn(Optional.of(micoService));

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(SERVICE_DTO_SERVICE_NAME, is(SERVICE_NAME)))
                .andExpect(jsonPath(SERVICE_DTO_REQUESTED_REPLICAS, is(requestedReplicas)))
                .andExpect(jsonPath(SERVICE_DTO_AVAILABLE_REPLICAS, is(availableReplicas)))
                .andExpect(jsonPath(SERVICE_DTO_INTERFACES_INFORMATION, hasSize(1)))
                .andExpect(jsonPath(SERVICE_DTO_INTERFACES_INFORMATION_NAME, is(SERVICE_INTERFACE_NAME)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO, hasSize(2)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_POD_NAME_1, is(podName1)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_PHASE_1, is(podPhase)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_NODE_NAME_1, is(nodeName)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_MEMORY_USAGE_1, is(memoryUsagePod1)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_CPU_LOAD_1, is(cpuLoadPod1)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_AVAILABLE_1, is(false)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_POD_NAME_2, is(podName2)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_PHASE_2, is(podPhase)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_NODE_NAME_2, is(nodeName)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_MEMORY_USAGE_2, is(memoryUsagePod2)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_CPU_LOAD_2, is(cpuLoadPod2)))
                .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_AVAILABLE_2, is(true)));
    }

    @Test
    public void getCompleteServiceList() throws Exception {
        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(
                Arrays.asList(
                        new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setDescription(DESCRIPTION_1),
                        new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_2).setDescription(DESCRIPTION_2),
                        new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_3).setDescription(DESCRIPTION_3)));

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
                Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION).setDescription(DESCRIPTION)));

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

    //TODO: Verify how to test an autogenerated id
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
                .willReturn(Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION).setDescription(DESCRIPTION)));

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
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test

    public void createInvalidService() throws Exception {
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME_INVALID)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        mvc.perform(post(SERVICES_PATH)
                .content(mapper.writeValueAsBytes(service)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(status().reason("The name of the service is not valid."))
                .andReturn();
    }

    public void createServiceWithExistingInterfaces() throws Exception {
        MicoServiceInterface serviceInterface1 = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME);
        MicoServiceInterface serviceInterface2 = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME_1);

        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION)
                .setServiceInterfaces(CollectionUtils.listOf(
                        serviceInterface1, serviceInterface2
                ));

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);
        given(serviceRepository.findInterfaceOfServiceByName(
                serviceInterface1.getServiceInterfaceName(), service.getShortName(), service.getVersion()))
                .willReturn(Optional.of(serviceInterface1));
        given(serviceRepository.findInterfaceOfServiceByName(
                serviceInterface2.getServiceInterfaceName(), service.getShortName(), service.getVersion()))
                .willReturn(Optional.of(serviceInterface2));

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(jsonPath(SHORT_NAME_PATH, is(service.getShortName())))
                .andExpect(jsonPath(VERSION_PATH, is(service.getVersion())))
                .andExpect(jsonPath(INTERFACES_LIST_PATH + "[*]", hasSize(2)));

        result.andExpect(status().isCreated());
    }

    @Test
    public void createServiceWithNotExistingInterfaces() throws Exception {
        MicoServiceInterface serviceInterface1 = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME);
        MicoServiceInterface serviceInterface2 = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME_1);

        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION)
                .setServiceInterfaces(CollectionUtils.listOf(
                        serviceInterface1, serviceInterface2
                ));

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);
        // Only one of the two interfaces exists -> exception
        given(serviceRepository.findInterfaceOfServiceByName(
                serviceInterface1.getServiceInterfaceName(), service.getShortName(), service.getVersion()))
                .willReturn(Optional.of(serviceInterface1));

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createServiceWithInconsistentInterfaceData() throws Exception {
        MicoServiceInterface existingServiceInterface = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                .setDescription(DESCRIPTION);
        MicoServiceInterface invalidServiceInterface = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                .setDescription("INVALID");

        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION)
                .setServiceInterfaces(CollectionUtils.listOf(
                        invalidServiceInterface
                ));

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);
        // Interface exists with different data
        given(serviceRepository.findInterfaceOfServiceByName(
                existingServiceInterface.getServiceInterfaceName(), service.getShortName(), service.getVersion()))
                .willReturn(Optional.of(existingServiceInterface));

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
                .content(mapper.writeValueAsBytes(service))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isConflict());

    }

    @Test
    public void deleteAllServiceDependees() throws Exception {
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);

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
        MicoService service = new MicoService().setShortName(shortName).setVersion(version).setDescription(description);
        MicoService serviceToDelete = new MicoService().setShortName(shortNameToDelete).setVersion(versionToDelete);

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
    public void deleteAllServices() throws Exception {
        MicoService micoServiceOne = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION);
        MicoService micoServiceTwo = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION_1_0_1);
        MicoService micoServiceThree = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION_1_0_2);

        given(serviceRepository.findByShortName(SHORT_NAME)).willReturn(CollectionUtils.listOf(micoServiceOne, micoServiceTwo, micoServiceThree));

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();
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
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);

        MicoService service1 = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
                .setShortName(SHORT_NAME_2)
                .setVersion(VERSION_1_0_2)
                .setDescription(DESCRIPTION_2);
        MicoService service3 = new MicoService()
                .setShortName(SHORT_NAME_3)
                .setVersion(VERSION_1_0_3)
                .setDescription(DESCRIPTION_3);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service1).setDependedService(service);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service2).setDependedService(service);
        MicoServiceDependency dependency3 = new MicoServiceDependency().setService(service3).setDependedService(service);

        service1.setDependencies(Collections.singletonList(dependency1));
        service2.setDependencies(Collections.singletonList(dependency2));
        service3.setDependencies(Collections.singletonList(dependency3));

        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(Arrays.asList(service, service1, service2, service3));
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
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);
        MicoService updatedService = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(updatedDescription);

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
    public void updateServiceUsesExistingInterfaces() throws Exception {
        String updatedDescription = "updated description.";
        MicoServiceInterface serviceInterface = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                .setDescription(DESCRIPTION);
        MicoService existingService = new MicoService()
                .setId(ID)
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION)
                .setServiceInterfaces(CollectionUtils.listOf(
                        serviceInterface
                ));
        MicoService updatedService = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(updatedDescription);
        MicoService expectedService = new MicoService()
                .setId(existingService.getId())
                .setShortName(updatedService.getShortName())
                .setVersion(updatedService.getVersion())
                .setDescription(updatedService.getDescription())
                .setServiceInterfaces(existingService.getServiceInterfaces());

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService));
        given(serviceRepository.save(eq(expectedService))).willReturn(expectedService);
        given(serviceRepository.findInterfaceOfServiceByName(serviceInterface.getServiceInterfaceName(),
                existingService.getShortName(), existingService.getVersion())).willReturn(Optional.of(serviceInterface));

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
                .andExpect(jsonPath(ID_PATH, is(existingService.getId().intValue())))
                .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedDescription)))
                .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
                .andExpect(jsonPath(VERSION_PATH, is(VERSION)))
                .andExpect(jsonPath(INTERFACES_LIST_PATH + "[*]", hasSize(1)));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void updateServiceIsOnlyAllowedWithoutUsingExistingInterfaces() throws Exception {
        String updatedDescription = "updated description.";
        MicoServiceInterface serviceInterface = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME)
                .setDescription(DESCRIPTION);
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION)
                .setServiceInterfaces(CollectionUtils.listOf(
                        serviceInterface
                ));
        MicoService updatedService = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(updatedDescription)
                .setServiceInterfaces(CollectionUtils.listOf(
                        serviceInterface
                ));

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

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
                .andDo(print());

        resultUpdate.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteService() throws Exception {
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);

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

        resultDelete.andExpect(status().isNoContent());
    }

    @Test
    public void getVersionsOfService() throws Exception {
        given(serviceRepository.findByShortName(SHORT_NAME)).willReturn(
                Arrays.asList(
                        new MicoService().setShortName(SHORT_NAME).setVersion(VERSION),
                        new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1),
                        new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_2)));

        mvc.perform(get("/services/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(3)))
                .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_MATCHER + " && " + VERSION_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_MATCHER + " && " + VERSION_1_0_1_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_MATCHER + " && " + VERSION_1_0_2_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(SELF_HREF, is(BASE_URL + SERVICES_PATH + "/" + SHORT_NAME)))
                .andReturn();
    }

    @Test
    public void createNewDependee() throws Exception {
        MicoService existingService1 = new MicoService()
                .setId(ID_1)
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setName(APPLICATION_NAME);

        MicoService existingService2 = new MicoService()
                .setId(ID_2)
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setName(APPLICATION_NAME);

        MicoServiceDependency newDependency = new MicoServiceDependency()
                .setService(new MicoService()
                    .setShortName(SHORT_NAME)
                    .setVersion(VERSION)
                    .setName(APPLICATION_NAME))
                .setDependedService(new MicoService()
                    .setShortName(SHORT_NAME_1)
                    .setVersion(VERSION_1_0_1)
                    .setName(APPLICATION_NAME));

        MicoService expectedService = new MicoService()
            .setId(ID_1)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(APPLICATION_NAME)
            .setDependencies(Collections.singletonList(newDependency));

        prettyPrint(expectedService);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService1));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1)).willReturn(Optional.of(existingService2));
        given(serviceRepository.save(any(MicoService.class))).willReturn(expectedService);

        final ResultActions result = mvc.perform(post(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH)
                .content(mapper.writeValueAsBytes(newDependency))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print());

        result.andExpect(status().isCreated());
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

    @Test
    public void getDependees() throws Exception {
        MicoService service1 = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
                .setShortName(SHORT_NAME_2)
                .setVersion(VERSION_1_0_2)
                .setDescription(DESCRIPTION_2);
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION);
        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service).setDependedService(service2);
        service.setDependencies(Arrays.asList(dependency1, dependency2));

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1)).willReturn(Optional.of(service1));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_2, VERSION_1_0_2)).willReturn(Optional.of(service2));


        mvc.perform(get("/services/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH).accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(2)))
                .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_1_MATCHER + " && " + VERSION_1_0_1_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_2_MATCHER + " && " + VERSION_1_0_2_MATCHER + ")]", hasSize(1)))
                .andExpect(jsonPath(SELF_HREF, is(BASE_URL + SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH)))
                .andReturn();
    }

    @Test
    public void createServiceViaGitHubCrawler() {
        //TODO: Implementation
    }

}
