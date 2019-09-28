package io.github.ust.mico.core;

import io.github.ust.mico.core.broker.MicoServiceBroker;
import io.github.ust.mico.core.exception.MicoServiceAlreadyExistsException;
import io.github.ust.mico.core.exception.MicoServiceIsDeployedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDependency;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.TestConstants.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("unit-testing")
public class MicoServiceBrokerTests {

    @MockBean
    private MicoServiceRepository serviceRepository;

    @MockBean
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoServiceBroker micoServiceBroker;

    @Test
    public void getAllServicesAsList() {
        given(serviceRepository.findAll(ArgumentMatchers.anyInt())).willReturn(
            CollectionUtils.listOf(
                new MicoService().setShortName(SHORT_NAME_1).setVersion(VERSION_1_0_1).setName(NAME_1).setDescription(DESCRIPTION_1),
                new MicoService().setShortName(SHORT_NAME_2).setVersion(VERSION_1_0_2).setName(NAME_2).setDescription(DESCRIPTION_2),
                new MicoService().setShortName(SHORT_NAME_3).setVersion(VERSION_1_0_3).setName(NAME_3).setDescription(DESCRIPTION_3)));

        List<MicoService> micoServiceList = micoServiceBroker.getAllServicesAsList();

        assertThat(micoServiceList.get(0).getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(micoServiceList.get(1).getShortName()).isEqualTo(SHORT_NAME_2);
        assertThat(micoServiceList.get(2).getShortName()).isEqualTo(SHORT_NAME_3);
    }

    @Test
    public void getServiceFromDatabase() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);

        given(serviceRepository.findByShortNameAndVersion(service.getShortName(), service.getVersion())).willReturn(Optional.of(service));

        MicoService micoService = micoServiceBroker.getServiceFromDatabase(SHORT_NAME_1, VERSION_1_0_1);
        assertThat(micoService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(micoService.getVersion()).isEqualTo(VERSION_1_0_1);
    }

