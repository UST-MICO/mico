package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDependencyRepository;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoCoreApplicationTests extends Neo4jTestClass {

    private static final String TEST_SHORT_NAME = "Test";
    private static final String TEST_SERVICE_DESCRIPTION = "Test Service";
    private static final String TEST_VCS_ROOT = "http://test.org/test";
    private static final String TEST_CONTACT = "Test Person";
    private static final String TEST_PORT_VARIABLE = "<PORT_VARIABLE>";
    private static final String TEST_SERVICE_INTERFACE_DESCRIPTION = "This is an interface of an service";
    private static final String TEST_PROTOCOL = "http";
    private static final String TEST_DNS = "DNS";
    private static final String TEST_SERVICE_INTERFACE_NAME = "Service interface name";
    private static final String TEST_LONGER_NAME = "TEST LONGER NAME";
    private static final String TEST_VERSION = "1.0";

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceDependencyRepository dependsOnRepository;

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Test
    public void testServiceRepository() {
        serviceRepository.save(createServiceInDB());

        Optional<Service> serviceTestOpt = serviceRepository.findByShortNameAndVersion(TEST_SHORT_NAME, TEST_VERSION);
        Service serviceTest = serviceTestOpt.get();
        checkDefaultService(serviceTest);
    }

    public static void checkDefaultService(Service serviceTest) {
        List<ServiceInterface> serviceInterfacesTest = serviceTest.getServiceInterfaces();

        assertEquals(TEST_VERSION, serviceTest.getVersion());
        assertEquals(TEST_LONGER_NAME, serviceTest.getName());
        assertEquals(TEST_SERVICE_DESCRIPTION, serviceTest.getDescription());
        assertEquals(TEST_VCS_ROOT, serviceTest.getVcsRoot());
        assertEquals(TEST_CONTACT, serviceTest.getContact());

        assertEquals(1, serviceInterfacesTest.size());
        ServiceInterface serviceInterfaceTest = serviceInterfacesTest.get(0);
        assertEquals(TEST_PORT_VARIABLE, serviceInterfaceTest.getPort());
        assertEquals(TEST_SERVICE_INTERFACE_DESCRIPTION, serviceInterfaceTest.getDescription());
        assertEquals(TEST_PROTOCOL, serviceInterfaceTest.getProtocol());
        assertEquals(TEST_DNS, serviceInterfaceTest.getPublicDns());
        assertEquals(TEST_SERVICE_INTERFACE_NAME, serviceInterfaceTest.getServiceInterfaceName());
    }

    public static Service createServiceInDB() {
        Service service = new Service(TEST_SHORT_NAME, TEST_VERSION);
        service.setName(TEST_LONGER_NAME);
        service.setDescription(TEST_SERVICE_DESCRIPTION);
        service.setVcsRoot(TEST_VCS_ROOT);
        service.setContact(TEST_CONTACT);

        ServiceInterface serviceInterface = new ServiceInterface();
        serviceInterface.setPort(TEST_PORT_VARIABLE);
        serviceInterface.setDescription(TEST_SERVICE_INTERFACE_DESCRIPTION);
        serviceInterface.setProtocol(TEST_PROTOCOL);
        serviceInterface.setPublicDns(TEST_DNS);
        serviceInterface.setServiceInterfaceName(TEST_SERVICE_INTERFACE_NAME);

        service.setServiceInterfaces(Collections.singletonList(serviceInterface));
        return service;
    }

    @Test
    public void testDependencyServiceRepository() {
        Service service = createServiceInDB();

        String testShortName2 = "ShortName2";
        String testVersion2 = "1.0";
        String testLongerName = "Longer Name2";
        String testServiceInterface = "test Service2";
        String testVcsRoot = "http://test.org/test2";
        String testContact = "Test Person 2";
        String testPort = "<PORT_VARIABLE2>";
        String testsServiceInterface = "This is an interface of an service2";
        String testServiceInterfaceName = "Interface2";

        //2. service
        Service service2 = new Service(testShortName2, testVersion2);
        service2.setName(testLongerName);
        service2.setDescription(testServiceInterface);
        service2.setVcsRoot(testVcsRoot);
        service2.setContact(testContact);

        ServiceInterface serviceInterface2 = new ServiceInterface();
        serviceInterface2.setPort(testPort);
        serviceInterface2.setDescription(testsServiceInterface);
        serviceInterface2.setProtocol(TEST_PROTOCOL);
        serviceInterface2.setPublicDns(TEST_DNS);
        serviceInterface2.setServiceInterfaceName(testServiceInterfaceName);
        service2.setServiceInterfaces(Collections.singletonList(serviceInterface2));
        DependsOn dependsOn = new DependsOn(service, service2);
        dependsOn.setMaxVersion("1.0");
        dependsOn.setMinVersion("1.0");
        service.setDependsOn(Collections.singletonList(dependsOn));
        serviceRepository.save(service);

        Optional<Service> serviceTestOpt = serviceRepository.findByShortNameAndVersion(TEST_SHORT_NAME, TEST_VERSION, 2);
        Service serviceTest = serviceTestOpt.get();
        checkDefaultService(serviceTest);
        List<DependsOn> dependsOnList = serviceTest.getDependsOn();
        assertEquals(1, dependsOnList.size());
        DependsOn dependsOn1 = dependsOnList.get(0);
        assertEquals("1.0", dependsOn1.getMinVersion());
        assertEquals("1.0", dependsOn1.getMaxVersion());

        Service testService2 = dependsOn1.getServiceDependee();
        assertNotNull(testService2);
        assertEquals(testVersion2, testService2.getVersion());
        assertEquals(testServiceInterface, testService2.getDescription());
        assertEquals(testVcsRoot, testService2.getVcsRoot());
        assertEquals(testContact, testService2.getContact());
    }

    @Test
    public void testStoreApplication() {
        Service service1 = new Service("Service1", "0.1");
        Service service2 = new Service("Service2", "0.1");
        Service service3 = new Service("Service3", "0.1");

        DependsOn depends1 = new DependsOn(service1, service2);
        DependsOn depends2 = new DependsOn(service2, service3);
        DependsOn depends3 = new DependsOn(service3, service1);

        Application application1 = new Application();
        application1.setShortName("App1");
        application1.setName("Application1");
        application1.setVersion("0.1");
        application1.setDependsOn(Arrays.asList(depends1, depends2, depends3));
        serviceRepository.save(application1);

        Application application2 = new Application("App2");
        application2.setVersion("0.1");
        serviceRepository.save(application2);

        Application application3 = new Application("App3", "0.1");
        serviceRepository.save(application3);

        Application storedApplication1 = (Application) serviceRepository.findByShortNameAndVersion("App1", "0.1", 2).get();

        Application storedApplication2 = (Application) serviceRepository.findByShortNameAndVersion("App2", "0.1", 2).get();

        Application storedApplication3 = (Application) serviceRepository.findByShortNameAndVersion("App3", "0.1", 2).get();

        assertNotNull(storedApplication1);
        assertEquals("App1", storedApplication1.getShortName());
        assertThat(storedApplication1.getDependsOn().get(0).getService().getShortName(), startsWith("Service"));
        assertThat(storedApplication1.getDependsOn().get(1).getService().getShortName(), startsWith("Service"));
        assertThat(storedApplication1.getDependsOn().get(2).getService().getShortName(), startsWith("Service"));

        assertNotNull(storedApplication2);
        assertEquals("App2", storedApplication2.getShortName());

        assertNotNull(storedApplication3);
        assertEquals("App3", storedApplication3.getShortName());
    }
}
