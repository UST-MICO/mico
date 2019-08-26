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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.util.MicoRepositoryTestUtils.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("local")
public class MicoServiceRepositoryTests extends MicoRepositoryTests {

    public void setUp() {
        deleteAllData();
    }

    @Commit
    @Test
    public void findAllServicesByApplication() {
        setUp();

        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);
        MicoService s3 = getMicoService(3);

        // Application #0 includes no service
        // Application #1 only includes service #1
        // Application #2 includes services #1, #2 and #3
        addMicoServicesWithDefaultServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s1, s2, s3);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        // Service #0 needs to be saved manually since not included by any application
        serviceRepository.save(s0);

        // Application #0 includes no service
        List<MicoService> servicesIncludedByA0 = serviceRepository.findAllByApplication(a0.getShortName(), s0.getVersion());
        assertTrue(servicesIncludedByA0.isEmpty());
        // Application #1 only includes service #1
        List<MicoService> servicesIncludedByA1 = serviceRepository.findAllByApplication(a1.getShortName(), a1.getVersion());
        assertEquals(a1.getServices().size(), servicesIncludedByA1.size());
        assertEquals(s1, matchMicoService(servicesIncludedByA1, s1));
        // Application #2 includes services #1, #2 and #3
        List<MicoService> servicesIncludedByA2 = serviceRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(a2.getServices().size(), servicesIncludedByA2.size());
        assertEquals(s1, matchMicoService(servicesIncludedByA2, s1));
        assertEquals(s2, matchMicoService(servicesIncludedByA2, s2));
        assertEquals(s3, matchMicoService(servicesIncludedByA2, s3));
    }

    @Commit
    @Test
    public void findAllServicesByApplicationAndServiceShortName() {
        setUp();

        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);

        // Application #0 includes no service
        // Application #1 only includes the service #1
        // Application #2 includes services #1 and #2
        addMicoServicesWithDefaultServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s1, s2);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        // Service #0 needs to be saved separately since not included by any application
        serviceRepository.save(s0);

        // Application #0 includes no service
        Optional<MicoService> a0s0Optional = serviceRepository.findAllByApplicationAndServiceShortName(a0.getShortName(), s0.getShortName(), s0.getVersion());
        assertFalse(a0s0Optional.isPresent());
        // Application #1 only includes service #1
        Optional<MicoService> a1s1Optional = serviceRepository.findAllByApplicationAndServiceShortName(a1.getShortName(), a1.getVersion(), s1.getShortName());
        assertTrue(a1s1Optional.isPresent());
        assertEquals(s1, a1s1Optional.get());
        // Application #2 includes services #1, #2 and #3
        Optional<MicoService> a2s1Optional = serviceRepository.findAllByApplicationAndServiceShortName(a2.getShortName(), a2.getVersion(), s1.getShortName());
        assertTrue(a2s1Optional.isPresent());
        assertEquals(s1, a2s1Optional.get());
        Optional<MicoService> a2s2Optional = serviceRepository.findAllByApplicationAndServiceShortName(a2.getShortName(), a2.getVersion(), s2.getShortName());
        assertTrue(a2s2Optional.isPresent());
        assertEquals(s2, a2s2Optional.get());
    }

    @Commit
    @Test
    public void findDependeesWithAndWithoutDepender() {
        setUp();

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);
        MicoService s3 = getMicoService(3);
        MicoService s4 = getMicoService(4);
        MicoService s5 = getMicoService(5);

        // Service #0 depends on service #1 and #2
        // Service #1 depends on service #4
        // Service #3 depends on service #0
        s0.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s0, s1), getMicoServiceDependency(s0, s2)));
        s1.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s1, s4)));
        s3.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s3, s0)));

        // Save
        serviceRepository.save(s0);
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        serviceRepository.save(s3);
        serviceRepository.save(s4);
        serviceRepository.save(s5);

        // Find dependees including depender
        List<MicoService> dependeesIncludingS0 = serviceRepository.findDependeesIncludeDepender(s0.getShortName(), s0.getVersion());
        assertEquals(4, dependeesIncludingS0.size());
        // Service #0 is the service itself
        assertTrue(dependeesIncludingS0.contains(s0));
        // Service #1 and #2 are direct dependees of service #0
        assertTrue(dependeesIncludingS0.contains(s1));
        assertTrue(dependeesIncludingS0.contains(s2));
        // Service #4 is direct dependee of service #1
        assertTrue(dependeesIncludingS0.contains(s4));
        // Services #3 and #5 are no dependees of service #0
        assertFalse(dependeesIncludingS0.contains(s3));
        assertFalse(dependeesIncludingS0.contains(s5));

        // Find dependees excluding depender
        List<MicoService> dependeesExcludingS0 = serviceRepository.findDependees(s0.getShortName(), s0.getVersion());
        assertEquals(3, dependeesExcludingS0.size());
        // Service #0 is the service itself
        assertFalse(dependeesExcludingS0.contains(s0));
        // Service #1 and #2 are direct dependees of service #0
        assertTrue(dependeesExcludingS0.contains(s1));
        assertTrue(dependeesExcludingS0.contains(s2));
        // Service #4 is direct dependee of service #1
        assertTrue(dependeesExcludingS0.contains(s4));
        // Services #3 and #5 are no dependees of service #0
        assertFalse(dependeesExcludingS0.contains(s3));
        assertFalse(dependeesExcludingS0.contains(s5));
    }

    @Commit
    @Test
    public void findDependers() {
        setUp();

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);
        MicoService s3 = getMicoService(3);
        MicoService s4 = getMicoService(4);
        MicoService s5 = getMicoService(5);
        MicoService s6 = getMicoService(6);

        // Service #0 depends on service #1
        // Services #2, #3 and #4 depend on service #0
        // Service #5 depends on service #3
        s0.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s0, s1)));
        s2.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s2, s0)));
        s3.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s3, s0)));
        s4.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s4, s0)));
        s5.setDependencies(CollectionUtils.listOf(getMicoServiceDependency(s5, s3)));

        // Save
        serviceRepository.save(s0);
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        serviceRepository.save(s3);
        serviceRepository.save(s4);
        serviceRepository.save(s5);
        serviceRepository.save(s6);

        // Find only direct dependers
        List<MicoService> dependersOfS0 = serviceRepository.findDependers(s0.getShortName(), s0.getVersion());
        assertEquals(3, dependersOfS0.size());
        // Service #0 is the service itself
        assertFalse(dependersOfS0.contains(s0));
        // Services #2, #3 and #4 are depender of service #0
        assertTrue(dependersOfS0.contains(s2));
        assertTrue(dependersOfS0.contains(s3));
        assertTrue(dependersOfS0.contains(s4));
        // Service #5 is indirect depender of service #0
        assertFalse(dependersOfS0.contains(s5));
        // Services #1 is dependee of service #0
        assertFalse(dependersOfS0.contains(s1));
        // Service #6 has no connection to service #0
        assertFalse(dependersOfS0.contains(s6));
    }

