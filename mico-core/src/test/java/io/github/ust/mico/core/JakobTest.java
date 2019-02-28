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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class JakobTest {

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    
    @Test
    @Commit
    public void test() {
        applicationRepository.deleteAll();
        serviceRepository.deleteAll();
        
        MicoApplication a = new MicoApplication().setShortName("App").setVersion("v1.0.0");
        MicoService s1 = new MicoService().setShortName("S1").setVersion("v1.0.0");
        MicoService s2 = new MicoService().setShortName("S2").setVersion("v1.0.0");
        MicoService s3 = new MicoService().setShortName("S3").setVersion("v1.0.0");
        
        applicationRepository.save(a);
        serviceRepository.save(s1);
        serviceRepository.save(s2);
        serviceRepository.save(s3);
        
        a.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setApplication(a).setService(s1));
        a.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setApplication(a).setService(s2));
        a.getServiceDeploymentInfos().add(new MicoServiceDeploymentInfo().setApplication(a).setService(s3));
        
        applicationRepository.save(a);
        
        MicoApplication original = applicationRepository.findByShortNameAndVersion(a.getShortName(), a.getVersion()).get();
        MicoApplication updated = original.setId(null).setVersion("v2.0.0");
        applicationRepository.save(updated);
    }
    
}
