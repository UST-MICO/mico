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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class JakobTest {

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;
    
//    	MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
//    	MicoApplication a2 = new MicoApplication().setShortName("a2").setVersion("v1.0.0");
//    	MicoApplication a3 = new MicoApplication().setShortName("a3").setVersion("v1.0.0");
    
//    	MicoServiceDeploymentInfo sdi_a1_s1 = new MicoServiceDeploymentInfo().setApplication(a1).setService(s1).setReplicas(5);
//    	MicoServiceDeploymentInfo sdi_a2_s1 = new MicoServiceDeploymentInfo().setApplication(a2).setService(s1).setReplicas(10);
//    	MicoServiceDeploymentInfo sdi_a3_s1 = new MicoServiceDeploymentInfo().setApplication(a3).setService(s1).setReplicas(15);
//    	
//    	a1.getServiceDeploymentInfos().add(sdi_a1_s1);
//    	a2.getServiceDeploymentInfos().add(sdi_a2_s1);
//    	a3.getServiceDeploymentInfos().add(sdi_a3_s1);
    
//    	applicationRepository.save(a1);
//    	applicationRepository.save(a2);
//    	applicationRepository.save(a3);
    
    @Commit
    @Test
    public void test() {
//    	applicationRepository.deleteAll();
//    	serviceRepository.deleteAll();
//    	serviceInterfaceRepository.deleteAll();
//    	
//    	MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
//    	MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.0");
//    	MicoService s3 = new MicoService().setShortName("s3").setVersion("v1.0.0");
//    	
//		s3.getServiceInterfaces().add(new MicoServiceInterface().setServiceInterfaceName("iii").setPorts(
//		    CollectionUtils.listOf(new MicoServicePort().setPort(1234).setType(MicoPortType.TCP).setTargetPort(4567))).setDescription("description-interface-s3"));
//    	
//    	serviceRepository.save(s1);
//    	serviceRepository.save(s2);
//    	serviceRepository.save(s3);
    	
    	serviceInterfaceRepository.findAll().forEach(i -> System.out.println(i));
    	System.out.println("=======================================================");
    	MicoServiceInterface i = serviceInterfaceRepository.findByServiceAndName("s3", "v1.0.0", "iii").get();
    	System.out.println(i);
    	System.out.println("=======================================================");
    	
    	i.setDescription("Update!");
    	serviceInterfaceRepository.save(i);
    	
    	System.out.println("=======================================================");
    	i = serviceInterfaceRepository.findByServiceAndName("s3", "v1.0.0", "iii").get();
    	System.out.println(i);
    	
    	System.out.println("=======================================================");
    	System.out.println(serviceRepository.findByShortNameAndVersion("s3", "v1.0.0"));
    	
//    	MicoService s01 = new MicoService().setShortName("s01").setVersion("v1.0.0");
//    	MicoService s02 = new MicoService().setShortName("s02").setVersion("v1.0.0");
//    	MicoService s03 = new MicoService().setShortName("s03").setVersion("v1.0.0");
//    	
//    	MicoService start = new MicoService().setShortName("start").setVersion("v1.0.0");
//    	MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
//    	MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.0");
//    	MicoService s3 = new MicoService().setShortName("s3").setVersion("v1.0.0");
//    	MicoService s4 = new MicoService().setShortName("s4").setVersion("v1.0.0");
//    	MicoService s5 = new MicoService().setShortName("s5").setVersion("v1.0.0");
//    	MicoService s6 = new MicoService().setShortName("s6").setVersion("v1.0.0");
//    	MicoService s7 = new MicoService().setShortName("s7").setVersion("v1.0.0");
//    	MicoService s8 = new MicoService().setShortName("s8").setVersion("v1.0.0");
//    	MicoService s9 = new MicoService().setShortName("s9").setVersion("v1.0.0");
//    	MicoService s10 = new MicoService().setShortName("s10").setVersion("v1.0.0");
//    	MicoService s11 = new MicoService().setShortName("s11").setVersion("v1.0.0");
//    	MicoService s12 = new MicoService().setShortName("s12").setVersion("v1.0.0");
//    	MicoService s13 = new MicoService().setShortName("s13").setVersion("v1.0.0");
//    	MicoService s14 = new MicoService().setShortName("s14").setVersion("v1.0.0");
//    	MicoService end = new MicoService().setShortName("end").setVersion("v1.0.0");
//    	
//    	MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo().setApplication(a).setService(s01);
//    	a.getServiceDeploymentInfos().add(sdi);
//
//    	s01.getDependencies().add(new MicoServiceDependency().setService(s01).setDependedService(start).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s02.getDependencies().add(new MicoServiceDependency().setService(s02).setDependedService(start).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s03.getDependencies().add(new MicoServiceDependency().setService(s03).setDependedService(start).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	
//    	start.getDependencies().add(new MicoServiceDependency().setService(start).setDependedService(s1).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s1.getDependencies().add(new MicoServiceDependency().setService(s1).setDependedService(s2).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s2.getDependencies().add(new MicoServiceDependency().setService(s2).setDependedService(s3).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s3.getDependencies().add(new MicoServiceDependency().setService(s3).setDependedService(s4).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s4.getDependencies().add(new MicoServiceDependency().setService(s4).setDependedService(s5).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s5.getDependencies().add(new MicoServiceDependency().setService(s5).setDependedService(s6).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s6.getDependencies().add(new MicoServiceDependency().setService(s6).setDependedService(s7).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s7.getDependencies().add(new MicoServiceDependency().setService(s7).setDependedService(s8).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s8.getDependencies().add(new MicoServiceDependency().setService(s8).setDependedService(s9).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s9.getDependencies().add(new MicoServiceDependency().setService(s9).setDependedService(s10).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s10.getDependencies().add(new MicoServiceDependency().setService(s10).setDependedService(s11).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s11.getDependencies().add(new MicoServiceDependency().setService(s11).setDependedService(s12).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s12.getDependencies().add(new MicoServiceDependency().setService(s12).setDependedService(s13).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s13.getDependencies().add(new MicoServiceDependency().setService(s13).setDependedService(s14).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	s14.getDependencies().add(new MicoServiceDependency().setService(s14).setDependedService(end).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	// Cycle
////    	end.getDependencies().add(new MicoServiceDependency().setService(end).setDependedService(start).setMinVersion("1.0.0").setMaxVersion("2.0.0"));
//    	
//    	
//    	serviceRepository.save(s01);
//    	serviceRepository.save(s02);
//    	serviceRepository.save(s03);
//    	serviceRepository.save(start);
//    	serviceRepository.save(s1);
//    	serviceRepository.save(s2);
//    	serviceRepository.save(s3);
//    	serviceRepository.save(s4);
//    	serviceRepository.save(s5);
//    	serviceRepository.save(s6);
//    	serviceRepository.save(s7);
//    	serviceRepository.save(s8);
//    	serviceRepository.save(s9);
//    	serviceRepository.save(s10);
//    	serviceRepository.save(s11);
//    	serviceRepository.save(s12);
//    	serviceRepository.save(s13);
//    	serviceRepository.save(s14);
//    	serviceRepository.save(end);
//    	
//    	List<MicoService> dependencies = serviceRepository.findDependees("start", "v1.0.0");
//    	System.out.print("Dependencies: ");
//    	dependencies.forEach(s -> System.out.print(s.getShortName() + ", "));
//    	System.out.println();
//    	
//    	List<MicoService> dependers = serviceRepository.findDependers("start", "v1.0.0");
//    	System.out.print("Dependers: ");
//    	dependers.forEach(s -> System.out.print(s.getShortName() + ", "));
//    	System.out.println();
    	
    	
//    	MicoService s = serviceRepository.findById(22L).get();
//    	MicoService dtd = serviceRepository.findById(20L).get();
//    	s.getDependencies().removeIf(d -> d.getDependedService().getId() == dtd.getId());
//    	MicoService saved = serviceRepository.save(s);
//    	System.out.println(saved);
    }
    
}