//    @Ignore
//    @Commit
//    @Test
//    public void deleteServiceByShortNameAndVersion() {
//        // Setup some applications
//        MicoApplication a0 = getPureMicoApplication(0);
//        MicoApplication a1 = getPureMicoApplication(1);
//        MicoApplication a2 = getPureMicoApplication(2);
//
//        // Setup some services
//        MicoService s0 = getMicoService(0);
//        MicoService s1 = getMicoService(1);
//        MicoService s2 = getMicoService(2);
//        MicoService s3 = getMicoService(3);
//
//        // Application #0 includes no service
//        // Application #1 only includes service #1
//        // Application #2 includes services #1, #2 and #3
//        addMicoServicesWithServiceDeploymentInfo(a1, s1);
//        addMicoServicesWithServiceDeploymentInfo(a2, s1, s2, s3);
//
//        // Save
//        applicationRepository.save(a0);
//        applicationRepository.save(a1);
//        applicationRepository.save(a2);
//        // Service #0 needs to be saved manually since not included by any application
//        serviceRepository.save(s0);
//
//        // Number of total mico services should be 4
//        assertEquals(4, serviceRepository.count());
//
//        // Delete service #1
//        serviceRepository.deleteServiceByShortNameAndVersion(s1.getShortName(), s1.getVersion());
//        // Number of total mico services should be 3 now
//        assertEquals(3, serviceRepository.count());
//        // Check if service #1 has been removed regarding applications #1 and #2
//        MicoApplication a1n = applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get();
//        assertEquals(0, a1n.getServices().size());
//        assertFalse(a1n.getServices().contains(s1));
//        MicoApplication a2n = applicationRepository.findByShortNameAndVersion(a2.getShortName(), a2.getVersion()).get();
//        assertEquals(2, a2n.getServices().size());
//        assertFalse(a2n.getServices().contains(s1));
//        // Application #0 should remain the same
//        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
//    }

    @Commit
    @Test
    public void deleteServiceByShortNameAndVersionPart1() {
        setUp();

        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);
        MicoService s3 = getMicoService(3);

        // Application #0 includes no service
        // Application #1 only includes service #1
        // Application #2 includes services #1, #2 and #3
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s1, s2, s3);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        // Service #0 needs to be saved manually since not included by any application
        serviceRepository.save(s0);

        // Number of total mico services should be 4
        assertEquals(4, serviceRepository.count());

        // Delete service #1
        serviceRepository.deleteServiceByShortNameAndVersion(s1.getShortName(), s1.getVersion());
    }

    @Commit
    @Test
    public void deleteServiceByShortNameAndVersionPart2() {
        // Number of total mico services should be 3 now
        assertEquals(3, serviceRepository.count());
        // Check if service #1 has been removed regarding applications #1 and #2
        MicoApplication a1n = applicationRepository.findByShortNameAndVersion("application-1", "application-v1.1.0").get();
        assertEquals(0, a1n.getServices().size());
//        assertFalse(a1n.getServices().contains(s1));
//        MicoApplication a2n = applicationRepository.findByShortNameAndVersion(a2.getShortName(), a2.getVersion()).get();
//        assertEquals(2, a2n.getServices().size());
//        assertFalse(a2n.getServices().contains(s1));
//        // Application #0 should remain the same
//        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
    }
}
