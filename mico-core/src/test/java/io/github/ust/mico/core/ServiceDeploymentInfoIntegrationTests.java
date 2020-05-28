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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.dto.request.MicoLabelRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoTopicRequestDTO;
import io.github.ust.mico.core.dto.response.MicoLabelResponseDTO;
import io.github.ust.mico.core.dto.response.MicoServiceDeploymentInfoResponseDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.persistence.KubernetesDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoEnvironmentVariableRepository;
import io.github.ust.mico.core.persistence.MicoInterfaceConnectionRepository;
import io.github.ust.mico.core.persistence.MicoLabelRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.persistence.MicoTopicRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static io.github.ust.mico.core.TestConstants.ID;
import static io.github.ust.mico.core.TestConstants.ID_1;
import static io.github.ust.mico.core.TestConstants.ID_2;
import static io.github.ust.mico.core.TestConstants.ID_3;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID;
import static io.github.ust.mico.core.TestConstants.SDI_IMAGE_PULLPOLICY_PATH;
import static io.github.ust.mico.core.TestConstants.SDI_LABELS_PATH;
import static io.github.ust.mico.core.TestConstants.SDI_REPLICAS_PATH;
import static io.github.ust.mico.core.TestConstants.SDI_TOPICS_PATH;
import static io.github.ust.mico.core.TestConstants.SERVICE_SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.SERVICE_VERSION;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("unit-testing")
public class ServiceDeploymentInfoIntegrationTests {

    private static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @MockBean
    private MicoLabelRepository micoLabelRepository;

    @MockBean
    private MicoTopicRepository micoTopicRepository;

    @MockBean
    private MicoEnvironmentVariableRepository micoEnvironmentVariableRepository;

