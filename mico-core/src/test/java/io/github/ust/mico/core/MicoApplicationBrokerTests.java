package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.KafkaFaasConnectorDeploymentInfoBroker;
import io.github.ust.mico.core.broker.MicoApplicationBroker;
import io.github.ust.mico.core.broker.MicoServiceBroker;
import io.github.ust.mico.core.broker.MicoServiceDeploymentInfoBroker;
import io.github.ust.mico.core.configuration.KafkaFaasConnectorConfig;
import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.TestConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class MicoApplicationBrokerTests {

    @MockBean
    private MicoApplicationRepository micoApplicationRepository;

    @MockBean
    private KafkaFaasConnectorDeploymentInfoBroker kafkaFaasConnectorDeploymentInfoBroker;

    @MockBean
    private MicoServiceBroker micoServiceBroker;

    @MockBean
    private MicoServiceDeploymentInfoBroker micoServiceDeploymentInfoBroker;

    @MockBean
    private KafkaFaasConnectorConfig kafkaFaasConnectorConfig;

    @Autowired
    private MicoApplicationBroker micoApplicationBroker;


    @Test
    public void getAllApplications() {
        given(micoApplicationRepository.findAll(ArgumentMatchers.anyInt())).willReturn(
            CollectionUtils.listOf(
                new MicoApplication().setName(NAME).setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setDescription(DESCRIPTION_1)));

        List<MicoApplication> listOfMicoApplications = micoApplicationBroker.getMicoApplications();
        assertThat(listOfMicoApplications.get(0).getShortName()).isEqualTo(SHORT_NAME_1);

    }

    @Test
    public void getApplicationsFromDatabase() throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION_1)
            .setName(NAME_1);

        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(Optional.of(micoApplication));

        MicoApplication application = micoApplicationBroker.getMicoApplicationByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1);
        assertThat(application.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(application.getVersion()).isEqualTo(VERSION_1_0_1);
    }

    @Test
    public void updateApplicationVersion() throws MicoApplicationNotFoundException, MicoApplicationAlreadyExistsException {
        MicoApplication micoApplication = new MicoApplication()
            .setName(NAME_2)
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setDescription(DESCRIPTION_2);

        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(Optional.of(micoApplication));
        given(micoApplicationRepository.save(any(MicoApplication.class))).willReturn(micoApplication);

        MicoApplication updatedMicoApplication = micoApplicationBroker.copyAndUpgradeMicoApplicationByShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion(), VERSION_1_0_3);

        assertThat(updatedMicoApplication.getShortName()).isEqualTo(SHORT_NAME_2);
        assertThat(updatedMicoApplication.getVersion()).isEqualTo(VERSION_1_0_3);
        assertThat(updatedMicoApplication.getName()).isEqualTo(NAME_2);
        assertThat(updatedMicoApplication.getDescription()).isEqualTo(DESCRIPTION_2);
    }

    @Test
    public void deleteApplication() throws MicoApplicationIsNotUndeployedException, MicoApplicationNotFoundException {
        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(Optional.of(micoApplication));

        micoApplicationBroker.deleteMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
    }


    @Test
    public void addMicoServiceToMicoApplication()
        throws KafkaFaasConnectorNotAllowedHereException, MicoApplicationNotFoundException,
        MicoServiceInstanceNotFoundException, MicoApplicationIsNotUndeployedException, MicoTopicRoleUsedMultipleTimesException,
        KubernetesResourceException, MicoServiceAddedMoreThanOnceToMicoApplicationException, MicoServiceNotFoundException,
        MicoServiceDeploymentInformationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {

        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        MicoService micoService = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setDescription(DESCRIPTION_2);

        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setService(micoService)
            .setId(ID)
            .setInstanceId(INSTANCE_ID);

        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(Optional.of(micoApplication));
        given(micoServiceBroker.getServiceFromDatabase(micoService.getShortName(), micoService.getVersion()))
            .willReturn(micoService);
        given(micoServiceDeploymentInfoBroker.getMicoServiceDeploymentInformation(micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName()))
            .willReturn(micoServiceDeploymentInfo);


        micoApplicationBroker.addMicoServiceToMicoApplicationByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1, micoService.getShortName(), micoService.getVersion(), Optional.empty());
        assert (micoApplication.getServices().get(0).equals(micoService));
    }

    @Test
    public void removeMicoServiceFromMicoApplicationByShortNameAndVersion()
        throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, MicoApplicationIsNotUndeployedException {
        MicoService micoService1 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setDescription(DESCRIPTION_2);

        MicoService micoService2 = new MicoService()
            .setShortName(SHORT_NAME_3)
            .setVersion(VERSION_1_0_3)
            .setDescription(DESCRIPTION_3);

        ArrayList<MicoService> listOfServices = new ArrayList<>();
        listOfServices.add(micoService1);
        listOfServices.add(micoService2);

        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1)
            .setServices(listOfServices);

        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion()))
            .willReturn(Optional.of(micoApplication));
        given(micoApplicationRepository.save(micoApplication)).willReturn(micoApplication);

        MicoApplication updatedMicoApplication = micoApplicationBroker.removeMicoServiceFromMicoApplicationByShortNameAndVersion(
            micoApplication.getShortName(), micoApplication.getVersion(), micoService1.getShortName());

        assert (updatedMicoApplication.getServices().size() == 1);
        assert (updatedMicoApplication.getServices().get(0).equals(micoService2));
    }


    @Test
    public void addKafkaFaasConnectorInstanceToMicoApplicationByVersion() throws MicoServiceNotFoundException,
        MicoApplicationNotFoundException, KafkaFaasConnectorVersionNotFoundException, MicoApplicationIsNotUndeployedException, KafkaFaasConnectorInstanceNotFoundException {
        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        MicoService kfConnector = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2);

        MicoServiceDeploymentInfo sdiTest = new MicoServiceDeploymentInfo();

        given(micoServiceBroker.getServiceFromDatabase(kfConnector.getShortName(), kfConnector.getVersion())).willReturn(kfConnector);
        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(Optional.of(micoApplication));
        given(kafkaFaasConnectorConfig.getServiceName()).willReturn(kfConnector.getShortName());
        given(kafkaFaasConnectorDeploymentInfoBroker.updateKafkaFaasConnectorDeploymentInformation(any(String.class), any(KFConnectorDeploymentInfoRequestDTO.class)))
            .willReturn(sdiTest);

        MicoServiceDeploymentInfo sdi = micoApplicationBroker.addKafkaFaasConnectorInstanceToMicoApplicationByVersion(
            micoApplication.getShortName(), micoApplication.getVersion(), kfConnector.getVersion());

        assert (sdi.getService().equals(kfConnector));
    }

    @Test
    public void updateKafkaFaasConnectorInstanceOfMicoApplicationByVersionAndInstanceId() throws MicoServiceNotFoundException,
        KafkaFaasConnectorInstanceNotFoundException, MicoApplicationIsNotUndeployedException, KafkaFaasConnectorVersionNotFoundException, MicoApplicationNotFoundException {
        MicoService kfConnector = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2);

        MicoService kfConnectorNew = new MicoService()
            .setShortName(SHORT_NAME_3)
            .setVersion(VERSION_1_0_3);

        MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setInstanceId(INSTANCE_ID_1)
            .setService(kfConnector);

        ArrayList<MicoServiceDeploymentInfo> listOfMicoServiceDeploymentInfos = new ArrayList<>();
        listOfMicoServiceDeploymentInfos.add(micoServiceDeploymentInfo);

        MicoApplication micoApplication = new MicoApplication()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1)
            .setKafkaFaasConnectorDeploymentInfos(listOfMicoServiceDeploymentInfos);


        given(micoServiceBroker.getServiceFromDatabase(kfConnector.getShortName(), kfConnector.getVersion())).willReturn(kfConnector);
        given(micoServiceBroker.getServiceFromDatabase(kfConnectorNew.getShortName(), kfConnectorNew.getVersion())).willReturn(kfConnectorNew);
        given(kafkaFaasConnectorConfig.getServiceName()).willReturn(kfConnectorNew.getShortName());
        given(micoApplicationRepository.findByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion())).willReturn(Optional.of(micoApplication));


        MicoServiceDeploymentInfo sdi = micoApplicationBroker.updateKafkaFaasConnectorInstanceOfMicoApplicationByVersionAndInstanceId(
            micoApplication.getShortName(), micoApplication.getVersion(), VERSION_1_0_3, micoServiceDeploymentInfo.getInstanceId());

        assert (sdi.getService().getVersion().equals(kfConnectorNew.getVersion()));
    }


}
