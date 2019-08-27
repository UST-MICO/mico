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
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.model.MicoEnvironmentVariable;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;

import static io.github.ust.mico.core.model.MicoEnvironmentVariable.DefaultNames.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("unit-testing")
public class DefaultEnvironmentVariablesConfigurationTests {

    @Autowired
    OpenFaaSConfig openFaaSConfig;

    @Autowired
    KafkaConfig kafkaConfig;

    @Test
    public void testOpenFaaSConfigForEnvironmentVariables() {
        List<MicoEnvironmentVariable> expectedEnvironmentVariables = new LinkedList<>();
        expectedEnvironmentVariables.add(new MicoEnvironmentVariable().setName(OPENFAAS_GATEWAY.name()).setValue(openFaaSConfig.getGateway()));

        assertThat(openFaaSConfig.getDefaultEnvironmentVariablesForOpenFaaS(), hasSize(1));
        assertThat(openFaaSConfig.getDefaultEnvironmentVariablesForOpenFaaS(), containsInAnyOrder(expectedEnvironmentVariables.toArray()));
    }

    @Test
    public void testKafkaConfigForEnvironmentVariables() {
        List<MicoEnvironmentVariable> expectedEnvironmentVariables = new LinkedList<>();
        expectedEnvironmentVariables.add(new MicoEnvironmentVariable().setName(KAFKA_BOOTSTRAP_SERVERS.name()).setValue(kafkaConfig.getBootstrapServers()));
        expectedEnvironmentVariables.add(new MicoEnvironmentVariable().setName(KAFKA_GROUP_ID.name()).setValue(kafkaConfig.getGroupId()));

        List<MicoEnvironmentVariable> actualEnvVars = kafkaConfig.getDefaultEnvironmentVariablesForKafka();
        assertThat(actualEnvVars, hasSize(2));
        assertThat(actualEnvVars, containsInAnyOrder(expectedEnvironmentVariables.toArray()));
    }

    @Test
    public void testKafkaConfigForTopics() {
        MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo().setId(1000L);
        List<MicoTopicRole> expectedTopics = new LinkedList<>();
        expectedTopics.add(new MicoTopicRole().setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.DEAD_LETTER).setTopic(new MicoTopic().setName(kafkaConfig.getDeadLetterTopic())));
        expectedTopics.add(new MicoTopicRole().setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.INVALID_MESSAGE).setTopic(new MicoTopic().setName(kafkaConfig.getInvalidMessageTopic())));
        expectedTopics.add(new MicoTopicRole().setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.TEST_MESSAGE_OUTPUT).setTopic(new MicoTopic().setName(kafkaConfig.getTestMessageOutputTopic())));

        List<MicoTopicRole> actualTopics = kafkaConfig.getDefaultTopics(sdi);
        assertThat(actualTopics, hasSize(3));
        assertThat(actualTopics, containsInAnyOrder(expectedTopics.toArray()));
    }

}
