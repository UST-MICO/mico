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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.ust.mico.core.persistence.*;
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
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.dto.request.MicoApplicationRequestDTO;
import io.github.ust.mico.core.dto.request.MicoLabelRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoVersionRequestDTO;
import io.github.ust.mico.core.dto.response.MicoApplicationResponseDTO;
import io.github.ust.mico.core.dto.response.MicoLabelResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.dto.response.status.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;
import io.github.ust.mico.core.resource.ApplicationResource;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationResource.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ApplicationResourceTests {

    public static final String APPLICATION_DTO_LIST_PATH = buildPath(EMBEDDED, "micoApplicationResponseDTOList");
    public static final String APPLICATION_WITH_SERVICES_DTO_LIST_PATH = buildPath(EMBEDDED, "micoApplicationWithServicesResponseDTOList");
    public static final String APPLICATION_PATH = buildPath(ROOT, "application");
    public static final String SHORT_NAME_PATH = buildPath(ROOT, "shortName");
    public static final String VERSION_PATH = buildPath(ROOT, "version");
    public static final String NAME_PATH = buildPath(ROOT, "name");
    public static final String DESCRIPTION_PATH = buildPath(ROOT, "description");
    public static final String OWNER_PATH = buildPath(ROOT, "owner");
    public static final String SERVICE_LIST_PATH = buildPath(ROOT, "services");
    public static final String INTERFACES_LIST_PATH = buildPath(ROOT, "serviceInterfaces");
    public static final String ID_PATH = buildPath(ROOT, "id");
    private static final String JSON_PATH_LINKS_SECTION = "$._links.";
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
    private MicoLabelRepository labelRepository;

    @MockBean
    private MicoEnvironmentVariableRepository environmentVariableRepository;

    @MockBean
    private MicoInterfaceConnectionRepository interfaceConnectionRepository;

    @MockBean
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private MicoStatusService micoStatusService;

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
        MicoService service = new MicoService()
        	.setId(ID_1)
        	.setShortName(SERVICE_SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);
        
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION).setOwner(OWNER);
        
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo().setService(service);
        
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
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
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(application)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(application.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(application.getVersion())));
    }

    @Test
    public void createApplicationWithExistingServices() throws Exception {
    	MicoApplication application = new MicoApplication()
    		.setId(ID)
    		.setShortName(SHORT_NAME).setVersion(VERSION)
    		.setName(NAME).setDescription(DESCRIPTION);
    	
    	MicoService service1 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService service2 = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION);

        application.getServices().add(service1);
        application.getServices().add(service2);
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(service1));
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(service2));

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(application);
        given(serviceRepository.findByShortNameAndVersion(eq(service1.getShortName()), eq(service1.getVersion())))
            .willReturn(Optional.of(service1));
        given(serviceRepository.findByShortNameAndVersion(eq(service2.getShortName()), eq(service2.getVersion())))
            .willReturn(Optional.of(service2));

        MicoApplicationResponseDTO newApplicationDto = (MicoApplicationResponseDTO) new MicoApplicationResponseDTO()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(newApplicationDto))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(jsonPath(SHORT_NAME_PATH, is(application.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(application.getVersion())))
            .andExpect(jsonPath(SERVICE_LIST_PATH, hasSize(2)))
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void createApplicationWithInconsistentServiceData() throws Exception {
        MicoService invalidMicoService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION)
            .setGitCloneUrl("http://example.com/INVALID");

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);

        application.getServices().add(invalidMicoService);
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(invalidMicoService));

        // MicoService exists with different data
        given(applicationRepository.findByShortNameAndVersion(eq(application.getShortName()),
            eq(application.getVersion()))).willReturn(Optional.of(application));

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(application)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    public void createApplicationWithoutRequiredName() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(null).setDescription(DESCRIPTION);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(application)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createApplicationWithDescriptionSetToNull() throws Exception {
        MicoApplication newApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(null);

        MicoApplication expectedApplication = new MicoApplication()
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME).setDescription("");

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(newApplication)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());
        MicoApplication savedApplication = applicationArgumentCaptor.getValue();
        assertNotNull(savedApplication);
        assertEquals("Actual application does not match expected", expectedApplication, savedApplication);
    }

    @Test
    public void createApplicationWithEmptyDescription() throws Exception {
        MicoApplication newApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription("");

        MicoApplication expectedApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription("");

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        mvc.perform(post(BASE_PATH)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(newApplication)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated());

        verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());
        MicoApplication savedApplication = applicationArgumentCaptor.getValue();
        assertNotNull(savedApplication);
        assertEquals("Actual application does not match expected", expectedApplication, savedApplication);
    }

    @Test
    public void updateApplication() throws Exception {
        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);
        
        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription("newDesc");
        
        MicoApplication expectedApplication = new MicoApplication()
            .setId(existingApplication.getId())
            .setShortName(updatedApplication.getShortName()).setVersion(updatedApplication.getVersion())
            .setName(updatedApplication.getName()).setDescription(updatedApplication.getDescription());

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);

        mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(updatedApplication)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())))
            .andExpect(jsonPath(SERVICE_LIST_PATH, hasSize(0)));
    }

    @Test
    public void updateApplicationUsesExistingServices() throws Exception {
        MicoService existingService = new MicoService()
            .setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION)
            .setName(NAME);

        MicoApplication existingApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription("newDesc");

        MicoApplication expectedApplication = new MicoApplication()
            .setId(existingApplication.getId())
            .setShortName(updatedApplication.getShortName()).setVersion(updatedApplication.getVersion())
            .setName(updatedApplication.getName()).setDescription(updatedApplication.getDescription());

        expectedApplication.getServices().add(existingService);
        expectedApplication.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(existingService));

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(existingApplication));
        given(applicationRepository.save(eq(updatedApplication.setId(ID)))).willReturn(expectedApplication);

		mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
		    .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(updatedApplication)))
		    .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
			.andDo(print())
			.andExpect(status().isOk())
		    .andExpect(jsonPath(DESCRIPTION_PATH, is(updatedApplication.getDescription())))
		    .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
		    .andExpect(jsonPath(VERSION_PATH, is(updatedApplication.getVersion())))
            .andExpect(jsonPath(NAME_PATH, is(updatedApplication.getName())))
            .andExpect(jsonPath(SERVICE_LIST_PATH, hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].shortName", is(SERVICE_SHORT_NAME)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].version", is(SERVICE_VERSION)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].name", is(NAME)));
    }

    @Test
    public void promoteApplication() throws Exception {
        MicoService service = new MicoService()
            .setId(ID_1)
            .setShortName(SERVICE_SHORT_NAME)
            .setVersion(SERVICE_VERSION)
            .setName(NAME);
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setId(2000L)
            .setService(service)
            .setReplicas(5)
            .setLabels(CollectionUtils.listOf(new MicoLabel(3000L, "key", "value")))
            .setEnvironmentVariables(CollectionUtils.listOf(new MicoEnvironmentVariable(4000L, "name", "key")))
            .setKubernetesDeploymentInfo(new KubernetesDeploymentInfo()
                .setId(5000L)
                .setNamespace("namespace")
                .setDeploymentName("deployment")
                .setServiceNames(CollectionUtils.listOf("service")));

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME)
            .setVersion(VERSION)
            .setName(NAME);
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(deploymentInfo);

        String newVersion = VERSION_1_0_1;
        Long newId = ID + 1;

        MicoApplication updatedApplication = new MicoApplication()
            .setId(null)
            .setShortName(SHORT_NAME)
            .setVersion(newVersion)
            .setName(NAME);
        updatedApplication.getServices().add(service);
        // Service deployment information is copied without the actual Kubernetes deployment information.
        MicoServiceDeploymentInfo updatedDeploymentInfo = new MicoServiceDeploymentInfo()
            .setId(null).setService(service).setReplicas(5).setKubernetesDeploymentInfo(null);
        updatedApplication.getServiceDeploymentInfos().add(updatedDeploymentInfo);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(newId)
            .setShortName(SHORT_NAME)
            .setVersion(newVersion)
            .setName(NAME);
        expectedApplication.getServices().add(service);
        MicoServiceDeploymentInfo expectedDeploymentInfo = new MicoServiceDeploymentInfo()
            .setId(ID_2).setService(service).setReplicas(5).setKubernetesDeploymentInfo(null);
        expectedApplication.getServiceDeploymentInfos().add(expectedDeploymentInfo);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_PROMOTE)
            .content(mapper.writeValueAsBytes(new MicoVersionRequestDTO(newVersion)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(NAME_PATH, is(updatedApplication.getName())))
            .andExpect(jsonPath(SHORT_NAME_PATH, is(updatedApplication.getShortName())))
            .andExpect(jsonPath(VERSION_PATH, is(newVersion)))
            .andExpect(jsonPath(SERVICE_LIST_PATH, hasSize(1)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].shortName", is(SERVICE_SHORT_NAME)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].version", is(SERVICE_VERSION)))
            .andExpect(jsonPath(SERVICE_LIST_PATH + "[0].name", is(NAME)));

        verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());
        MicoApplication savedMicoApplication = applicationArgumentCaptor.getValue();
        assertNotNull(savedMicoApplication);
        assertEquals("Expected that new application includes 1 MicoService",1, savedMicoApplication.getServices().size());
        assertEquals("MicoService does not match expected", service, savedMicoApplication.getServices().get(0));
        assertEquals("Expected that new application includes 1 service deployment information",1, savedMicoApplication.getServiceDeploymentInfos().size());
        assertEquals("MicoService in service deployment information does not match expected", service, savedMicoApplication.getServiceDeploymentInfos().get(0).getService());
        assertEquals("Replicas do not match expected", 5, savedMicoApplication.getServiceDeploymentInfos().get(0).getReplicas());
        assertEquals("Expected one Kubernetes label", 1, savedMicoApplication.getServiceDeploymentInfos().get(0).getLabels().size());
        assertEquals("Expected one Kubernetes environment variable", 1, savedMicoApplication.getServiceDeploymentInfos().get(0).getEnvironmentVariables().size());
        assertNull("Expected actual Kubernetes deployment information to be null", savedMicoApplication.getServiceDeploymentInfos().get(0).getKubernetesDeploymentInfo());
    }

    @Test
    public void updateApplicationWithoutRequiredName() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(null).setDescription(DESCRIPTION);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));

        mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(updatedApplication)))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateApplicationWithDescriptionSetToNull() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);

        MicoApplication updatedApplication = new MicoApplication()
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(null);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(application.getId())
            .setShortName(application.getShortName()).setVersion(application.getVersion())
            .setName(updatedApplication.getName()).setDescription("");

        ArgumentCaptor<MicoApplication> applicationArgumentCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(applicationRepository.save(any(MicoApplication.class))).willReturn(expectedApplication);

        mvc.perform(put(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION)
            .content(mapper.writeValueAsBytes(new MicoApplicationRequestDTO(updatedApplication)))
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
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME).setDescription(DESCRIPTION);
        
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));

        ArgumentCaptor<MicoApplication> appCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();

        verify(applicationRepository).delete(appCaptor.capture());
        assertEquals("Wrong short name.", application.getShortName(), appCaptor.getValue().getShortName());
        assertEquals("Wrong version.", application.getVersion(), appCaptor.getValue().getVersion());
    }

    @Test
    public void getStatusOfApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION)
            .setName(NAME);

        MicoApplication otherMicoApplication = new MicoApplication()
            .setId(ID_1)
            .setShortName(SHORT_NAME_OTHER).setVersion(VERSION)
            .setName(NAME);

        MicoServiceInterface serviceInterface = new MicoServiceInterface().setServiceInterfaceName(SERVICE_INTERFACE_NAME);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION)
            .setServiceInterfaces(CollectionUtils.listOf(serviceInterface));

        String nodeName = "testNode";
        String podPhase = "Running";
        String hostIp = "192.168.0.0";
        int availableReplicas = 1;
        int replicas = 2;

        // Properties for pod 1
        String podName1 = "pod1";
        String startTimePod1 = new Date().toString();
        int restartsPod1 = 0;
        int memoryUsage1 = 50;
        int cpuLoad1 = 10;

        // Properties for pod 2
        String podName2 = "pod2";
        String startTimePod2 = new Date().toString();
        int restartsPod2 = 0;
        int memoryUsage2 = 70;
        int cpuLoad2 = 40;

        MicoApplicationStatusResponseDTO micoApplicationStatus = new MicoApplicationStatusResponseDTO()
            .setTotalNumberOfMicoServices(1)
            .setTotalNumberOfAvailableReplicas(availableReplicas)
            .setTotalNumberOfRequestedReplicas(replicas)
            .setTotalNumberOfPods(2);
        MicoServiceStatusResponseDTO micoServiceStatus = new MicoServiceStatusResponseDTO();

        // Set information for first pod of a MicoService
        KubernetesPodInformationResponseDTO kubernetesPodInfo1 = new KubernetesPodInformationResponseDTO();
        kubernetesPodInfo1
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName1)
            .setStartTime(startTimePod1)
            .setRestarts(restartsPod1)
            .setMetrics(new KubernetesPodMetricsResponseDTO()
                .setAvailable(false)
                .setCpuLoad(cpuLoad1)
                .setMemoryUsage(memoryUsage1));

        // Set information for second pod of a MicoService
        KubernetesPodInformationResponseDTO kubernetesPodInfo2 = new KubernetesPodInformationResponseDTO();
        kubernetesPodInfo2
            .setHostIp(hostIp)
            .setNodeName(nodeName)
            .setPhase(podPhase)
            .setPodName(podName2)
            .setStartTime(startTimePod2)
            .setRestarts(restartsPod2)
            .setMetrics(new KubernetesPodMetricsResponseDTO()
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
            .setApplicationsUsingThisService(CollectionUtils.listOf(new MicoApplicationResponseDTO(otherMicoApplication)))
            .setInterfacesInformation(CollectionUtils.listOf(new MicoServiceInterfaceStatusResponseDTO().setName(SERVICE_INTERFACE_NAME)))
            .setPodsInformation(Arrays.asList(kubernetesPodInfo1, kubernetesPodInfo2))
            .setNodeMetrics(CollectionUtils.listOf(
                new KubernetesNodeMetricsResponseDTO()
                    .setNodeName(nodeName)
                    .setAverageCpuLoad(25)
                    .setAverageMemoryUsage(60)
            ));
        micoApplicationStatus.getServiceStatuses().add(micoServiceStatus);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(applicationRepository.findAllByUsedService(any(), any())).willReturn(CollectionUtils.listOf(otherMicoApplication));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service));

        // Mock MicoStatusService
        given(micoStatusService.getApplicationStatus(any(MicoApplication.class))).willReturn(micoApplicationStatus);

        mvc.perform(get(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_STATUS))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SERVICE_INFORMATION_NAME, is(NAME)))
            .andExpect(jsonPath(TOTAL_NUMBER_OF_AVAILABLE_REPLICAS, is(availableReplicas)))
            .andExpect(jsonPath(TOTAL_NUMBER_OF_REQUESTED_REPLICAS, is(replicas)))
            .andExpect(jsonPath(TOTAL_NUMBER_OF_PODS, is(2)))
            .andExpect(jsonPath(TOTAL_NUMBER_OF_MICO_SERVICES, is(1)))
            .andExpect(jsonPath(NODE_METRICS_NAME, is(nodeName)))
            .andExpect(jsonPath(NODE_METRICS_AVERAGE_CPU_LOAD, is(25)))
            .andExpect(jsonPath(NODE_METRICS_AVERAGE_MEMORY_USAGE, is(60)))
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
            .andExpect(jsonPath(POD_INFO_METRICS_AVAILABLE_2, is(true)))
            .andExpect(jsonPath(ERROR_MESSAGES, is(CollectionUtils.listOf())));
    }

    @Test
    public void addServiceToApplication() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.findByShortNameAndVersion(SERVICE_SHORT_NAME, SERVICE_VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findAllByApplication(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service));
        
        ArgumentCaptor<MicoApplication> micoApplicationCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(applicationRepository, times(1)).save(micoApplicationCaptor.capture());
        MicoApplication savedMicoApplication = micoApplicationCaptor.getValue();
        assertEquals("Expected one service deployment information", 1, savedMicoApplication.getServiceDeploymentInfos().size());
        assertEquals(service, savedMicoApplication.getServiceDeploymentInfos().get(0).getService());
    }

    @Test
    public void deleteServiceFromApplication() throws Exception {
        MicoApplication application = new MicoApplication()
        	.setId(ID)
        	.setShortName(SHORT_NAME).setVersion(VERSION);
        
        MicoService service = new MicoService()
        	.setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);
        
        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(service));

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceRepository.findByShortNameAndVersion(SERVICE_SHORT_NAME, SERVICE_VERSION)).willReturn(Optional.of(service));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(SHORT_NAME, VERSION, SERVICE_SHORT_NAME))
            .willReturn(Optional.of(application.getServiceDeploymentInfos().get(0)));

        mvc.perform(delete(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/services/" + SERVICE_SHORT_NAME))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    public void getServiceDeploymentInformation() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);

        List<MicoLabel> labels = CollectionUtils.listOf(new MicoLabel().setKey("key").setValue("value"));
        ImagePullPolicy imagePullPolicy = ImagePullPolicy.IF_NOT_PRESENT;
        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(service)
            .setReplicas(3)
            .setLabels(labels)
            .setImagePullPolicy(imagePullPolicy);

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
		given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(),
		    application.getVersion(), service.getShortName())).willReturn(Optional.of(serviceDeploymentInfo));

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
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoService service = new MicoService()
        	.setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
        	.setId(ID_1)
            .setService(service)
            .setReplicas(3)
            .setLabels(CollectionUtils.listOf(new MicoLabel().setKey("key").setValue("value")))
            .setImagePullPolicy(ImagePullPolicy.IF_NOT_PRESENT);

        MicoServiceDeploymentInfoRequestDTO updatedServiceDeploymentInfoDTO = new MicoServiceDeploymentInfoRequestDTO()
            .setReplicas(5)
            .setLabels(CollectionUtils.listOf(new MicoLabelRequestDTO().setKey("key-updated").setValue("value-updated")))
            .setImagePullPolicy(ImagePullPolicy.NEVER);

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServices().add(service);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion())).willReturn(Optional.of(application));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);
        given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(), application.getVersion(), service.getShortName())).willReturn(Optional.of(serviceDeploymentInfo));
        given(serviceDeploymentInfoRepository.save(eq(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO)))).willReturn(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

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
        MicoApplication micoApplicationV1 = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoApplication micoApplicationV2 = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        MicoApplication micoApplicationV3 = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION_1_0_2);
        MicoApplication otherApplication = new MicoApplication().setShortName(SHORT_NAME_2).setVersion(VERSION);

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
        MicoApplication micoApplicationV1 = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoApplication micoApplicationV2 = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        MicoApplication micoApplicationV3 = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION_1_0_2);

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
        List<MicoLabel> labels = CollectionUtils.listOf(new MicoLabel().setKey("invalid-key!").setValue("value"));

        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoApplication expectedApplication = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoService service = new MicoService()
        	.setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo().setService(service);

        MicoServiceDeploymentInfoResponseDTO updatedServiceDeploymentInfoDTO = 
        	(MicoServiceDeploymentInfoResponseDTO) new MicoServiceDeploymentInfoResponseDTO()
            .setLabels(labels.stream().map(MicoLabelResponseDTO::new).collect(Collectors.toList()));

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServices().add(service);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion()))
            .willReturn(Optional.of(application));

        final ResultActions result = mvc.perform(put(BASE_PATH + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateVersionOfAssociatedService() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService serviceOld = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(VERSION_1_0_1);
        MicoService serviceNew = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(VERSION_1_0_2);
        MicoServiceDeploymentInfo serviceDeploymentInfoOld = new MicoServiceDeploymentInfo().setService(serviceOld);
        
        application.getServices().add(serviceOld);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfoOld);

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion()))
            .willReturn(Optional.of(application));
        given(serviceRepository.findByShortNameAndVersion(serviceNew.getShortName(), serviceNew.getVersion()))
    		.willReturn(Optional.of(serviceNew));

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + VERSION_1_0_2)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent());

        ArgumentCaptor<MicoApplication> applicationCaptor = ArgumentCaptor.forClass(MicoApplication.class);

        verify(applicationRepository, times(1)).save(applicationCaptor.capture());
        MicoApplication savedApplication = applicationCaptor.getValue();
        assertEquals("Expected one service", 1, savedApplication.getServices().size());
        assertEquals("Expected one serviceDeploymentInfo", 1, savedApplication.getServiceDeploymentInfos().size());
        assertEquals(serviceNew, savedApplication.getServiceDeploymentInfos().get(0).getService());
        assertEquals(serviceNew, savedApplication.getServices().get(0));
    }

    @Test
    public void updateVersionOfAssociatedServiceConflict() throws Exception {
        MicoApplication micoApplication = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService service1 = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(VERSION_1_0_1);
        MicoService service2 = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(VERSION_1_0_2);
        micoApplication.getServices().add(service1);
        micoApplication.getServices().add(service2);

        given(applicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(Optional.of(micoApplication));
        given(serviceRepository.findByShortNameAndVersion(service1.getShortName(), service1.getVersion()))
            .willReturn(Optional.of(service1));
        given(serviceRepository.findAllByApplicationAndShortName(SHORT_NAME, VERSION, SERVICE_SHORT_NAME))
        	.willReturn(CollectionUtils.listOf(service1, service2));

        mvc.perform(post(BASE_PATH + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + VERSION_1_0_1)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isConflict());
    }
}
