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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@ActiveProfiles("local")
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

        // Application #0 includes no service
        // Application #1 only includes the service #1
        // Application #2 includes services #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);

        // Application #0 includes no service
        List<MicoServiceDeploymentInfo> deploymentInfosOfA0 = serviceDeploymentInfoRepository.findAllByApplication(a0.getShortName(), a0.getVersion());
        assertEquals(0, deploymentInfosOfA0.size());
        // Application #1 only includes service #1
        List<MicoServiceDeploymentInfo> deploymentInfosOfA1 = serviceDeploymentInfoRepository.findAllByApplication(a1.getShortName(), a1.getVersion());
        assertEquals(1, deploymentInfosOfA1.size());
        assertEquals(a1.getServiceDeploymentInfos().get(0), deploymentInfosOfA1.get(0));
        // Application #2 includes services #0 and #1
        List<MicoServiceDeploymentInfo> deploymentInfosOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(2, deploymentInfosOfA2.size());
        assertEquals(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s0), matchMicoServiceDeploymentInfoByService(deploymentInfosOfA2, s0));
        assertEquals(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s1), matchMicoServiceDeploymentInfoByService(deploymentInfosOfA2, s1));
    }

    @Commit
    @Test
    public void findAllDeploymentInfoByService() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services (#2 is not included by any application)
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);

        // Application #0 includes no service
        // Application #1 only includes the service #1
        // Application #2 includes services #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        // Service #2 needs to be saved manually since not included by any application
        serviceRepository.save(s2);

        // Service #0 is included only by application #2
        List<MicoServiceDeploymentInfo> deploymentInfosOfS0 = serviceDeploymentInfoRepository.findAllByService(s0.getShortName(), s0.getVersion());
        assertEquals(1, deploymentInfosOfS0.size());
        assertEquals(a2.getServiceDeploymentInfos().get(0), deploymentInfosOfS0.get(0));
        // Service #1 is included by applications #1 and #2
        List<MicoServiceDeploymentInfo> deploymentInfosOfS1 = serviceDeploymentInfoRepository.findAllByService(s1.getShortName(), s1.getVersion());
        assertEquals(2, deploymentInfosOfS1.size());
        assertTrue(deploymentInfosOfS1.contains(matchMicoServiceDeploymentInfoByService(a1.getServiceDeploymentInfos(), s1)));
        assertTrue(deploymentInfosOfS1.contains(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s1)));
        // Service #2 is not included by any application
        List<MicoServiceDeploymentInfo> deploymentInfosOfS2 = serviceDeploymentInfoRepository.findAllByService(s2.getShortName(), s2.getVersion());
        assertEquals(0, deploymentInfosOfS2.size());
    }

    @Commit
    @Test
    public void findDeploymentInfoByApplicationAndServiceShortName() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 includes no service
        // Application #1 only includes the service #1
        // Application #2 includes services #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);

        // Application #0 includes no service
        assertTrue(serviceDeploymentInfoRepository.findByApplicationAndService(a0.getShortName(), a0.getVersion(), s0.getShortName()).isEmpty());
        assertTrue(serviceDeploymentInfoRepository.findByApplicationAndService(a0.getShortName(), a0.getVersion(), s1.getShortName()).isEmpty());
        // Application #1 only includes service #1
        assertTrue(serviceDeploymentInfoRepository.findByApplicationAndService(a1.getShortName(), a1.getVersion(), s0.getShortName()).isEmpty());
        List<MicoServiceDeploymentInfo> deploymentInfosA1S1 = serviceDeploymentInfoRepository.findByApplicationAndService(a1.getShortName(), a1.getVersion(), s1.getShortName());
        assertEquals(1, deploymentInfosA1S1.size());
        assertEquals(a1.getServiceDeploymentInfos().get(0), deploymentInfosA1S1.get(0));
        // Application #2 includes services #0 and #1
        List<MicoServiceDeploymentInfo> deploymentInfosA2S0 = serviceDeploymentInfoRepository.findByApplicationAndService(a2.getShortName(), a2.getVersion(), s0.getShortName());
        assertEquals(1, deploymentInfosA2S0.size());
        assertEquals(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s0), deploymentInfosA2S0.get(0));
        List<MicoServiceDeploymentInfo> deploymentInfosA2S1 = serviceDeploymentInfoRepository.findByApplicationAndService(a2.getShortName(), a2.getVersion(), s1.getShortName());
        assertEquals(1, deploymentInfosA2S1.size());
        assertEquals(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s1), deploymentInfosA2S1.get(0));
    }

    @Commit
    @Test
    public void findDeploymentInfoByApplicationAndService() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 includes no service
        // Application #1 only includes the service #1
        // Application #2 includes services #0 and #1
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s0, s1);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);

        // Application #0 includes no service
        assertTrue(serviceDeploymentInfoRepository.findByApplicationAndService(a0.getShortName(), a0.getVersion(), s0.getShortName(), s0.getVersion()).isEmpty());
        assertTrue(serviceDeploymentInfoRepository.findByApplicationAndService(a0.getShortName(), a0.getVersion(), s1.getShortName(), s1.getVersion()).isEmpty());
        // Application #1 only includes service #1
        assertTrue(serviceDeploymentInfoRepository.findByApplicationAndService(a1.getShortName(), a1.getVersion(), s0.getShortName(), s0.getVersion()).isEmpty());
        List<MicoServiceDeploymentInfo> deploymentInfosA1S1 = serviceDeploymentInfoRepository.findByApplicationAndService(a1.getShortName(), a1.getVersion(), s1.getShortName(), s1.getVersion());
        assertEquals(1, deploymentInfosA1S1.size());
        assertEquals(a1.getServiceDeploymentInfos().get(0), deploymentInfosA1S1.get(0));
        // Application #2 includes services #0 and #1
        List<MicoServiceDeploymentInfo> deploymentInfosA2S0 = serviceDeploymentInfoRepository.findByApplicationAndService(a2.getShortName(), a2.getVersion(), s0.getShortName(), s0.getVersion());
        assertEquals(1, deploymentInfosA2S0.size());
        assertEquals(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s0), deploymentInfosA2S0.get(0));
        List<MicoServiceDeploymentInfo> deploymentInfosA2S1 = serviceDeploymentInfoRepository.findByApplicationAndService(a2.getShortName(), a2.getVersion(), s1.getShortName(), s1.getVersion());
        assertEquals(1, deploymentInfosA2S1.size());
        assertEquals(matchMicoServiceDeploymentInfoByService(a2.getServiceDeploymentInfos(), s1), deploymentInfosA2S1.get(0));
    }

    @Commit
    @Test
    public void deleteAllServiceDeploymentInfosByApplicationShortName() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);
        MicoApplication a3 = getPureMicoApplication(3);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);

        // Application #0 only includes service #0
        // Application #1 only includes service #1
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
        assertEquals(0, serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion()).size());
        // Check that remaining applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
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

        // Delete all service deployment infos of application #1
        serviceDeploymentInfoRepository.deleteAllByApplication(a1.getShortName(), a1.getVersion());
        assertEquals(0, serviceDeploymentInfoRepository.findAllByApplication(a1.getShortName(), a1.getVersion()).size());
        // Check that remaining applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a2, applicationRepository.findByShortNameAndVersion(a2.getShortName(), a2.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
    }

    @Commit
    @Test
    public void deleteAllServiceDeploymentInfosByApplicationAndServiceShortName() {
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

        // Delete service deployment info of application #2 for service #0
        serviceDeploymentInfoRepository.deleteByApplicationAndService(a2.getShortName(), a2.getVersion(), s0.getShortName());
        List<MicoServiceDeploymentInfo> deploymentInfoOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(1, deploymentInfoOfA2.size());
        // Check that remaining applications did not change
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

        // Delete service deployment info of application #2 for service #1
        serviceDeploymentInfoRepository.deleteByApplicationAndService(a2.getShortName(), a2.getVersion(), s1.getShortName(), s1.getVersion());
        List<MicoServiceDeploymentInfo> deploymentInfoOfA2 = serviceDeploymentInfoRepository.findAllByApplication(a2.getShortName(), a2.getVersion());
        assertEquals(1, deploymentInfoOfA2.size());
        // Check that remaining applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a3, applicationRepository.findByShortNameAndVersion(a3.getShortName(), a3.getVersion()).get());
    }

    @Commit
    @Test
    public void updateTopics() {
        MicoApplication a0 = getPureMicoApplication(0);
        MicoService s0 = getMicoService(0);
        addMicoServicesWithServiceDeploymentInfo(a0, s0);

        MicoServiceDeploymentInfo sdi0 = a0.getServiceDeploymentInfos().get(0);
        MicoTopic t0 = getMicoServiceDeploymentInfoTopic("topic0");
        MicoTopicRole tr0 = getMicoServiceDeploymentInfoTopicRole(t0, sdi0, MicoTopicRole.Role.INPUT);
        sdi0.getTopics().add(tr0);
        applicationRepository.save(a0);

        List<MicoServiceDeploymentInfo> storedSDIs = serviceDeploymentInfoRepository
            .findByApplicationAndService(a0.getShortName(), a0.getVersion(), s0.getShortName());
        assertEquals(1, storedSDIs.size());
        MicoServiceDeploymentInfo storedSDI = storedSDIs.get(0);
        List<MicoTopicRole> topicRoles = storedSDI.getTopics();

        assertEquals(1, topicRoles.size());
        assertEquals(MicoTopicRole.Role.INPUT, topicRoles.get(0).getRole());
        assertEquals(sdi0, topicRoles.get(0).getServiceDeploymentInfo());
        assertEquals(t0.getName(), topicRoles.get(0).getTopic().getName());

        // Update topic
        storedSDI.getTopics().get(0).getTopic().setName("topic0-updated");
        storedSDI.getTopics().get(0).setRole(MicoTopicRole.Role.OUTPUT);
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(storedSDI);

        List<MicoServiceDeploymentInfo> updatedSDIs = serviceDeploymentInfoRepository
            .findByApplicationAndService(a0.getShortName(), a0.getVersion(), s0.getShortName());
        assertEquals(1, updatedSDIs.size());
        MicoServiceDeploymentInfo updatedSDI = updatedSDIs.get(0);
        List<MicoTopicRole> updatedTopicRoles = updatedSDI.getTopics();

        assertEquals(1, updatedTopicRoles.size());
        assertEquals(MicoTopicRole.Role.OUTPUT, updatedTopicRoles.get(0).getRole());
        assertEquals("topic0-updated", updatedTopicRoles.get(0).getTopic().getName());
    }

}
