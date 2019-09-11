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
import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.JsonPathBuilder.EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.TestConstants.*;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static io.github.ust.mico.core.resource.KafkaFaasConnectorDeploymentInfoResource.PATH_KAFKA_FAAS_CONNECTOR_DEPLOYMENT_INFORMATION;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
    private MockMvc mvc;

    @Autowired
    MicoApplicationRepository applicationRepository;

    @Autowired
    MicoServiceRepository micoServiceRepository;

    @Autowired
    MicoServiceDeploymentInfoRepository deploymentInfoRepository;

    @Autowired
    private ObjectMapper mapper;

    // get requests
    @Test
    public void getKafkaFaasConnectorDeploymentInfoEmptyList() throws Exception {
        MicoApplication application = new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION);
        applicationRepository.save(application);

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR_DEPLOYMENT_INFORMATION))
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

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR_DEPLOYMENT_INFORMATION))
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

        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR_DEPLOYMENT_INFORMATION))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(DEPLOYMENT_INFO_RESPONSE_DTO_LIST_JSON_PATH, hasSize(count)));
    }

    @Test
    public void getKafkaFaasConnectorDeploymentInfoNoApplication() throws Exception {
        mvc.perform(get(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR_DEPLOYMENT_INFORMATION))
            .andDo(print())
            .andExpect(status().isNotFound());
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
        updateKafkaFaasConnectorDeploymentInfoTestProcedure(deploymentInfo);
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
                        new MicoTopicRole(ID_1, deploymentInfo, new MicoTopic(ID_1, INPUT_TOPIC), MicoTopicRole.Role.INPUT),
                        new MicoTopicRole(ID_2, deploymentInfo, new MicoTopic(ID_2, OUTPUT_TOPIC), MicoTopicRole.Role.OUTPUT)));
        updateKafkaFaasConnectorDeploymentInfoTestProcedure(deploymentInfo);
    }

    private KFConnectorDeploymentInfoRequestDTO getKFConnectorDeploymentInfoRequestDTO(MicoServiceDeploymentInfo deploymentInfo) {
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = new KFConnectorDeploymentInfoRequestDTO(deploymentInfo);
        kfConnectorDeploymentInfoRequestDTO.setInputTopicName(INPUT_TOPIC);
        kfConnectorDeploymentInfoRequestDTO.setOutputTopicName(OUTPUT_TOPIC);
        kfConnectorDeploymentInfoRequestDTO.setOpenFaaSFunctionName(OPEN_FAAS_FUNCTION_NAME);
        return kfConnectorDeploymentInfoRequestDTO;

    }

    private void updateKafkaFaasConnectorDeploymentInfoTestProcedure(MicoServiceDeploymentInfo deploymentInfo) throws Exception {
        deploymentInfoRepository.save(deploymentInfo);

        // create the request for updating the deployment info
        KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO = getKFConnectorDeploymentInfoRequestDTO(deploymentInfo);

        // send the request to the endpoint
        mvc.perform(put(PATH_APPLICATIONS + "/" + SHORT_NAME + "/" + VERSION + "/" + PATH_KAFKA_FAAS_CONNECTOR_DEPLOYMENT_INFORMATION + "/" + INSTANCE_ID)
                .content(mapper.writeValueAsBytes(kfConnectorDeploymentInfoRequestDTO))
                .contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk());

        // check if the deploymentInfo was acutally updated in the database
        MicoServiceDeploymentInfo deploymentInfoCheck = deploymentInfoRepository.findByInstanceId(INSTANCE_ID).get();
        assertEquals(OPEN_FAAS_FUNCTION_NAME, deploymentInfo.getOpenFaaSFunction().getName());
        List<String> topicNames = deploymentInfoCheck.getTopics().stream().map(topicRole -> topicRole.getTopic().getName()).collect(Collectors.toList());
        assertArrayEquals(Arrays.asList(INPUT_TOPIC, OUTPUT_TOPIC).toArray(), topicNames.toArray());
    }

    private void addKafkaFaasConnectorToApplication(MicoApplication application, MicoService kafkaFaasConnectorMicoService) {
        String instanceId = UIDUtils.uidFor(kafkaFaasConnectorMicoService);
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
