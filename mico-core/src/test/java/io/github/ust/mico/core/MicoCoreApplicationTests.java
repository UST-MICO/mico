package io.github.ust.mico.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoPortType;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceCrawlingOrigin;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;
import io.github.ust.mico.core.model.MicoVersion;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

@RunWith(SpringRunner.class)
@SpringBootTest

// TODO Only ignored because of Neo4j error
@Ignore
public class MicoCoreApplicationTests extends Neo4jTestClass {

    private static final String TEST_SHORT_NAME = "Test";
    private static final String TEST_SERVICE_DESCRIPTION = "Test Service";
    private static final String TEST_GIT_CLONE_URL = "http://github.com/org/repo.git";
    private static final String TEST_GIT_RELEASE_INFO_URL = "http://api.github.com/repos/org/repo/releases/1337";
    private static final String TEST_CONTACT = "Test Person";
    private static final String TEST_PORT = "8080";
    private static final String TEST_TARGET_PORT = "8081";
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

    @Ignore
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
        assertEquals(TEST_GIT_RELEASE_INFO_URL, serviceTest.getGitCloneUrl());
        assertEquals(TEST_GIT_CLONE_URL, serviceTest.getGitReleaseInfoUrl());
        assertEquals(TEST_CONTACT, serviceTest.getContact());

        assertEquals(1, serviceInterfacesTest.size());
        MicoServiceInterface serviceInterfaceTest = serviceInterfacesTest.get(0);
        assertEquals(TEST_PORT, serviceInterfaceTest.getPorts().get(0).getNumber());
        assertEquals(TEST_TARGET_PORT, serviceInterfaceTest.getPorts().get(0).getTargetPort());
        assertEquals(TEST_SERVICE_INTERFACE_DESCRIPTION, serviceInterfaceTest.getDescription());
        assertEquals(TEST_PROTOCOL, serviceInterfaceTest.getProtocol());
        assertEquals(TEST_DNS, serviceInterfaceTest.getPublicDns());
        assertEquals(TEST_SERVICE_INTERFACE_NAME, serviceInterfaceTest.getServiceInterfaceName());
    }

    public static MicoService createServiceInDB() throws VersionNotSupportedException {
        return MicoService.builder()
                .shortName(TEST_SHORT_NAME)
                .name(TEST_LONGER_NAME)
                .version(TEST_VERSION)
                .description(TEST_SERVICE_DESCRIPTION)
                .serviceInterface(MicoServiceInterface.builder()
                        .serviceInterfaceName(TEST_SERVICE_INTERFACE_NAME)
                        .port(MicoServicePort.builder()
                                .number(8080)
                                .type(MicoPortType.TCP)
                                .targetPort(8081)
                                .build())
                        .publicDns(TEST_DNS)
                        .description(TEST_SERVICE_INTERFACE_DESCRIPTION)
                        .protocol(TEST_PROTOCOL)
                        .build())
                .serviceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
                .gitCloneUrl(TEST_GIT_CLONE_URL)
                .gitReleaseInfoUrl(TEST_GIT_CLONE_URL)
                .contact(TEST_CONTACT)
                .build();
    }

    @Ignore
    @Test
    public void testDependencyServiceRepository() throws VersionNotSupportedException {
        MicoService service1 = createServiceInDB();

        String testServivce2ShortName = "S2";
        String testServivce2Name = "Service 2";
        String testService2Version = "1.2.3";
        String testServivce2Description = "This is service 2.";
        String testServivce2GitReleaseInfoUrl = "Some GitHub root.";
        String testServivce2GitCloneUrl = "Some GitHub clone url.";
        String testServivce2Contact = "Me";

        String testInterface2Name = "Service Interface 2";
        int testInterface2Port = 9000;
        int testInterface2TargetPort = 9001;
        String testInterface2PublicDns = "DNS 2";
        String testInterface2Description = "This is service interface 2";
        String testInterface2Protocol = "TCP";

        MicoService service2 = MicoService.builder()
                .shortName(testServivce2ShortName)
                .name(testServivce2Name)
                .version(testService2Version)
                .description(testServivce2Description)
                .serviceInterface(MicoServiceInterface.builder()
                        .serviceInterfaceName(testInterface2Name)
                        .port(MicoServicePort.builder()
                                .number(testInterface2Port)
                                .type(MicoPortType.TCP)
                                .targetPort(testInterface2TargetPort)
                                .build())
                        .publicDns(testInterface2PublicDns)
                        .description(testInterface2Description)
                        .protocol(testInterface2Protocol)
                        .build())
                .serviceCrawlingOrigin(MicoServiceCrawlingOrigin.GITHUB)
                .gitCloneUrl(testServivce2GitCloneUrl)
                .gitReleaseInfoUrl(testServivce2GitReleaseInfoUrl)
                .contact(testServivce2Contact)
                .build();

        service1.setDependencies(Collections.singletonList(MicoServiceDependency.builder()
                .service(service1)
                .dependedService(service2)
                .minVersion(MicoVersion.forIntegers(1, 0, 0))
                .maxVersion(MicoVersion.forIntegers(2, 0, 0))
                .build()));

        serviceRepository.save(service1);

        Optional<MicoService> serviceTestOpt = serviceRepository.findByShortNameAndVersion(TEST_SHORT_NAME, TEST_VERSION, 2);
        MicoService serviceTest = serviceTestOpt.get();
        checkDefaultService(serviceTest);
        List<MicoServiceDependency> dependsOnList = serviceTest.getDependencies();
        assertEquals(1, dependsOnList.size());
        MicoServiceDependency dependency1 = dependsOnList.get(0);
        assertEquals("1.0.0", dependency1.getMinVersion().toString());
        assertEquals("2.0.0", dependency1.getMaxVersion().toString());

        MicoService testService2 = dependency1.getDependedService();
        assertNotNull(testService2);
        assertEquals(testService2, testService2.getVersion());
        assertEquals(testServivce2Description, testService2.getDescription());
        assertEquals(testServivce2GitReleaseInfoUrl, testService2.getGitReleaseInfoUrl());
        assertEquals(testServivce2GitCloneUrl, testService2.getGitCloneUrl());
        assertEquals(testServivce2Contact, testService2.getContact());
    }

    @Test
    public void testStoreApplication() {
        MicoApplication application1 = MicoApplication.builder()
                .shortName("App1")
                .name("Application1")
                .version("1.0.0")
                .build();
        applicationRepository.save(application1);

        MicoApplication application2 = MicoApplication.builder()
                .shortName("App2")
                .name("Application2")
                .version("1.0.0")
                .build();
        applicationRepository.save(application2);

        MicoApplication application3 = MicoApplication.builder()
                .shortName("App3")
                .name("Application3")
                .version("1.0.0")
                .build();
        applicationRepository.save(application3);

        MicoApplication storedApplication1 = applicationRepository.findByShortNameAndVersion("App1", "1.0.0", 2).get();
        MicoApplication storedApplication2 = applicationRepository.findByShortNameAndVersion("App2", "1.0.0", 2).get();
        MicoApplication storedApplication3 = applicationRepository.findByShortNameAndVersion("App3", "1.0.0", 2).get();

        assertNotNull(storedApplication1);
        assertEquals("App1", storedApplication1.getShortName());

        assertNotNull(storedApplication2);
        assertEquals("App2", storedApplication2.getShortName());

        assertNotNull(storedApplication3);
        assertEquals("App3", storedApplication3.getShortName());
    }
}
