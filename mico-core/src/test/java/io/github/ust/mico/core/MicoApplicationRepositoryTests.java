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

import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.EmbeddedRedisServer;
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

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoApplicationRepositoryTests {
    public static @ClassRule
    RuleChain rules = RuleChain.outerRule(EmbeddedRedisServer.runningAt(6379).suppressExceptions());

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

    @Test
    public void findAllApplicationsByUsedService() {
        // Create some applications and services
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoApplication a2 = new MicoApplication().setShortName("a2").setVersion("v1.0.1");
        MicoApplication a3 = new MicoApplication().setShortName("a3").setVersion("v1.0.2");
        MicoApplication a4 = new MicoApplication().setShortName("a4").setVersion("v1.0.3");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.4");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.5");
        MicoServiceInterface i1 = new MicoServiceInterface().setServiceInterfaceName("i1").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));

        // Add some services to the applications
        s1.setServiceInterfaces(CollectionUtils.listOf(i1));
        a1.getServices().add(s1);
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(3));
        a2.getServices().add(s1);
        a2.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(4));
        a3.getServices().addAll(CollectionUtils.listOf(s1, s2));
        a3.getServiceDeploymentInfos().addAll(CollectionUtils.listOf(
            new MicoServiceDeploymentInfo().setService(s1).setReplicas(5), new MicoServiceDeploymentInfo().setService(s2).setReplicas(6)));
        a4.getServices().add(s2);
        a4.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s2).setReplicas(7));

        // Save all created objects in their corresponding repositories
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        applicationRepository.save(a3);
        applicationRepository.save(a4);
        serviceRepository.save(s1);
        serviceRepository.save(s2);

        List<MicoApplication> micoApplicationList = applicationRepository.findAllByUsedService("s1", "v1.0.4");

        // Only applications a1, a2 and a3 belong to the service s1, application a4 shall not
        assertEquals(3, micoApplicationList.size());
        assertTrue(micoApplicationList.contains(a1));
        assertTrue(micoApplicationList.contains(a2));
        assertTrue(micoApplicationList.contains(a3));
        assertFalse(micoApplicationList.contains(a4));

        // Get application a1 and check if it still contains all services and deployment infos
        MicoApplication a1n = null;

        for (int i = 0; i < micoApplicationList.size(); i++) {
            if (micoApplicationList.get(i).getShortName().equals("a1")) {
                a1n = micoApplicationList.get(i);
            }
        }

        assertNotNull(a1n);

        // Test if service is still attached
        assertEquals(1, a1n.getServices().size());
        MicoService s1n = a1n.getServices().get(0);
        assertEquals("s1", s1n.getShortName());
        assertEquals("v1.0.4", s1n.getVersion());

        // Test if service interface is still attached
        assertEquals(1, s1n.getServiceInterfaces().size());
        MicoServiceInterface i1n = s1n.getServiceInterfaces().get(0);
        assertEquals("i1", i1n.getServiceInterfaceName());
        assertEquals(1, i1n.getPorts().size());

        // Test if deployment info is still attached
        assertEquals(1, a1n.getServiceDeploymentInfos().size());
        MicoServiceDeploymentInfo d1 = a1n.getServiceDeploymentInfos().get(0);
        assertEquals(3, d1.getReplicas());
    }

}
