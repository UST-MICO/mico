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
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OpenFaasBroker {


    @Autowired
    MicoKubernetesClient micoKubernetesClient;

    @Autowired
    MicoKubernetesConfig micoKubernetesConfig;

    @Autowired
    OpenFaaSConfig openFaaSConfig;

    /**
     * The supported protocol of the OpenFaaS UI
     */
    public static final String OPEN_FAAS_UI_PROTOCOL = "http";


    /**
     * Requests the external address of the OpenFaaS UI and returns it or {@code null} if OpenFaaS does not exist.
     *
     * @return the external address of the OpenFaaS UI or {@code null}.
     * @throws MalformedURLException       if the address is not in the URL format.
     * @throws KubernetesResourceException if the IP address or the ports of the external gateway svc can't be retrieved
     */
    public Optional<URL> getExternalAddress() throws MalformedURLException, KubernetesResourceException {
        Optional<String> ipOptional = micoKubernetesClient.getPublicIpOfKubernetesService(
            openFaaSConfig.getGatewayExternalServiceName(), micoKubernetesConfig.getNamespaceOpenFaasWorkspace());
        List<Integer> ports = micoKubernetesClient.getPublicPortsOfKubernetesService(
            openFaaSConfig.getGatewayExternalServiceName(), micoKubernetesConfig.getNamespaceOpenFaasWorkspace());
        if (!ipOptional.isPresent() || ports.size() < 1) {
            log.warn("The optional of the external Ip '{}' is empty or there are no ports in the ports list '{}'", ipOptional, ports);
            return Optional.empty();
        }
        if (ports.size() > 1) {
            log.warn("There are multiple different ports available. Picking the first");
        }
        String ip = ipOptional.get();
        int port = ports.get(0);
        log.debug("OpenFaaS gateway external has the IP address '{}' and port '{}'.", ip, port);
        URL externalAddress = new URL(OPEN_FAAS_UI_PROTOCOL, ip, port, "");
        return Optional.ofNullable(externalAddress);
    }
}
