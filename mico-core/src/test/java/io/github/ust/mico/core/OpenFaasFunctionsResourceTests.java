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

import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.util.RestTemplates;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static io.github.ust.mico.core.resource.OpenFaasResource.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class OpenFaasFunctionsResourceTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    OpenFaaSConfig openFaaSConfig;

    @MockBean
    @Qualifier(RestTemplates.QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE)
    RestTemplate restTemplate;

    @Test
    public void getFunctionsListNotReachable() throws Exception {
        given(openFaaSConfig.getGateway()).willReturn("http://notReachableHost.test");
        given(restTemplate.getForEntity(openFaaSConfig.getGateway() + OPEN_FAAS_FUNCTION_LIST_PATH, String.class)).willThrow(new ResourceAccessException(" I/O error"));

        mvc.perform(get(OPEN_FAAS_BASE_PATH + FUNCTIONS_PATH).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isGatewayTimeout())
            .andReturn();
    }

    @Test
    public void getFunctionsListReachable() throws Exception {
        given(openFaaSConfig.getGateway()).willReturn("http://reachableHost.test");
        String testBody = "TestBody";
        ResponseEntity<String> responseEntity = new ResponseEntity<>("TestBody", HttpStatus.OK);
        given(restTemplate.getForEntity(openFaaSConfig.getGateway() + OPEN_FAAS_FUNCTION_LIST_PATH, String.class)).willReturn(responseEntity);
        mvc.perform(get(OPEN_FAAS_BASE_PATH + FUNCTIONS_PATH).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(testBody))
            .andReturn();
    }
}
