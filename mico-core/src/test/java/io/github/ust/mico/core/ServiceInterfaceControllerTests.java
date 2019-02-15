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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.github.ust.mico.core.configuration.CorsConfig;
import io.github.ust.mico.core.model.MicoPortType;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.web.ServiceInterfaceController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.JsonPathBuilder.*;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ServiceInterfaceController.class)
@OverrideAutoConfiguration(enabled = true) //Needed to override our neo4j config
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {CorsConfig.class})
public class ServiceInterfaceControllerTests {

    private static final String JSON_PATH_LINKS_SECTION = buildPath(ROOT, LINKS);
    private static final String SELF_HREF = buildPath(JSON_PATH_LINKS_SECTION, SELF, HREF);
    private static final String INTERFACES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "interfaces", HREF);
    private static final String INTERFACE_NAME = "interface-name";
    private static final int INTERFACE_PORT = 1024;
    private static final MicoPortType INTERFACE_PORT_TYPE = MicoPortType.TCP;
    private static final int INTERFACE_TARGET_PORT = 1025;
    private static final String INTERFACE_DESCRIPTION = "This is a service interface.";
    private static final String INTERFACE_PUBLIC_DNS = "DNS";
    private static final String SERVICES_HREF = buildPath(ROOT, LINKS, "service", HREF);
    private static final String SERVICE_URL = "/services/" + SHORT_NAME + "/" + VERSION;
    private static final String INTERFACES_URL = SERVICE_URL + "/interfaces";
    private static final String PATH_PART_PUBLIC_IP = "publicIP";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void postServiceInterface() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION))
        );

        MicoServiceInterface serviceInterface = getTestServiceInterface();
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(getServiceInterfaceMatcher(serviceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();
    }

    @Test
    public void postServiceNotFound() throws Exception {
        given(serviceRepository.findByShortNameAndVersion(any(), any())).willReturn(
            Optional.empty()
        );
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void postServiceInterfaceExists() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        MicoService service = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        service.getServiceInterfaces().add(serviceInterface);
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(service)
        );
        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(serviceInterface)).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("An interface with this name is already associated with this service."))
            .andReturn();
    }

    @Test
    public void getSpecificServiceInterface() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoService().setServiceInterfaces(CollectionUtils.listOf(serviceInterface))));

        mvc.perform(get(INTERFACES_URL + "/" + serviceInterface.getServiceInterfaceName()).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(getServiceInterfaceMatcher(serviceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();

    }

    @Test
    public void getSpecificServiceInterfaceNotFound() throws Exception {
        given(serviceRepository.findInterfaceOfServiceByName(any(), any(), any())).willReturn(
            Optional.empty());

        mvc.perform(get(INTERFACES_URL + "/NotThereInterface").accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

    }

    @Test
    public void getAllServiceInterfacesOfService() throws Exception {
        MicoServiceInterface serviceInterface0 = new MicoServiceInterface().setServiceInterfaceName("ServiceInterface0");
        MicoServiceInterface serviceInterface1 = new MicoServiceInterface().setServiceInterfaceName("ServiceInterface1");
        List<MicoServiceInterface> serviceInterfaces = Arrays.asList(serviceInterface0, serviceInterface1);
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(
            Optional.of(new MicoService().setServiceInterfaces(serviceInterfaces)));
        mvc.perform(get(INTERFACES_URL).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceList[*]", hasSize(serviceInterfaces.size())))
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceList[?(@.serviceInterfaceName =='" + serviceInterface0.getServiceInterfaceName() + "')]", hasSize(1)))
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceList[?(@.serviceInterfaceName =='" + serviceInterface1.getServiceInterfaceName() + "')]", hasSize(1)))
            .andReturn();
    }

    @Test
    public void getInterfacePublicIpByName() throws Exception {

        List<String> externalIPs = new ArrayList<>();
        externalIPs.add("1.2.3.4");
        MicoService service = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        String serviceInterfaceName = serviceInterface.getServiceInterfaceName();
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, SHORT_NAME, VERSION)).willReturn(
            Optional.of(serviceInterface));

        Optional<Service> kubernetesService = Optional.of(getKubernetesService(serviceInterface.getServiceInterfaceName(), externalIPs));
        given(micoKubernetesClient.getInterfaceByNameOfMicoService(eq(service), eq(serviceInterfaceName))).willReturn(kubernetesService);

        mvc.perform(get(INTERFACES_URL + "/" + serviceInterfaceName + "/" + PATH_PART_PUBLIC_IP).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$", is(externalIPs)))
            .andReturn();
    }

    @Test
    public void getInterfacePublicIpByNameWithPendingIP() throws Exception {

        List<String> externalIPs = new ArrayList<>();
        MicoService service = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        String serviceInterfaceName = serviceInterface.getServiceInterfaceName();
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, SHORT_NAME, VERSION)).willReturn(
            Optional.of(serviceInterface));

        Optional<Service> kubernetesService = Optional.of(getKubernetesService(serviceInterface.getServiceInterfaceName(), externalIPs));
        given(micoKubernetesClient.getInterfaceByNameOfMicoService(eq(service), eq(serviceInterfaceName))).willReturn(kubernetesService);

        mvc.perform(get(INTERFACES_URL + "/" + serviceInterfaceName + "/" + PATH_PART_PUBLIC_IP).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json("[]"))
            .andReturn();
    }

    private Service getKubernetesService(String serviceInterfaceName, List<String> externalIPs) {
        Service service = new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceInterfaceName)
            .endMetadata()
            .withNewStatus()
            .withNewLoadBalancer()
            .endLoadBalancer()
            .endStatus()
            .build();

        if (externalIPs != null && !externalIPs.isEmpty()) {
            List<LoadBalancerIngress> ingressList = new ArrayList<>();
            for (String externalIP : externalIPs) {
                LoadBalancerIngress ingress = new LoadBalancerIngress();
                ingress.setIp(externalIP);
                ingressList.add(ingress);
            }
            service.getStatus().getLoadBalancer().setIngress(ingressList);
        }
        return service;
    }

    private MicoServiceInterface getTestServiceInterface() {
        return new MicoServiceInterface()
            .setServiceInterfaceName(INTERFACE_NAME)
            .setPorts(CollectionUtils.listOf(new MicoServicePort()
                .setNumber(INTERFACE_PORT)
                .setType(INTERFACE_PORT_TYPE)
                .setTargetPort(INTERFACE_TARGET_PORT)))
            .setDescription(INTERFACE_DESCRIPTION)
            .setPublicDns(INTERFACE_PUBLIC_DNS);
    }

    private ResultMatcher getServiceInterfaceMatcher(MicoServiceInterface serviceInterface, String selfBaseUrl, String serviceUrl) {
        URI selfHrefEnding = UriComponentsBuilder.fromUriString(selfBaseUrl + "/" + serviceInterface.getServiceInterfaceName()).build().encode().toUri();
        return ResultMatcher.matchAll(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE),
            jsonPath("$.serviceInterfaceName", is(serviceInterface.getServiceInterfaceName())),
            jsonPath("$.ports", hasSize(serviceInterface.getPorts().size())),
            jsonPath("$.ports", not(empty())),
            jsonPath("$.protocol", is(serviceInterface.getProtocol())),
            jsonPath("$.description", is(serviceInterface.getDescription())),
            jsonPath("$.publicDns", is(serviceInterface.getPublicDns())),
            jsonPath("$.transportProtocol", is(serviceInterface.getTransportProtocol())),
            jsonPath(SELF_HREF, endsWith(selfHrefEnding.toString())),
            jsonPath(INTERFACES_HREF, endsWith(selfBaseUrl)),
            jsonPath(SERVICES_HREF, endsWith(serviceUrl)));
    }
}
