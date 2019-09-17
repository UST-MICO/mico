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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceCrawlingOrigin;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.UIDUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.ust.mico.core.JsonPathBuilder.EMBEDDED;
import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_APPLICATIONS;
import static io.github.ust.mico.core.resource.ApplicationResource.PATH_KAFKA_FAAS_CONNECTOR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
