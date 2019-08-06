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


import io.github.ust.mico.core.broker.OpenFaasBroker;
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.dto.response.ExternalUrlDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
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

import java.net.MalformedURLException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = OpenFaasResource.OPEN_FAAS_BASE_PATH, produces = MediaTypes.HAL_JSON_VALUE)
public class OpenFaasResource {

    public static final String OPEN_FAAS_BASE_PATH = "/openFaaS";

    public static final String FUNCTIONS_PATH = "/functions";
    public static final String OPEN_FAAS_FUNCTION_LIST_PATH = "/system/functions";

    public static final String EXTERNAL_ADDRESS = "/externalAddress";

    @Autowired
    @Qualifier(RestTemplates.QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE)
    RestTemplate restTemplate;

    @Autowired
    OpenFaaSConfig openFaaSConfig;

    @Autowired
    OpenFaasBroker openFaasBroker;

    @GetMapping(FUNCTIONS_PATH)
    public ResponseEntity<String> getOpenFaasFunctions() {
        try {
            String openFaasFunctionListURI = openFaaSConfig.getGateway() + OPEN_FAAS_FUNCTION_LIST_PATH;
            log.debug("OpenFaaS function list uri {}", openFaasFunctionListURI);
            return restTemplate.getForEntity(openFaasFunctionListURI, String.class);
        } catch (ResourceAccessException e) {
            log.debug("There was an I/O Error for GET {}{}", OPEN_FAAS_BASE_PATH, FUNCTIONS_PATH, e);
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, e.getMessage());
        }
    }

    @GetMapping(EXTERNAL_ADDRESS)
    public ResponseEntity<ExternalUrlDTO> getOpenFaasURL() {
        try {
            Optional<String> externalAddressOptional = openFaasBroker.getExternalAddress();
            String externalAddress = externalAddressOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "There is no external address associated with the OpenFaaS UI"));
            ExternalUrlDTO externalUrlDTO = new ExternalUrlDTO(externalAddress);
            return ResponseEntity.ok().body(externalUrlDTO);
        } catch (MalformedURLException | KubernetesResourceException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }
}