    @Test
    public void getServiceInstanceFromDatabase() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);

        MicoServiceDeploymentInfo serviceDeploymentInfo = new MicoServiceDeploymentInfo()
            .setInstanceId(INSTANCE_ID)
            .setService(service);

        given(serviceDeploymentInfoRepository.findByInstanceId(serviceDeploymentInfo.getInstanceId())).willReturn(Optional.of(serviceDeploymentInfo));

        MicoServiceDeploymentInfo micoServiceDeploymentInfo = micoServiceBroker.getServiceInstanceFromDatabase(SHORT_NAME_1, VERSION_1_0_1, INSTANCE_ID);
        assertThat(micoServiceDeploymentInfo.getInstanceId()).isEqualTo(INSTANCE_ID);
        assertThat(micoServiceDeploymentInfo.getService()).isEqualTo(service);
    }

    @Test
    public void updateExistingService() throws MicoServiceIsDeployedException {
        MicoService micoServiceTwo = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_1);

        MicoService resultUpdatedService = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);

        given(serviceRepository.findByShortNameAndVersion(resultUpdatedService.getShortName(), resultUpdatedService.getVersion())).willReturn(Optional.of(resultUpdatedService));
        given(serviceRepository.save(any(MicoService.class))).willReturn(resultUpdatedService);

        MicoService updatedService = micoServiceBroker.updateExistingService(micoServiceTwo);

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

        Long id = 1L;

        given(serviceRepository.findById(ArgumentMatchers.anyLong())).willReturn(java.util.Optional.ofNullable(micoServiceOne));

        MicoService micoService = micoServiceBroker.getServiceById(id);

        assertThat(micoService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(micoService.getVersion()).isEqualTo(VERSION_1_0_1);
        assertThat(micoService.getName()).isEqualTo(NAME_1);
        assertThat(micoService.getDescription()).isEqualTo(DESCRIPTION_1);
    }

    @Test
    public void deleteService() throws Exception {
        //TODO: Implementation haha
        MicoService micoServiceOne = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        micoServiceBroker.deleteService(micoServiceOne);
    }

    @Test
    public void persistService() throws Exception {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        given(serviceRepository.save(any(MicoService.class))).willReturn(service);

        MicoService savedService = micoServiceBroker.persistService(service);

        assertThat(savedService.getShortName()).isEqualTo(SHORT_NAME_1);
        assertThat(savedService.getVersion()).isEqualTo(VERSION_1_0_1);
        assertThat(savedService.getName()).isEqualTo(NAME_1);
        assertThat(savedService.getDescription()).isEqualTo(DESCRIPTION_1);
    }

    @Test
    public void deleteAllVersionsOfService() throws Exception {
        MicoService micoServiceOne = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        given(serviceRepository.findByShortName(SHORT_NAME_1)).willReturn(CollectionUtils.listOf(micoServiceOne));

        micoServiceBroker.deleteAllVersionsOfService(SHORT_NAME_1);
    }

    @Test
    public void getAllDependersOfService() {
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

        List<MicoService> dependers = micoServiceBroker.getDependers(service);

        assertThat(dependers).contains(service1);
        assertThat(dependers).contains(service2);
        assertThat(dependers).contains(service3);
    }

    @Test
    public void getAllDependersOfServiceByQuery() {
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

        given(serviceRepository.findDependers(service.getShortName(), service.getVersion())).willReturn(CollectionUtils.listOf(service, service1, service2, service3));

        List<MicoService> dependers = micoServiceBroker.findDependers(service);

        assertThat(dependers).contains(service1);
        assertThat(dependers).contains(service2);
        assertThat(dependers).contains(service3);
    }

    @Test
    public void getDependentServicesOfService() {
        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION);
        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service).setDependedService(service2);
        service.setDependencies(CollectionUtils.listOf(dependency1, dependency2));

        given(serviceRepository.findDependees(SHORT_NAME, VERSION)).willReturn(CollectionUtils.listOf(service1, service2));

        List<MicoService> dependentServices = micoServiceBroker.getDependeesByMicoService(service);

        assertThat(dependentServices).contains(service1);
        assertThat(dependentServices).contains(service2);
    }

    @Test
    public void getDependeesByQuery() {
        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION);
        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service).setDependedService(service2);
        service.setDependencies(CollectionUtils.listOf(dependency1, dependency2));

        given(serviceRepository.findDependees(service.getShortName(), service.getVersion())).willReturn(CollectionUtils.listOf(service1, service2));

        List<MicoService> dependentServices = micoServiceBroker.getDependeesByMicoService(service);

        assertThat(dependentServices).contains(service1);
        assertThat(dependentServices).contains(service2);
    }

    @Test
    public void checkIfDependencyAlreadyExistsAndCheckForTrue() {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION);

        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);

        service.setDependencies(Collections.singletonList(dependency1));

        boolean result = micoServiceBroker.checkIfDependencyAlreadyExists(service, service1);

        assertThat(result).isEqualTo(true);
    }

    @Test
    public void checkIfDependencyAlreadyExistsAndCheckForFalse() {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION);

        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        boolean result = micoServiceBroker.checkIfDependencyAlreadyExists(service, service1);

        assertThat(result).isEqualTo(false);
    }

    @Test
    public void persistNewDependencyBetweenServices() throws MicoServiceIsDeployedException {
        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1)
            .setDependencies(new ArrayList<>());
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);

        MicoServiceDependency dependency = new MicoServiceDependency().setService(service1).setDependedService(service2);

        MicoService expectedService = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1)
            .setDependencies(Collections.singletonList(dependency));

        given(serviceRepository.save(service1)).willReturn(expectedService);

        MicoService updatedService = micoServiceBroker.persistNewDependencyBetweenServices(service1, service2);

        assertThat(updatedService.getDependencies().size()).isEqualTo(1);
        assertThat(updatedService.getDependencies().get(0).getDependedService()).isEqualTo(service2);
    }

    @Test
    public void deleteDependencyBetweenServices() throws MicoServiceIsDeployedException {
        MicoService service1 = new MicoService()
            .setId(1L)
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1)
            .setDependencies(new ArrayList<>());
        MicoService service2 = new MicoService()
            .setId(2L)
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);

        MicoServiceDependency dependency = new MicoServiceDependency().setService(service1).setDependedService(service2);

        service1.getDependencies().add(dependency);

        MicoService expectedService = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);

        given(serviceRepository.save(service1)).willReturn(expectedService);

        MicoService updatedService = micoServiceBroker.deleteDependencyBetweenServices(service1, service2);

        assertThat(updatedService.getDependencies()).doesNotContain(dependency);
    }

    @Test
    public void deleteAllDependees() throws MicoServiceIsDeployedException {
        MicoService service1 = new MicoService()
            .setShortName(SHORT_NAME_1)
            .setVersion(VERSION_1_0_1)
            .setName(NAME_1)
            .setDescription(DESCRIPTION_1);
        MicoService service2 = new MicoService()
            .setShortName(SHORT_NAME_2)
            .setVersion(VERSION_1_0_2)
            .setName(NAME_2)
            .setDescription(DESCRIPTION_2);
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoServiceDependency dependency1 = new MicoServiceDependency().setService(service).setDependedService(service1);
        MicoServiceDependency dependency2 = new MicoServiceDependency().setService(service).setDependedService(service2);

        ArrayList<MicoServiceDependency> micoServiceDependencies = new ArrayList<>();
        micoServiceDependencies.add(dependency1);
        micoServiceDependencies.add(dependency2);

        service.setDependencies(micoServiceDependencies);

        MicoService expectedService = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        given(serviceRepository.save(service)).willReturn(expectedService);

        MicoService updatedService = micoServiceBroker.deleteAllDependees(service);

        assertThat(updatedService.getDependencies()).isEqualTo(Collections.EMPTY_LIST);
    }

    @Test
    public void promoteService() throws MicoServiceAlreadyExistsException {
        MicoService service = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION)
            .setDescription(DESCRIPTION);

        MicoService expectedService = new MicoService()
            .setShortName(SHORT_NAME)
            .setName(NAME)
            .setVersion(VERSION_1_0_1)
            .setDescription(DESCRIPTION);

        given(serviceRepository.save(service)).willReturn(expectedService);

        MicoService updatedService = micoServiceBroker.promoteService(service, VERSION_1_0_1);

        assertThat(updatedService).isEqualTo(expectedService);
    }

}
