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


import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.util.RestTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = OpenFaasResource.OPEN_FAAS_BASE_PATH, produces = MediaTypes.HAL_JSON_VALUE)
public class OpenFaasResource {

    public static final String OPEN_FAAS_BASE_PATH = "/openfaas";

    public static final String FUNCTIONS_PATH = "/functions";
    public static final String OPEN_FAAS_FUNCTION_LIST_PATH = "/system/functions";

    @Autowired
    @Qualifier(RestTemplates.QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE)
    RestTemplate restTemplate;

    @Autowired
    OpenFaaSConfig openFaaSConfig;

    @GetMapping(FUNCTIONS_PATH)
    public ResponseEntity<String> getOpenFaasFunctions() {
        try {
            return restTemplate.getForEntity(openFaaSConfig.getGateway() + OPEN_FAAS_FUNCTION_LIST_PATH, String.class);
        } catch (ResourceAccessException e){
            log.debug("There was an I/O Error for GET {}{}",OPEN_FAAS_BASE_PATH,FUNCTIONS_PATH,e);
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, e.getMessage());
        }
    }
}
