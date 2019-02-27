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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.github.ust.mico.core.dto.MicoServiceDeploymentInfoDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoLabel;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.ImagePullPolicy;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo.RestartPolicy;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class JakobTest {

//    @MockBean
    @Autowired
    private MicoApplicationRepository applicationRepository;

//  @MockBean
    @Autowired
    private MicoServiceRepository serviceRepository;

//@MockBean
    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    
    @Test
    @Commit
    public void test() {
        applicationRepository.deleteAll();
        serviceRepository.deleteAll();
        
        MicoApplication application = new MicoApplication().setShortName("App").setVersion("v1.0.0");
        
        MicoService service1 = new MicoService().setShortName("Service-1").setVersion("v1.0.0");
        MicoService service2 = new MicoService().setShortName("Service-2").setVersion("v1.0.0");
        MicoService service3 = new MicoService().setShortName("Service-3").setVersion("v1.0.0");
        
        applicationRepository.save(application);
        
        serviceRepository.save(service1);
        serviceRepository.save(service2);
        serviceRepository.save(service3);
        
        MicoServiceDeploymentInfo sdi1 = new MicoServiceDeploymentInfo().setApplication(application).setService(service1);
        MicoServiceDeploymentInfo sdi2 = new MicoServiceDeploymentInfo().setApplication(application).setService(service2);
        MicoServiceDeploymentInfo sdi3 = new MicoServiceDeploymentInfo().setApplication(application).setService(service3);
        application.setServiceDeploymentInfos(CollectionUtils.listOf(sdi1, sdi2, sdi3));
        
        applicationRepository.save(application);
        
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        
        application.getServiceDeploymentInfos().forEach(sdi -> System.out.println(sdi));
        
        List<MicoLabel<String, String>> labels = CollectionUtils.listOf(new MicoLabel<String, String>("key-1", "value-1"));
        MicoServiceDeploymentInfoDTO dto = new MicoServiceDeploymentInfoDTO(10, 5, labels, ImagePullPolicy.IF_NOT_PRESENT, RestartPolicy.NEVER);
        
//        for (int i = 0; i < application.getServiceDeploymentInfos().size(); i++) {
//            if (application.getServiceDeploymentInfos().get(i).getService().getShortName().equals(service2.getShortName())) {
//                application.getServiceDeploymentInfos().set(i, dto.to());
//            }
//        }
        for (MicoServiceDeploymentInfo serviceDeploymentInfo : application.getServiceDeploymentInfos()) {
            if (serviceDeploymentInfo.getService().getShortName().equals(service2.getShortName())) {
                serviceDeploymentInfo.applyValuesFrom(dto);
            }
        }
        
        application.getServiceDeploymentInfos().forEach(sdi -> System.out.println(sdi));
        
        applicationRepository.save(application);
        
        MicoApplication a = applicationRepository.findByShortNameAndVersion(application.getShortName(), application.getVersion()).get();
        a.getServiceDeploymentInfos().forEach(sdi -> System.out.println(sdi));
    }

}
