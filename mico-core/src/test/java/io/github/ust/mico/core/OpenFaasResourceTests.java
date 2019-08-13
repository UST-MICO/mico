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

import io.github.ust.mico.core.broker.OpenFaasBroker;
import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.service.MicoKubernetesClient;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static io.github.ust.mico.core.broker.OpenFaasBroker.GATEWAY_EXTERNAL_NAME;
import static io.github.ust.mico.core.broker.OpenFaasBroker.OPEN_FAAS_UI_PROTOCOL;
import static io.github.ust.mico.core.resource.OpenFaasResource.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class OpenFaasResourceTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    OpenFaaSConfig openFaaSConfig;

    @MockBean
    @Qualifier(RestTemplates.QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE)
    RestTemplate restTemplate;

    @Autowired
    OpenFaasBroker openFaaSBroker;

    @MockBean
    MicoKubernetesClient micoKubernetesClient;

    @Autowired
    MicoKubernetesConfig micoKubernetesConfig;

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

    @Test
    public void getExternalIp() throws Exception {
        //openFaaSBroker.getExternalAddress();

        //40.115.25.83
        //8080
        //micoKubernetesClient.getPublicIpOfKubernetesService("mico-core", "mico-system");
        //externalUrl":"http://192.168.0.1:8080"
        String ip = "192.168.0.1";
        int port = 8080;
        given(micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Optional.ofNullable(ip));
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Collections.singletonList(Integer.valueOf(port)));
        URL externalAddress = new URL(OPEN_FAAS_UI_PROTOCOL, ip, port, "");
        mvc.perform(get(OPEN_FAAS_BASE_PATH + EXTERNAL_ADDRESS).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.externalUrl", is(externalAddress.toExternalForm())))
            .andExpect(jsonPath("$.externalUrlAvailable", is(true)))
            .andReturn();
    }

    @Test
    public void getEnternalIpNoPorts() throws Exception {
        String ip = "192.168.0.1";
        given(micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Optional.ofNullable("192.168.0.1"));
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(new LinkedList<>());
        mvc.perform(get(OPEN_FAAS_BASE_PATH + EXTERNAL_ADDRESS).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(getEmptyResultMatcher())
            .andReturn();
    }

    @Test
    public void getEnternalIpNoIp() throws Exception {
        int port = 8080;
        given(micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Optional.empty());
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Collections.singletonList(Integer.valueOf(port)));
        mvc.perform(get(OPEN_FAAS_BASE_PATH + EXTERNAL_ADDRESS).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(getEmptyResultMatcher())
            .andReturn();
    }

    @Test
    public void getEnternalIpNoPortNoIp() throws Exception {
        given(micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Optional.empty());
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(new LinkedList<>());
        mvc.perform(get(OPEN_FAAS_BASE_PATH + EXTERNAL_ADDRESS).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(getEmptyResultMatcher())
            .andReturn();
    }

    @Test
    public void getEnternalIpKubernetesResourceException() throws Exception {
        given(micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willThrow(new KubernetesResourceException());
        mvc.perform(get(OPEN_FAAS_BASE_PATH + EXTERNAL_ADDRESS).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void getEnternalIpMalformedURLException() throws Exception {
        String ip = "192.168.0.1";
        int port = -1000; //triggers MalformedURLException
        given(micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Optional.ofNullable(ip));
        given(micoKubernetesClient.getPublicPortsOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace())).willReturn(Collections.singletonList(Integer.valueOf(port)));
        mvc.perform(get(OPEN_FAAS_BASE_PATH + EXTERNAL_ADDRESS).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andReturn();
    }


    ResultMatcher getEmptyResultMatcher() {
        return ResultMatcher.matchAll(status().isOk(), jsonPath("$.externalUrl", isEmptyString()), jsonPath("$.externalUrlAvailable", is(false)));
    }
}