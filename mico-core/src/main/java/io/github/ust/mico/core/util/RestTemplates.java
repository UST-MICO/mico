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

package io.github.ust.mico.core.util;

import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.net.PasswordAuthentication;

@Slf4j
@Configuration
public class RestTemplates {

    public static final String QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE = "AuthenticatedOpenFaaSRestTemplate";

    @Autowired
    MicoKubernetesClient kubernetesClient;

    @Bean
    @Qualifier(QUALIFIER_AUTHENTICATED_OPEN_FAAS_REST_TEMPLATE)
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RestTemplate getAuthenticatedOpenFaaSRestTemplate(RestTemplateBuilder builder) {
        PasswordAuthentication passwordAuthentication = kubernetesClient.getOpenFaasCredentials();
        log.debug("Building authenticated openFaaS rest template");
        return builder.basicAuthentication(passwordAuthentication.getUserName(), new String(passwordAuthentication.getPassword())).build();
    }

    /**
     * Prefer the not authenticated rest template
     * @param builder
     * @return
     */
    @Primary
    @Bean
    public RestTemplate getRestTemplate(RestTemplateBuilder builder){
        return new RestTemplate();
    }
}
