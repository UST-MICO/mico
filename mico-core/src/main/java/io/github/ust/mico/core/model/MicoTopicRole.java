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
import io.github.ust.mico.core.dto.request.MicoTopicRequestDTO;
import lombok.*;
import lombok.experimental.Accessors;
import org.neo4j.ogm.annotation.*;

/**
 * Represents a role of a {@link MicoTopic}.
 * <p>
 * An instance of this class is persisted as a relationship between
 * a {@link MicoServiceDeploymentInfo} and a {@link MicoTopic} node
 * in the Neo4j database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@RelationshipEntity(type = "HAS")
public class MicoTopicRole {

    /**
     * The id of this topic role.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required Fields ---
    // ----------------------

    /**
     * This is the {@link MicoServiceDeploymentInfo} that includes
     * the {@link MicoTopicRole#topic}.
     */
    @JsonBackReference
    @StartNode
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MicoServiceDeploymentInfo serviceDeploymentInfo;

    /**
     * This is the {@link MicoTopic} included by the
     * {@link MicoTopicRole#serviceDeploymentInfo}.
     */
    @EndNode
    private MicoTopic topic;

    /**
     * This is the role of the {@link MicoTopicRole#topic}
     */
    private Role role;

    public enum Role {
        INPUT, OUTPUT, DEAD_LETTER, INVALID_MESSAGE, TEST_MESSAGE_OUTPUT
    }


    // ----------------------
    // -> Static Creators ---
    // ----------------------

    /**
     * Creates a new {@code MicoTopicRole} based on a {@code MicoTopicRequestDTO}.
     */
    public static MicoTopicRole valueOf(MicoTopicRequestDTO topicDto,
                                        MicoServiceDeploymentInfo serviceDeploymentInfo) {
        return new MicoTopicRole()
            .setTopic(new MicoTopic()
                .setName(topicDto.getName()))
            .setRole(topicDto.getRole())
            .setServiceDeploymentInfo(serviceDeploymentInfo);
    }
}
