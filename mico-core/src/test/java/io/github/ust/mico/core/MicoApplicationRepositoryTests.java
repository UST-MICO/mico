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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoApplicationRepositoryTests extends MicoRepositoryTests {
	
    @Before
    public void setUp() {
        deleteAllData();
    }
    
    @Test
    @Commit
    public void findAllApplicationsByUsedService() {
    	// Setup some applications
    	MicoApplication a0 = getPureMicoApplication(0);
    	MicoApplication a1 = getPureMicoApplication(1);
    	MicoApplication a2 = getPureMicoApplication(2);
    	MicoApplication a3 = getPureMicoApplication(3);
    	
    	// Setup some services
    	MicoService s0 = getMicoService(0);
    	MicoService s1 = getMicoService(1);
    	
    	// Application #0 only includes the service #0
    	// Application #1 only includes the service #1
    	// Application #2 includes services #0 and #1
    	// Application #2 includes no service
    	addMicoServicesWithServiceDeploymentInfo(a0, s0);
    	addMicoServicesWithDefaultServiceDeploymentInfo(a1, s1);
    	addMicoServicesWithDefaultServiceDeploymentInfo(a2, s0, s1);
    	
    	// Save
    	applicationRepository.save(a0);
    	applicationRepository.save(a1);
    	applicationRepository.save(a2);
    	applicationRepository.save(a3);
    	
    	// Get all applications that include service #0
    	List<MicoApplication> applicationsUsingS0 = applicationRepository.findAllByUsedService(s0.getShortName(), s0.getVersion());
    	// Applications #0 and #2 include service #0
    	assertEquals(2, applicationsUsingS0.size());
    	assertTrue(applicationsUsingS0.contains(a0));
    	assertEquals(a0, matchMicoApplication(applicationsUsingS0, a0));
    	assertTrue(applicationsUsingS0.contains(a2));
    	assertEquals(a2, matchMicoApplication(applicationsUsingS0, a2));
    	// Applications #1 and #3 do not include service #0
    	assertFalse(applicationsUsingS0.contains(a1));
    	assertFalse(applicationsUsingS0.contains(a3));
    }

//    @Test
//    public void findAllApplicationsByUsedServiceOld() {
//        // Create some applications and services
//        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
//        MicoApplication a2 = new MicoApplication().setShortName("a2").setVersion("v1.0.1");
//        MicoApplication a3 = new MicoApplication().setShortName("a3").setVersion("v1.0.2");
//        MicoApplication a4 = new MicoApplication().setShortName("a4").setVersion("v1.0.3");
//        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.4");
//        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.5");
//        MicoServiceInterface i1 = new MicoServiceInterface().setServiceInterfaceName("i1").setPorts(
//            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));
//
//        // Add some services to the applications
//        s1.setServiceInterfaces(CollectionUtils.listOf(i1));
//        a1.getServices().add(s1);
//        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(3));
//        a2.getServices().add(s1);
//        a2.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(4));
//        a3.getServices().addAll(CollectionUtils.listOf(s1, s2));
//        a3.getServiceDeploymentInfos().addAll(CollectionUtils.listOf(
//            new MicoServiceDeploymentInfo().setService(s1).setReplicas(5), new MicoServiceDeploymentInfo().setService(s2).setReplicas(6)));
//        a4.getServices().add(s2);
//        a4.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s2).setReplicas(7));
//
//        // Save all created objects in their corresponding repositories
//        applicationRepository.save(a1);
//        applicationRepository.save(a2);
//        applicationRepository.save(a3);
//        applicationRepository.save(a4);
//        serviceRepository.save(s1);
//        serviceRepository.save(s2);
//
//        List<MicoApplication> micoApplicationList = applicationRepository.findAllByUsedService("s1", "v1.0.4");
//
//        // Only applications a1, a2 and a3 belong to the service s1, application a4 shall not
//        assertEquals(3, micoApplicationList.size());
//        assertTrue(micoApplicationList.contains(a1));
//        assertTrue(micoApplicationList.contains(a2));
//        assertTrue(micoApplicationList.contains(a3));
//        assertFalse(micoApplicationList.contains(a4));
//
//        // Get application a1 and check if it still contains all services and deployment infos
//        MicoApplication a1n = null;
//
//        for (int i = 0; i < micoApplicationList.size(); i++) {
//            if (micoApplicationList.get(i).getShortName().equals("a1")) {
//                a1n = micoApplicationList.get(i);
//            }
//        }
//
//        assertNotNull(a1n);
//
//        // Test if service is still attached
//        assertEquals(1, a1n.getServices().size());
//        MicoService s1n = a1n.getServices().get(0);
//        assertEquals("s1", s1n.getShortName());
//        assertEquals("v1.0.4", s1n.getVersion());
//
//        // Test if service interface is still attached
//        assertEquals(1, s1n.getServiceInterfaces().size());
//        MicoServiceInterface i1n = s1n.getServiceInterfaces().get(0);
//        assertEquals("i1", i1n.getServiceInterfaceName());
//        assertEquals(1, i1n.getPorts().size());
//
//        // Test if deployment info is still attached
//        assertEquals(1, a1n.getServiceDeploymentInfos().size());
//        MicoServiceDeploymentInfo d1 = a1n.getServiceDeploymentInfos().get(0);
//        assertEquals(3, d1.getReplicas());
//    }

}
