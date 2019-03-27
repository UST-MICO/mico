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
import io.github.ust.mico.core.service.MicoKubernetesClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.JsonPathBuilder.ROOT;
import static io.github.ust.mico.core.TestConstants.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("local")
public class ServiceResourceEndToEndTests extends Neo4jTestClass {

    private static final String MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH = JsonPathBuilder.buildPath(ROOT, "micoServices");

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @Test
    public void deleteService() throws Exception {
        MicoService micoService = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1).setName(NAME);
        serviceRepository.save(micoService);
        mvc.perform(delete(SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1).accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();
        Optional<MicoService> micoServiceOptional = serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION_1_0_1);
        assertFalse("The service was not deleted properly", micoServiceOptional.isPresent());
    }

    @Test
    public void getServiceDependencyGraph() throws Exception {
        //Setup mico services
        MicoService micoService0 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        MicoService micoService1 = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1);
        MicoService micoService2 = new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_1);
        MicoService micoService3 = new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_1);
        List<MicoService> fullDependencyList = new ArrayList<>();
        fullDependencyList.add(micoService0);
        fullDependencyList.add(micoService1);
        fullDependencyList.add(micoService2);
        fullDependencyList.add(micoService3);
        String independentServiceShortName = SHORT_NAME_3 + "Independent";
        MicoService micoServiceIndependent = new MicoService().setShortName(independentServiceShortName).setVersion(VERSION_1_0_1);

        //Set dependencies
        MicoServiceDependency micoServiceDependency0To1 = new MicoServiceDependency().setService(micoService0).setDependedService(micoService1);
        MicoServiceDependency micoServiceDependency0To2 = new MicoServiceDependency().setService(micoService0).setDependedService(micoService2);
        List<MicoServiceDependency> micoServiceDependenciesOfService0 = new ArrayList<>();
        micoServiceDependenciesOfService0.add(micoServiceDependency0To1);
        micoServiceDependenciesOfService0.add(micoServiceDependency0To2);
        micoService0.setDependencies(micoServiceDependenciesOfService0);
        MicoServiceDependency micoServiceDependency1To3 = new MicoServiceDependency().setService(micoService1).setDependedService(micoService3);
        micoService1.setDependencies(Collections.singletonList(micoServiceDependency1To3));

        //save to db
        fullDependencyList.forEach(serviceRepository::save);
        serviceRepository.save(micoServiceIndependent);


        //Add a matcher for each mico service
        ResultMatcher[] fulldependencyMatcherList = getResultMatchersMicoServiceList(fullDependencyList);
        mvc.perform(get(SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1 + "/dependencyGraph").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH, hasSize(4)))
                .andExpect(ResultMatcher.matchAll(fulldependencyMatcherList))
                .andExpect(jsonPath(MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH + "[?(@.shortName=='" + independentServiceShortName + "')]", hasSize(0))) //Check that the independent service is not in the result list
                .andExpect(jsonPath(getJsonPathForEdgeInDependencyGraph(micoService0, micoService1), hasSize(1)))
                .andExpect(jsonPath(getJsonPathForEdgeInDependencyGraph(micoService0, micoService2), hasSize(1)))
                .andExpect(jsonPath(getJsonPathForEdgeInDependencyGraph(micoService1, micoService3), hasSize(1)))
                .andReturn();
    }

    /**
     * Based on https://github.com/UST-MICO/mico/pull/499#pullrequestreview-209043717
     *
     * @throws Exception
     */
    @Test
    public void getServiceDependencyGraphWithCycle() throws Exception {
        //Setup mico services
        MicoService micoServiceA = new MicoService().setShortName("a").setVersion(VERSION_1_0_1);
        MicoService micoServiceB = new MicoService().setShortName("b").setVersion(VERSION_1_0_1);
        MicoService micoServiceC = new MicoService().setShortName("c").setVersion(VERSION_1_0_1);
        List<MicoService> fullDependencyList = new ArrayList<>();
        fullDependencyList.add(micoServiceA);
        fullDependencyList.add(micoServiceB);
        fullDependencyList.add(micoServiceC);

        //Set dependencies
        MicoServiceDependency micoServiceDependencyAtoB = new MicoServiceDependency().setService(micoServiceA).setDependedService(micoServiceB);
        MicoServiceDependency micoServiceDependencyBToC = new MicoServiceDependency().setService(micoServiceB).setDependedService(micoServiceC);
        MicoServiceDependency micoServiceDependencyCToB = new MicoServiceDependency().setService(micoServiceC).setDependedService(micoServiceB);
        micoServiceA.setDependencies(Collections.singletonList(micoServiceDependencyAtoB));
        micoServiceB.setDependencies(Collections.singletonList(micoServiceDependencyBToC));
        micoServiceC.setDependencies(Collections.singletonList(micoServiceDependencyCToB));

        //save to db
        fullDependencyList.forEach(serviceRepository::save);

        //Add a matcher for each mico service
        ResultMatcher[] fulldependencyMatcherList = getResultMatchersMicoServiceList(fullDependencyList);
        mvc.perform(get(SERVICES_PATH + "/" + micoServiceA.getShortName() + "/" + "/" + micoServiceA.getVersion() + "/dependencyGraph").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH, hasSize(fullDependencyList.size())))
                .andExpect(ResultMatcher.matchAll(fulldependencyMatcherList))
                .andExpect(jsonPath(getJsonPathForEdgeInDependencyGraph(micoServiceA, micoServiceB), hasSize(1)))
                .andExpect(jsonPath(getJsonPathForEdgeInDependencyGraph(micoServiceB, micoServiceC), hasSize(1)))
                .andExpect(jsonPath(getJsonPathForEdgeInDependencyGraph(micoServiceC, micoServiceB), hasSize(1)))
                .andReturn();
    }

    /**
     * Generates the json path to match an edge in a service dependency graph. It
     * matches the edge {source} -> {target}.
     *
     * @param source the mico service at the beginning of the edge
     * @param target the mico service at the end of the edge
     * @return a valid json path which matches the edge.
     */
    private String getJsonPathForEdgeInDependencyGraph(MicoService source, MicoService target) {
        String edgeJsonPath = "[?(@.sourceShortName=='%s' && @.targetShortName== '%s')]";
        String edgeListPath = JsonPathBuilder.buildPath(ROOT, "micoServiceDependencyGraphEdgeList");
        return edgeListPath + String.format(edgeJsonPath, source.getShortName(), target.getShortName());
    }

    /**
     * Generates an array of result matchers. Each matcher will match for one mico service in the given list.
     *
     * @param fullDependencyList
     * @return
     */
    private ResultMatcher[] getResultMatchersMicoServiceList(List<MicoService> fullDependencyList) {
        ResultMatcher[] fulldependencyMatcherList = new ResultMatcher[fullDependencyList.size()];
        for (int i = 0; i < fullDependencyList.size(); i++) {
            fulldependencyMatcherList[i] = jsonPath(MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH + "[?(@.shortName=='" + fullDependencyList.get(i).getShortName() + "')]", hasSize(1));
        }
        return fulldependencyMatcherList;
    }

    @Test
    public void getServiceDependencyGraphEmptyResult() throws Exception {
        //No dependencies in Database
        MicoService micoService0 = new MicoService().setShortName(SHORT_NAME).setVersion(VERSION_1_0_1);
        serviceRepository.save(micoService0);
        mvc.perform(get(SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1 + "/dependencyGraph").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath(MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH, hasSize(1))) //Should only contain the root mico service it self
                .andExpect(jsonPath(MICO_SERVICE_LIST_IN_DEPENDENCY_GRAPH_PATH + "[?(@.shortName=='" + micoService0.getShortName() + "')]", hasSize(1)))
                .andReturn();
    }

    @Test
    public void getServiceDependencyGraphEmptyDatabase() throws Exception {
        mvc.perform(get(SERVICES_PATH + "/" + SHORT_NAME + "/" + "/" + VERSION_1_0_1 + "/dependencyGraph").accept(MediaTypes.HAL_JSON_UTF8_VALUE))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
