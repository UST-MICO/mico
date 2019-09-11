package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.MicoApplicationBroker;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.TestConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class MicoApplicationBrokerTests {

    @MockBean
    private MicoApplicationRepository micoApplicationRepository;

    @Autowired
    private MicoApplicationBroker micoApplicationBroker;


    @Test
    public void getAllApplications() {
        given(micoApplicationRepository.findAll(ArgumentMatchers.anyInt())).willReturn(
            CollectionUtils.listOf(
                new MicoApplication().setName(NAME).setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setDescription(DESCRIPTION_1)));

        List<MicoApplication> listOfMicoApplications = micoApplicationBroker.getMicoApplications();
        assertThat(listOfMicoApplications.get(0).getShortName()).isEqualTo(SHORT_NAME_1);
        /*

         */
    }

    @Test
    public void getApplicationsFromDatabase() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1)
            .setName(NAME_1);

        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(Optional.of(micoApplication));

        MicoApplication application = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(SHORT_NAME, VERSION);
        assertThat(application.getShortName()).isEqualTo(SHORT_NAME);
        assertThat(application.getVersion()).isEqualTo(VERSION);
    }

    @Test
    public void updateApplication() {

    }

    @Test
    public void getApplicationById() {


    }

    @Test
    public void deleteApplication() {

    }

    @Test
    public void deleteAllVersionsOfAnApplication() {


    }

    @Test
    public void getAllServicesFromApplication() {

    }
}
