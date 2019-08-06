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

package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.configuration.MicoKubernetesConfig;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
public class OpenFaaSBroker {


    @Autowired
    MicoKubernetesClient micoKubernetesClient;

    @Autowired
    MicoKubernetesConfig micoKubernetesConfig;

    public static final String GATEWAY_EXTERNAL_NAME = "gateway-external";
    public static final String OPEN_FAAS_UI_PROTOCOL = "http";

    public String getExternalAddress() throws MalformedURLException {
        String ip = micoKubernetesClient.getPublicIpOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace());
        List<Integer> ports = micoKubernetesClient.getPublicPortsOfKubernetesService(GATEWAY_EXTERNAL_NAME, micoKubernetesConfig.getNamespaceOpenFaasWorkspace());
        int port = ports.get(0);
        URL externalAddress = new URL(OPEN_FAAS_UI_PROTOCOL, ip, port, "");
        return externalAddress.toExternalForm();
    }
}
