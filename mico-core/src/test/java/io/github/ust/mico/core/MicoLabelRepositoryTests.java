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
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("local")
public class MicoLabelRepositoryTests extends MicoRepositoryTests {

    @Before
    public void setUp() {
        deleteAllData();
    }

    @Commit
    @Test
    public void removeUnnecessaryLabels() {
        // Setup some applications
        MicoApplication a0 = getPureMicoApplication(0);
        MicoApplication a1 = getPureMicoApplication(1);
        MicoApplication a2 = getPureMicoApplication(2);

        // Setup some services
        MicoService s0 = getMicoService(0);
        MicoService s1 = getMicoService(1);
        MicoService s2 = getMicoService(2);

        // Application #0 includes services #0 and #1
        // Application #1 only includes the service #1
        // Application #2 only includes the service #2
        addMicoServicesWithServiceDeploymentInfo(a0, s0, s1);
        addMicoServicesWithServiceDeploymentInfo(a1, s1);
        addMicoServicesWithServiceDeploymentInfo(a2, s2);

        // Setup some labels
        MicoLabel l0 = getMicoServiceDeploymentInfoLabel(5);
        MicoLabel l1 = getMicoServiceDeploymentInfoLabel(6);
        MicoLabel l2 = getMicoServiceDeploymentInfoLabel(7);

        // Save
        applicationRepository.save(a0);
        applicationRepository.save(a1);
        applicationRepository.save(a2);
        labelRepository.save(l0);
        labelRepository.save(l1);
        labelRepository.save(l2);

        // 4 (because of service deployment info) + 3 (created labels) = 7
        assertEquals(7, labelRepository.count());

        // Remove all labels that do not have any relationship with another node
        labelRepository.cleanUp();
        assertEquals(4, labelRepository.count());
        // Check if all applications did not change
        assertEquals(a0, applicationRepository.findByShortNameAndVersion(a0.getShortName(), a0.getVersion()).get());
        assertEquals(a1, applicationRepository.findByShortNameAndVersion(a1.getShortName(), a1.getVersion()).get());
        assertEquals(a2, applicationRepository.findByShortNameAndVersion(a2.getShortName(), a2.getVersion()).get());
    }
}
