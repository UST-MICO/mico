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
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoServiceInterfaceRepositoryTests {
    @Autowired
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoBackgroundJobRepository backgroundJobRepository;

    @Autowired
    private MicoEnvironmentVariableRepository environmentVariableRepository;

    @Autowired
    private MicoInterfaceConnectionRepository interfaceConnectionRepository;

    @Autowired
    private MicoLabelRepository labelRepository;

    @Autowired
    private MicoServiceDependencyRepository serviceDependencyRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoServicePortRepository servicePortRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Before
    public void setUp() {
        kubernetesDeploymentInfoRepository.deleteAll();
        applicationRepository.deleteAll();
        backgroundJobRepository.deleteAll();
        environmentVariableRepository.deleteAll();
        interfaceConnectionRepository.deleteAll();
        labelRepository.deleteAll();
        serviceDependencyRepository.deleteAll();
        serviceDeploymentInfoRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
        servicePortRepository.deleteAll();
        serviceRepository.deleteAll();
    }

    @After
    public void cleanUp() {

    }

    private MicoService s1;

    private MicoServiceInterface i1;
    private MicoServiceInterface i2;
    private MicoServiceInterface i3;

    @Commit
    @Test
    public void findServiceInterfaceByService() {
        createTestData();

        // Find serviceInterfaces
        List<MicoServiceInterface> micoServiceInterfaceList = serviceInterfaceRepository.findByService("s1", "v1.0.0");
        assertEquals(3, micoServiceInterfaceList.size());

        // Check if every serviceInterface with its corresponding ports is returned
        for (int i = 0; i < micoServiceInterfaceList.size(); i++) {
            MicoServiceInterface serviceInterface = micoServiceInterfaceList.get(i);

            if (serviceInterface.getServiceInterfaceName().equals("i1")) {
                assertEquals(1, serviceInterface.getPorts().size());

            } else if (serviceInterface.getServiceInterfaceName().equals("i2")) {
                assertEquals(1, serviceInterface.getPorts().size());

            } else {
                assertEquals(2, serviceInterface.getPorts().size());
            }
        }
    }

    @Commit
    @Test
    public void findServiceInterfaceByServiceAndName() {
        createTestData();

        // Find serviceInterface
        Optional<MicoServiceInterface> micoServiceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i2");
        assertTrue(micoServiceInterfaceOptional.isPresent());
        assertEquals("i2", micoServiceInterfaceOptional.get().getServiceInterfaceName());
    }

    @Commit
    @Test
    public void deleteServiceInterfaceByServiceAndName () {
        createTestData();

        // Delete serviceInterface
        serviceInterfaceRepository.deleteByServiceAndName("s1", "v1.0.0", "i2");

        // Check if the correct interface is deleted
        assertFalse(serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i2").isPresent());
        assertTrue(serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i1").isPresent());
        assertTrue(serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i3").isPresent());

        // Three ports should be left
        Iterable<MicoServicePort> micoServicePortIterable = servicePortRepository.findAll();
        int size = 0;

        for (MicoServicePort micoServicePort : micoServicePortIterable) {
            size++;
        }
        assertEquals(3, size);
    }

    /**
     * Create some services and interfaces
     */
    private void createTestData() {
        s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");

        i1 = new MicoServiceInterface().setServiceInterfaceName("i1").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));
        i2 = new MicoServiceInterface().setServiceInterfaceName("i2").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(82).setTargetPort(83)));
        i3 = new MicoServiceInterface().setServiceInterfaceName("i3").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(84).setTargetPort(85), new MicoServicePort().setPort(86).setTargetPort(87)));

        s1.setServiceInterfaces(CollectionUtils.listOf(i1, i2, i3));

        serviceRepository.save(s1);
    }


}
