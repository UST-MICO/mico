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
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoServiceDependencyRepository serviceDependencyRepository;

    @Autowired
    private MicoServicePortRepository servicePortRepository;

    @Before
    public void setUp() {
        applicationRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
        serviceDeploymentInfoRepository.deleteAll();
        serviceDependencyRepository.deleteAll();
        servicePortRepository.deleteAll();
    }

    @After
    public void cleanUp() {

    }

    @Commit
    @Test
    public void findServiceInterfaceByServiceAndNameAndDelete() {
        // Create some services and interfaces
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.1");

        MicoServiceInterface i1 = new MicoServiceInterface().setServiceInterfaceName("i1");
        MicoServiceInterface i2 = new MicoServiceInterface().setServiceInterfaceName("i2").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));
        MicoServiceInterface i3 = new MicoServiceInterface().setServiceInterfaceName("i3").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(82).setTargetPort(83), new MicoServicePort().setPort(84).setTargetPort(85)));

        s1.setServiceInterfaces(CollectionUtils.listOf(i1, i2, i3));

        serviceRepository.save(s1);
        serviceRepository.save(s2);

        // Find serviceInterface
        Optional<MicoServiceInterface> micoServiceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i2");
        assertTrue(micoServiceInterfaceOptional.isPresent());
        assertEquals("i2", micoServiceInterfaceOptional.get().getServiceInterfaceName());

        // Delete serviceInterface
        serviceInterfaceRepository.deleteByServiceAndName("s1", "v1.0.0", "i2");
        assertFalse(serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i2").isPresent());
        assertTrue(serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i1").isPresent()); // TODO fix
        assertTrue(serviceInterfaceRepository.findByServiceAndName("s1", "v1.0.0", "i3").isPresent());

        // Two ports should be left
        Iterable<MicoServicePort> micoServicePortIterable = servicePortRepository.findAll();
        int size = 0;
        for (MicoServicePort micoServicePort : micoServicePortIterable) {
            size++;
        }
        assertEquals(2, size);
    }


}
