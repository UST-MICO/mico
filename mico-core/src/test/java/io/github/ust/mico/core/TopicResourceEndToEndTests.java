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

import io.github.ust.mico.core.model.MicoTopic;
import io.github.ust.mico.core.persistence.MicoTopicRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.resource.TopicResource.TOPIC_BASE_PATH;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("unit-testing")
public class TopicResourceEndToEndTests extends Neo4jTestClass {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MicoTopicRepository micoTopicRepository;

    @Test
    public void getAllTopicsTest() throws Exception {
        String testTopicName = "TestTopic";
        micoTopicRepository.save(new MicoTopic().setName(testTopicName));
        mvc.perform(get(TOPIC_BASE_PATH))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(buildPath(ROOT, EMBEDDED, "topicDTOList[?(@.name=='" + testTopicName + "')]"), hasSize(1)));
    }
}
