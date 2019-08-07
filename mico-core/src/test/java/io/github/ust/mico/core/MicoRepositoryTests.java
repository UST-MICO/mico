package io.github.ust.mico.core;

import org.springframework.beans.factory.annotation.Autowired;

import io.github.ust.mico.core.persistence.*;

public class MicoRepositoryTests {

    @Autowired
    MicoApplicationRepository applicationRepository;

    @Autowired
    MicoServiceRepository serviceRepository;

    @Autowired
    MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    MicoServicePortRepository servicePortRepository;

    @Autowired
    MicoServiceDependencyRepository serviceDependencyRepository;

    @Autowired
    MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

	@Autowired
    KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
    MicoLabelRepository labelRepository;

    @Autowired
    MicoTopicRepository topicRepository;

    @Autowired
    MicoEnvironmentVariableRepository environmentVariableRepository;

    @Autowired
    MicoInterfaceConnectionRepository interfaceConnectionRepository;

    /**
     * Deletes all data in the database.
     */
    void deleteAllData() {
        applicationRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceInterfaceRepository.deleteAll();
        servicePortRepository.deleteAll();
        serviceDependencyRepository.deleteAll();
        serviceDeploymentInfoRepository.deleteAll();
        kubernetesDeploymentInfoRepository.deleteAll();
        labelRepository.deleteAll();
        topicRepository.deleteAll();
        environmentVariableRepository.deleteAll();
        interfaceConnectionRepository.deleteAll();
    }

}
