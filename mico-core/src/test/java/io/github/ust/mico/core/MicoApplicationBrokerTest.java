package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.MicoApplicationBroker;
import io.github.ust.mico.core.broker.MicoServiceBroker;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static io.github.ust.mico.core.TestConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicoApplicationBrokerTest {

    @Autowired
    private MicoApplicationBroker micoApplicationBroker;

    @MockBean
    private MicoServiceBroker micoServiceBroker; //TODO: remove?

    @MockBean
    private MicoApplicationRepository applicationRepository;

    @MockBean
    private MicoServiceRepository serviceRepository; //TODO: remove?

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository; //TODO: remove?

    @MockBean
    private MicoKubernetesClient micoKubernetesClient; //TODO: remove?

    @MockBean
    private MicoStatusService micoStatusService; //TODO: remove?

    @Test
    public void getMicoApplicationByShortNameAndVersion() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(SHORT_NAME, VERSION);

        assertNotNull(micoApplication);
        assertEquals(SHORT_NAME, micoApplication.getShortName());
        assertEquals(VERSION, micoApplication.getVersion());
        assertEquals(ID, micoApplication.getId());
    }

    @Test
    public void getMicoApplicationById() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = micoApplicationBroker.getMicoApplicationById(ID);

        assertNotNull(micoApplication);
        assertEquals(SHORT_NAME, micoApplication.getShortName());
        assertEquals(VERSION, micoApplication.getVersion());
        assertEquals(ID, micoApplication.getId());
    }

    @Test
    public void getMicoApplicationsByShortName() {
    }

    @Test
    public void getMicoApplications() {
    }

    @Test
    public void deleteMicoApplicationByShortNameAndVersion() {
    }

    @Test
    public void deleteMicoApplicationById() {
    }

    @Test
    public void deleteMicoApplicationsByShortName() {
    }

    @Test
    public void deleteMicoApplications() {
    }

    @Test
    public void createMicoApplication() {
    }

    @Test
    public void updateMicoApplication() {
    }

    @Test
    public void copyAndUpgradeMicoApplicationByShortNameAndVersion() {
    }

    @Test
    public void copyAndUpgradeMicoApplicationById() {
    }

    @Test
    public void getMicoServicesOfMicoApplicationByShortNameAndVersion() {
    }

    @Test
    public void getMicoServicesOfMicoApplicationById() {
    }

    @Test
    public void addMicoServiceToMicoApplicationByShortNameAndVersion() {
    }

    @Test
    public void addMicoServiceToMicoApplicationById() {
    }

    @Test
    public void getMicoServiceDeploymentInformationOfMicoApplication() {
    }

    @Test
    public void getMicoServiceDeploymentInformationOfMicoApplication1() {
    }

    @Test
    public void removeMicoServiceFromMicoApplicationByShortNameAndVersion() {
    }

    @Test
    public void removeMicoServiceFromMicoApplicationByShortNameAndVersion1() {
    }

    @Test
    public void updateMicoServiceDeploymentInformationOfMicoApplication() {
    }

    @Test
    public void getMicoApplicationStatusOfMicoApplication() {
    }

    @Before
    public void setUp() throws Exception {
        given(applicationRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION).setId(ID)));
        given(applicationRepository.findById(ID)).willReturn(Optional.of(new MicoApplication().setShortName(SHORT_NAME).setVersion(VERSION).setId(ID)));
    }

    @After
    public void tearDown() throws Exception {

    }
}
