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

import io.github.ust.mico.core.configuration.KafkaConfig;
import io.github.ust.mico.core.configuration.KafkaFaasConnectorConfig;
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("unit-testing")
public class ApplicationResourceEndToEndTests extends Neo4jTestClass {

    private static final String PATH_SERVICES = "services";
    private static final String PATH_KAFKA_FAAS_CONNECTOR = "kafka-faas-connector";

    @Autowired
    MicoApplicationRepository applicationRepository;

    @Autowired
    MicoServiceRepository serviceRepository;

    @Autowired
    MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    MicoTopicRepository micoTopicRepository;

    @Autowired
    MicoEnvironmentVariableRepository micoEnvironmentVariableRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private OpenFaaSConfig openFaaSConfig;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaFaasConnectorConfig kafkaFaasConnectorConfig;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @Test
    public void addServiceToApplicationShouldBeIdempotent() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);

        MicoService service = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);
        serviceRepository.save(service);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result.isPresent());
        assertThat(result.get().getServices().size(), is(1));
        assertThat(result.get().getServices().get(0), is(service));
        assertThat(result.get().getServiceDeploymentInfos().size(), is(1));
        assertThat(result.get().getServiceDeploymentInfos().get(0).getService(), is(service));

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result2 = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result2.isPresent());
        assertThat(result2.get().getServices().size(), is(1));
        assertThat(result2.get().getServices().get(0), is(service));
        assertThat(result2.get().getServiceDeploymentInfos().size(), is(1));
        assertThat(result2.get().getServiceDeploymentInfos().get(0).getService(), is(service));

    }

    @Test
    public void addServiceToApplicationByReusingExistingInstance() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);
        MicoService service = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);
        serviceRepository.save(service);
        MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo().setService(service).setInstanceId(INSTANCE_ID);
        serviceDeploymentInfoRepository.save(sdi);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + SERVICE_VERSION + "/" + INSTANCE_ID))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result.isPresent());
        assertThat(result.get().getServices().size(), is(1));
        assertThat(result.get().getServices().get(0), is(service));
        assertThat(result.get().getServiceDeploymentInfos().size(), is(1));
        assertThat(result.get().getServiceDeploymentInfos().get(0).getService(), is(service));
        assertThat(result.get().getServiceDeploymentInfos().get(0).getInstanceId(), is(INSTANCE_ID));
    }

    @Test
    public void deleteServiceFromApplication() throws Exception {
        MicoApplication application1 = new MicoApplication().setShortName(SHORT_NAME_1).setVersion(VERSION);
        MicoApplication application2 = new MicoApplication().setShortName(SHORT_NAME_2).setVersion(VERSION);
        MicoService service = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(SERVICE_VERSION);
        serviceRepository.save(service);

        MicoTopic micoTopic1 = new MicoTopic().setName("topic-name-1");
        MicoTopic micoTopic2 = new MicoTopic().setName("topic-name-2");
        MicoServiceDeploymentInfo sdi1 = new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(INSTANCE_ID_1);
        MicoTopicRole topicRole1 = new MicoTopicRole()
            .setServiceDeploymentInfo(sdi1)
            .setRole(MicoTopicRole.Role.INPUT)
            .setTopic(micoTopic1);
        sdi1.getTopics().add(topicRole1);
        MicoServiceDeploymentInfo savedSDI1 = serviceDeploymentInfoRepository.save(sdi1);
        // Save it twice to ensure topic is created correctly
        savedSDI1 = serviceDeploymentInfoRepository.save(savedSDI1);

        MicoServiceDeploymentInfo sdi2 = new MicoServiceDeploymentInfo()
            .setService(service)
            .setInstanceId(INSTANCE_ID_2);
        MicoTopicRole topicRole2 = new MicoTopicRole()
            .setServiceDeploymentInfo(sdi2)
            .setRole(MicoTopicRole.Role.INPUT)
            .setTopic(micoTopic1);
        MicoTopicRole topicRole3 = new MicoTopicRole()
            .setServiceDeploymentInfo(sdi2)
            .setRole(MicoTopicRole.Role.OUTPUT)
            .setTopic(micoTopic2);
        sdi2.getTopics().add(topicRole2);
        sdi2.getTopics().add(topicRole3);
        MicoEnvironmentVariable envVar1 = new MicoEnvironmentVariable().setName("envVarName").setValue("envVarValue");
        sdi2.getEnvironmentVariables().add(envVar1);
        MicoServiceDeploymentInfo savedSDI2 = serviceDeploymentInfoRepository.save(sdi2);
        // Save it twice to ensure topic is created correctly
        savedSDI2 = serviceDeploymentInfoRepository.save(savedSDI2);

        application1.getServices().add(service);
        application1.getServiceDeploymentInfos().add(savedSDI1);
        applicationRepository.save(application1);

        application2.getServices().add(service);
        application2.getServiceDeploymentInfos().add(savedSDI2);
        applicationRepository.save(application2);

        given(micoKubernetesClient.isApplicationUndeployed(application1)).willReturn(true);
        given(micoKubernetesClient.isApplicationUndeployed(application2)).willReturn(true);

        List<MicoServiceDeploymentInfo> sdiBefore = serviceDeploymentInfoRepository.findByApplicationAndService(
            application2.getShortName(), application2.getVersion(), service.getShortName());
        assertThat(sdiBefore.size(), is(1));
        assertThat(sdiBefore.get(0).getTopics().size(), greaterThan(0));
        assertThat(sdiBefore.get(0).getEnvironmentVariables().size(), greaterThan(0));
        assertThat(micoTopicRepository.findByName(topicRole3.getTopic().getName()).isPresent(), is(true));
        assertThat(micoEnvironmentVariableRepository.findAll(), containsInAnyOrder(envVar1));

        mvc.perform(delete(PATH_APPLICATIONS + "/" + SHORT_NAME_2 + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME))
            .andDo(print())
            .andExpect(status().isNoContent());

        Optional<MicoService> resultingService = serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion());
        assertTrue(resultingService.isPresent());
        assertThat(resultingService.get(), is(service));

        Optional<MicoApplication> resultingApplication2 = applicationRepository.findByShortNameAndVersion(application2.getShortName(), application2.getVersion());
        assertTrue(resultingApplication2.isPresent());
        assertThat(resultingApplication2.get().getServices().size(), is(0));
        assertThat(resultingApplication2.get().getServiceDeploymentInfos().size(), is(0));

        Optional<MicoApplication> resultingApplication1 = applicationRepository.findByShortNameAndVersion(application1.getShortName(), application1.getVersion());
        assertTrue(resultingApplication1.isPresent());
        assertThat(resultingApplication1.get().getServices().size(), is(1));
        assertThat(resultingApplication1.get().getServices().get(0), is(service));
        assertThat(resultingApplication1.get().getServiceDeploymentInfos().size(), is(1));
        assertThat(resultingApplication1.get().getServiceDeploymentInfos().get(0).getService(), is(service));
        assertThat(resultingApplication1.get().getServiceDeploymentInfos().get(0).getTopics().size(), is(1));
        assertThat(resultingApplication1.get().getServiceDeploymentInfos().get(0).getTopics().get(0), is(topicRole1));
        assertThat(resultingApplication1.get().getServiceDeploymentInfos().get(0), is(sdi1));

        assertThat("Topic was not deleted", micoTopicRepository.findByName(topicRole3.getTopic().getName()).isPresent(), is(false));
        assertThat("Environment variable was not deleted", micoEnvironmentVariableRepository.findAll(), not(containsInAnyOrder(envVar1)));
    }

    @Test
    public void addKafkaFaasConnectorInstanceOfApplication() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);

        String kafkaFaasConnectorServiceName = kafkaFaasConnectorConfig.getServiceName();
        MicoService kfConnectorService = new MicoService().setShortName(kafkaFaasConnectorServiceName).setVersion(SERVICE_VERSION).setKafkaEnabled(true);
        serviceRepository.save(kfConnectorService);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "?" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "=" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result.isPresent());
        assertThat(result.get().getServices().size(), is(0));
        assertThat(result.get().getServiceDeploymentInfos().size(), is(0));
        assertThat(result.get().getKafkaFaasConnectorDeploymentInfos().size(), is(1));
        assertThat(result.get().getKafkaFaasConnectorDeploymentInfos().get(0).getService(), is(kfConnectorService));
    }

    @Test
    public void addKafkaFaasConnectorInstanceOfApplicationWithoutSpecifyingVersion() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);

        String kafkaFaasConnectorServiceName = kafkaFaasConnectorConfig.getServiceName();
        MicoService kfConnectorService1 = new MicoService().setShortName(kafkaFaasConnectorServiceName).setVersion(VERSION_1_0_1).setKafkaEnabled(true);
        MicoService kfConnectorService2 = new MicoService().setShortName(kafkaFaasConnectorServiceName).setVersion(VERSION_1_0_2).setKafkaEnabled(true);
        serviceRepository.save(kfConnectorService1);
        serviceRepository.save(kfConnectorService2);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR))
            .andDo(print())
            .andExpect(status().isOk());
        Optional<MicoApplication> result = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertThat(result.get().getKafkaFaasConnectorDeploymentInfos().size(), is(1));
        assertThat(result.get().getKafkaFaasConnectorDeploymentInfos().get(0).getService().getVersion(), is(kfConnectorService2.getVersion()));
    }

    @Test
    public void updateKafkaFaasConnectorInstanceOfApplicationShouldBeIdempotent() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);

        String kafkaFaasConnectorServiceName = kafkaFaasConnectorConfig.getServiceName();
        MicoService kfConnectorService = new MicoService().setShortName(kafkaFaasConnectorServiceName).setVersion(SERVICE_VERSION).setKafkaEnabled(true);
        serviceRepository.save(kfConnectorService);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "?" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "=" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result.isPresent());
        assertThat(result.get().getKafkaFaasConnectorDeploymentInfos().size(), is(1));
        assertThat(result.get().getKafkaFaasConnectorDeploymentInfos().get(0).getService(), is(kfConnectorService));
        String instanceId = result.get().getKafkaFaasConnectorDeploymentInfos().get(0).getInstanceId();

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "/" + instanceId + "?" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "=" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result2 = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result2.isPresent());
        assertThat(result2.get().getKafkaFaasConnectorDeploymentInfos().size(), is(1));
        assertThat(result2.get().getKafkaFaasConnectorDeploymentInfos().get(0).getService(), is(kfConnectorService));
        assertThat(result2.get().getKafkaFaasConnectorDeploymentInfos().get(0).getInstanceId(), is(instanceId));

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "/" + instanceId + "?" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "=" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> result3 = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(result3.isPresent());
        assertThat(result3.get().getKafkaFaasConnectorDeploymentInfos().size(), is(1));
        assertThat(result3.get().getKafkaFaasConnectorDeploymentInfos().get(0).getService(), is(kfConnectorService));
        assertThat(result3.get().getKafkaFaasConnectorDeploymentInfos().get(0).getInstanceId(), is(instanceId));
    }

    @Test
    public void deleteAllKafkaFaasConnectorInstancesFromApplication() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);

        String kafkaFaasConnectorServiceName = kafkaFaasConnectorConfig.getServiceName();
        MicoService kfConnectorService = new MicoService().setShortName(kafkaFaasConnectorServiceName).setVersion(SERVICE_VERSION).setKafkaEnabled(true);
        MicoServiceDeploymentInfo sdi1 = new MicoServiceDeploymentInfo()
            .setService(kfConnectorService)
            .setInstanceId(INSTANCE_ID_1);
        MicoServiceDeploymentInfo sdi2 = new MicoServiceDeploymentInfo()
            .setService(kfConnectorService)
            .setInstanceId(INSTANCE_ID_2);
        application.getKafkaFaasConnectorDeploymentInfos().add(sdi1);
        application.getKafkaFaasConnectorDeploymentInfos().add(sdi2);
        serviceRepository.save(kfConnectorService);
        applicationRepository.save(application);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        List<MicoServiceDeploymentInfo> sdiBefore = serviceDeploymentInfoRepository.findByApplicationAndService(
            application.getShortName(), application.getVersion(), kafkaFaasConnectorServiceName);
        assertThat(sdiBefore.size(), is(2));
        Optional<MicoApplication> appBefore = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(appBefore.isPresent());
        assertThat(appBefore.get().getKafkaFaasConnectorDeploymentInfos().size(), is(2));

        mvc.perform(delete(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR))
            .andDo(print())
            .andExpect(status().isNoContent());

        List<MicoServiceDeploymentInfo> sdiAfter = serviceDeploymentInfoRepository.findByApplicationAndService(
            application.getShortName(), application.getVersion(), kafkaFaasConnectorServiceName);
        assertThat(sdiAfter.size(), is(0));
        Optional<MicoApplication> appAfter = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(appAfter.isPresent());
        assertThat(appAfter.get().getKafkaFaasConnectorDeploymentInfos().size(), is(0));
    }

    @Test
    public void deleteKafkaFaasConnectorInstanceFromApplicationByInstanceId() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);

        String kafkaFaasConnectorServiceName = kafkaFaasConnectorConfig.getServiceName();
        MicoService kfConnectorService = new MicoService().setShortName(kafkaFaasConnectorServiceName).setVersion(SERVICE_VERSION).setKafkaEnabled(true);
        MicoServiceDeploymentInfo sdi1 = new MicoServiceDeploymentInfo()
            .setService(kfConnectorService)
            .setInstanceId(INSTANCE_ID_1);
        MicoTopic micoTopic = new MicoTopic().setName("input-topic");
        micoTopic.setId(micoTopicRepository.save(micoTopic).getId());
        MicoTopicRole topicRole1 = new MicoTopicRole().setRole(MicoTopicRole.Role.INPUT).setTopic(micoTopic).setServiceDeploymentInfo(sdi1);
        sdi1.getTopics().add(topicRole1);
        MicoEnvironmentVariable envVar1 = new MicoEnvironmentVariable().setName("envVarName").setValue("envVarValue");
        sdi1.getEnvironmentVariables().add(envVar1);
        MicoServiceDeploymentInfo sdi2 = new MicoServiceDeploymentInfo()
            .setService(kfConnectorService)
            .setInstanceId(INSTANCE_ID_2);
        application.getKafkaFaasConnectorDeploymentInfos().add(sdi1);
        application.getKafkaFaasConnectorDeploymentInfos().add(sdi2);
        serviceRepository.save(kfConnectorService);
        applicationRepository.save(application);

        given(micoKubernetesClient.isApplicationUndeployed(application)).willReturn(true);

        List<MicoServiceDeploymentInfo> sdiBefore = serviceDeploymentInfoRepository.findByApplicationAndService(
            application.getShortName(), application.getVersion(), kafkaFaasConnectorServiceName);
        assertThat(sdiBefore.size(), is(2));
        Optional<MicoServiceDeploymentInfo> sdiFirstOptional = sdiBefore.stream().filter(sdi -> sdi.getInstanceId().equals(INSTANCE_ID_1)).findFirst();
        assertTrue(sdiFirstOptional.isPresent());
        assertThat(sdiFirstOptional.get().getTopics().size(), greaterThan(0));
        assertThat(sdiFirstOptional.get().getEnvironmentVariables().size(), greaterThan(0));
        Optional<MicoApplication> appBefore = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(appBefore.isPresent());
        assertThat(appBefore.get().getKafkaFaasConnectorDeploymentInfos().size(), is(2));
        assertThat(micoTopicRepository.findByName(topicRole1.getTopic().getName()).isPresent(), is(true));
        assertThat(micoEnvironmentVariableRepository.findAll(), containsInAnyOrder(envVar1));

        mvc.perform(delete(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "/" + INSTANCE_ID_1 + "?" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_VERSION + "=" + SERVICE_VERSION))
            .andDo(print())
            .andExpect(status().isNoContent());

        List<MicoServiceDeploymentInfo> sdiAfter = serviceDeploymentInfoRepository.findByApplicationAndService(
            application.getShortName(), application.getVersion(), kafkaFaasConnectorServiceName);
        assertThat(sdiAfter.size(), is(1));
        assertThat(sdiAfter.get(0).getInstanceId(), is(INSTANCE_ID_2));
        Optional<MicoApplication> appAfter = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion());
        assertTrue(appAfter.isPresent());
        assertThat(appAfter.get().getKafkaFaasConnectorDeploymentInfos().size(), is(1));
        assertThat(appAfter.get().getKafkaFaasConnectorDeploymentInfos().get(0).getInstanceId(), is(INSTANCE_ID_2));
        assertThat("Topic was not deleted", micoTopicRepository.findByName(topicRole1.getTopic().getName()).isPresent(), is(false));
        assertThat("Environment variable was not deleted", micoEnvironmentVariableRepository.findAll(), not(containsInAnyOrder(envVar1)));
    }

    @Test
    public void testDefaultVariablesAddedToKafkaEnabledService() throws Exception {
        MicoApplication micoApplication = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(micoApplication);
        MicoService service1 = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(VERSION_1_0_1).setKafkaEnabled(true);
        serviceRepository.save(service1);

        given(micoKubernetesClient.isApplicationUndeployed(micoApplication)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + VERSION_1_0_1)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> savedApplicationOpt = applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION);
        assertTrue(savedApplicationOpt.isPresent());
        MicoApplication savedApplication = savedApplicationOpt.get();
        assertThat(savedApplication.getServiceDeploymentInfos(), hasSize(1));

        MicoServiceDeploymentInfo actualServiceDeploymentInfo = savedApplication.getServiceDeploymentInfos().get(0);
        LinkedList<MicoEnvironmentVariable> expectedMicoEnvironmentVariables = new LinkedList<>();
        expectedMicoEnvironmentVariables.addAll(openFaaSConfig.getDefaultEnvironmentVariablesForOpenFaaS());
        expectedMicoEnvironmentVariables.addAll(kafkaConfig.getDefaultEnvironmentVariablesForKafka());
        expectedMicoEnvironmentVariables.add(new MicoEnvironmentVariable()
            .setName(MicoEnvironmentVariable.DefaultNames.KAFKA_GROUP_ID.name())
            .setValue(actualServiceDeploymentInfo.getInstanceId()));
        List<MicoEnvironmentVariable> actualEnvironmentVariables = actualServiceDeploymentInfo.getEnvironmentVariables();
        assertEquals(3, actualEnvironmentVariables.size());
        assertThat(actualEnvironmentVariables.stream().map(MicoEnvironmentVariable::getName).collect(Collectors.toList()),
            containsInAnyOrder(expectedMicoEnvironmentVariables.stream().map(MicoEnvironmentVariable::getName).toArray()));

        LinkedList<MicoTopicRole> expectedMicoTopics = new LinkedList<>();
        expectedMicoTopics.addAll(kafkaConfig.getDefaultTopics(actualServiceDeploymentInfo));
        List<MicoTopicRole> actualTopics = actualServiceDeploymentInfo.getTopics();
        assertEquals(3, actualTopics.size());
        assertThat(actualTopics.stream().map(MicoTopicRole::getRole).collect(Collectors.toList()),
            containsInAnyOrder(expectedMicoTopics.stream().map(MicoTopicRole::getRole).toArray()));
    }

    @Test
    public void testDefaultVariablesNotAddedToNormalService() throws Exception {
        MicoApplication micoApplication = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(micoApplication);
        MicoService service1 = new MicoService().setShortName(SERVICE_SHORT_NAME).setVersion(VERSION_1_0_1);
        //Kafka enabled is not set
        serviceRepository.save(service1);

        given(micoKubernetesClient.isApplicationUndeployed(micoApplication)).willReturn(true);

        mvc.perform(post(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_SERVICES + "/" + SERVICE_SHORT_NAME + "/" + VERSION_1_0_1)
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk());

        Optional<MicoApplication> savedApplicationOpt = applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION);
        assertTrue(savedApplicationOpt.isPresent());
        MicoApplication savedApplication = savedApplicationOpt.get();
        assertThat(savedApplication.getServiceDeploymentInfos(), hasSize(1));
        List<MicoEnvironmentVariable> actualEnvironmentVariables = savedApplication.getServiceDeploymentInfos().get(0).getEnvironmentVariables();
        assertThat(actualEnvironmentVariables, hasSize(0));

    }
}
