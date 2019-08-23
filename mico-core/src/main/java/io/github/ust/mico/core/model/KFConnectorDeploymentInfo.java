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

package io.github.ust.mico.core.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import lombok.*;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.*;

/**
 * Represents an instance of a {@link MicoServiceDeploymentInfo}
 * for the deployment of a KafkaFaasConnector.
 * <p>
 * An instance of this class is persisted as a relationship between
 * between a {@link MicoApplication} and a {@link MicoServiceDeploymentInfo} node
 * in the Neo4j database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@RelationshipEntity(type = "PROVIDES_KF_CONNECTOR")
public class KFConnectorDeploymentInfo {

    /**
     * The id of this Kafka Faas Connector deployment info.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required Fields ---
    // ----------------------

    /**
     * This is the {@link MicoApplication} that includes
     * the {@link KFConnectorDeploymentInfo#serviceDeploymentInfo}.
     */
    @JsonBackReference
    @StartNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MicoApplication application;

    /**
     * This is the {@link MicoServiceDeploymentInfo} included by the
     * {@link KFConnectorDeploymentInfo#application}.
     */
    @EndNode
    private MicoServiceDeploymentInfo serviceDeploymentInfo;

    /**
     * This is the instance id of the {@link KFConnectorDeploymentInfo#serviceDeploymentInfo}
     */
    private String instanceId;


    // ----------------------
    // -> Static Creators ---
    // ----------------------

    /**
     * Creates a new {@link KFConnectorDeploymentInfo} based on a {@link KFConnectorDeploymentInfoRequestDTO}.
     */
    public static KFConnectorDeploymentInfo valueOf(KFConnectorDeploymentInfoRequestDTO sdiDto,
                                                    MicoApplication application) {
        // Use default service deployment info and set the topics.
        MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo();
        MicoTopicRole inputTopic = new MicoTopicRole()
            .setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.INPUT)
            .setTopic(new MicoTopic().setName(sdiDto.getInputTopicName()));
        MicoTopicRole outputTopic = new MicoTopicRole()
            .setServiceDeploymentInfo(sdi)
            .setRole(MicoTopicRole.Role.OUTPUT)
            .setTopic(new MicoTopic().setName(sdiDto.getOutputTopicName()));
        sdi.getTopics().add(inputTopic);
        sdi.getTopics().add(outputTopic);

        return new KFConnectorDeploymentInfo()
            .setInstanceId(sdiDto.getInstanceId())
            .setServiceDeploymentInfo(sdi)
            .setApplication(application);
    }
}
