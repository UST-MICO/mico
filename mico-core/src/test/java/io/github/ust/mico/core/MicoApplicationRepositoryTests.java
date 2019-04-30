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

        // Application #0 only includes service #0
        // Application #1 only includes service #1
        // Application #2 includes services #0 and #1
        // Application #3 includes no service
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

}
