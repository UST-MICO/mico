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

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.ServiceControllerTests.SERVICE_LIST;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ServiceControllerIntegrationTests extends Neo4jTestClass {

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    public void deleteService() throws Exception {
        MicoService micoService = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        serviceRepository.save(micoService);
        mvc.perform(delete( SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();
        Optional<MicoService> micoServiceOptional = serviceRepository.findByShortNameAndVersion(SHORT_NAME,VERSION_1_0_1);
        assertFalse("The service was not deleted properly",micoServiceOptional.isPresent());
    }

    @Test
    public void getServiceDependencyGraph() throws Exception {
        //Setup mico services
        MicoService micoService0 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        MicoService micoService1 = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1);
        MicoService micoService2 = new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_1);
        MicoService micoService3 = new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_1);

        //Set dependencies
        MicoServiceDependency micoServiceDependency0To1 = new MicoServiceDependency().setService(micoService0).setDependedService(micoService1);
        MicoServiceDependency micoServiceDependency0To2 = new MicoServiceDependency().setService(micoService0).setDependedService(micoService2);
        List<MicoServiceDependency> micoServiceDependenciesOfService0 = new LinkedList<>();
        micoServiceDependenciesOfService0.add(micoServiceDependency0To1);
        micoServiceDependenciesOfService0.add(micoServiceDependency0To2);
        micoService0.setDependencies(micoServiceDependenciesOfService0);

        MicoServiceDependency micoServiceDependency1To3 = new MicoServiceDependency().setService(micoService1).setDependedService(micoService3);
        micoService1.setDependencies(Collections.singletonList(micoServiceDependency1To3));

        //save to db
        serviceRepository.save(micoService0);
        serviceRepository.save(micoService1);
        serviceRepository.save(micoService2);
        serviceRepository.save(micoService3);

        mvc.perform(get(SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1 + "/dependencyGraph").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath(SERVICE_LIST, hasSize(4)))
            .andReturn();
    }
}
