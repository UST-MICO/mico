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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceCrawlingOrigin;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import io.github.ust.mico.core.model.OpenFaaSFunction;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.persistence.MicoTopicRepository;
import io.github.ust.mico.core.util.UIDUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.ust.mico.core.JsonPathBuilder.EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.TestConstants.ID;
import static io.github.ust.mico.core.TestConstants.INPUT_TOPIC;
import static io.github.ust.mico.core.TestConstants.INPUT_TOPIC_1;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID_1;
import static io.github.ust.mico.core.TestConstants.INSTANCE_ID_2;
import static io.github.ust.mico.core.TestConstants.OPEN_FAAS_FUNCTION_NAME;
import static io.github.ust.mico.core.TestConstants.OPEN_FAAS_FUNCTION_NAME_1;
import static io.github.ust.mico.core.TestConstants.OUTPUT_TOPIC;
import static io.github.ust.mico.core.TestConstants.OUTPUT_TOPIC_1;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_KAFKA_FAAS_CONNECTOR;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
public class KafkaFaasConnectorDeploymentInfoResourceEndToEndTests extends Neo4jTestClass {

    private static final String DEPLOYMENT_INFO_RESPONSE_DTO_LIST_JSON_PATH = JsonPathBuilder.buildPath(ROOT, EMBEDDED, "kFConnectorDeploymentInfoResponseDTOList");
    @Autowired
    MicoApplicationRepository applicationRepository;
    @Autowired
    MicoServiceRepository micoServiceRepository;
    @Autowired
    MicoServiceDeploymentInfoRepository deploymentInfoRepository;
    @Autowired
    MicoTopicRepository topicRepository;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    // get requests
    @Test
    public void getKafkaFaasConnectorDeploymentInfoEmptyList() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void getKafkaFaasConnectorDeploymentInfoOneElement() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService kafkaFaasConnectorMicoService = getKafkaFaasConnectorMicoService();
        micoServiceRepository.save(kafkaFaasConnectorMicoService);
        addKafkaFaasConnectorToApplication(application, kafkaFaasConnectorMicoService);
        applicationRepository.save(application);

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(DEPLOYMENT_INFO_RESPONSE_DTO_LIST_JSON_PATH, hasSize(1)));
    }

    @Test
    public void getKafkaFaasConnectorDeploymentInfoMoreElements() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService kafkaFaasConnectorMicoService = getKafkaFaasConnectorMicoService();
        micoServiceRepository.save(kafkaFaasConnectorMicoService);
        int count = 3;
        for (int i = 0; i < count; i++) {
            addKafkaFaasConnectorToApplication(application, kafkaFaasConnectorMicoService);
        }
        applicationRepository.save(application);

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(DEPLOYMENT_INFO_RESPONSE_DTO_LIST_JSON_PATH, hasSize(count)));
    }

    @Test
    public void getKafkaFaasConnectorDeploymentInfoNoApplication() throws Exception {
        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void getKafkaFaasConnectorDeploymentInfoInstanceNoApplication() throws Exception {
        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "/Instance"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void getKafkaFaasConnectorDeploymentInfoInstance() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoService kafkaFaasConnectorMicoService = getKafkaFaasConnectorMicoService();
        String instanceId = "instanceId";
        micoServiceRepository.save(kafkaFaasConnectorMicoService);
        addKafkaFaasConnectorToApplication(application, kafkaFaasConnectorMicoService, instanceId);
        applicationRepository.save(application);

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "/" + instanceId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonPathBuilder.buildPath(ROOT, "instanceId"), is(instanceId)));
    }

    @Test
    public void updateKafkaFaasConnectorDeploymentInfo_1() throws Exception {

        // Test for updating MicoServiceDeploymentInfo with the following properties:
        //  - OpenFaas function is set: no
        //  - Input topic is set: no
        //  - output topic is set: no
        //

        // create the deployment info, that shall be updated
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo()
            .setInstanceId(INSTANCE_ID);
        deploymentInfo.setService(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION));
        deploymentInfoRepository.save(deploymentInfo);

        // create the request for updating the deployment info
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = getKFConnectorDeploymentInfoRequestDTO(deploymentInfo)
            .setInputTopicNames(Collections.singletonList(INPUT_TOPIC_1))
            .setOutputTopicNames(Collections.singletonList(OUTPUT_TOPIC_1))
            .setOpenFaaSFunctionName(OPEN_FAAS_FUNCTION_NAME_1);

        executeUpdateRequest(kfConnectorDeploymentInfoRequestDTO);
        MicoServiceDeploymentInfo deploymentInfoCheck = deploymentInfoRepository.findByInstanceId(INSTANCE_ID).get();
        Set<String> topicNames = deploymentInfoCheck.getTopics().stream().map(topicRole -> topicRole.getTopic().getName()).collect(Collectors.toSet());
        Set<String> expectedNames = new HashSet<>(Arrays.asList(INPUT_TOPIC_1, OUTPUT_TOPIC_1));
        assertEquals(expectedNames, topicNames);
        assertEquals(OPEN_FAAS_FUNCTION_NAME_1, deploymentInfo.getOpenFaaSFunction().getName());
    }

    @Test
    public void updateKafkaFaasConnectorDeploymentInfo_2() throws Exception {

        // Test for updating MicoServiceDeploymentInfo with the following properties:
        //  - OpenFaas function is set: yes
        //  - Input topic is set: yes
        //  - output topic is set: yes
        //

        // create the deployment info, that shall be updated
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo();
        deploymentInfo
            .setInstanceId(INSTANCE_ID)
            .setOpenFaaSFunction(new OpenFaaSFunction(ID, OPEN_FAAS_FUNCTION_NAME))
            .setTopics(Arrays.asList(
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo).setTopic(new MicoTopic()
                    .setName(INPUT_TOPIC)).setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRole()
                    .setServiceDeploymentInfo(deploymentInfo).setTopic(new MicoTopic()
                    .setName(OUTPUT_TOPIC)).setRole(MicoTopicRole.Role.OUTPUT)));
        deploymentInfo.setService(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION));
        deploymentInfoRepository.save(deploymentInfo);

        // create the request for updating the deployment info
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = getKFConnectorDeploymentInfoRequestDTO(deploymentInfo)
            .setInputTopicNames(Collections.singletonList(INPUT_TOPIC_1))
            .setOutputTopicNames(Collections.singletonList(OUTPUT_TOPIC_1))
            .setOpenFaaSFunctionName(OPEN_FAAS_FUNCTION_NAME_1);

        executeUpdateRequest(kfConnectorDeploymentInfoRequestDTO);
        MicoServiceDeploymentInfo deploymentInfoCheck = deploymentInfoRepository.findByInstanceId(INSTANCE_ID).get();
        Set<String> topicNames = deploymentInfoCheck.getTopics().stream().map(topicRole -> topicRole.getTopic().getName()).collect(Collectors.toSet());
        Set<String> expectedNames = new HashSet<>(Arrays.asList(INPUT_TOPIC_1, OUTPUT_TOPIC_1));
        assertEquals(expectedNames, topicNames);
        assertEquals(OPEN_FAAS_FUNCTION_NAME_1, deploymentInfo.getOpenFaaSFunction().getName());
    }

    @Test
    public void updateKafkaFaasConnectorDeploymentInfoWithNullValues() throws Exception {

        // Test for updating MicoServiceDeploymentInfo with the following properties:
        //  - OpenFaas function is set: yes
        //  - Input topic is set: yes
        //  - output topic is set: yes
        //

        // create the deployment info, that shall be updated
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo();
        deploymentInfo
            .setInstanceId(INSTANCE_ID)
            .setOpenFaaSFunction(new OpenFaaSFunction(ID, OPEN_FAAS_FUNCTION_NAME))
            .setTopics(Arrays.asList(
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo).setTopic(new MicoTopic()
                    .setName(INPUT_TOPIC)).setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRole()
                    .setServiceDeploymentInfo(deploymentInfo).setTopic(new MicoTopic()
                    .setName(OUTPUT_TOPIC)).setRole(MicoTopicRole.Role.OUTPUT)));
        deploymentInfo.setService(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION));
        deploymentInfoRepository.save(deploymentInfo);

        // create the request for updating the deployment info
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = getKFConnectorDeploymentInfoRequestDTO(deploymentInfo)
            .setInputTopicNames(null)
            .setOutputTopicNames(null)
            .setOpenFaaSFunctionName(null);

        executeUpdateRequest(kfConnectorDeploymentInfoRequestDTO);
        MicoServiceDeploymentInfo deploymentInfoCheck = deploymentInfoRepository.findByInstanceId(INSTANCE_ID).get();
        assertEquals(0, deploymentInfoCheck.getTopics().size());
        assertNull(deploymentInfoCheck.getOpenFaaSFunction());
    }

    @Test
    public void updateKafkaFaasConnectorDeploymentInfoWithEmptyTopicNames() throws Exception {

        // Test for updating MicoServiceDeploymentInfo with the following properties:
        //  - OpenFaas function is set: no
        //  - Input topic is set: yes
        //  - output topic is set: yes
        //

        // create the deployment info, that shall be updated
        MicoServiceDeploymentInfo deploymentInfo = new MicoServiceDeploymentInfo();
        deploymentInfo
            .setInstanceId(INSTANCE_ID)
            .setTopics(Arrays.asList(
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo).setTopic(new MicoTopic()
                    .setName(INPUT_TOPIC)).setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRole()
                    .setServiceDeploymentInfo(deploymentInfo).setTopic(new MicoTopic()
                    .setName(OUTPUT_TOPIC)).setRole(MicoTopicRole.Role.OUTPUT)));
        deploymentInfo.setService(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION));
        deploymentInfoRepository.save(deploymentInfo);

        // create the request for updating the deployment info
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = getKFConnectorDeploymentInfoRequestDTO(deploymentInfo)
            .setInputTopicNames(Collections.singletonList(""))
            .setOutputTopicNames(Collections.singletonList(""));

        executeUpdateRequest(kfConnectorDeploymentInfoRequestDTO);
        MicoServiceDeploymentInfo deploymentInfoCheck = deploymentInfoRepository.findByInstanceId(INSTANCE_ID).get();
        assertEquals(0, deploymentInfoCheck.getTopics().size());
    }

    @Test
    public void updateKafkaFaasConnectorDeploymentInfoWithExistingTopic() throws Exception {
        // Test ensures that an existing topic is reused, when the update request contains the same name as the
        // existing topic

        // create the deployment info, that already exists
        MicoServiceDeploymentInfo deploymentInfo1 = new MicoServiceDeploymentInfo();
        MicoTopic inputTopic1 = new MicoTopic().setName(INPUT_TOPIC_1);
        MicoTopic inputTopic2 = new MicoTopic().setName(INPUT_TOPIC);
        MicoTopic outputTopic = new MicoTopic().setName(OUTPUT_TOPIC);

        for (MicoTopic topic : Arrays.asList(inputTopic1, inputTopic2, outputTopic)) {
            topic.setId(topicRepository.save(topic).getId());
        }
        deploymentInfo1
            .setInstanceId(INSTANCE_ID_1)
            .setOpenFaaSFunction(new OpenFaaSFunction(ID, OPEN_FAAS_FUNCTION_NAME))
            .setTopics(Arrays.asList(
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo1).setTopic(inputTopic1).setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo1).setTopic(outputTopic).setRole(MicoTopicRole.Role.OUTPUT)));

        MicoServiceDeploymentInfo savedDeploymentInfo1 = deploymentInfoRepository.save(deploymentInfo1);
        deploymentInfoRepository.save(savedDeploymentInfo1);

        // create the deployment info, that shall be updated
        MicoServiceDeploymentInfo deploymentInfo2 = new MicoServiceDeploymentInfo();
        deploymentInfo2
            .setInstanceId(INSTANCE_ID_2)
            .setOpenFaaSFunction(new OpenFaaSFunction(ID, OPEN_FAAS_FUNCTION_NAME))
            .setTopics(Arrays.asList(
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo2).setTopic(inputTopic2).setRole(MicoTopicRole.Role.INPUT),
                new MicoTopicRole().setServiceDeploymentInfo(deploymentInfo2).setTopic(outputTopic).setRole(MicoTopicRole.Role.OUTPUT)));
        MicoServiceDeploymentInfo savedDeploymentInfo2 = deploymentInfoRepository.save(deploymentInfo2);
        deploymentInfo2.setService(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION));
        deploymentInfoRepository.save(savedDeploymentInfo2);

        Iterable<MicoTopic> topicsAll = topicRepository.findAll();
        System.out.println(topicsAll);

        // create the request for updating the deployment info
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = getKFConnectorDeploymentInfoRequestDTO(deploymentInfo2)
            .setInputTopicNames(Collections.singletonList(INPUT_TOPIC_1))
            .setOutputTopicNames(Collections.singletonList(OUTPUT_TOPIC_1))
            .setOpenFaaSFunctionName(OPEN_FAAS_FUNCTION_NAME_1);

        executeUpdateRequest(kfConnectorDeploymentInfoRequestDTO);

        long countInputTopic1 = topicRepository.findAllByName(INPUT_TOPIC_1).size();
        assertEquals(1, countInputTopic1);
    }

    private KFConnectorDeploymentInfoRequestDTO getKFConnectorDeploymentInfoRequestDTO(MicoServiceDeploymentInfo deploymentInfo) {
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = new KFConnectorDeploymentInfoRequestDTO(deploymentInfo);
        kfConnectorDeploymentInfoRequestDTO.setInputTopicNames(Collections.singletonList(INPUT_TOPIC));
        kfConnectorDeploymentInfoRequestDTO.setOutputTopicNames(Collections.singletonList(OUTPUT_TOPIC));
        kfConnectorDeploymentInfoRequestDTO.setOpenFaaSFunctionName(OPEN_FAAS_FUNCTION_NAME);
        return kfConnectorDeploymentInfoRequestDTO;
    }

    private void executeUpdateRequest(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO) throws Exception {
        mvc.perform(put(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR + "/" + kfConnectorDeploymentInfoRequestDTO.getInstanceId())
            .content(mapper.writeValueAsBytes(kfConnectorDeploymentInfoRequestDTO))
            .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonPathBuilder.buildPath(JsonPathBuilder.ROOT, JsonPathBuilder.LINKS, "application", JsonPathBuilder.HREF), endsWith("/applications/" + SHORT_NAME + "/" + VERSION)))
            .andExpect(jsonPath(JsonPathBuilder.buildPath(JsonPathBuilder.ROOT, JsonPathBuilder.LINKS, "kafkaFaasConnector", JsonPathBuilder.HREF), endsWith("/services/" + SHORT_NAME + "/" + VERSION)))
            .andExpect(jsonPath(JsonPathBuilder.buildPath(JsonPathBuilder.ROOT, JsonPathBuilder.LINKS_SELF_HREF), endsWith("/applications/" + SHORT_NAME + "/" + VERSION + "/kafka-faas-connector/" + kfConnectorDeploymentInfoRequestDTO.getInstanceId())));
    }

    private void addKafkaFaasConnectorToApplication(MicoApplication application, MicoService kafkaFaasConnectorMicoService) {
        String instanceId = UIDUtils.uidFor(kafkaFaasConnectorMicoService);
        addKafkaFaasConnectorToApplication(application, kafkaFaasConnectorMicoService, instanceId);
    }

    private void addKafkaFaasConnectorToApplication(MicoApplication application, MicoService kafkaFaasConnectorMicoService, String instanceId) {
        MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo()
            .setService(kafkaFaasConnectorMicoService)
            .setInstanceId(instanceId);
        application.getKafkaFaasConnectorDeploymentInfos().add(sdi);
    }

    private MicoService getKafkaFaasConnectorMicoService() {
        return new MicoService()
            .setShortName("kafka-faas-connector")
            .setVersion("v1.0.1")
            .setKafkaEnabled(true)
            .setName("UST-MICO/kafka-faas-connector")
            .setGitCloneUrl("https://github.com/UST-MICO/kafka-faas-connector.git")
            .setServiceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB);
    }
}
