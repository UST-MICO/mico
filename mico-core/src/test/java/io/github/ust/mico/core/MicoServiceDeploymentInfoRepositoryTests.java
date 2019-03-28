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

import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.util.CollectionUtils;
import io.github.ust.mico.core.util.EmbeddedRedisServer;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MicoServiceDeploymentInfoRepositoryTests extends MicoRepositoryTests {

    @Before
    public void setUp() {
        deleteAllData();
    }

    @Commit
    @Test
    public void findAllDeploymentInfoByApplication() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 only includes the service #0
        // Application #1 only includes the service #1
        // Application #2 includes services #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);

        // Get deployment infos of application #0
        List<MicoServiceDeploymentInfo> deploymentInfosOfA0 = serviceDeploymentInfoRepository.findAllByApplication(a0.getShortName(), a0.getVersion());
        // Only service s0 has a deployment info for application #0
        assertEquals(1, deploymentInfosOfA0.size());
        // The deployment info connects application #0 and service #0
        assertEquals(s0, deploymentInfosOfA0.get(0).getService());
        assertEquals(a0.getServiceDeploymentInfos().get(0), deploymentInfosOfA0.get(0));
    }

    @Commit
    @Test
    public void findAllDeploymentInfoByService() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 includes the services #0 and #1
        // Application #1 only includes the service #1
        addMicoServicesWithServiceDeploymentInfo(a0, s0, s1);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);

        // Get deployment infos of service #0
        List<MicoServiceDeploymentInfo> deploymentInfosOfS0 = serviceDeploymentInfoRepository.findAllByService(s0.getShortName(), s0.getVersion());
        // Only application #0 has a deployment info for service #0
        assertEquals(1, deploymentInfosOfS0.size());
        // The deployment info connects application #0 and service #0
        assertEquals(s0, deploymentInfosOfS0.get(0).getService());
        assertEquals(a0.getServiceDeploymentInfos().get(0), deploymentInfosOfS0.get(0));
    }

    @Commit
    @Test
    public void findDeploymentInfoByApplicationAndService() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 includes the services #0 and #1
        // Application #1 only includes the service #1
        addMicoServicesWithServiceDeploymentInfo(a0, s0, s1);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);

        // Get deployment infos for application #0 and service #0
        Optional<MicoServiceDeploymentInfo> deploymentInfoOfA0andS0Optional = serviceDeploymentInfoRepository.findByApplicationAndService(
            a0.getShortName(), a0.getVersion(), s0.getShortName());
        assertTrue(deploymentInfoOfA0andS0Optional.isPresent());
        MicoServiceDeploymentInfo deploymentInfoOfA0andS0 = deploymentInfoOfA0andS0Optional.get();
        // The deployment info connects application #0 and service #0
        assertEquals(s0, deploymentInfoOfA0andS0.getService());
        assertEquals(matchMicoServiceDeploymentInfoByService(a0.getServiceDeploymentInfos(), s0), deploymentInfoOfA0andS0);

        // Get deployment infos for application #0 and service #0
        deploymentInfoOfA0andS0Optional = serviceDeploymentInfoRepository.findByApplicationAndService(
            a0.getShortName(), a0.getVersion(), s0.getShortName(), s0.getVersion());
        assertTrue(deploymentInfoOfA0andS0Optional.isPresent());
        deploymentInfoOfA0andS0 = deploymentInfoOfA0andS0Optional.get();
        // The deployment info connects application #0 and service #0
        assertEquals(s0, deploymentInfoOfA0andS0.getService());
        assertEquals(matchMicoServiceDeploymentInfoByService(a0.getServiceDeploymentInfos(), s0), deploymentInfoOfA0andS0);
    }

    @Commit
    @Test
    public void deleteAllServiceDeploymentInfosByApplication() {
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
        // Application #3 includes no service
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        applicationRepository.save(a3);

        // Delete all service deployment infos of application #2
        serviceDeploymentInfoRepository.deleteAllByApplication(a2.getShortName());
        List<MicoServiceDeploymentInfo> deploymentInfoOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(0, deploymentInfoOfA2.size());
        // Check if all other applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
    }

    @Commit
    @Test
    public void deleteAllServiceDeploymentInfosByApplicationWithVersion() {
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
        // Application #3 includes no service
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        applicationRepository.save(a3);

        // Delete all service deployment infos of application #2
        serviceDeploymentInfoRepository.deleteAllByApplication(a2.getShortName(), a2.getVersion());
        List<MicoServiceDeploymentInfo> deploymentInfoOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(0, deploymentInfoOfA2.size());
        // Check if all other applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
    }

    @Commit
    @Test
    public void deleteAllServiceDeploymentInfosByApplicationAndService() {
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
        // Application #3 includes no service
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        applicationRepository.save(a3);

        // Delete all service deployment infos of application #2
        serviceDeploymentInfoRepository.deleteByApplicationAndService(a2.getShortName(), a2.getVersion(), s0.getShortName());
        List<MicoServiceDeploymentInfo> deploymentInfoOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(1, deploymentInfoOfA2.size());
        // Check if all other applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
    }

    @Commit
    @Test
    public void deleteAllServiceDeploymentInfosByApplicationAndServiceWithVersion() {
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
        // Application #3 includes no service
        addMicoServicesWithServiceDeploymentInfo(a0, s0);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        applicationRepository.save(a3);

        // Delete all service deployment infos of application #2
        serviceDeploymentInfoRepository.deleteByApplicationAndService(a2.getShortName(), a2.getVersion(), s0.getShortName(), s0.getVersion());
        List<MicoServiceDeploymentInfo> deploymentInfoOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(1, deploymentInfoOfA2.size());
        // Check if all other applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
    }
}
