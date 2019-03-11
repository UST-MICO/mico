package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ApplicationBroker {

    @Autowired
    private ServiceBroker serviceBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository; //TODO: remove?

    @Autowired
    private MicoServiceRepository serviceRepository; //TODO: remove?

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository; //TODO: remove?

    @Autowired
    private MicoKubernetesClient micoKubernetesClient; //TODO: remove?

    @Autowired
    private MicoStatusService micoStatusService; //TODO: remove?

    public MicoApplication getMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!micoApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(shortName, version);
        }
        return micoApplicationOptional.get();
    }

    public List<MicoApplication> getMicoApplicationsByShortName() {
        //TODO: Implementation
    }

    public List<MicoApplication> getMicoApplications() {
        //TODO: Implementation
    }

    public MicoApplication getMicoApplicationById() {

    }

    public void deleteMicoApplicationByShortNameAndVersion() {
        //TODO: Implementation
    }

    public void deleteMicoApplicationById() {
        //TODO: Implementation
    }

    public void deleteMicoApplicationsByShortName() {
        //TODO: Implementation
    }

    public MicoApplication createMicoApplication() {
        //TODO: Implementation
    }

    public MicoApplication updateMicoApplication() {
        //TODO: Implementation
    }

    public MicoApplication copyAndUpgradeMicoApplicationByShortNameAndVersion() {
        //TODO: Implementation
    }

    public MicoApplication copyAndUpgradeMicoApplicationById() {
        //TODO: Implementation
    }

    public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion() {
        //TODO: Implementation
    }

    public List<MicoService> getMicoServicesOfMicoApplicationById() {
        //TODO: Implementation
    }

    public MicoApplication addMicoServiceToMicoApplication() {
        //TODO: Implementation
    }

    public MicoApplication removeMicoServiceFromMicoApplication() {
        //TODO: Implementation
    }

    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformationOfMicoApplication() {
        //TODO: Implementation
    }

    public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformationOfMicoApplication() {
        //TODO: Implementation
    }

    //TODO: change return value?
    public MicoApplicationStatusDTO getMicoApplicationStatusOfMicoApplication() {
        //TODO: Implementation
    }


}
