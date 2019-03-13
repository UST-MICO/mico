package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.ServiceBroker;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.GitHubCrawler;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import io.github.ust.mico.core.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.TestConstants.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceBrokerTests {

    @MockBean
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ServiceBroker serviceBroker;

    @MockBean
    private MicoStatusService micoStatusService;

    @MockBean
    private MicoKubernetesClient micoKubernetesClient;

    @MockBean
    private GitHubCrawler crawler;

    @Before
    public void setUp() throws Exception {
        MicoService micoServiceOne = new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setName(NAME_1).setDescription(DESCRIPTION_1);
        MicoService micoServiceTwo = new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_2).setName(NAME_2).setDescription(DESCRIPTION_2);
        MicoService micoServiceThree = new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_3).setName(NAME_3).setDescription(DESCRIPTION_3);

        List<MicoService> micoServiceList = new LinkedList<>();
        micoServiceList.add(micoServiceOne);
        micoServiceList.add(micoServiceTwo);
        micoServiceList.add(micoServiceThree);

        //TODO: Verfiy why this is not working
        //when(serviceRepository.findAll()).thenReturn(micoServiceList);
        when(serviceBroker.getAllServicesAsList()).thenReturn(micoServiceList);
        when(serviceRepository.findByShortNameAndVersion(SHORT_NAME_1, VERSION_1_0_1)).thenReturn(java.util.Optional.ofNullable(micoServiceOne));
    }

    @Test
    public void getAllServicesAsList() throws Exception {
        List<MicoService> micoServiceList = serviceBroker.getAllServicesAsList();

        assertThat(micoServiceList.get(0).getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(micoServiceList.get(1).getShortName()).isEqualTo(SHORT_NAME_2);
        assertThat(micoServiceList.get(2).getShortName()).isEqualTo(SHORT_NAME_3);
    }

    @Test
    public void getServiceFromDatabase() throws Exception {
        MicoService micoService = serviceBroker.getServiceFromDatabase(SHORT_NAME_1, VERSION_1_0_1);
        assertThat(micoService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(micoService.getVersion()).isEqualTo(VERSION_1_0_1);
    }

    @Test
    public void updateExistingService() throws Exception {
        MicoService micoServiceTwo = new MicoService()
                .setShortName(SHORT_NAME_2)
                .setVersion(VERSION_1_0_2)
                .setName(NAME_2)
                .setDescription(DESCRIPTION_2);

        MicoService resultUpdatedService = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setName(NAME_2)
                .setDescription(DESCRIPTION_2);

        given(serviceRepository.save(any(MicoService.class))).willReturn(resultUpdatedService);

        MicoService updatedService = serviceBroker.updateExistingService(SHORT_NAME_1, VERSION_1_0_1, micoServiceTwo);

        assertThat(updatedService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(updatedService.getVersion()).isEqualTo(VERSION_1_0_1);
        assertThat(updatedService.getName()).isEqualTo(NAME_2);
        assertThat(updatedService.getDescription()).isEqualTo(DESCRIPTION_2);
    }

    @Test
    public void getServiceById() throws Exception {
        MicoService micoServiceOne = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setName(NAME_1)
                .setDescription(DESCRIPTION_1);

        Long id = new Long(1);

        given(serviceRepository.findById(ArgumentMatchers.anyLong())).willReturn(java.util.Optional.ofNullable(micoServiceOne));

        MicoService micoService = serviceBroker.getServiceById(id);

        assertThat(micoService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(micoService.getVersion()).isEqualTo(VERSION_1_0_1);
        assertThat(micoService.getName()).isEqualTo(NAME_1);
        assertThat(micoService.getDescription()).isEqualTo(DESCRIPTION_1);
    }

    @Test
    public void deleteService() throws Exception {
        //TODO: Implementation haha
        serviceBroker.deleteService(SHORT_NAME_1, VERSION_1_0_1);
    }

    @Test
    public void persistService() throws Exception {
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setName(NAME_1)
                .setDescription(DESCRIPTION_1);

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        MicoService savedService = serviceBroker.persistService(service);

        assertThat(savedService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(savedService.getVersion()).isEqualTo(VERSION_1_0_1);
        assertThat(savedService.getName()).isEqualTo(NAME_1);
        assertThat(savedService.getDescription()).isEqualTo(DESCRIPTION_1);
    }

    @Test
    public void deleteAllVersionsOfService() throws Exception {
        serviceBroker.deleteAllVersionsOfService(SHORT_NAME_1);
    }

    @Test
    public void getAllDependersOfService() throws Exception {
        MicoService service = new MicoService()
                .setShortName(SHORT_NAME)
                .setVersion(VERSION)
                .setName(NAME)
                .setDescription(DESCRIPTION);

        MicoService service1 = new MicoService()
                .setShortName(SHORT_NAME_1)
                .setVersion(VERSION_1_0_1)
                .setName(NAME)
                .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
                .setShortName(SHORT_NAME_2)
                .setVersion(VERSION_1_0_2)
                .setDescription(DESCRIPTION_2);
        MicoService service3 = new MicoService()
                .setShortName(SHORT_NAME_3)
                .setVersion(VERSION_1_0_3)
                .setName(NAME)
                .setDescription(DESCRIPTION_3);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service1).setDependedService(service);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service2).setDependedService(service);
        MicoServiceDependency dependency3 = new MicoServiceDependency().setService(service3).setDependedService(service);

        service1.setDependencies(Collections.singletonList(dependency1));
        service2.setDependencies(Collections.singletonList(dependency2));
        service3.setDependencies(Collections.singletonList(dependency3));

        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(CollectionUtils.listOf(service, service1, service2, service3));
        given(serviceRepository.findByShortNameAndVersion(SHORT_NAME, VERSION)).willReturn(Optional.of(service));

        List<MicoService> dependers = serviceBroker.getDependers(service);

        assertThat(dependers).contains(service1);
        assertThat(dependers).contains(service2);
        assertThat(dependers).contains(service3);
    }

}
