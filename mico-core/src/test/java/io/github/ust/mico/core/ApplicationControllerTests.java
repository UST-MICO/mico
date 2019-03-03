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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.dto.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.validation.MicoDataValidator;
import io.github.ust.mico.core.web.ApplicationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ApplicationControllerTests {

    private static final String JSON_PATH_LINKS_SECTION = "$._links.";

    public static final String APPLICATION_DTO_LIST_PATH = buildPath(EMBEDDED, "micoApplicationDTOList");
    public static final String APPLICATION_WITH_SERVICES_DTO_LIST_PATH = buildPath(EMBEDDED, "micoApplicationWithServicesDTOList");
    public static final String APPLICATION_PATH = buildPath(ROOT, "application");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_PATH = buildPath(ROOT, "version");
    public static final String NAME_PATH = buildPath(ROOT, "name");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String OWNER_PATH = buildPath(ROOT, "owner");
    public static final String SERVICE_LIST_PATH = buildPath(ROOT, "services");
    public static final String INTERFACES_LIST_PATH = buildPath(ROOT, "serviceInterfaces");
    public static final String ID_PATH = buildPath(ROOT, "id");

    private static final String BASE_PATH = "/applications";
    private static final String PATH_SERVICES = "services";
    private static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";
    private static final String PATH_PROMOTE = "promote";
    private static final String PATH_STATUS = "status";

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private MicoStatusService micoStatusService;

    // Use real implementation of validator class
    @SpyBean
    private MicoDataValidator micoDataValidator;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Captor
    private ArgumentCaptor<List<MicoApplication>> micoApplicationListCaptor;

    @Test
    public void getAllApplications() throws Exception {
        given(applicationRepository.findAll(3)).willReturn(
            CollectionUtils.listOf(
                new MicoApplication().setId(ID_1).setShortName(SHORT_NAME).setVersion(VERSION_1_0_1),
                new MicoApplication().setId(ID_2).setShortName(SHORT_NAME).setVersion(VERSION),
                new MicoApplication().setId(ID_3).setShortName(SHORT_NAME_1).setVersion(VERSION)));
        mvc.perform(get(BASE_PATH).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[*]", hasSize(3)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[0].shortName", is(SHORT_NAME)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[0].version", is(VERSION_1_0_1)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[1].shortName", is(SHORT_NAME)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[1].version", is(VERSION)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[2].shortName", is(SHORT_NAME_1)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[2].version", is(VERSION)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "self.href", is("http://localhost/applications")))
            .andReturn();
    }

    @Test
    public void getApplicationByShortName() throws Exception {
        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(
            CollectionUtils.listOf(new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[0].shortName", is(SHORT_NAME)))
            .andExpect(jsonPath(APPLICATION_WITH_SERVICES_DTO_LIST_PATH + "[0].version", is(VERSION)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/" + SHORT_NAME)))
            .andReturn();
    }

    @Test
    public void getApplicationByShortNameAndVersion() throws Exception {
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
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
    public void getApplicationByShortNameAndVersionWithServices() throws Exception {
        MicoService existingService = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION)
            .setOwner(OWNER);
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setApplication(existingApplication)
            .setService(existingService);
        existingApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(existingApplication));

        MvcResult mvcResult = mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(SHORT_NAME)))
            .andExpect(jsonPath(VERSION_PATH, is(VERSION)))
            .andExpect(jsonPath(OWNER_PATH, is(OWNER)))
            .andExpect(jsonPath(SERVICE_LIST_PATH, hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].shortName", is(SERVICE_SHORT_NAME)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].version", is(VERSION)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].name", is(NAME)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + SELF_HREF, is("http://localhost/applications/" + SHORT_NAME + "/" + VERSION)))
            .andExpect(jsonPath(JSON_PATH_LINKS_SECTION + "applications.href", is("http://localhost/applications")))
            .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        MicoApplicationWithServicesDTO micoApplicationWithServicesDTO = mapper.readValue(contentAsString, MicoApplicationWithServicesDTO.class);
        System.out.println("Deserialized application with services DTO:\n" + micoApplicationWithServicesDTO);
    }

    @Test
    public void getApplicationByShortNameWithTrailingSlash() throws Exception {
        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(
            CollectionUtils.listOf(new MicoApplication().setId(ID).setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/").accept(MediaTypes.HAL_JSON_VALUE))
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
            .setName(NAME)
            .setDescription(DESCRIPTION);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(MicoApplicationDTO.valueOf(application)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(application.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(application.getVersion())));

        result.andExpect(status().isCreated());
    }

    @Test
    public void createApplicationWithExistingServices() throws Exception {
        MicoService existingService1 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService existingService2 = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION);

        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        existingApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(existingApplication)
            .setService(existingService1));
        existingApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(existingApplication)
            .setService(existingService2));

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(existingApplication);
        given(serviceRepository.findByShortNameAndVersion(
            eq(existingService1.getShortName()), eq(existingService1.getVersion())))
            .willReturn(Optional.of(existingService1));
        given(serviceRepository.findByShortNameAndVersion(
            eq(existingService2.getShortName()), eq(existingService2.getVersion())))
            .willReturn(Optional.of(existingService2));

        MicoApplicationDTO newApplication = new MicoApplicationDTO()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(newApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(existingApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(existingApplication.getVersion())))
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void createApplicationWithInconsistentServiceData() throws Exception {
        MicoService invalidMicoService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setGitCloneUrl("http://example.com/INVALID");

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(application)
            .setService(invalidMicoService));

        // MicoService exist with different data
        given(applicationRepository.findByShortNameAndVersion(eq(application.getShortName()),
            eq(application.getVersion()))).willReturn(Optional.of(application));

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(MicoApplicationDTO.valueOf(application)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isConflict());
    }

    @Test
    public void createApplicationWithoutRequiredName() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(null)
            .setDescription(DESCRIPTION);

        final ResultActions result = mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(application))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createApplicationWithDescriptionSetToNull() throws Exception {
        MicoApplication newApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(null);

        MicoApplication expectedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("")
            .setServiceDeploymentInfos(new ArrayList<>());

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(newApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());
        MicoApplication savedMicoApplication = applicationArgumentCaptor.getValue();
        assertNotNull(savedMicoApplication);
        assertEquals("Actual application does not match expected", expectedApplication, savedMicoApplication);
    }

    @Test
    public void createApplicationWithEmptyDescription() throws Exception {
        MicoApplication newApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("");

        MicoApplication expectedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("")
            .setServiceDeploymentInfos(new ArrayList<>());

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(newApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());
        MicoApplication savedMicoApplication = applicationArgumentCaptor.getValue();
        assertNotNull(savedMicoApplication);
        assertEquals("Actual application does not match expected", expectedApplication, savedMicoApplication);
    }

    @Test
    public void updateApplication() throws Exception {
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);
        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("newDesc");
        MicoApplication expectedApplication = new MicoApplication()
            .setId(existingApplication.getId())
            .setShortName(updatedApplication.getShortName())
            .setVersion(updatedApplication.getVersion())
            .setName(updatedApplication.getName())
            .setDescription(updatedApplication.getDescription());

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(MicoApplicationDTO.valueOf(updatedApplication)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void updateApplicationUsesExistingServices() throws Exception {
        MicoService existingService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setGitCloneUrl(GIT_TEST_REPO_URL);

        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription("newDesc");

        MicoApplication expectedApplication = new MicoApplication()
            .setId(existingApplication.getId())
            .setShortName(updatedApplication.getShortName())
            .setVersion(updatedApplication.getVersion())
            .setName(updatedApplication.getName())
            .setDescription(updatedApplication.getDescription());

        expectedApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(expectedApplication)
            .setService(existingService));

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(updatedApplication.setId(ID)))).willReturn(expectedApplication);

        ResultActions resultUpdate = mvc
            .perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
                .content(mapper.writeValueAsBytes(MicoApplicationDTO.valueOf(updatedApplication)))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())))
            .andExpect(jsonPath(NAME_PATH, is(updatedApplication.getName())));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void promoteApplication() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION);

        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        existingApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo()
            .setApplication(existingApplication)
            .setService(service));

        String newVersion = VERSION_1_0_1;
        Long newId = ID + 1;

        MicoApplication updatedApplication = existingApplication.setId(null).setVersion(newVersion);
        MicoApplication expectedApplication = updatedApplication.setId(newId);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(updatedApplication))).willReturn(expectedApplication);

        ResultActions resultUpdate = mvc
            .perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_PROMOTE)
                .content(newVersion)
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(newVersion)));

        resultUpdate.andExpect(status().isOk());
    }

    @Test
    public void updateApplicationWithoutRequiredName() throws Exception {
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(null)
            .setDescription(DESCRIPTION);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));

        ResultActions resultUpdate = mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        resultUpdate.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateApplicationWithDescriptionSetToNull() throws Exception {
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(null);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(existingApplication.getId())
            .setShortName(existingApplication.getShortName())
            .setVersion(existingApplication.getVersion())
            .setName(updatedApplication.getName())
            .setDescription("")
            .setServiceDeploymentInfos(existingApplication.getServiceDeploymentInfos());

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(updatedApplication))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());
        MicoApplication savedMicoApplication = applicationArgumentCaptor.getValue();
        assertNotNull(savedMicoApplication);
        assertEquals("Actual application does not match expected", expectedApplication, savedMicoApplication);
    }

    @Test
    public void deleteApplication() throws Exception {
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));

        ArgumentCaptor<MicoApplication> appCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();

        verify(applicationRepository).delete(appCaptor.capture());
        assertEquals("Wrong short name.", existingApplication.getShortName(), appCaptor.getValue().getShortName());
        assertEquals("Wrong version.", existingApplication.getVersion(), appCaptor.getValue().getVersion());
    }

    @Test
    public void getStatusOfApplication() throws Exception {
        // Create a new application with one service
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setName(NAME)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        MicoServiceInterface serviceInterface = new MicoServiceInterface()
            .setServiceInterfaceName(SERVICE_INTERFACE_NAME);

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
            .setName(NAME)
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setAvailableReplicas(availableReplicas)
            .setRequestedReplicas(replicas)
            .setInterfacesInformation(Collections.singletonList(new MicoServiceInterfaceDTO().setName(SERVICE_INTERFACE_NAME)))
            .setPodInfo(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2));

        micoApplicationStatus.getServiceStatus().add(micoServiceStatus);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service));

        // Mock MicoStatusService
        given(micoStatusService.getApplicationStatus(any(MicoApplication.class))).willReturn(micoApplicationStatus);

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_STATUS))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SERVICE_INFORMATION_NAME, is(NAME)))
            .andExpect(jsonPath(REQUESTED_REPLICAS, is(replicas)))
            .andExpect(jsonPath(AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(INTERFACES_INFORMATION, hasSize(1)))
            .andExpect(jsonPath(INTERFACES_INFORMATION_NAME, is(SERVICE_INTERFACE_NAME)))
            .andExpect(jsonPath(POD_INFO, hasSize(2)))
            .andExpect(jsonPath(POD_INFO_POD_NAME_1, is(podName1)))
            .andExpect(jsonPath(POD_INFO_PHASE_1, is(podPhase)))
            .andExpect(jsonPath(POD_INFO_NODE_NAME_1, is(nodeName)))
            .andExpect(jsonPath(POD_INFO_METRICS_MEMORY_USAGE_1, is(memoryUsage1)))
            .andExpect(jsonPath(POD_INFO_METRICS_CPU_LOAD_1, is(cpuLoad1)))
            .andExpect(jsonPath(POD_INFO_METRICS_AVAILABLE_1, is(false)))
            .andExpect(jsonPath(POD_INFO_POD_NAME_2, is(podName2)))
            .andExpect(jsonPath(POD_INFO_PHASE_2, is(podPhase)))
            .andExpect(jsonPath(POD_INFO_NODE_NAME_2, is(nodeName)))
            .andExpect(jsonPath(POD_INFO_METRICS_MEMORY_USAGE_2, is(memoryUsage2)))
            .andExpect(jsonPath(POD_INFO_METRICS_CPU_LOAD_2, is(cpuLoad2)))
            .andExpect(jsonPath(POD_INFO_METRICS_AVAILABLE_2, is(true)));
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

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES)
            .content(mapper.writeValueAsBytes(service))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(applicationRepository, times(1)).save(micoApplicationCaptor.capture());
        MicoApplication savedMicoApplication = micoApplicationCaptor.getValue();
        assertEquals("Expected one service deployment information", 1, savedMicoApplication.getServiceDeploymentInfos().size());
        assertEquals(service, savedMicoApplication.getServiceDeploymentInfos().get(0).getService());
    }

    @Test
    public void addServiceOnlyWithShortNameAndVersionToApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION);

        MicoService existingService = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setName(NAME)
            .setDescription(DESCRIPTION);

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

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES)
            .content(mapper.writeValueAsBytes(providedService))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(applicationRepository, times(1)).save(micoApplicationCaptor.capture());
        MicoApplication savedMicoApplication = micoApplicationCaptor.getValue();
        assertEquals("Expected one service deployment information", 1, savedMicoApplication.getServiceDeploymentInfos().size());
        assertEquals(existingService, savedMicoApplication.getServiceDeploymentInfos().get(0).getService());
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

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES)
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
        MicoApplication savedMicoApplication = micoApplicationCaptor.getValue();
        assertEquals("Expected no service deployment information", 0, savedMicoApplication.getServiceDeploymentInfos().size());
    }

    @Test
    public void getServiceDeploymentInformation() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION);

        List<MicoLabel> labels = CollectionUtils.listOf(new MicoLabel("key", "value"));
        ImagePullPolicy imagePullPolicy = ImagePullPolicy.IF_NOT_PRESENT;
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setApplication(application)
            .setService(service)
            .setReplicas(3)
            .setLabels(labels)
            .setImagePullPolicy(imagePullPolicy);

        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(), application.getVersion(), service.getShortName()))
            .willReturn(Optional.of(new MicoServiceDeploymentInfoQueryResult(application, serviceDeploymentInfo, service)));

        mvc.perform(get(BASE_PATH + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SDI_REPLICAS_PATH, is(serviceDeploymentInfo.getReplicas())))
            .andExpect(jsonPath(SDI_LABELS_PATH + "[0].key", is(labels.get(0).getKey())))
            .andExpect(jsonPath(SDI_LABELS_PATH + "[0].value", is(labels.get(0).getValue())))
            .andExpect(jsonPath(SDI_IMAGE_PULLPOLICY_PATH, is(imagePullPolicy.toString())))
            .andReturn();
    }

    @Test
    public void updateServiceDeploymentInformation() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION);

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setApplication(application)
            .setService(service)
            .setReplicas(3)
            .setLabels(CollectionUtils.listOf(new MicoLabel("key", "value")))
            .setImagePullPolicy(ImagePullPolicy.IF_NOT_PRESENT);

        MicoServiceDeploymentInfoDTO updatedServiceDeploymentInfoDTO = new MicoServiceDeploymentInfoDTO()
            .setReplicas(5)
            .setLabels(CollectionUtils.listOf(new MicoLabel("key-updated", "value-updated")))
            .setImagePullPolicy(ImagePullPolicy.NEVER);

        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion()))
            .willReturn(Optional.of(application));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);

        mvc.perform(put(BASE_PATH + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SDI_REPLICAS_PATH, is(serviceDeploymentInfo.getReplicas())))
            .andExpect(jsonPath(SDI_LABELS_PATH + "[0].key", is(updatedServiceDeploymentInfoDTO.getLabels().get(0).getKey())))
            .andExpect(jsonPath(SDI_LABELS_PATH + "[0].value", is(updatedServiceDeploymentInfoDTO.getLabels().get(0).getValue())))
            .andExpect(jsonPath(SDI_IMAGE_PULLPOLICY_PATH, is(updatedServiceDeploymentInfoDTO.getImagePullPolicy().toString())))
            .andReturn();
    }

    @Test
    public void deleteAllVersionsOfAnApplication() throws Exception {
        MicoApplication micoApplicationV1 = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        MicoApplication micoApplicationV2 = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION_1_0_1);
        MicoApplication micoApplicationV3 = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION_1_0_2);
        MicoApplication otherApplication = new MicoApplication()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION);

        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(CollectionUtils.listOf(micoApplicationV1, micoApplicationV2, micoApplicationV3));
        given(applicationRepository.findByShortName(SHORT_NAME_2)).willReturn(CollectionUtils.listOf(otherApplication));
        given(micoKubernetesClient.isApplicationDeployed(any(MicoApplication.class))).willReturn(false);

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();

        verify(applicationRepository, times(1)).deleteAll(micoApplicationListCaptor.capture());
        List<MicoApplication> actualDeletedApplications = micoApplicationListCaptor.getValue();
        assertEquals("Expected 3 applications were deleted", 3, actualDeletedApplications.size());
        List<MicoApplication> otherApplications = actualDeletedApplications.stream()
            .filter(app -> !app.getShortName().equals(SHORT_NAME)).collect(Collectors.toList());
        assertEquals("Excepted no other application was deleted", 0, otherApplications.size());
    }

    @Test
    public void deleteOnlyIfNoVersionOfTheApplicationIsDeployed() throws Exception {
        MicoApplication micoApplicationV1 = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);
        MicoApplication micoApplicationV2 = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION_1_0_1);
        MicoApplication micoApplicationV3 = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION_1_0_2);

        given(applicationRepository.findByShortName(SHORT_NAME)).willReturn(CollectionUtils.listOf(micoApplicationV1, micoApplicationV2, micoApplicationV3));
        given(micoKubernetesClient.isApplicationDeployed(micoApplicationV1)).willReturn(false);
        given(micoKubernetesClient.isApplicationDeployed(micoApplicationV2)).willReturn(true);
        given(micoKubernetesClient.isApplicationDeployed(micoApplicationV3)).willReturn(false);

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(status().reason("Application is currently deployed in version 1.0.1!"))
            .andReturn();

        verify(applicationRepository, never()).deleteAll(micoApplicationListCaptor.capture());
    }


    @Test
    public void invalidLabelsThrowAnException() throws Exception {

        List<MicoLabel> labels = CollectionUtils.listOf(new MicoLabel("invalid-key!", "value"));

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION);

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setApplication(application)
            .setService(service);

        MicoServiceDeploymentInfoDTO updatedServiceDeploymentInfoDTO = new MicoServiceDeploymentInfoDTO()
            .setLabels(labels);

        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion()))
            .willReturn(Optional.of(application));

        final ResultActions result = mvc.perform(put(BASE_PATH + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isUnprocessableEntity());
    }
}
