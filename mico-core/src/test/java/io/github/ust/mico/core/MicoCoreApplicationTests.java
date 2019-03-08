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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.exception.VersionNotSupportedException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.util.CollectionUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoCoreApplicationTests extends Neo4jTestClass {

    private static final String TEST_SHORT_NAME = "Test";
    private static final String TEST_SERVICE_DESCRIPTION = "Test Service";
    private static final String TEST_GIT_CLONE_URL = "http://github.com/org/repo.git";
    private static final String TEST_CONTACT = "Test Person";
    private static final int TEST_PORT = 8080;
    private static final int TEST_TARGET_PORT = 8081;
    private static final String TEST_SERVICE_INTERFACE_DESCRIPTION = "This is an interface of an service";
    private static final String TEST_PROTOCOL = "http";
    private static final String TEST_DNS = "DNS";
    private static final String TEST_SERVICE_INTERFACE_NAME = "Service interface name";
    private static final String TEST_LONGER_NAME = "TEST LONGER NAME";
    private static final String TEST_VERSION = "1.0";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Test
    public void testServiceRepository() throws VersionNotSupportedException {
        serviceRepository.save(createServiceInDB());

        Optional<MicoService> serviceTestOpt = serviceRepository.findByShortNameAndVersion(TEST_SHORT_NAME, TEST_VERSION);
        MicoService serviceTest = serviceTestOpt.get();
        checkDefaultService(serviceTest);
    }

    public static void checkDefaultService(MicoService serviceTest) {
        List<MicoServiceInterface> serviceInterfacesTest = serviceTest.getServiceInterfaces();

        assertEquals(TEST_VERSION, serviceTest.getVersion());
        assertEquals(TEST_LONGER_NAME, serviceTest.getName());
        assertEquals(TEST_SERVICE_DESCRIPTION, serviceTest.getDescription());
        assertEquals(TEST_GIT_CLONE_URL, serviceTest.getGitCloneUrl());
        assertEquals(TEST_CONTACT, serviceTest.getContact());

        assertEquals(1, serviceInterfacesTest.size());
        MicoServiceInterface serviceInterfaceTest = serviceInterfacesTest.get(0);
        assertEquals(TEST_PORT, serviceInterfaceTest.getPorts().get(0).getPort());
        assertEquals(TEST_TARGET_PORT, serviceInterfaceTest.getPorts().get(0).getTargetPort());
        assertEquals(TEST_SERVICE_INTERFACE_DESCRIPTION, serviceInterfaceTest.getDescription());
        assertEquals(TEST_PROTOCOL, serviceInterfaceTest.getProtocol());
        assertEquals(TEST_DNS, serviceInterfaceTest.getPublicDns());
        assertEquals(TEST_SERVICE_INTERFACE_NAME, serviceInterfaceTest.getServiceInterfaceName());
    }

    public static MicoService createServiceInDB() throws VersionNotSupportedException {
        return new MicoService()
                .setShortName(TEST_SHORT_NAME)
                .setName(TEST_LONGER_NAME)
                .setVersion(TEST_VERSION)
                .setDescription(TEST_SERVICE_DESCRIPTION)
                .setServiceInterfaces(CollectionUtils.listOf(new MicoServiceInterface()
                        .setServiceInterfaceName(TEST_SERVICE_INTERFACE_NAME)
                        .setPorts(CollectionUtils.listOf(new MicoServicePort()
                                .setPort(8080)
                                .setType(MicoPortType.TCP)
                                .setTargetPort(8081)))
                        .setPublicDns(TEST_DNS)
                        .setDescription(TEST_SERVICE_INTERFACE_DESCRIPTION)
                        .setProtocol(TEST_PROTOCOL)))
                .setServiceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
                .setGitCloneUrl(TEST_GIT_CLONE_URL)
                .setContact(TEST_CONTACT);
    }

    @Test
    public void testDependencyServiceRepository() throws VersionNotSupportedException {
        MicoService service1 = createServiceInDB();

        String testServivce2ShortName = "S2";
        String testServivce2Name = "Service 2";
        String testService2Version = "1.2.3";
        String testServivce2Description = "This is service 2.";
        String testServivce2GitCloneUrl = "Some GitHub clone url.";
        String testServivce2Contact = "Me";

        String testInterface2Name = "Service Interface 2";
        int testInterface2Port = 9000;
        int testInterface2TargetPort = 9001;
        String testInterface2PublicDns = "DNS 2";
        String testInterface2Description = "This is service interface 2";
        String testInterface2Protocol = "TCP";

        MicoService service2 = new MicoService()
        .setShortName(testServivce2ShortName)
        .setName(testServivce2Name)
        .setVersion(testService2Version)
        .setDescription(testServivce2Description)
        .setServiceInterfaces(CollectionUtils.listOf(new MicoServiceInterface()
                .setServiceInterfaceName(testInterface2Name)
                .setPorts(CollectionUtils.listOf(new MicoServicePort()
                        .setPort(testInterface2Port)
                        .setType(MicoPortType.TCP)
                        .setTargetPort(testInterface2TargetPort)))
                .setPublicDns(testInterface2PublicDns)
                .setDescription(testInterface2Description)
                .setProtocol(testInterface2Protocol)))
        .setServiceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
        .setGitCloneUrl(testServivce2GitCloneUrl)
        .setContact(testServivce2Contact);

        service1.setDependencies(Collections.singletonList(new MicoServiceDependency()
                .setService(service1)
                .setDependedService(service2)
                .setMinVersion("1.0.0")
                .setMaxVersion("2.0.0")));

        serviceRepository.save(service1);

        Optional<MicoService> serviceTestOpt = serviceRepository.findByShortNameAndVersion(TEST_SHORT_NAME, TEST_VERSION);
        MicoService serviceTest = serviceTestOpt.get();
        checkDefaultService(serviceTest);
        List<MicoServiceDependency> dependsOnList = serviceTest.getDependencies();
        assertEquals(1, dependsOnList.size());
        MicoServiceDependency dependency1 = dependsOnList.get(0);
        assertEquals("1.0.0", dependency1.getMinVersion());
        assertEquals("2.0.0", dependency1.getMaxVersion());

        MicoService testService2 = dependency1.getDependedService();
        assertNotNull(testService2);
        assertEquals(testService2Version, testService2.getVersion());
        assertEquals(testServivce2Description, testService2.getDescription());
        assertEquals(testServivce2GitCloneUrl, testService2.getGitCloneUrl());
        assertEquals(testServivce2Contact, testService2.getContact());
    }

    @Test
    public void testStoreApplication() {
        MicoApplication application1 = new MicoApplication()
                .setShortName("App1")
                .setName("Application1")
                .setVersion("1.0.0");
        applicationRepository.save(application1);

        MicoApplication application2 = new MicoApplication()
                .setShortName("App2")
                .setName("Application2")
                .setVersion("1.0.0");
        applicationRepository.save(application2);

        MicoApplication application3 = new MicoApplication()
                .setShortName("App3")
                .setName("Application3")
                .setVersion("1.0.0");
        applicationRepository.save(application3);

        MicoApplication storedApplication1 = applicationRepository.findByShortNameAndVersion("App1", "1.0.0").get();
        MicoApplication storedApplication2 = applicationRepository.findByShortNameAndVersion("App2", "1.0.0").get();
        MicoApplication storedApplication3 = applicationRepository.findByShortNameAndVersion("App3", "1.0.0").get();

        assertNotNull(storedApplication1);
        assertEquals("App1", storedApplication1.getShortName());

        assertNotNull(storedApplication2);
        assertEquals("App2", storedApplication2.getShortName());

        assertNotNull(storedApplication3);
        assertEquals("App3", storedApplication3.getShortName());
    }
}