    @MockBean
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @MockBean
    private MicoInterfaceConnectionRepository micoInterfaceConnectionRepository;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private MicoStatusService micoStatusService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

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
            .setInstanceId(INSTANCE_ID)
            .setReplicas(3)
            .setLabels(labels)
            .setImagePullPolicy(imagePullPolicy);

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(application));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(),
            application.getVersion(), service.getShortName())).willReturn(CollectionUtils.listOf(serviceDeploymentInfo));

        mvc.perform(get(PATH_APPLICATIONS + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName()))
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
            .setInstanceId(INSTANCE_ID)
            .setReplicas(3)
            .setLabels(CollectionUtils.listOf(new MicoLabel().setKey("key").setValue("value")))
            .setImagePullPolicy(ImagePullPolicy.IF_NOT_PRESENT);

        MicoServiceDeploymentInfoRequestDTO updatedServiceDeploymentInfoDTO = new MicoServiceDeploymentInfoRequestDTO()
            .setReplicas(5)
            .setLabels(CollectionUtils.listOf(new MicoLabelRequestDTO().setKey("key-updated").setValue("value-updated")))
            .setImagePullPolicy(ImagePullPolicy.NEVER)
            .setTopics(CollectionUtils.listOf(
                new MicoTopicRequestDTO().setName("input-topic").setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRequestDTO().setName("output-topic").setRole(MicoTopicRole.Role.OUTPUT)));

        MicoTopic expectedTopicInput = new MicoTopic().setName("input-topic");
        MicoTopic expectedTopicOutput = new MicoTopic().setName("output-topic");

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServices().add(service);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion())).willReturn(Optional.of(application));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);
        given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(), application.getVersion(), service.getShortName())).willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
        given(serviceDeploymentInfoRepository.save(any(MicoServiceDeploymentInfo.class))).willReturn(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));
        given(micoTopicRepository.findByName(anyString())).willReturn(Optional.empty());
        given(micoTopicRepository.save(eq(expectedTopicInput))).willReturn(expectedTopicInput);
        given(micoTopicRepository.save(eq(expectedTopicOutput))).willReturn(expectedTopicOutput);

        mvc.perform(put(PATH_APPLICATIONS + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SDI_REPLICAS_PATH, is(serviceDeploymentInfo.getReplicas())))
            .andExpect(jsonPath(SDI_LABELS_PATH + "[0].key", is(updatedServiceDeploymentInfoDTO.getLabels().get(0).getKey())))
            .andExpect(jsonPath(SDI_LABELS_PATH + "[0].value", is(updatedServiceDeploymentInfoDTO.getLabels().get(0).getValue())))
            .andExpect(jsonPath(SDI_IMAGE_PULLPOLICY_PATH, is(updatedServiceDeploymentInfoDTO.getImagePullPolicy().toString())))
            .andExpect(jsonPath(SDI_TOPICS_PATH, hasSize(2)))
            .andExpect(jsonPath(SDI_TOPICS_PATH + "[0].name", is(updatedServiceDeploymentInfoDTO.getTopics().get(0).getName())))
            .andExpect(jsonPath(SDI_TOPICS_PATH + "[0].role", is(updatedServiceDeploymentInfoDTO.getTopics().get(0).getRole().toString())))
            .andExpect(jsonPath(SDI_TOPICS_PATH + "[1].name", is(updatedServiceDeploymentInfoDTO.getTopics().get(1).getName())))
            .andExpect(jsonPath(SDI_TOPICS_PATH + "[1].role", is(updatedServiceDeploymentInfoDTO.getTopics().get(1).getRole().toString())))
            .andReturn();
    }

    @Test
    public void updateServiceDeploymentInformationWithDuplicatedTopicRoles() throws Exception {
        MicoApplication application = new MicoApplication()
            .setId(ID)
            .setShortName(SHORT_NAME).setVersion(VERSION);

        MicoService service = new MicoService()
            .setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setId(ID_1)
            .setInstanceId(INSTANCE_ID)
            .setService(service);

        MicoServiceDeploymentInfoRequestDTO updatedServiceDeploymentInfoDTO = new MicoServiceDeploymentInfoRequestDTO()
            .setTopics(CollectionUtils.listOf(
                new MicoTopicRequestDTO().setName("topic-1").setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRequestDTO().setName("topic-2").setRole(MicoTopicRole.Role.INPUT)));

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion())).willReturn(Optional.of(application));
        given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(), application.getVersion(), service.getShortName())).willReturn(CollectionUtils.listOf(serviceDeploymentInfo));

        mvc.perform(put(PATH_APPLICATIONS + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateServiceDeploymentInformationReusesExistingTopics() throws Exception {
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
            .setInstanceId(INSTANCE_ID)
            .setService(service);

        MicoTopic existingTopic1 = new MicoTopic()
            .setId(ID_2)
            .setName("topic-name-1");

        MicoTopic existingTopic2 = new MicoTopic()
            .setId(ID_3)
            .setName("topic-name-2");

        String newTopicName = "new-topic";
        MicoServiceDeploymentInfoRequestDTO updatedServiceDeploymentInfoDTO = new MicoServiceDeploymentInfoRequestDTO()
            .setTopics(CollectionUtils.listOf(
                new MicoTopicRequestDTO().setName(existingTopic1.getName()).setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRequestDTO().setName(newTopicName).setRole(MicoTopicRole.Role.OUTPUT)));
        MicoTopic expectedTopicNew = new MicoTopic().setName(newTopicName);

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServices().add(service);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion())).willReturn(Optional.of(application));
        given(applicationRepository.save(eq(expectedApplication))).willReturn(expectedApplication);
        given(serviceDeploymentInfoRepository.findByApplicationAndService(application.getShortName(), application.getVersion(), service.getShortName())).willReturn(CollectionUtils.listOf(serviceDeploymentInfo));
        given(serviceDeploymentInfoRepository.save(any(MicoServiceDeploymentInfo.class))).willReturn(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));
        given(micoTopicRepository.findFirstByName(existingTopic1.getName())).willReturn(existingTopic1);
        given(micoTopicRepository.findFirstByName(existingTopic2.getName())).willReturn(existingTopic2);
        given(micoTopicRepository.save(eq(existingTopic1))).willReturn(existingTopic1);
        given(micoTopicRepository.save(eq(expectedTopicNew))).willReturn(expectedTopicNew);

        mvc.perform(put(PATH_APPLICATIONS + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SDI_TOPICS_PATH, hasSize(2)))
            .andExpect(jsonPath(SDI_TOPICS_PATH + "[0].name", is(serviceDeploymentInfo.getTopics().get(0).getTopic().getName())))
            .andExpect(jsonPath(SDI_TOPICS_PATH + "[1].name", is(serviceDeploymentInfo.getTopics().get(1).getTopic().getName())))
            .andReturn();

        ArgumentCaptor<MicoServiceDeploymentInfo> sdiCaptor = ArgumentCaptor.forClass(MicoServiceDeploymentInfo.class);

        verify(serviceDeploymentInfoRepository, atLeast(1)).save(sdiCaptor.capture());
        MicoServiceDeploymentInfo actualSdi = sdiCaptor.getValue();
        assertEquals(2, actualSdi.getTopics().size());
        MicoTopicRole actualTopicRole1 = actualSdi.getTopics().get(0);
        assertEquals("Existing topic was not reused!", existingTopic1.getId(), actualTopicRole1.getTopic().getId());
        assertEquals(existingTopic1, actualTopicRole1.getTopic());
        MicoTopicRole actualTopicRole2 = actualSdi.getTopics().get(1);
        assertEquals("New topic was not saved.", newTopicName, actualTopicRole2.getTopic().getName());
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

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(INSTANCE_ID);

        MicoServiceDeploymentInfoResponseDTO updatedServiceDeploymentInfoDTO =
            (MicoServiceDeploymentInfoResponseDTO) new MicoServiceDeploymentInfoResponseDTO()
                .setLabels(labels.stream().map(MicoLabelResponseDTO::new).collect(Collectors.toList()));

        application.getServices().add(service);
        application.getServiceDeploymentInfos().add(serviceDeploymentInfo);
        expectedApplication.getServices().add(service);
        expectedApplication.getServiceDeploymentInfos().add(serviceDeploymentInfo.applyValuesFrom(updatedServiceDeploymentInfoDTO));

        given(applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion()))
            .willReturn(Optional.of(application));

        final ResultActions result = mvc.perform(put(PATH_APPLICATIONS + "/" + application.getShortName() + "/" + application.getVersion() + "/" + PATH_DEPLOYMENT_INFORMATION + "/" + service.getShortName())
            .content(mapper.writeValueAsBytes(updatedServiceDeploymentInfoDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print());

        result.andExpect(status().isUnprocessableEntity());
    }
}
