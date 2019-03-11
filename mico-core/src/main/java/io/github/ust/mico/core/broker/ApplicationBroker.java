package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.dto.MicoApplicationStatusDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.MicoApplicationAlreadyExistsException;
import io.github.ust.mico.core.exception.MicoApplicationIsDeployedException;
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

    public MicoApplication getMicoApplicationById(Long id) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findById(id);
        if (!micoApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(id);
        }
        return micoApplicationOptional.get();
    }

    public List<MicoApplication> getMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);
        if (micoApplicationList.isEmpty()) {
            throw new MicoApplicationNotFoundException(shortName);
        }
        return micoApplicationList;
    }

    public List<MicoApplication> getMicoApplications() throws MicoApplicationNotFoundException {
        List<MicoApplication> micoApplicationList = applicationRepository.findAll(3);
        if (micoApplicationList.isEmpty()) {
            throw new MicoApplicationNotFoundException();
        }
        return micoApplicationList;
    }

    public void deleteMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            throw new MicoApplicationIsDeployedException(shortName, version);
        }
        applicationRepository.delete(micoApplication);
    }

    public void deleteMicoApplicationById(Long id) throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        MicoApplication micoApplication = getMicoApplicationById(id);
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            throw new MicoApplicationIsDeployedException(id);
        }
        applicationRepository.deleteById(id);
    }

    public void deleteMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplicationsByShortName(shortName);
        for (MicoApplication micoApplication : micoApplicationList) {
            if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
                throw new MicoApplicationIsDeployedException(micoApplication.getShortName(), micoApplication.getVersion());
            }
        }
        applicationRepository.deleteAll(micoApplicationList);
    }

    public void deleteMicoApplications() throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplications();
        for (MicoApplication micoApplication : micoApplicationList) {
            if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
                throw new MicoApplicationIsDeployedException(micoApplication.getShortName(), micoApplication.getVersion());
            }
        }
        applicationRepository.deleteAll(micoApplicationList);
    }

    public MicoApplication createMicoApplication(MicoApplication micoApplication) throws MicoApplicationAlreadyExistsException {
        try {
            MicoApplication existingMicoApplication = getMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
        } catch (MicoApplicationNotFoundException e) {
            return applicationRepository.save(micoApplication);
        }
        throw new MicoApplicationAlreadyExistsException(micoApplication.getShortName(), micoApplication.getVersion());
    }

    public MicoApplication updateMicoApplication(MicoApplication micoApplication) throws MicoApplicationNotFoundException {
        MicoApplication existingMicoApplication = getMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
        micoApplication.setId(existingMicoApplication.getId());
        return applicationRepository.save(micoApplication);
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
