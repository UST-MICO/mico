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

import static io.github.ust.mico.core.JsonPathBuilder.EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.JsonPathBuilder.SELF_HREF;
import static io.github.ust.mico.core.JsonPathBuilder.buildPath;
import static io.github.ust.mico.core.TestConstants.DESCRIPTION;
import static io.github.ust.mico.core.TestConstants.GIT_TEST_REPO_URL;
import static io.github.ust.mico.core.TestConstants.ID;
import static io.github.ust.mico.core.TestConstants.ID_1;
import static io.github.ust.mico.core.TestConstants.ID_2;
import static io.github.ust.mico.core.TestConstants.ID_3;
import static io.github.ust.mico.core.TestConstants.SERVICE_SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SERVICE_VERSION;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_1_MATCHER;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME_MATCHER;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.VERSION_1_0_1;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.dto.KuberenetesPodMetricsDTO;
import io.github.ust.mico.core.dto.KubernetesPodInfoDTO;
import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.dto.MicoServiceInterfaceDTO;
import io.github.ust.mico.core.dto.MicoServiceStatusDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfoQueryResult;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.web.ApplicationController;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ApplicationControllerTests {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String APPLICATION_LIST = buildPath(EMBEDDED, "micoApplicationList");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_PATH = buildPath(ROOT, "version");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String SERVICE_DEPLOYMENT_INFO_LIST_PATH = buildPath(ROOT, "serviceDeploymentInfos");
    public static final String INTERFACES_LIST_PATH = buildPath(ROOT, "serviceInterfaces");
    public static final String ID_PATH = buildPath(ROOT, "id");

    private static final String BASE_PATH = "/applications";

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private MicoStatusService micoStatusService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getAllApplications() throws Exception {
        given(applicationRepository.findAll(3)).willReturn(
            Arrays.asList(new MicoApplication().setId(ID_1).setShortName(SHORT_NAME).setVersion(VERSION_1_0_1),
                new MicoApplication().setId(ID_2).setShortName(SHORT_NAME).setVersion(VERSION),
                new MicoApplication().setId(ID_3).setShortName(SHORT_NAME_1).setVersion(VERSION)));

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
            Optional.of(new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get("/applications/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
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
    public void getApplicationByShortName() throws Exception {
        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(
            CollectionUtils.listOf(new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get("/applications/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(APPLICATION_LIST + "[?(" + SHORT_NAME_MATCHER + "&& @.version=='" + VERSION + "')]", hasSize(1)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/" + SHORT_NAME)))
            .andReturn();
    }

    @Test
    public void getApplicationByShortNameWithTrailingSlash() throws Exception {
        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(
            CollectionUtils.listOf(new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get("/applications/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void createApplicationWithoutServices() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);

        prettyPrint(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(application.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(application.getVersion())))
            .andExpect(jsonPath(SERVICE_DEPLOYMENT_INFO_LIST_PATH, IsEmptyCollection.empty()));

        result.andExpect(status().isCreated());
    }

    @Test
    public void createApplicationWithExistingServices() throws Exception {
        MicoService service1 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService service2 = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION);

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);
        
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(application)
                .setService(service1));
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(application)
                .setService(service2));

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);
        given(serviceRepository.findByShortNameAndVersion(
            eq(service1.getShortName()), eq(service1.getVersion())))
            .willReturn(Optional.of(service1));
        given(serviceRepository.findByShortNameAndVersion(
            eq(service2.getShortName()), eq(service2.getVersion())))
            .willReturn(Optional.of(service2));

        prettyPrint(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(application.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(application.getVersion())))
            .andExpect(jsonPath(SERVICE_DEPLOYMENT_INFO_LIST_PATH + "[*]", hasSize(2)));

        result.andExpect(status().isCreated());
    }

    @Test
    public void createApplicationWithNotExistingServices() throws Exception {
        MicoService service1 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService service2 = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION);

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);
        
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(application)
                .setService(service1));
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(application)
                .setService(service2));

        // Only one of the two MicoService exists -> exception
        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);

        prettyPrint(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createApplicationWithInconsistentServiceData() throws Exception {
        MicoService invalidMicoService = new MicoService()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setGitCloneUrl("http://example.com/INVALID");

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);
        
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(application)
                .setService(invalidMicoService));

        // MicoService exist with different data
        given(applicationRepository.findByShortNameAndVersion(eq(application.getShortName()),
                eq(application.getVersion()))).willReturn(Optional.of(application));

        prettyPrint(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isConflict());
    }

    @Test
    public void updateApplication() throws Exception {
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription("newDesc");

        MicoApplication expectedApplication = new MicoApplication()
            .setId(existingApplication.getId())
            .setShortName(updatedApplication.getShortName())
            .setVersion(updatedApplication.getVersion())
            .setDescription(updatedApplication.getDescription());

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);
        //given(applicationRepository.save(any(MicoApplication.class))).willThrow(new RuntimeException("Unexpected MicoApplication"));

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(ID_PATH, is(existingApplication.getId().intValue())))
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())))
            .andExpect(jsonPath(SERVICE_DEPLOYMENT_INFO_LIST_PATH + "[*]", IsEmptyCollection.empty()));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void updateApplicationUsesExistingServices() throws Exception {
        MicoService existingService = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setGitCloneUrl(GIT_TEST_REPO_URL);

        MicoApplication existingApplication = new MicoApplication()
                .setId(ID)
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setDescription("newDesc");

        MicoApplication expectedApplication = new MicoApplication()
                .setId(existingApplication.getId())
                .setShortName(updatedApplication.getShortName())
                .setVersion(updatedApplication.getVersion())
                .setDescription(updatedApplication.getDescription());

        updatedApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(updatedApplication)
                .setService(existingService));
        
        expectedApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(expectedApplication)
                .setService(existingService));

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION))
                .willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);

        ResultActions resultUpdate = mvc
                .perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
                        .content(mapper.writeValueAsBytes(updatedApplication))
                        .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print()).andExpect(jsonPath(ID_PATH, is(existingApplication.getId().intValue())))
                .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
                .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
                .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())))
                .andExpect(jsonPath(SERVICE_DEPLOYMENT_INFO_LIST_PATH + "[*]", hasSize(1)));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void updateApplicationIsOnlyAllowedWithoutServices() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription("newDesc");

        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setGitCloneUrl(GIT_TEST_REPO_URL);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service));

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultUpdate.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteApplication() throws Exception {
        MicoApplication app = new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION);
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(app));

        ArgumentCaptor<MicoApplication> appCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(delete("/applications/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();

        verify(applicationRepository).delete(appCaptor.capture());
        assertEquals("Wrong short name.", app.getShortName(), appCaptor.getValue().getShortName());
        assertEquals("Wrong version.", app.getVersion(), appCaptor.getValue().getVersion());
    }
    
    @Test
    public void getStatusOfApplication() throws Exception {
     // Create a new application with one service
      MicoApplication application = new MicoApplication()
          .setId(ID)
          .setName(TestConstants.APPLICATION_NAME)
          .setShortName(SHORT_NAME)
          .setVersion(VERSION);
      
      MicoServiceInterface serviceInterface = new MicoServiceInterface()
              .setServiceInterfaceName(TestConstants.SERVICE_INTERFACE_NAME);
      
      MicoService service = new MicoService()
              .setShortName(SERVICE_SHORT_NAME)
              .setVersion(SERVICE_VERSION)
              .setServiceInterfaces(CollectionUtils.listOf(serviceInterface));

        // Test properties for pods of the service
        String nodeName = "testNode";
        String podPhase = "Running";
        String hostIp = "192.168.0.0";
        String podName1 = "pod1";
        String podName2 = "pod2";
        int availableReplicas = 1;
        int replicas = 2;
        int memoryUsage1 = 50;
        int cpuLoad1 = 10;
        int memoryUsage2 = 70;
        int cpuLoad2 = 40;

        MicoApplicationStatusDTO micoApplicationStatus = new MicoApplicationStatusDTO();
        MicoServiceStatusDTO micoServiceStatus = new MicoServiceStatusDTO();

        // Set information for first pod of a MicoService
        KubernetesPodInfoDTO kubernetesPodInfo1 = new KubernetesPodInfoDTO();
        kubernetesPodInfo1
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName1)
            .setMetrics(new KuberenetesPodMetricsDTO()
                .setAvailable(false)
                .setCpuLoad(cpuLoad1)
                .setMemoryUsage(memoryUsage1));

        // Set information for second pod of a MicoService
        KubernetesPodInfoDTO kubernetesPodInfo2 = new KubernetesPodInfoDTO();
        kubernetesPodInfo2
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName2)
            .setMetrics(new KuberenetesPodMetricsDTO()
                .setAvailable(true)
                .setCpuLoad(cpuLoad2)
                .setMemoryUsage(memoryUsage2));

        // Set deployment information for MicoService
        micoServiceStatus
            .setName(TestConstants.SERVICE_NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setAvailableReplicas(availableReplicas)
            .setRequestedReplicas(replicas)
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(TestConstants.SERVICE_INTERFACE_NAME)))
            .setPodInfo(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2));

        micoApplicationStatus.getServiceStatus().add(micoServiceStatus);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service));

        // Mock MicoStatusService
        given(micoStatusService.getApplicationStatus(any(MicoApplication.class))).willReturn(micoApplicationStatus);

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/status"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(TestConstants.SERVICE_INFORMATION_NAME, is(TestConstants.SERVICE_NAME)))
            .andExpect(jsonPath(TestConstants.REQUESTED_REPLICAS, is(replicas)))
            .andExpect(jsonPath(TestConstants.AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(TestConstants.INTERFACES_INFORMATION, hasSize(1)))
            .andExpect(jsonPath(TestConstants.INTERFACES_INFORMATION_NAME, is(TestConstants.SERVICE_INTERFACE_NAME)))
            .andExpect(jsonPath(TestConstants.POD_INFO, hasSize(2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_POD_NAME_1, is(podName1)))
            .andExpect(jsonPath(TestConstants.POD_INFO_PHASE_1, is(podPhase)))
            .andExpect(jsonPath(TestConstants.POD_INFO_NODE_NAME_1, is(nodeName)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_MEMORY_USAGE_1, is(memoryUsage1)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_CPU_LOAD_1, is(cpuLoad1)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_AVAILABLE_1, is(false)))
            .andExpect(jsonPath(TestConstants.POD_INFO_POD_NAME_2, is(podName2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_PHASE_2, is(podPhase)))
            .andExpect(jsonPath(TestConstants.POD_INFO_NODE_NAME_2, is(nodeName)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_MEMORY_USAGE_2, is(memoryUsage2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_CPU_LOAD_2, is(cpuLoad2)))
            .andExpect(jsonPath(TestConstants.POD_INFO_METRICS_AVAILABLE_2, is(true)));
    }

    @Test
    public void addServiceToApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        
        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION))
                .willReturn(Optional.of(application));
        given(serviceRepository.findByShortNameAndVersion(SERVICE_SHORT_NAME, SERVICE_VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service));
        ArgumentCaptor<MicoApplication> micoApplicationCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/services")
            .content(mapper.writeValueAsBytes(service))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(applicationRepository, times(1)).save(micoApplicationCaptor.capture());
        List<MicoService> savedServices = serviceRepository.findAllByApplication(SHORT_NAME, VERSION);
        assertEquals("Expected one service", savedServices.size(), 1);
        assertEquals(savedServices.get(0), service);
    }

    @Test
    public void addServiceOnlyWithNameAndVersionToApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        
        MicoService existingService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setDescription(DESCRIPTION)
            .setGitCloneUrl(GIT_TEST_REPO_URL);
        
        MicoService providedService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION))
                .willReturn(Optional.of(application));
        given(serviceRepository.findByShortNameAndVersion(SERVICE_SHORT_NAME, SERVICE_VERSION))
                .willReturn(Optional.of(existingService));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION))
                .willReturn(CollectionUtils.listOf(existingService));
        
        ArgumentCaptor<MicoApplication> micoApplicationCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/services")
            .content(mapper.writeValueAsBytes(providedService))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(applicationRepository, times(1)).save(micoApplicationCaptor.capture());
        List<MicoService> savedServices = serviceRepository.findAllByApplication(SHORT_NAME, VERSION);
        assertEquals("Expected one service", savedServices.size(), 1);
        assertEquals(savedServices.get(0), existingService);
    }

    @Test
    public void addServiceWithInconsistentDataToApplication() throws Exception {
        MicoApplication micoApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        
        MicoService existingService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setDescription(DESCRIPTION);
        
        MicoService providedService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setDescription("NewDesc");

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(micoApplication));
        given(serviceRepository.findByShortNameAndVersion(SERVICE_SHORT_NAME, SERVICE_VERSION)).willReturn(Optional.of(existingService));

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/services")
            .content(mapper.writeValueAsBytes(providedService))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    public void deleteServiceFromApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        
        MicoService service = new MicoService()
                .setShortName(SERVICE_SHORT_NAME)
                .setVersion(SERVICE_VERSION);
        
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
                .setApplication(application)
                .setService(service));
        
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.findByShortNameAndVersion(SERVICE_SHORT_NAME, SERVICE_VERSION)).willReturn(Optional.of(service));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(SHORT_NAME, VERSION, SERVICE_SHORT_NAME))
                .willReturn(Optional.of(new MicoServiceDeploymentInfoQueryResult()
                        .setApplication(application)
                        .setServiceDeploymentInfo(application.getServiceDeploymentInfos().get(0))
                        .setService(service)));
        
        ArgumentCaptor<MicoApplication> micoApplicationCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/services/" + SERVICE_SHORT_NAME))
            .andDo(print())
            .andExpect(status().isNoContent());
        verify(applicationRepository, times(1)).save(micoApplicationCaptor.capture());
        List<MicoService> savedServices = serviceRepository.findAllByApplication(SHORT_NAME, VERSION);
        assertTrue("Expected services are empty", savedServices.isEmpty());
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
