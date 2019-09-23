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

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;

import static io.github.ust.mico.core.broker.KafkaMetricsBroker.ATTRIBUTE_NAME_PER_MINUTE_RATE_SINCE_START;
import static io.github.ust.mico.core.broker.KafkaMetricsBroker.ATTRIBUTE_NAME_TOTAL_MESSAGES_COUNT;
import static io.github.ust.mico.core.resource.KafkaMetricsResource.KAFKA_METRICS_BASE_PATH;
import static io.github.ust.mico.core.resource.KafkaMetricsResource.PER_TOPIC_PATH;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("unit-testing")
@AutoConfigureMockMvc
public class KafkaMetricsResourceTests {

    @MockBean
    MBeanServerConnection jmxConnector;

    @Autowired
    private MockMvc mvc;

    @Test
    public void getFunctionsListNotReachable() throws Exception {
        int totalMessageCount = 100;
        double minuteAverageSinceStart = 1.0;
        given(jmxConnector.getAttribute(any(), eq(ATTRIBUTE_NAME_TOTAL_MESSAGES_COUNT))).willReturn(Long.valueOf(totalMessageCount));
        given(jmxConnector.getAttribute(any(), eq(ATTRIBUTE_NAME_PER_MINUTE_RATE_SINCE_START))).willReturn(Double.valueOf(minuteAverageSinceStart));
        String testTopic = "testtopic";

        mvc.perform(get(KAFKA_METRICS_BASE_PATH + PER_TOPIC_PATH + testTopic).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("topicName", is(testTopic)))
            .andExpect(jsonPath("totalMessageCount", is(totalMessageCount)))
            .andExpect(jsonPath("minuteAverageSinceStart", is(minuteAverageSinceStart)))
            .andReturn();
    }

    @Test
    public void getMetricsNoTopic() throws Exception {
        given(jmxConnector.getAttribute(any(), any())).willThrow(new InstanceNotFoundException());
        String testTopic = "topicNotExists";

        mvc.perform(get(KAFKA_METRICS_BASE_PATH + PER_TOPIC_PATH + testTopic).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }
}
