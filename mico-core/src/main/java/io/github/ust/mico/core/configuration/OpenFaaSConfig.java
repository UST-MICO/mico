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

package io.github.ust.mico.core.configuration;

import io.github.ust.mico.core.model.MicoEnvironmentVariable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Configuration for the OpenFaaS connection.
 */
@Slf4j
@Component
@Setter
@Getter
@ConfigurationProperties("openfaas")
public class OpenFaaSConfig {

    /**
     * The URL of the OpenFaaS gateway.
     */
    @NotBlank
    private String gateway;

    public List<MicoEnvironmentVariable> getDefaultEnvironmentVariablesForOpenFaaS(){
        LinkedList<MicoEnvironmentVariable> micoEnvironmentVariables = new LinkedList<>();
        micoEnvironmentVariables.add(new MicoEnvironmentVariable().setName(MicoEnvironmentVariable.DefaultEnvironemntVariableKafkaNames.OPENFAAS_GATEWAY.name()).setValue(gateway));
        return micoEnvironmentVariables;
    }
}
