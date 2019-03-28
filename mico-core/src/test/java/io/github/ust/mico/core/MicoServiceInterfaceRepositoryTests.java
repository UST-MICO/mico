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

import static io.github.ust.mico.core.util.MicoRepositoryTestUtils.*;
import static io.github.ust.mico.core.util.MicoRepositoryTestUtils.addMicoServicesWithDefaultServiceDeploymentInfo;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import io.github.ust.mico.core.model.MicoApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.EmbeddedRedisServer;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoServiceInterfaceRepositoryTests extends MicoRepositoryTests {

    @Before
    public void setUp() {
        deleteAllData();
    }

    @Commit
    @Test
    public void findInterfaceByService() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 only includes the service #0
        // Application #1 only includes the service #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);

        // Get all service interfaces that include service #0
        List<MicoServiceInterface> interfacesUsingS0 = serviceInterfaceRepository.findByService(s0.getShortName(), s0.getVersion());
        assertEquals(2, interfacesUsingS0.size());
        // Check if the services interfaces are the correct ones
        assertTrue(s0.getServiceInterfaces().containsAll(interfacesUsingS0) && interfacesUsingS0.containsAll(s0.getServiceInterfaces()));
    }

    @Commit
    @Test
    public void findInterfaceByServiceAndName() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 only includes the service #0
        // Application #1 only includes the service #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);

        // Get service interface by name and used by service #0
        Optional<MicoServiceInterface> interfaceUsingS0 = serviceInterfaceRepository.findByServiceAndName(
            s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(0).getServiceInterfaceName());
        assertTrue(interfaceUsingS0.isPresent());
        // Check if both service interfaces are the same
        assertEquals(s0.getServiceInterfaces().get(0), interfaceUsingS0.get());
    }

    @Commit
    @Test
    public void deleteInterfaceByServiceAndName() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 only includes the service #0
        // Application #1 only includes the service #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithDefaultServiceDeploymentInfo(a1, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);

        // Number of total mico service interfaces should be 4
        assertEquals(4, serviceInterfaceRepository.count());

        // Delete service interface of service #0
        serviceInterfaceRepository.deleteByServiceAndName(s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(0).getServiceInterfaceName());
        assertEquals(3, serviceInterfaceRepository.count());
        // Verify that service interface of service #0 is deleted
        Optional<MicoServiceInterface> interfaceUsingS0 = serviceInterfaceRepository.findByServiceAndName(
            s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(0).getServiceInterfaceName());
        assertFalse(interfaceUsingS0.isPresent());
        // Verify that service interface of service #1 is still available
        interfaceUsingS0 = serviceInterfaceRepository.findByServiceAndName(
            s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(1).getServiceInterfaceName());
        assertTrue(interfaceUsingS0.isPresent());
    }

}
