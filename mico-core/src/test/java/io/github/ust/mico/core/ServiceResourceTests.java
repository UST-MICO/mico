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

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.dto.request.MicoServiceRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.status.*;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.resource.ServiceResource;
import io.github.ust.mico.core.service.GitHubCrawler;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceResource.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ServiceResourceTests {

    private static final String BASE_PATH = "/services";

    private static final String JSON_PATH_LINKS_SECTION = buildPath(ROOT, LINKS);
    private static final String SELF_HREF = buildPath(JSON_PATH_LINKS_SECTION, SELF, HREF);
    private static final String SERVICES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "services", HREF);
    private static final String SERVICE_LIST = buildPath(ROOT_EMBEDDED, "micoServiceResponseDTOList");
    private static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    private static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    private static final String VERSION_PATH = buildPath(ROOT, "version");
    private static final String SERVICE_VERSIONS_LIST = buildPath(ROOT_EMBEDDED, "micoVersionRequestDTOList");
    private static final String PATH_PROMOTE = "promote";

    //TODO: Use these variables inside the tests instead of the local variables

    @Value("${cors-policy.allowed-origins}")
    String[] allowedOrigins;

    @MockBean
    MicoStatusService micoStatusService;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private GitHubCrawler crawler;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @Test
    public void getStatusOfService() throws Exception {
        MicoService micoService = new MicoService()
            .setName(NAME)
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

        MicoServiceStatusResponseDTO micoServiceStatus = new MicoServiceStatusResponseDTO();

        KubernetesPodInformationResponseDTO kubernetesPodInfo1 = new KubernetesPodInformationResponseDTO();
        kubernetesPodInfo1
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName1)
            .setMetrics(new KubernetesPodMetricsResponseDTO()
                .setAvailable(false)
                .setCpuLoad(cpuLoadPod1)
                .setMemoryUsage(memoryUsagePod1));
        KubernetesPodInformationResponseDTO kubernetesPodInfo2 = new KubernetesPodInformationResponseDTO();
        kubernetesPodInfo2
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName2)
            .setMetrics(new KubernetesPodMetricsResponseDTO()
                .setAvailable(true)
                .setCpuLoad(cpuLoadPod2)
                .setMemoryUsage(memoryUsagePod2));

        micoServiceStatus
            .setVersion(VERSION)
            .setName(NAME)
            .setShortName(SHORT_NAME)
            .setAvailableReplicas(availableReplicas)
            .setRequestedReplicas(requestedReplicas)
            .setNodeMetrics(CollectionUtils.listOf(new KubernetesNodeMetricsResponseDTO()
                .setNodeName(nodeName)
                .setAverageCpuLoad(25)
                .setAverageMemoryUsage(60)
            ))
            .setInterfacesInformation(CollectionUtils.listOf(new MicoServiceInterfaceStatusResponseDTO().setName(SERVICE_INTERFACE_NAME)))
            .setPodsInformation(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2));

        given(micoStatusService.getServiceStatus(any(MicoService.class))).willReturn(micoServiceStatus);
        given(serviceRepository.findByShortNameAndVersion(ArgumentMatchers.anyString(), ArgumentMatchers.any())).willReturn(Optional.of(micoService));

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/status"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SERVICE_DTO_SERVICE_NAME, is(NAME)))
            .andExpect(jsonPath(SERVICE_DTO_REQUESTED_REPLICAS, is(requestedReplicas)))
            .andExpect(jsonPath(SERVICE_DTO_AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(SERVICE_DTO_NODE_NAME, is(nodeName)))
            .andExpect(jsonPath(SERVICE_DTO_NODE_METRICS_AVERAGE_CPU_LOAD, is(25)))
            .andExpect(jsonPath(SERVICE_DTO_NODE_METRICS_AVERAGE_MEMORY_USAGE, is(60)))
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
            .andExpect(jsonPath(SERVICE_DTO_POD_INFO_METRICS_AVAILABLE_2, is(true)))
            .andExpect(jsonPath(SERVICE_DTO_ERROR_MESSAGES, is(CollectionUtils.listOf())));
    }

    @Test
    public void getCompleteServiceList() throws Exception {
        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(
            CollectionUtils.listOf(
                new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setName(NAME_1).setDescription(DESCRIPTION_1),
                new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_2).setName(NAME_2).setDescription(DESCRIPTION_2),
                new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_3).setName(NAME_3).setDescription(DESCRIPTION_3)));

        mvc.perform(get("/services").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SERVICE_LIST + "[*]", hasSize(3)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_1_MATCHER + " && " + VERSION_1_0_1_MATCHER +
                " && " + NAME_1_MATCHER + " && " + DESCRIPTION_1_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_2_MATCHER + " && " + VERSION_1_0_2_MATCHER +
                " && " + NAME_2_MATCHER + " && " + DESCRIPTION_2_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST + "[?(" + SHORT_NAME_3_MATCHER + " && " + VERSION_1_0_3_MATCHER +
                " && " + NAME_3_MATCHER + " && " + DESCRIPTION_3_MATCHER + ")]", hasSize(1)))
            .andExpect(jsonPath(SELF_HREF, is(BASE_URL + SERVICES_PATH)))
            .andReturn();
    }

    @Test
    public void getServiceViaShortNameAndVersion() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION).setDescription(DESCRIPTION)));

        String urlPath = SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION;
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

    @Test
    public void createService() throws Exception {
    	MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        final ResultActions result = mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(new MicoServiceRequestDTO(service)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isCreated());
    }

    @Test
    public void createServiceWithInvalidShortName() throws Exception {
    	MicoServiceRequestDTO serviceRequestDto = new MicoServiceRequestDTO()
            .setShortName(SHORT_NAME_INVALID)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(serviceRequestDto)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createServiceWithoutRequiredName() throws Exception {
    	MicoServiceRequestDTO serviceRequestDto = new MicoServiceRequestDTO()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(null)
            .setDescription(DESCRIPTION);

        mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(serviceRequestDto)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createServiceWithDescriptionSetToNull() throws Exception {
        MicoServiceRequestDTO serviceRequestDto = new MicoServiceRequestDTO()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(null);

        MicoService expectedService = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("");

        ArgumentCaptor<MicoService> serviceArgumentCaptor = ArgumentCaptor.forClass(MicoService.class);

        given(serviceRepository.save(any(MicoService.class))).willReturn(expectedService);

        mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(serviceRequestDto)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(serviceRepository, times(1)).save(serviceArgumentCaptor.capture());
        MicoService savedMicoService = serviceArgumentCaptor.getValue();
        assertNotNull(savedMicoService);
        assertEquals("Actual service does not match expected", expectedService, savedMicoService);
    }

    @Test
    public void createServiceWithEmptyDescription() throws Exception {
        MicoServiceRequestDTO serviceRequestDto = new MicoServiceRequestDTO()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("");

        MicoService expectedService = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("");

        ArgumentCaptor<MicoService> serviceArgumentCaptor = ArgumentCaptor.forClass(MicoService.class);

        given(serviceRepository.save(any(MicoService.class))).willReturn(expectedService);

        mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(serviceRequestDto)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(serviceRepository, times(1)).save(serviceArgumentCaptor.capture());
        MicoService savedMicoService = serviceArgumentCaptor.getValue();
        assertNotNull(savedMicoService);
        assertEquals("Actual service does not match expected", expectedService, savedMicoService);
    }

    @Test
    public void createServiceWithInvalidGitCloneUrl() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setGitCloneUrl("invalid-url");

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        mvc.perform(post(SERVICES_PATH)
            .content(mapper.writeValueAsBytes(new MicoServiceRequestDTO(service))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteAllServiceDependees() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        ResultActions resultDelete = mvc.perform(delete(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + DEPENDEES_SUBPATH)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultDelete.andExpect(status().isNoContent());
    }

    @Test
    public void deleteSpecificServiceDependee() throws Exception {
        String shortName = SHORT_NAME_1;
        String version = VERSION_1_0_1;
        String shortNameToDelete = SHORT_NAME_2;
        String versionToDelete = VERSION_1_0_2;
        MicoService service = new MicoService().setShortName(shortName).setVersion(version).setName(NAME);
        MicoService serviceToDelete = new MicoService().setShortName(shortNameToDelete).setVersion(versionToDelete).setName(NAME);

        given(serviceRepository.findByShortNameAndVersion(shortName, version)).willReturn(Optional.of(service));
        given(serviceRepository.save(any(MicoService.class))).willReturn(service);
        given(serviceRepository.findByShortNameAndVersion(shortNameToDelete, versionToDelete)).willReturn(Optional.of(serviceToDelete));

        ResultActions resultDelete = mvc.perform(delete(SERVICES_PATH + "/" + shortName + "/" + version +
            DEPENDEES_SUBPATH + "/" + shortNameToDelete + "/" + versionToDelete)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultDelete.andExpect(status().isNoContent());
    }

    @Test
    public void deleteAllServices() throws Exception {
        MicoService micoServiceOne = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);
        MicoService micoServiceTwo = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION_1_0_1)
            .setName(NAME);
        MicoService micoServiceThree = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION_1_0_2)
            .setName(NAME);

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
            .setName(NAME)
            .setDescription(DESCRIPTION);

        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
                .setShortName(SHORT_NAME_2)
                .setVersion(VERSION_1_0_2)
                .setDescription(DESCRIPTION_2);
        MicoService service3 = new MicoService()
            .setShortName(SHORT_NAME_3)
            .setVersion(VERSION_1_0_3)
            .setName(NAME)
            .setDescription(DESCRIPTION_3);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service1).setDependedService(service);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service2).setDependedService(service);
        MicoServiceDependency dependency3 = new MicoServiceDependency().setService(service3).setDependedService(service);

        service1.setDependencies(Collections.singletonList(dependency1));
        service2.setDependencies(Collections.singletonList(dependency2));
        service3.setDependencies(Collections.singletonList(dependency3));

        given(serviceRepository.findDependers(service.getShortName(), service.getVersion())).willReturn(CollectionUtils.listOf(service1, service2, service3));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        String urlPath = SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + DEPENDERS_SUBPATH;
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
        MicoService existingService = new MicoService()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);
        MicoServiceRequestDTO updatedServiceRequestDto = new MicoServiceRequestDTO()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(updatedDescription);
        MicoService expectedService = new MicoService()
            .setId(existingService.getId())
            .setShortName(updatedServiceRequestDto.getShortName())
            .setVersion(updatedServiceRequestDto.getVersion())
            .setName(updatedServiceRequestDto.getName())
            .setDescription(updatedServiceRequestDto.getDescription());

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService));
        given(serviceRepository.save(eq(expectedService))).willReturn(expectedService);

        ResultActions resultUpdate = mvc.perform(put(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedServiceRequestDto))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedDescription)))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(VERSION_PATH, is(VERSION)));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void updateServiceWithoutRequiredName() throws Exception {
        MicoService existingService = new MicoService()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);
        MicoService updatedService = new MicoService()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(null);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService));

        ResultActions resultUpdate = mvc.perform(put(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(new MicoServiceRequestDTO(updatedService)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultUpdate.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateServiceWithDescriptionSetToNull() throws Exception {
        MicoService existingService = new MicoService()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);
        MicoServiceRequestDTO updatedServiceRequestDto = new MicoServiceRequestDTO()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(null);

        MicoService expectedService = new MicoService()
            .setId(existingService.getId())
            .setShortName(existingService.getShortName())
            .setVersion(existingService.getVersion())
            .setName(existingService.getName())
            .setDescription("");

        ArgumentCaptor<MicoService> serviceArgumentCaptor = ArgumentCaptor.forClass(MicoService.class);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService));
        given(serviceRepository.save(any(MicoService.class))).willReturn(expectedService);

        mvc.perform(put(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedServiceRequestDto))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        verify(serviceRepository, times(1)).save(serviceArgumentCaptor.capture());
        MicoService savedMicoService = serviceArgumentCaptor.getValue();
        assertNotNull(savedMicoService);
        assertEquals("Actual service does not match expected", expectedService, savedMicoService);
    }

    @Test
    public void deleteService() throws Exception {
        MicoService existingService = new MicoService()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService));

        ResultActions resultDelete = mvc.perform(delete(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultDelete.andExpect(status().isNoContent());
    }

    @Test
    public void getVersionsOfService() throws Exception {
        given(serviceRepository.findByShortName(SHORT_NAME)).willReturn(
            CollectionUtils.listOf(
                new MicoService().setShortName(SHORT_NAME).setVersion(VERSION).setName(NAME),
                new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1).setName(NAME),
                new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_2).setName(NAME)));

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
            .setName(NAME);

        MicoService existingService2 = new MicoService()
            .setId(ID_2)
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME);

        MicoServiceDependency newDependency = new MicoServiceDependency()
            .setService(new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setName(NAME))
            .setDependedService(new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setName(NAME));

        MicoService expectedService = new MicoService()
            .setId(ID_1)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDependencies(Collections.singletonList(newDependency));

        prettyPrint(expectedService);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingService1));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1)).willReturn(Optional.of(existingService2));
        given(serviceRepository.save(any(MicoService.class))).willReturn(expectedService);

        final ResultActions result = mvc.perform(post(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION +
            DEPENDEES_SUBPATH + "/" + newDependency.getDependedService().getShortName() + "/" + newDependency.getDependedService().getVersion())
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isNoContent());
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
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION);
        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service).setDependedService(service2);
        service.setDependencies(CollectionUtils.listOf(dependency1, dependency2));

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findDependees(service.getShortName(), service.getVersion())).willReturn(CollectionUtils.listOf(service1, service2));

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
    public void deleteServiceWithDeployedService() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(serviceRepository.findByShortName(SHORT_NAME)).willReturn(Collections.singletonList(service));
        given(micoKubernetesClient.isMicoServiceDeployed(any())).willReturn(true);

        mvc.perform(delete(SERVICES_PATH + "/" + SHORT_NAME)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    public void deleteSpecificServiceWithDeployedService() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(micoKubernetesClient.isMicoServiceDeployed(any())).willReturn(true);

        mvc.perform(delete(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    public void createServiceViaGitHubCrawler() {
        //TODO: Implementation
    }

    @Test
    public void promoteService() throws Exception {
        MicoServiceInterface micoServiceInterface = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME);

        MicoServiceInterface micoServiceInterfaceTwo = new MicoServiceInterface()
                .setServiceInterfaceName(SERVICE_INTERFACE_NAME_1);

        List<MicoServiceInterface> micoServiceInterfaces = new LinkedList<>();
        micoServiceInterfaces.add(micoServiceInterface);
        micoServiceInterfaces.add(micoServiceInterfaceTwo);

        MicoService micoService = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setDescription(DESCRIPTION_1)
                .setId(ID)
                .setServiceInterfaces(micoServiceInterfaces);

        String newVersion = VERSION_1_0_1;
        Long newId = ID_1;

        MicoService promotedService = micoService.setId(null).setVersion(newVersion);
        MicoService savedPromotedService = promotedService.setId(newId);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoService));
        given(serviceRepository.save(eq(promotedService))).willReturn(savedPromotedService);

        ResultActions resultPromotion = mvc.perform(post(SERVICES_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_PROMOTE)
                .content(mapper.writeValueAsBytes(new MicoVersionRequestDTO(newVersion)))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(jsonPath(SHORT_NAME_PATH, is(savedPromotedService.getShortName())))
                .andExpect(jsonPath(VERSION_PATH, is(newVersion)))
                .andExpect(jsonPath(DESCRIPTION_PATH, is(savedPromotedService.getDescription())));

        resultPromotion.andExpect(status().isOk());
    }
    
    @Test
    public void getVersionsFromGitHub() throws Exception {
    	List<String> versions = CollectionUtils.listOf("v1.0.0", "v2.0.0", "v3.0.0");
    	
    	given(crawler.getVersionsFromGitHubRepo(anyString())).willReturn(versions);

        ResultActions resultPromotion = mvc.perform(get(SERVICES_PATH + "/import/github")
                .param("url", anyString()))
                .andDo(print())
                .andExpect(jsonPath(SERVICE_VERSIONS_LIST + "[*]", hasSize(3)))
                .andExpect(jsonPath(SERVICE_VERSIONS_LIST + "[0].version", is(versions.get(0))))
                .andExpect(jsonPath(SERVICE_VERSIONS_LIST + "[1].version", is(versions.get(1))))
                .andExpect(jsonPath(SERVICE_VERSIONS_LIST + "[2].version", is(versions.get(2))));

        resultPromotion.andExpect(status().isOk());
    }

}
