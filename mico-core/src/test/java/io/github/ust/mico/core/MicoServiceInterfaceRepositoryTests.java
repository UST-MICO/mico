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
import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("local")
public class MicoServiceInterfaceRepositoryTests extends MicoRepositoryTests {

    @Before
    public void setUp() {
        deleteAllData();
    }

    @Commit
    @Test
    public void findInterfaceByService() {
        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Save
        serviceRepository.save(s0);
        serviceRepository.save(s1);

        // Check if the services interfaces are the correct ones
        List<MicoServiceInterface> interfacesProvidedByS0 = serviceInterfaceRepository.findByService(s0.getShortName(), s0.getVersion());
        assertEquals(s0.getServiceInterfaces().size(), interfacesProvidedByS0.size());
        s0.getServiceInterfaces().forEach(serviceInterface -> assertEquals(serviceInterface, matchMicoServiceInterface(interfacesProvidedByS0, serviceInterface.getServiceInterfaceName())));
    }

    @Commit
    @Test
    public void findInterfaceByServiceAndName() {
        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Save
        serviceRepository.save(s0);
        serviceRepository.save(s1);

        // Check if the services interfaces are the correct ones
        List<MicoServiceInterface> interfacesProvidedByS0 = serviceInterfaceRepository.findByService(s0.getShortName(), s0.getVersion());
        assertEquals(s0.getServiceInterfaces().size(), interfacesProvidedByS0.size());
        s0.getServiceInterfaces().forEach(serviceInterface -> assertEquals(serviceInterface, matchMicoServiceInterface(interfacesProvidedByS0, serviceInterface.getServiceInterfaceName())));

        // Check a service interface of service #1
        Optional<MicoServiceInterface> firstIntefaceOfS1Optional = serviceInterfaceRepository.findByServiceAndName(s1.getShortName(), s1.getVersion(), s1.getServiceInterfaces().get(0).getServiceInterfaceName());
        assertTrue(firstIntefaceOfS1Optional.isPresent());
        assertEquals(s1.getServiceInterfaces().get(0), firstIntefaceOfS1Optional.get());
    }

    @Commit
    @Test
    public void deleteInterfaceByServiceAndName() {
        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Save
        serviceRepository.save(s0);
        serviceRepository.save(s1);

        // Number of total mico service interfaces should be 4
        int expectedTotalNumberOfServiceInterfaces = s0.getServiceInterfaces().size() + s1.getServiceInterfaces().size();
        assertEquals(expectedTotalNumberOfServiceInterfaces, serviceInterfaceRepository.count());

        // Delete first service interface of service #0
        serviceInterfaceRepository.deleteByServiceAndName(s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(0).getServiceInterfaceName());
        expectedTotalNumberOfServiceInterfaces--;
        assertEquals(expectedTotalNumberOfServiceInterfaces, serviceInterfaceRepository.count());
        // Check that service interface of service #0 has been deleted
        Optional<MicoServiceInterface> firstInterfaceOfS0Optional = serviceInterfaceRepository.findByServiceAndName(s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(0).getServiceInterfaceName());
        assertFalse(firstInterfaceOfS0Optional.isPresent());
        // Check that remaining service interfaces are still there
        Optional<MicoServiceInterface> otherInterfaceOfS0Optional = serviceInterfaceRepository.findByServiceAndName(s0.getShortName(), s0.getVersion(), s0.getServiceInterfaces().get(1).getServiceInterfaceName());
        assertTrue(otherInterfaceOfS0Optional.isPresent());
        assertEquals(s0.getServiceInterfaces().get(1), otherInterfaceOfS0Optional.get());
        assertEquals(s1.getServiceInterfaces().size(), serviceInterfaceRepository.findByService(s1.getShortName(), s1.getVersion()).size());
    }

}
