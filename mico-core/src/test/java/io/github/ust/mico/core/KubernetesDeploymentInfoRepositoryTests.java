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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class KubernetesDeploymentInfoRepositoryTests {
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

    @Commit
    @Test
    public void removeUnnecessaryKubernetesDeploymentInfos() {
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.1");
        MicoServiceInterface i1 = new MicoServiceInterface().setServiceInterfaceName("i1").setPorts(
            CollectionUtils.listOf(new MicoServicePort().setPort(80).setTargetPort(81)));
        KubernetesDeploymentInfo d1 = new KubernetesDeploymentInfo().setNamespace("namespace1").setDeploymentName("deployment1")
            .setServiceNames(CollectionUtils.listOf("service1"));
        KubernetesDeploymentInfo d2 = new KubernetesDeploymentInfo().setNamespace("namespace2").setDeploymentName("deployment2")
            .setServiceNames(CollectionUtils.listOf("service2"));
        KubernetesDeploymentInfo d3 = new KubernetesDeploymentInfo().setNamespace("namespace3").setDeploymentName("deployment3")
            .setServiceNames(CollectionUtils.listOf("service3"));

        s1.getServiceInterfaces().add(i1);
        a1.getServices().add(s1);
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(3).setKubernetesDeploymentInfo(d1));

        applicationRepository.save(a1);
        serviceRepository.save(s1);
        kubernetesDeploymentInfoRepository.save(d1);
        kubernetesDeploymentInfoRepository.save(d2);
        kubernetesDeploymentInfoRepository.save(d3);

        // Delete all kubernetesDeploymentInfos without an edge to another node
        kubernetesDeploymentInfoRepository.cleanUp();

        // Only kubernetesDeploymentInfo d1 should be left
        Iterable<KubernetesDeploymentInfo> kubernetesDeploymentInfoIterable = kubernetesDeploymentInfoRepository.findAll();
        for (KubernetesDeploymentInfo kubernetesDeploymentInfo : kubernetesDeploymentInfoIterable) {
            assertEquals("namespace1", kubernetesDeploymentInfo.getNamespace());
        }
    }
}
