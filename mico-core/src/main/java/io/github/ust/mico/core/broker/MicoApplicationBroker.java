package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MicoApplicationBroker {

    @Autowired
    private MicoServiceBroker micoServiceBroker;

    @Autowired
    private MicoApplicationRepository applicationRepository;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @Autowired
    private MicoLabelRepository micoLabelRepository;

    @Autowired
    private MicoEnvironmentVariableRepository micoEnvironmentVariableRepository;

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
        deleteListOfMicoApplications(micoApplicationList);
    }

    public void deleteMicoApplications() throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplications();
        deleteListOfMicoApplications(micoApplicationList);
    }

    private void deleteListOfMicoApplications(List<MicoApplication> micoApplicationList) throws KubernetesResourceException, MicoApplicationIsDeployedException {
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

    public MicoApplication copyAndUpgradeMicoApplicationByShortNameAndVersion(String shortName, String version, String newVersion) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        micoApplication.setVersion(newVersion).setId(null);
        return applicationRepository.save(micoApplication);
    }

    public MicoApplication copyAndUpgradeMicoApplicationById(Long id, String newVersion) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationById(id);
        micoApplication.setVersion(newVersion).setId(null);
        return applicationRepository.save(micoApplication);
    }

    public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
    }

    public List<MicoService> getMicoServicesOfMicoApplicationById(Long id) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationById(id);
        return serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
    }

    public MicoApplication addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        MicoService micoService = micoServiceBroker.getServiceFromDatabase(serviceShortName, serviceVersion);

        return addMicoServiceToMicoApplication(micoApplication, micoService);
    }

    public MicoApplication addMicoServiceToMicoApplicationById(Long applicationId, Long serviceId) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException {
        MicoApplication micoApplication = getMicoApplicationById(applicationId);
        MicoService micoService = micoServiceBroker.getServiceById(serviceId);

        return addMicoServiceToMicoApplication(micoApplication, micoService);
    }

    private MicoApplication addMicoServiceToMicoApplication(MicoApplication micoApplication, MicoService micoService) throws MicoServiceAlreadyAddedToMicoApplicationException {
        if (micoApplication.getServices().contains(micoService)) {
            throw new MicoServiceAlreadyAddedToMicoApplicationException(micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName(), micoService.getVersion());
        } else {
            MicoServiceDeploymentInfo micoServiceDeploymentInfo = new MicoServiceDeploymentInfo().setService(micoService);
            micoApplication.getServices().add(micoService);
            micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);
            return applicationRepository.save(micoApplication);
        }
    }

    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException, MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

        Optional<MicoServiceDeploymentInfo> micoServiceDeploymentInfoOptional = serviceDeploymentInfoRepository.findByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
        if (micoServiceDeploymentInfoOptional.isPresent()) {
            return micoServiceDeploymentInfoOptional.get();
        } else {
            throw new MicoServiceDeploymentInformationNotFoundException(applicationShortName, applicationVersion, serviceShortName);
        }
    }

    public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        MicoApplication micoApplication = checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

        micoApplication.getServices().removeIf(service -> service.getShortName().equals(serviceShortName));
        serviceDeploymentInfoRepository.deleteByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);

        // TODO: Update Kubernetes deployment
        return applicationRepository.save(micoApplication);
    }

    public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName, MicoServiceDeploymentInfo micoServiceDeploymentInfo) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException, MicoServiceDeploymentInformationNotFoundException {
        checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

        MicoServiceDeploymentInfo existingMicoServiceDeploymentInfo = getMicoServiceDeploymentInformation(applicationShortName, applicationVersion, serviceShortName);
        micoServiceDeploymentInfo.setId(existingMicoServiceDeploymentInfo.getId());

        micoLabelRepository.cleanUp();
        micoEnvironmentVariableRepository.cleanUp();

        // TODO: Update actual Kubernetes deployment (see issue mico#416).
        return serviceDeploymentInfoRepository.save(micoServiceDeploymentInfo);
    }

    private MicoApplication checkForMicoServiceInMicoApplication(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);

        if (micoApplication.getServices().stream().noneMatch(service -> service.getShortName().equals(serviceShortName))) {
            throw new MicoApplicationDoesNotIncludeMicoServiceException(applicationShortName, applicationVersion, serviceShortName);
        }
        return micoApplication;
    }

    //TODO: Change return value to MicoApplicationStatus
    public MicoApplicationStatusResponseDTO getMicoApplicationStatusOfMicoApplication(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return micoStatusService.getApplicationStatus(micoApplication);
    }
}
