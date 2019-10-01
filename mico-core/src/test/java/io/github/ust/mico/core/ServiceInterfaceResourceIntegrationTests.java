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
import io.github.ust.mico.core.dto.request.MicoServiceInterfaceRequestDTO;
import io.github.ust.mico.core.dto.response.status.MicoServiceInterfaceStatusResponseDTO;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
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
import static io.github.ust.mico.core.TestConstants.IntegrationTest.INSTANCE_ID;
import static io.github.ust.mico.core.TestConstants.SHORT_NAME;
import static io.github.ust.mico.core.TestConstants.VERSION;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("unit-testing")
public class ServiceInterfaceResourceIntegrationTests {

    private static final String JSON_PATH_LINKS_SECTION = buildPath(ROOT, LINKS);
    private static final String SELF_HREF = buildPath(JSON_PATH_LINKS_SECTION, SELF, HREF);
    private static final String INTERFACES_HREF = buildPath(JSON_PATH_LINKS_SECTION, "interfaces", HREF);
    private static final String INTERFACE_NAME = "interface-name";
    private static final String INTERFACE_NAME_INVALID = "interface_NAME";
    private static final int INTERFACE_PORT = 1024;
    private static final MicoPortType INTERFACE_PORT_TYPE = MicoPortType.TCP;
    private static final int INTERFACE_TARGET_PORT = 1025;
    private static final String INTERFACE_DESCRIPTION = "This is a service interface.";
    private static final String INTERFACE_PROTOCOL = "HTTPS";
    private static final String SERVICES_HREF = buildPath(ROOT, LINKS, "service", HREF);
    private static final String SERVICE_URL = "/services/" + SHORT_NAME + "/" + VERSION;
    private static final String INTERFACES_URL = SERVICE_URL + "/interfaces";
    private static final String PATH_PART_PUBLIC_IP = "publicIP";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @MockBean
    MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private MicoStatusService micoStatusService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void postServiceInterface() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(getServiceInterfaceMatcher(serviceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();
    }

    @Test
    public void postServiceNotFound() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();

        given(serviceRepository.findByShortNameAndVersion(any(), any())).willReturn(Optional.empty());

        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void postServiceInterfaceExists() throws Exception {
        MicoService service = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceInterface serviceInterface = getTestServiceInterface();
        service.getServiceInterfaces().add(serviceInterface);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceInterfaceRepository.findByServiceAndName(any(), any(), any())).willReturn(Optional.of(serviceInterface));

        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isConflict())
            .andReturn();
    }

    @Test
    public void postInvalidServiceInterface() throws Exception {
        MicoServiceInterface serviceInterface = getInvalidTestServiceInterface();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(new MicoService().setShortName(SHORT_NAME).setVersion(VERSION)));

        mvc.perform(post(INTERFACES_URL)
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andReturn();
    }

    @Test
    public void getSpecificServiceInterface() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();

        given(serviceInterfaceRepository.findByServiceAndName(SHORT_NAME, VERSION,
            serviceInterface.getServiceInterfaceName())).willReturn(Optional.of(serviceInterface));

        mvc.perform(get(INTERFACES_URL + "/" + serviceInterface.getServiceInterfaceName()).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(getServiceInterfaceMatcher(serviceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();
    }

    @Test
    public void getSpecificServiceInterfaceNotFound() throws Exception {
        given(serviceInterfaceRepository.findByServiceAndName(any(), any(), any())).willReturn(Optional.empty());

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

        given(serviceInterfaceRepository.findByService(SHORT_NAME, VERSION)).willReturn(serviceInterfaces);

        mvc.perform(get(INTERFACES_URL).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceResponseDTOList[*]", hasSize(serviceInterfaces.size())))
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceResponseDTOList[?(@.serviceInterfaceName =='" + serviceInterface0.getServiceInterfaceName() + "')]", hasSize(1)))
            .andExpect(jsonPath("$._embedded.micoServiceInterfaceResponseDTOList[?(@.serviceInterfaceName =='" + serviceInterface1.getServiceInterfaceName() + "')]", hasSize(1)))
            .andReturn();
    }

    @Test
    public void getInterfacePublicIpByName() throws Exception {
        String externalIP = "1.2.3.4";
        String instanceId = INSTANCE_ID;
        MicoService micoService = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setInstanceId(instanceId).setService(micoService);
        MicoServiceInterface micoServiceInterface = getTestServiceInterface();
        String serviceInterfaceName = micoServiceInterface.getServiceInterfaceName();

        Optional<Service> kubernetesService = Optional.of(getKubernetesService(micoServiceInterface.getServiceInterfaceName(), externalIP));

        given(serviceDeploymentInfoRepository.findByInstanceId(instanceId)).willReturn(Optional.of(micoServiceDeploymentInfo));
        given(serviceInterfaceRepository.findByServiceAndName(SHORT_NAME, VERSION, serviceInterfaceName)).willReturn(Optional.of(micoServiceInterface));
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(eq(micoServiceDeploymentInfo), eq(serviceInterfaceName))).willReturn(kubernetesService);
        given(micoStatusService.getPublicIpOfKubernetesService(micoServiceDeploymentInfo, micoServiceInterface))
            .willReturn(new MicoServiceInterfaceStatusResponseDTO().setName(serviceInterfaceName).setExternalIp(externalIP).setPort(INTERFACE_PORT));

        mvc.perform(get(INTERFACES_URL + "/" + serviceInterfaceName + "/" + PATH_PART_PUBLIC_IP + "/" + instanceId).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(serviceInterfaceName)))
            .andExpect(jsonPath("$.externalIp", is(externalIP)))
            .andReturn();
    }

    @Test
    public void getInterfacePublicIpByNameWithPendingIP() throws Exception {
        String instanceId = INSTANCE_ID;
        MicoService micoService = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setInstanceId(instanceId).setService(micoService);
        MicoServiceInterface micoServiceInterface = getTestServiceInterface();
        String serviceInterfaceName = micoServiceInterface.getServiceInterfaceName();
        MicoServiceInterfaceStatusResponseDTO interfaceStatusResponseDTO = new MicoServiceInterfaceStatusResponseDTO().setName(serviceInterfaceName);

        Optional<Service> kubernetesService = Optional.of(getKubernetesService(micoServiceInterface.getServiceInterfaceName(), ""));

        given(serviceDeploymentInfoRepository.findByInstanceId(instanceId)).willReturn(Optional.of(micoServiceDeploymentInfo));
        given(serviceInterfaceRepository.findByServiceAndName(SHORT_NAME, VERSION, serviceInterfaceName)).willReturn(Optional.of(micoServiceInterface));
        given(micoKubernetesClient.getInterfaceByNameOfMicoServiceInstance(eq(micoServiceDeploymentInfo), eq(serviceInterfaceName))).willReturn(kubernetesService);
        given(micoStatusService.getPublicIpOfKubernetesService(micoServiceDeploymentInfo, micoServiceInterface)).willReturn(interfaceStatusResponseDTO);

        mvc.perform(get(INTERFACES_URL + "/" + serviceInterfaceName + "/" + PATH_PART_PUBLIC_IP + "/" + instanceId).accept(MediaTypes.HAL_JSON_VALUE))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.name", is(serviceInterfaceName)))
            .andExpect(jsonPath("$.externalIpIsAvailable", is(false)))
            .andExpect(jsonPath("$.externalIp", is(nullValue())))
            .andReturn();
    }

    @Test
    public void putMicoServiceInterfaceNotFoundService() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();

        mvc.perform(put(INTERFACES_URL + "/" + serviceInterface.getServiceInterfaceName())
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void putMicoServiceInterfaceNameNotEqual() throws Exception {
        MicoServiceInterface serviceInterface = getTestServiceInterface();

        mvc.perform(put(INTERFACES_URL + "/" + serviceInterface.getServiceInterfaceName() + "NotEqual")
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().is(422))
            .andExpect(status().reason("The variable 'serviceInterfaceName' must be equal to the name specified in the request body"))
            .andReturn();
    }

    @Test
    public void putMicoServiceInterfaceNotFound() throws Exception {
        MicoService service = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceInterface serviceInterface = getTestServiceInterface();

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        mvc.perform(put(INTERFACES_URL + "/" + serviceInterface.getServiceInterfaceName())
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(serviceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void putMicoServiceInterface() throws Exception {
        MicoService service = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION);
        MicoServiceInterface serviceInterface = new MicoServiceInterface().setServiceInterfaceName(INTERFACE_NAME);
        service.getServiceInterfaces().add(serviceInterface);

        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));
        given(serviceInterfaceRepository.findByServiceAndName(any(), any(), any())).willReturn(Optional.of(serviceInterface));

        MicoServiceInterface modifiedServiceInterface = getTestServiceInterface();

        given(serviceInterfaceRepository.save(modifiedServiceInterface)).willReturn(modifiedServiceInterface);

        mvc.perform(put(INTERFACES_URL + "/" + modifiedServiceInterface.getServiceInterfaceName())
            .content(mapper.writeValueAsBytes(new MicoServiceInterfaceRequestDTO(modifiedServiceInterface))).accept(MediaTypes.HAL_JSON_VALUE).contentType(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(getServiceInterfaceMatcher(modifiedServiceInterface, INTERFACES_URL, SERVICE_URL))
            .andReturn();
    }


    private Service getKubernetesService(String serviceInterfaceName, String externalIP) {
        Service service = new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceInterfaceName)
            .endMetadata()
            .withNewStatus()
            .withNewLoadBalancer()
            .endLoadBalancer()
            .endStatus()
            .build();

        if (externalIP != null && !externalIP.isEmpty()) {
            List<LoadBalancerIngress> ingressList = new ArrayList<>();
            LoadBalancerIngress ingress = new LoadBalancerIngress();
            ingress.setIp(externalIP);
            ingressList.add(ingress);
            service.getStatus().getLoadBalancer().setIngress(ingressList);
        }
        return service;
    }

    private MicoServiceInterface getTestServiceInterface() {
        return new MicoServiceInterface()
            .setServiceInterfaceName(INTERFACE_NAME)
            .setPorts(CollectionUtils.listOf(new MicoServicePort()
                .setPort(INTERFACE_PORT)
                .setType(INTERFACE_PORT_TYPE)
                .setTargetPort(INTERFACE_TARGET_PORT)))
            .setDescription(INTERFACE_DESCRIPTION)
            .setProtocol(INTERFACE_PROTOCOL);
    }

    private MicoServiceInterface getInvalidTestServiceInterface() {
        return new MicoServiceInterface()
            .setServiceInterfaceName(INTERFACE_NAME_INVALID)
            .setPorts(CollectionUtils.listOf(new MicoServicePort()
                .setPort(INTERFACE_PORT)
                .setType(INTERFACE_PORT_TYPE)
                .setTargetPort(INTERFACE_TARGET_PORT)))
            .setDescription(INTERFACE_DESCRIPTION)
            .setProtocol(INTERFACE_PROTOCOL);
    }

    private ResultMatcher getServiceInterfaceMatcher(MicoServiceInterface serviceInterface, String selfBaseUrl, String serviceUrl) {
        URI selfHrefEnding = UriComponentsBuilder.fromUriString(selfBaseUrl + "/" + serviceInterface.getServiceInterfaceName()).build().encode().toUri();
        return ResultMatcher.matchAll(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE),
            jsonPath("$.serviceInterfaceName", is(serviceInterface.getServiceInterfaceName())),
            jsonPath("$.ports", hasSize(serviceInterface.getPorts().size())),
            jsonPath("$.ports", not(empty())),
            jsonPath("$.protocol", is(serviceInterface.getProtocol())),
            jsonPath("$.description", is(serviceInterface.getDescription())),
            jsonPath(SELF_HREF, endsWith(selfHrefEnding.toString())),
            jsonPath(INTERFACES_HREF, endsWith(selfBaseUrl)),
            jsonPath(SERVICES_HREF, endsWith(serviceUrl)));
    }
}
