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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoInterfaceConnectionRepositoryTests {
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

    @Commit
    @Test
    public void removeUnnecessaryMicoInterfaceConnections() {
        // Create some applications and services
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.1");
        MicoServiceInterface i1 = new MicoServiceInterface().setServiceInterfaceName("i1").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));
        MicoEnvironmentVariable v1 = new MicoEnvironmentVariable().setName("env1").setValue("val1");
        MicoInterfaceConnection c1 = new MicoInterfaceConnection().setEnvironmentVariableName("v1").setMicoServiceInterfaceName("i1").setMicoServiceShortName("s1");
        MicoInterfaceConnection c2 = new MicoInterfaceConnection().setEnvironmentVariableName("v2").setMicoServiceInterfaceName("i2").setMicoServiceShortName("s2");
        MicoInterfaceConnection c3 = new MicoInterfaceConnection().setEnvironmentVariableName("v3").setMicoServiceInterfaceName("i3").setMicoServiceShortName("s3");

        s1.getServiceInterfaces().add(i1);
        a1.getServices().add(s1);
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(3).setEnvironmentVariables(
            CollectionUtils.listOf(v1)).setInterfaceConnections(CollectionUtils.listOf(c1)));

        applicationRepository.save(a1);
        serviceRepository.save(s1);
        environmentVariableRepository.save(v1);
        interfaceConnectionRepository.save(c1);
        interfaceConnectionRepository.save(c2);
        interfaceConnectionRepository.save(c3);

        // Delete all interface connections without an edge to another node
        interfaceConnectionRepository.cleanUp();

        // Only interfaceConnection c1 should be left
        Iterable<MicoInterfaceConnection> micoInterfaceConnectionIterable = interfaceConnectionRepository.findAll();
        for (MicoInterfaceConnection interfaceConnection : micoInterfaceConnectionIterable) {
            assertEquals("s1", interfaceConnection.getMicoServiceShortName());
        }
    }
}
