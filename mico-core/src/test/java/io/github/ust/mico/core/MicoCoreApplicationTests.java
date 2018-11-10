package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
		serviceRepository.save(createServiceinDB());

		Service serviceTest = serviceRepository.findByName(TEST_LONGER_NAME);
        checkDefaultService(serviceTest);

    }

    private void checkDefaultService(Service serviceTest) {
        List<ServiceDescription> serviceDescriptionsTest = serviceTest.getServiceDescriptions();

        assertEquals(TEST_VERSION,serviceTest.getVersion());
        assertEquals(TEST_LONGER_NAME,serviceTest.getName());
        assertEquals(TEST_SERVICE_DESCRIPTION,serviceTest.getDescription());
        assertEquals(TEST_VCS_ROOT,serviceTest.getVcsRoot());
        assertEquals(TEST_CONTACT,serviceTest.getContact());

        assertEquals(1,serviceDescriptionsTest.size());
        ServiceDescription serviceDescriptionTest = serviceDescriptionsTest.get(0);
        assertEquals(TEST_PORT_VARIABLE,serviceDescriptionTest.getPort());
        assertEquals(TEST_SERVICE_INTERFACE_DESCRIPTION,serviceDescriptionTest.getDescription());
        assertEquals(TEST_PROTOCOL,serviceDescriptionTest.getProtocol());
        assertEquals(TEST_DNS,serviceDescriptionTest.getPublic_dns());
        assertEquals(TEST_SERVICE_INTERFACE_NAME,serviceDescriptionTest.getService_name());
    }

    private Service createServiceinDB() {
		Service service = new Service(TEST_SHORT_NAME, TEST_VERSION);
		service.setName(TEST_LONGER_NAME);
		service.setDescription(TEST_SERVICE_DESCRIPTION);
		service.setVcsRoot(TEST_VCS_ROOT);
		service.setContact(TEST_CONTACT);

		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setPort(TEST_PORT_VARIABLE);
		serviceDescription.setDescription(TEST_SERVICE_INTERFACE_DESCRIPTION);
		serviceDescription.setProtocol(TEST_PROTOCOL);
		serviceDescription.setPublic_dns(TEST_DNS);
		serviceDescription.setService_name(TEST_SERVICE_INTERFACE_NAME);
		service.setServiceDescriptions(Collections.singletonList(serviceDescription));
		return service;
	}

	@Test
	public void testDependencyServiceRepository(){
		serviceRepository.deleteAll();
		Service service = createServiceinDB();

		String testShortName2 = "ShortName2";
		String testVersion2 = "1.0";
		String testLongerName = "Longer Name2";
		String testServiceDescribtion = "test Service2";
		String testVcsRoot = "http://test.org/test2";
		String testContact = "Test Person 2";
		String testPort = "<PORT_VARIABLE2>";
		String testsServiceInterface = "This is an interface of an service2";
		String testServiceInterfaceName = "Interface2";

		//2. service
        Service service2 = new Service(testShortName2,testVersion2);
        service2.setName(testLongerName);
        service2.setDescription(testServiceDescribtion);
        service2.setVcsRoot(testVcsRoot);
        service2.setContact(testContact);

        ServiceDescription serviceDescription2 = new ServiceDescription();
        serviceDescription2.setPort(testPort);
        serviceDescription2.setDescription(testsServiceInterface);
        serviceDescription2.setProtocol(TEST_PROTOCOL);
        serviceDescription2.setPublic_dns(TEST_DNS);
        serviceDescription2.setService_name(testServiceInterfaceName);
        service2.setServiceDescriptions(Collections.singletonList(serviceDescription2));
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
        assertEquals(testServiceDescribtion,testService2.getDescription());
        assertEquals(testVcsRoot,testService2.getVcsRoot());
        assertEquals(testContact,testService2.getContact());

	}



}
