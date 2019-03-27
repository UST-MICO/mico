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
import static org.mockito.ArgumentMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.util.CollectionUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoServiceRepositoryTests extends MicoRepositoryTests {

    @Before
    public void setUp() {
        deleteAllData();
    }
    
    @Test
    public void findAllServicesByApplication() {
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
    	// Application #1 only includes the service #1
    	// Application #2 includes services #1, #2 and #3
    	addMicoServicesWithDefaultServiceDeploymentInfo(a1, s1);
    	addMicoServicesWithServiceDeploymentInfo(a2, s1, s2, s3);
    	
    	// Save
    	applicationRepository.save(a0);
    	applicationRepository.save(a1);
    	applicationRepository.save(a2);
    	// Service #0 needs to be saved separately since not included by any application
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

//    @Test
//    public void findAllServicesByApplicationOld() {
//        // Create some applications and services
//        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
//        MicoApplication a2 = new MicoApplication().setShortName("a2").setVersion("v1.0.1");
//        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.2");
//        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.3");
//        MicoService s3 = new MicoService().setShortName("s3").setVersion("v1.0.4");
//
//        // Add some services to the applications
//        a1.getServices().add(s1);
//        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(3));
//        a1.getServices().add(s2);
//        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s2).setReplicas(4));
//        a2.getServices().add(s3);
//        a2.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s3).setReplicas(5));
//
//        // Save all created objects in their corresponding repositories
//        applicationRepository.save(a1);
//        applicationRepository.save(a2);
//        serviceRepository.save(s1);
//        serviceRepository.save(s2);
//        serviceRepository.save(s3);
//
//        // Only services s1 and s2 belong to the application a1, service s3 shall not
//        List<MicoService> micoServiceList = serviceRepository.findAllByApplication("a1", "v1.0.0");
//        assertEquals(2, micoServiceList.size());
//        assertTrue(micoServiceList.contains(s1));
//        assertTrue(micoServiceList.contains(s2));
//        assertFalse(micoServiceList.contains(s3));
//    }
    
    @Test
    public void findAllServicesByApplicationAndShortName() {
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
    	Optional<MicoService> a0s0Optional = serviceRepository.findAllByApplicationAndShortName(a0.getShortName(), anyString(), anyString());
    	assertFalse(a0s0Optional.isPresent());
    	// Application #1 only includes the service #1
    	Optional<MicoService> a1s1Optional = serviceRepository.findAllByApplicationAndShortName(a1.getShortName(), a1.getVersion(), s1.getShortName());
    	assertTrue(a1s1Optional.isPresent());
    	assertEquals(s1, a1s1Optional.get());
    	// Application #2 includes services #1, #2 and #3
    	Optional<MicoService> a2s1Optional = serviceRepository.findAllByApplicationAndShortName(a2.getShortName(), a2.getVersion(), s1.getShortName());
    	assertTrue(a2s1Optional.isPresent());
    	assertEquals(s1, a2s1Optional.get());
    	Optional<MicoService> a2s2Optional = serviceRepository.findAllByApplicationAndShortName(a2.getShortName(), a2.getVersion(), s2.getShortName());
    	assertTrue(a2s2Optional.isPresent());
    	assertEquals(s2, a2s2Optional.get());
    }

//    @Test
//    public void findAllServicesByApplicationAndShortNameOld() {
//        // Create some applications and services
//        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
//        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.1");
//        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.2");
//        MicoService s3 = new MicoService().setShortName("s3").setVersion("v1.0.3");
//
//        // Add some services to the applications
//        a1.getServices().addAll(CollectionUtils.listOf(s1, s2, s3));
//        a1.getServiceDeploymentInfos().addAll(CollectionUtils.listOf(
//            new MicoServiceDeploymentInfo().setService(s1).setReplicas(3),
//            new MicoServiceDeploymentInfo().setService(s2).setReplicas(4),
//            new MicoServiceDeploymentInfo().setService(s2).setReplicas(5)));
//
//        // Save all created objects in their corresponding repositories
//        applicationRepository.save(a1);
//        serviceRepository.save(s1);
//        serviceRepository.save(s2);
//        serviceRepository.save(s3);
//
//        Optional<MicoService> micoServiceOptional = serviceRepository.findAllByApplicationAndShortName("a1", "v1.0.0", "s1");
//        assertTrue(micoServiceOptional.isPresent());
//        assertEquals("s1", micoServiceOptional.get().getShortName());
//        assertEquals("v1.0.1", micoServiceOptional.get().getVersion());
//    }

    @Test
    public void findDependeesWithAndWithoutDepender() {
    	
    }

    @Test
    public void findDependeesWithAndWithoutDependerOld() {
        // Create some services
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.1");
        MicoService s3 = new MicoService().setShortName("s3").setVersion("v1.0.2");
        MicoService s4 = new MicoService().setShortName("s4").setVersion("v1.0.3");
        MicoService s5 = new MicoService().setShortName("s5").setVersion("v1.0.4");
        MicoService s6 = new MicoService().setShortName("s6").setVersion("v1.0.5");

        // Add some dependencies between the created services
        MicoServiceDependency dp_s1_s2 = new MicoServiceDependency().setService(s1).setDependedService(s2);
        MicoServiceDependency dp_s1_s3 = new MicoServiceDependency().setService(s1).setDependedService(s3);
        MicoServiceDependency dp_s2_s5 = new MicoServiceDependency().setService(s2).setDependedService(s5);
        MicoServiceDependency dp_s4_s1 = new MicoServiceDependency().setService(s4).setDependedService(s1);

        s1.setDependencies(CollectionUtils.listOf(dp_s1_s2, dp_s1_s3));
        s2.setDependencies(CollectionUtils.listOf(dp_s2_s5));
        s4.setDependencies(CollectionUtils.listOf(dp_s4_s1));

        // Save all created objects in their corresponding repositories
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        serviceRepository.save(s3);
        serviceRepository.save(s4);
        serviceRepository.save(s5);
        serviceRepository.save(s6);

        // Find dependees including depender
        List<MicoService> micoServiceList = serviceRepository.findDependeesIncludeDepender("s1", "v1.0.0");
        assertEquals(4, micoServiceList.size());
        assertTrue(micoServiceList.contains(s1)); // the service itself
        assertTrue(micoServiceList.contains(s2)); // direct dependee
        assertTrue(micoServiceList.contains(s3)); // direct dependee
        assertTrue(micoServiceList.contains(s5)); // dependee of s2
        assertFalse(micoServiceList.contains(s4));
        assertFalse(micoServiceList.contains(s6));

        // Find dependees excluding depender
        micoServiceList = serviceRepository.findDependees("s1", "v1.0.0");
        assertEquals(3, micoServiceList.size());
        assertTrue(micoServiceList.contains(s2)); // direct dependee
        assertTrue(micoServiceList.contains(s3)); // direct dependee
        assertTrue(micoServiceList.contains(s5)); // dependee of s2
        assertFalse(micoServiceList.contains(s1));
        assertFalse(micoServiceList.contains(s4));
        assertFalse(micoServiceList.contains(s6));
    }

    @Test
    public void findDependers() {
        // Create some services
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.1");
        MicoService s3 = new MicoService().setShortName("s3").setVersion("v1.0.2");
        MicoService s4 = new MicoService().setShortName("s4").setVersion("v1.0.3");
        MicoService s5 = new MicoService().setShortName("s5").setVersion("v1.0.4");
        MicoService s6 = new MicoService().setShortName("s6").setVersion("v1.0.5");
        MicoService s7 = new MicoService().setShortName("s7").setVersion("v1.0.6");

        // Add some dependencies between the created services
        MicoServiceDependency dp_s1_s2 = new MicoServiceDependency().setService(s1).setDependedService(s2);
        MicoServiceDependency dp_s3_s1 = new MicoServiceDependency().setService(s3).setDependedService(s1);
        MicoServiceDependency dp_s4_s1 = new MicoServiceDependency().setService(s4).setDependedService(s1);
        MicoServiceDependency dp_s5_s1 = new MicoServiceDependency().setService(s5).setDependedService(s1);
        MicoServiceDependency dp_s6_s3 = new MicoServiceDependency().setService(s6).setDependedService(s3);

        s1.setDependencies(CollectionUtils.listOf(dp_s1_s2));
        s3.setDependencies(CollectionUtils.listOf(dp_s3_s1));
        s4.setDependencies(CollectionUtils.listOf(dp_s4_s1));
        s5.setDependencies(CollectionUtils.listOf(dp_s5_s1));
        s6.setDependencies(CollectionUtils.listOf(dp_s6_s3));

        // Save all created objects in their corresponding repositories
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        serviceRepository.save(s3);
        serviceRepository.save(s4);
        serviceRepository.save(s5);
        serviceRepository.save(s6);
        serviceRepository.save(s7);

        // Find only direct dependers
        List<MicoService> micoServiceList = serviceRepository.findDependers("s1", "v1.0.0");
        assertEquals(3, micoServiceList.size());
        assertTrue(micoServiceList.contains(s3)); // direct depender
        assertTrue(micoServiceList.contains(s4)); // direct depender
        assertTrue(micoServiceList.contains(s5)); // direct depender
        assertFalse(micoServiceList.contains(s2));
        assertFalse(micoServiceList.contains(s6)); // indirect depender
        assertFalse(micoServiceList.contains(s7));
    }

    @Test
    public void deleteServiceByShortNameAndVersion() {
        // Create some services and interfaces
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.1");

        MicoServiceInterface i1 = new MicoServiceInterface().setServiceInterfaceName("i1");
        MicoServiceInterface i2 = new MicoServiceInterface().setServiceInterfaceName("i2").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));
        MicoServiceInterface i3 = new MicoServiceInterface().setServiceInterfaceName("i3").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(82).setTargetPort(83), new MicoServicePort().setPort(84).setTargetPort(85)));
        MicoServiceInterface i4 = new MicoServiceInterface().setServiceInterfaceName("i4");

        s1.setServiceInterfaces(CollectionUtils.listOf(i1, i2, i3));

        // Create a dependency between s1 and s2
        MicoServiceDependency dp_s1_s2 = new MicoServiceDependency().setService(s1).setDependedService(s2);
        s1.setDependencies(CollectionUtils.listOf(dp_s1_s2));

        // Save created objects in their corresponding repositories
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        serviceInterfaceRepository.save(i4);

        serviceRepository.deleteServiceByShortNameAndVersion("s1", "v1.0.0");

        // Only service s1 should be left
        List<MicoService> micoServiceList = serviceRepository.findAll();
        assertTrue(micoServiceList.contains(s2));
        assertFalse(micoServiceList.contains(s1));

        // There should be only serviceInterface i4 left
        Iterable<MicoServiceInterface> micoServiceInterfaceIterable = serviceInterfaceRepository.findAll();
        int size = 0;
        for (MicoServiceInterface micoServiceInterface : micoServiceInterfaceIterable) {
            assertEquals("i4", micoServiceInterface.getServiceInterfaceName());
            size++;
        }
        assertEquals(1, size);

        // There should be no servicePorts left
        Iterable<MicoServicePort> micoServicePortIterable = servicePortRepository.findAll();
        size = 0;
        for (MicoServicePort micoServicePort : micoServicePortIterable) {
            System.out.println("ServicePortRepository should not contain port " + micoServicePort.getPort() + " -> " + micoServicePort.getTargetPort());
            size++;
        }
        assertEquals(0, size);
    }
}
