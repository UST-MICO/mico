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

package io.github.ust.mico.core.configuration;

import io.github.ust.mico.core.model.MicoEnvironmentVariable;
import io.github.ust.mico.core.model.MicoEnvironmentVariable.DefaultNames;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.model.MicoTopicRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the Kafka connection.
 */
@Component
@Setter
@Getter
@ConfigurationProperties("kafka")
public class KafkaConfig {

    /**
     * The URLs of the Kafka bootstrap servers in a comma separated list.
     * Example: localhost:9092,localhost:9093
     */
    @NotBlank
    private String bootstrapServers;

    /**
     * The group id is a string that uniquely identifies the group
     * of consumer processes to which this consumer belongs.
     */
    @NotBlank
    private String groupId;

    /**
     * Used to report message processing errors
     */
    @NotBlank
    private String invalidMessageTopic;

    /**
     * Used to report routing errors
     */
    @NotBlank
    private String deadLetterTopic;

    @NotBlank
    private String testMessageOutputTopic;

    public List<MicoEnvironmentVariable> getDefaultEnvironmentVariablesForKafka() {
        LinkedList<MicoEnvironmentVariable> micoEnvironmentVariables = new LinkedList<>();
        micoEnvironmentVariables.add(new MicoEnvironmentVariable().setName(DefaultNames.KAFKA_BOOTSTRAP_SERVERS.name()).setValue(bootstrapServers));
        micoEnvironmentVariables.add(new MicoEnvironmentVariable().setName(DefaultNames.KAFKA_GROUP_ID.name()).setValue(groupId));
        return micoEnvironmentVariables;
    }

    public List<MicoTopicRole> getDefaultTopics(MicoServiceDeploymentInfo sdi) {
        LinkedList<MicoTopicRole> micoEnvironmentVariables = new LinkedList<>();
        micoEnvironmentVariables.add(new MicoTopicRole().setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.DEAD_LETTER).setTopic(new MicoTopic().setName(deadLetterTopic)));
        micoEnvironmentVariables.add(new MicoTopicRole().setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.INVALID_MESSAGE).setTopic(new MicoTopic().setName(invalidMessageTopic)));
        micoEnvironmentVariables.add(new MicoTopicRole().setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.TEST_MESSAGE_OUTPUT).setTopic(new MicoTopic().setName(testMessageOutputTopic)));
        return micoEnvironmentVariables;
    }
}
