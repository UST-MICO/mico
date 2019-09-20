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


import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = KafkaMetricsResource.KAFKA_METRICS_BASE_PATH, produces = MediaTypes.HAL_JSON_VALUE)
public class KafkaMetricsResource {

    public static final String KAFKA_METRICS_BASE_PATH = "/kafkaMetrics";
    public static final String TOPIC_NAME = "TopicName";

    @GetMapping("/perTopic/" + "{" + TOPIC_NAME + "}")
    public ResponseEntity getTopicMetrics(@PathVariable(TOPIC_NAME) String applicationShortName) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

}
