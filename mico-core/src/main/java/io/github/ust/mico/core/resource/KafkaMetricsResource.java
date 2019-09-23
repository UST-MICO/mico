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

package io.github.ust.mico.core.resource;


import io.github.ust.mico.core.broker.KafkaMetricsBroker;
import io.github.ust.mico.core.dto.response.KafkaTopicMetricsDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.management.*;
import java.io.IOException;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = KafkaMetricsResource.KAFKA_METRICS_BASE_PATH, produces = MediaTypes.HAL_JSON_VALUE)
public class KafkaMetricsResource {

    public static final String KAFKA_METRICS_BASE_PATH = "/kafkaMetrics";
    public static final String TOPIC_NAME = "TopicName";
    public static final String PER_TOPIC_PATH = "/perTopic/";

    @Autowired
    KafkaMetricsBroker kafkaMetricsBroker;

    @ApiOperation(value = "Returns per topic metrics like the total message count and the average message per min rate")
    @GetMapping(PER_TOPIC_PATH + "{" + TOPIC_NAME + "}")
    public ResponseEntity<Resource<KafkaTopicMetricsDTO>> getTopicMetrics(@PathVariable(TOPIC_NAME) String topicName) {
        try {
            long totalMessageCount = kafkaMetricsBroker.getTotalMessageCount(topicName);
            double minuteAverage = kafkaMetricsBroker.getMessagesPerMinuteSinceStart(topicName);
            KafkaTopicMetricsDTO kafkaTopicMetricsDTO = new KafkaTopicMetricsDTO()
                .setTopicName(topicName)
                .setMinuteAverageSinceStart(minuteAverage)
                .setTotalMessageCount(totalMessageCount);
            return ResponseEntity.ok(new Resource<>(kafkaTopicMetricsDTO, linkTo(methodOn(KafkaMetricsResource.class).getTopicMetrics(topicName)).withSelfRel()));
        } catch (InstanceNotFoundException e) {
            log.error("No such topic", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IOException | ReflectionException | AttributeNotFoundException | MBeanException | MalformedObjectNameException e) {
            log.error("A Exception occurred", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
