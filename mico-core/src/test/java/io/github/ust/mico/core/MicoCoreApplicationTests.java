package io.github.ust.mico.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
		serviceRepository.save(service);

		Service serviceTest = serviceRepository.findByName(TEST_LONGER_NAME);
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

}
