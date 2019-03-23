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
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoLabelRepositoryTests {
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
    public void removeUnnecessaryLabel() {
        // Create some applications and services
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoApplication a2 = new MicoApplication().setShortName("a4").setVersion("v1.0.3");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.4");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.5");
        MicoLabel l1 = new MicoLabel().setKey("key1").setValue("value1");
        MicoLabel l2 = new MicoLabel().setKey("key2").setValue("value2");
        MicoLabel l3 = new MicoLabel().setKey("key3").setValue("value3");
        MicoLabel l4 = new MicoLabel().setKey("key4").setValue("value4");
        MicoLabel l5 = new MicoLabel().setKey("key5").setValue("value5");

        // Add some services to the applications
        a1.getServices().add(s1);
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(3).setLabels(CollectionUtils.listOf(l1, l2)));
        a2.getServices().add(s2);
        a2.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s2).setReplicas(4).setLabels(CollectionUtils.listOf(l3)));

        // Save all created objects in their corresponding repositories
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        labelRepository.save(l1);
        labelRepository.save(l2);
        labelRepository.save(l3);
        labelRepository.save(l4);
        labelRepository.save(l5);

        // Remove all labels that do not have any relationship with another node
        labelRepository.cleanUp();

        // Only label l4 shall be removed
        Iterable<MicoLabel> micoLabelIterable = labelRepository.findAll();
        int size = 0;
        for (MicoLabel micoLabel : micoLabelIterable) {
            assertNotEquals("key4", micoLabel.getKey());
            size++;
        }
        assertEquals(3, size);

    }
}
