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

import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoServiceDeploymentInfoRepositoryTests {
    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Before
    public void setUp() {
        applicationRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
        serviceDeploymentInfoRepository.deleteAll();
    }

    @After
    public void cleanUp() {

    }

    @Test
    public void findAllByApplication() {
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");
        MicoService s2 = new MicoService().setShortName("s2").setVersion("v1.0.0");

        a1.getServices().add(s1);
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(5));
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s2).setReplicas(10));

        applicationRepository.save(a1);

        List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfoRepository.findAllByApplication("a1", "v1.0.0");

        assertEquals(2, serviceDeploymentInfos.size());
        assertNotNull(serviceDeploymentInfos.get(0).getService());
    }

    @Test
    public void findByApplicationAndService() {
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");

        a1.getServices().add(s1);
        a1.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setService(s1).setReplicas(5));

        applicationRepository.save(a1);

        Optional<MicoServiceDeploymentInfo> serviceDeploymentInfo = serviceDeploymentInfoRepository.findByApplicationAndService(
            "a1", "v1.0.0", "s1", "v1.0.0");

        assertTrue(serviceDeploymentInfo.isPresent());
        System.out.println(serviceDeploymentInfo.get().toString());
        assertEquals(5, serviceDeploymentInfo.get().getReplicas());
        assertNotNull(serviceDeploymentInfo.get().getService());
        assertEquals("s1", serviceDeploymentInfo.get().getService().getShortName());
    }

    @Test
    public void findByApplicationAndServiceAfterUpdate() {
        MicoApplication a1 = new MicoApplication().setShortName("a1").setVersion("v1.0.0");
        MicoService s1 = new MicoService().setShortName("s1").setVersion("v1.0.0");

        a1.getServices().add(s1);
        applicationRepository.save(a1);

        MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo().setService(s1);
        MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO = new MicoServiceDeploymentInfoRequestDTO().setReplicas(5);
        a1.getServiceDeploymentInfos().add(sdi.applyValuesFrom(serviceDeploymentInfoDTO));
        MicoApplication updatedApplication = applicationRepository.save(a1);

        System.out.println(updatedApplication.toString());
        assertEquals(1, updatedApplication.getServiceDeploymentInfos().size());
        assertEquals(5, updatedApplication.getServiceDeploymentInfos().get(0).getReplicas());
        assertNotNull(updatedApplication.getServiceDeploymentInfos().get(0).getService());

        Optional<MicoServiceDeploymentInfo> serviceDeploymentInfo = serviceDeploymentInfoRepository.findByApplicationAndService(
            "a1", "v1.0.0", "s1", "v1.0.0");

        assertTrue(serviceDeploymentInfo.isPresent());
        System.out.println(serviceDeploymentInfo.get().toString());
        assertEquals(5, serviceDeploymentInfo.get().getReplicas());
        assertNotNull(serviceDeploymentInfo.get().getService());
        assertEquals("s1", serviceDeploymentInfo.get().getService().getShortName());
    }

}
