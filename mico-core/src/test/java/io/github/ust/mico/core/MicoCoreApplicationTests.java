package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoCoreApplicationTests {

	public static final String TEST_SHORT_NAME = "Test";
	public static final String TEST_SERVICE_DESCRIPTION = "Test Service";
	public static final String TEST_VCS_ROOT = "http://test.org/test";
	public static final String TEST_CONTACT = "Test Person";
	public static final String TEST_PORT_VARIABLE = "<PORT_VARIABLE>";
	public static final String TEST_SERVICE_INTERFACE_DESCRIPTION = "This is an interface of an service";
	public static final String TEST_PROTOCOL = "http";
	public static final String TEST_DNS = "DNS";
	public static final String TEST_SERVICE_INTERFACE_NAME = "Service interface name";
	public static final String TEST_LONGER_NAME = "TEST LONGER NAME";
	public static final String TEST_VERSION = "1.0";

	@Autowired
	private ServiceRepository serviceRepository;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testServiceRepository(){
		serviceRepository.deleteAll();
		serviceRepository.save(createServiceInDB());

		Service serviceTest = serviceRepository.findByName(TEST_LONGER_NAME);
        checkDefaultService(serviceTest);
    }

    public static void checkDefaultService(Service serviceTest) {
        List<ServiceInterface> serviceInterfacesTest = serviceTest.getServiceInterfaces();

        assertEquals(TEST_VERSION,serviceTest.getVersion());
        assertEquals(TEST_LONGER_NAME,serviceTest.getName());
        assertEquals(TEST_SERVICE_DESCRIPTION,serviceTest.getDescription());
        assertEquals(TEST_VCS_ROOT,serviceTest.getVcsRoot());
        assertEquals(TEST_CONTACT,serviceTest.getContact());

        assertEquals(1,serviceInterfacesTest.size());
        ServiceInterface serviceInterfaceTest = serviceInterfacesTest.get(0);
        assertEquals(TEST_PORT_VARIABLE, serviceInterfaceTest.getPort());
        assertEquals(TEST_SERVICE_INTERFACE_DESCRIPTION, serviceInterfaceTest.getDescription());
        assertEquals(TEST_PROTOCOL, serviceInterfaceTest.getProtocol());
        assertEquals(TEST_DNS, serviceInterfaceTest.getPublic_dns());
        assertEquals(TEST_SERVICE_INTERFACE_NAME, serviceInterfaceTest.getService_name());
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
		serviceInterface.setPublic_dns(TEST_DNS);
		serviceInterface.setService_name(TEST_SERVICE_INTERFACE_NAME);

		service.setServiceInterfaces(Collections.singletonList(serviceInterface));
		return service;
	}

	@Test
	public void testDependencyServiceRepository(){
		serviceRepository.deleteAll();
		Service service = createServiceInDB();

		String testShortName2 = "ShortName2";
		String testVersion2 = "1.0";
		String testLongerName = "Longer Name2";
		String testserviceInterface = "test Service2";
		String testVcsRoot = "http://test.org/test2";
		String testContact = "Test Person 2";
		String testPort = "<PORT_VARIABLE2>";
		String testsServiceInterface = "This is an interface of an service2";
		String testServiceInterfaceName = "Interface2";

		//2. service
        Service service2 = new Service(testShortName2,testVersion2);
        service2.setName(testLongerName);
        service2.setDescription(testserviceInterface);
        service2.setVcsRoot(testVcsRoot);
        service2.setContact(testContact);

        ServiceInterface serviceInterface2 = new ServiceInterface();
        serviceInterface2.setPort(testPort);
        serviceInterface2.setDescription(testsServiceInterface);
        serviceInterface2.setProtocol(TEST_PROTOCOL);
        serviceInterface2.setPublic_dns(TEST_DNS);
        serviceInterface2.setService_name(testServiceInterfaceName);
        service2.setServiceInterfaces(Collections.singletonList(serviceInterface2));
        DependsOn dependsOn = new DependsOn();
        dependsOn.setMaxVersion("1.0");
        dependsOn.setMinVersion("1.0");
        dependsOn.setService(service2);
        service.setDependsOn(Collections.singletonList(dependsOn));
        serviceRepository.save(service);

        Service serviceTest = serviceRepository.findByName(TEST_LONGER_NAME,2);
        checkDefaultService(serviceTest);
        List<DependsOn> dependsOnList = serviceTest.getDependsOn();
        assertEquals(1,dependsOnList.size());
        DependsOn dependsOn1 = dependsOnList.get(0);
        assertEquals("1.0",dependsOn1.getMinVersion());
        assertEquals("1.0",dependsOn1.getMaxVersion());
        Service testService2 = dependsOn1.getService();
        assertNotNull(testService2);
        assertEquals(testVersion2,testService2.getVersion());
        assertEquals(testLongerName,testService2.getName());
        assertEquals(testserviceInterface,testService2.getDescription());
        assertEquals(testVcsRoot,testService2.getVcsRoot());
        assertEquals(testContact,testService2.getContact());
	}

	@Test
	public void testStoreApplication(){
		//TODO: We might want to delete all database entries at the beginning of each test?
		serviceRepository.deleteAll();

		Service service1 = new Service("Service1","0.1");
		Service service2 = new Service("Service2","0.1");

		//TODO: We might want to have the following possibility?
		//DependsOn dependency = new DependsOn(service, min (optional), max (optional));
		DependsOn depends1 = new DependsOn();
		depends1.setService(service1);
		DependsOn depends2 = new DependsOn();
		depends2.setService(service2);

		//TODO: We might want to have the following possibility?
		//Application application = new Application("shortName", version (optional));
		Application application = new Application();
		application.setShortName("App");
		application.setName("Application"); //TODO: Should not be required
		application.setDependsOn(Arrays.asList(depends1, depends2));
		serviceRepository.save(application);

		//TODO: We might want to have the following possibilities?
        //Application storedApplication = serviceRepository.findByName("Application");
		//Application storedApplication = serviceRepository.findByShortName("App");
		Service storedApplication = serviceRepository.findByName("Application",2);

		assertNotNull(storedApplication);
		assertEquals("App", storedApplication.getShortName());
		assertEquals("Service1", storedApplication.getDependsOn().get(0).getService().getShortName());
		assertEquals("Service2", storedApplication.getDependsOn().get(1).getService().getShortName());
	}
}
