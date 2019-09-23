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


import io.github.ust.mico.core.broker.TopicBroker;
import io.github.ust.mico.core.dto.response.TopicDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = TopicResource.TOPIC_BASE_PATH, produces = MediaTypes.HAL_JSON_VALUE)
public class TopicResource {

    public static final String TOPIC_BASE_PATH = "/topics";

    @Autowired
    private TopicBroker topicBroker;

    @GetMapping()
    public ResponseEntity<Resources<Resource<TopicDTO>>> getAllTopics() {
        log.debug("Request to get all topics");
        List<String> topics = topicBroker.getAllTopics();
        log.info("Request got '{}' topics", topics.size());
        List<Resource<TopicDTO>> resourceList = new LinkedList<>();
        topics.forEach(topic -> resourceList.add(new Resource<>(new TopicDTO().setName(topic))));
        Resources<Resource<TopicDTO>> responseResourcesList = new Resources<>(resourceList);
        responseResourcesList.add(linkTo(methodOn(TopicResource.class).getAllTopics()).withSelfRel());
        return ResponseEntity.ok(responseResourcesList);
    }

}
