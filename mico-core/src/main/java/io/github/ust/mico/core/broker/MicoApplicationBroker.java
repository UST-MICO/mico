package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.dto.response.status.MicoApplicationStatusResponseDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfoQueryResult;
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

/**
 *
 */
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

    /**
     * @param shortName
     * @param version
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public MicoApplication getMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findByShortNameAndVersion(shortName, version);
        if (!micoApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(shortName, version);
        }
        return micoApplicationOptional.get();
    }

    /**
     *
     * @param id
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public MicoApplication getMicoApplicationById(Long id) throws MicoApplicationNotFoundException {
        Optional<MicoApplication> micoApplicationOptional = applicationRepository.findById(id);
        if (!micoApplicationOptional.isPresent()) {
            throw new MicoApplicationNotFoundException(id);
        }
        return micoApplicationOptional.get();
    }

    /**
     *
     * @param shortName
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public List<MicoApplication> getMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException {
        List<MicoApplication> micoApplicationList = applicationRepository.findByShortName(shortName);
        if (micoApplicationList.isEmpty()) {
            throw new MicoApplicationNotFoundException(shortName);
        }
        return micoApplicationList;
    }

    /**
     *
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public List<MicoApplication> getMicoApplications() throws MicoApplicationNotFoundException {
        List<MicoApplication> micoApplicationList = applicationRepository.findAll(3);
        if (micoApplicationList.isEmpty()) {
            throw new MicoApplicationNotFoundException();
        }
        return micoApplicationList;
    }

    /**
     *
     * @param shortName
     * @param version
     * @throws MicoApplicationNotFoundException
     * @throws KubernetesResourceException
     * @throws MicoApplicationIsDeployedException
     */
    public void deleteMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            throw new MicoApplicationIsDeployedException(shortName, version);
        }
        applicationRepository.delete(micoApplication);
    }

    /**
     *
     * @param id
     * @throws MicoApplicationNotFoundException
     * @throws KubernetesResourceException
     * @throws MicoApplicationIsDeployedException
     */
    public void deleteMicoApplicationById(Long id) throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        MicoApplication micoApplication = getMicoApplicationById(id);
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            throw new MicoApplicationIsDeployedException(id);
        }
        applicationRepository.deleteById(id);
    }

    /**
     *
     * @param shortName
     * @throws MicoApplicationNotFoundException
     * @throws KubernetesResourceException
     * @throws MicoApplicationIsDeployedException
     */
    public void deleteMicoApplicationsByShortName(String shortName) throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplicationsByShortName(shortName);
        deleteListOfMicoApplications(micoApplicationList);
    }

    /**
     *
     * @throws MicoApplicationNotFoundException
     * @throws KubernetesResourceException
     * @throws MicoApplicationIsDeployedException
     */
    public void deleteMicoApplications() throws MicoApplicationNotFoundException, KubernetesResourceException, MicoApplicationIsDeployedException {
        List<MicoApplication> micoApplicationList = getMicoApplications();
        deleteListOfMicoApplications(micoApplicationList);
    }

    /**
     *
     * @param micoApplicationList
     * @throws KubernetesResourceException
     * @throws MicoApplicationIsDeployedException
     */
    private void deleteListOfMicoApplications(List<MicoApplication> micoApplicationList) throws KubernetesResourceException, MicoApplicationIsDeployedException {
        for (MicoApplication micoApplication : micoApplicationList) {
            if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
                throw new MicoApplicationIsDeployedException(micoApplication.getShortName(), micoApplication.getVersion());
            }
        }
        applicationRepository.deleteAll(micoApplicationList);
    }

    /**
     *
     * @param micoApplication
     * @return
     * @throws MicoApplicationAlreadyExistsException
     */
    public MicoApplication createMicoApplication(MicoApplication micoApplication) throws MicoApplicationAlreadyExistsException {
        try {
            MicoApplication existingMicoApplication = getMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
        } catch (MicoApplicationNotFoundException e) {
            return applicationRepository.save(micoApplication);
        }
        throw new MicoApplicationAlreadyExistsException(micoApplication.getShortName(), micoApplication.getVersion());
    }

    /**
     *
     * @param micoApplication
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public MicoApplication updateMicoApplication(MicoApplication micoApplication) throws MicoApplicationNotFoundException {
        MicoApplication existingMicoApplication = getMicoApplicationByShortNameAndVersion(micoApplication.getShortName(), micoApplication.getVersion());
        micoApplication.setId(existingMicoApplication.getId());
        return applicationRepository.save(micoApplication);
    }

    /**
     *
     * @param shortName
     * @param version
     * @param newVersion
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public MicoApplication copyAndUpgradeMicoApplicationByShortNameAndVersion(String shortName, String version, String newVersion) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        micoApplication.setVersion(newVersion).setId(null);
        return applicationRepository.save(micoApplication);
    }

    /**
     *
     * @param id
     * @param newVersion
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public MicoApplication copyAndUpgradeMicoApplicationById(Long id, String newVersion) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationById(id);
        micoApplication.setVersion(newVersion).setId(null);
        return applicationRepository.save(micoApplication);
    }

    /**
     *
     * @param shortName
     * @param version
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public List<MicoService> getMicoServicesOfMicoApplicationByShortNameAndVersion(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
    }

    /**
     *
     * @param id
     * @return
     * @throws MicoApplicationNotFoundException
     */
    public List<MicoService> getMicoServicesOfMicoApplicationById(Long id) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationById(id);
        return serviceRepository.findAllByApplication(micoApplication.getShortName(), micoApplication.getVersion());
    }

    /**
     *
     * @param applicationShortName
     * @param applicationVersion
     * @param serviceShortName
     * @param serviceVersion
     * @return
     * @throws MicoApplicationNotFoundException
     * @throws MicoServiceNotFoundException
     * @throws MicoServiceAlreadyAddedToMicoApplicationException
     */
    public MicoApplication addMicoServiceToMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        MicoService micoService = micoServiceBroker.getServiceFromDatabase(serviceShortName, serviceVersion);

        //TODO: update following implementation (make use of getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion)
        if (micoApplication.getServiceDeploymentInfos().stream().noneMatch(sdi -> sdi.getService().equals(micoService))) {
            MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo();
            sdi.setApplication(micoApplication).setService(micoService);
            micoApplication.getServiceDeploymentInfos().add(sdi);
            return applicationRepository.save(micoApplication);
        } else {
            throw new MicoServiceAlreadyAddedToMicoApplicationException(applicationShortName, applicationVersion, serviceShortName, serviceVersion);
        }
    }

    /**
     *
     * @param applicationId
     * @param serviceId
     * @return
     * @throws MicoApplicationNotFoundException
     * @throws MicoServiceNotFoundException
     * @throws MicoServiceAlreadyAddedToMicoApplicationException
     */
    public MicoApplication addMicoServiceToMicoApplicationById(Long applicationId, Long serviceId) throws MicoApplicationNotFoundException, MicoServiceNotFoundException, MicoServiceAlreadyAddedToMicoApplicationException {
        MicoApplication micoApplication = getMicoApplicationById(applicationId);
        MicoService micoService = micoServiceBroker.getServiceById(serviceId);

        //TODO: update following implementation (make use of getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion)
        if (micoApplication.getServiceDeploymentInfos().stream().noneMatch(sdi -> sdi.getService().equals(micoService))) {
            MicoServiceDeploymentInfo sdi = new MicoServiceDeploymentInfo();
            sdi.setApplication(micoApplication).setService(micoService);
            micoApplication.getServiceDeploymentInfos().add(sdi);
            return applicationRepository.save(micoApplication);
        } else {
            throw new MicoServiceAlreadyAddedToMicoApplicationException(applicationId, serviceId);
        }
    }

    /**
     *
     * @param applicationShortName
     * @param applicationVersion
     * @param serviceShortName
     * @return
     * @throws MicoServiceDeploymentInformationNotFoundException
     */
    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException {
        Optional<MicoServiceDeploymentInfoQueryResult> micoServiceDeploymentInfoQueryResultOptional = serviceDeploymentInfoRepository.findByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
        if (micoServiceDeploymentInfoQueryResultOptional.isPresent()) {
            return micoServiceDeploymentInfoQueryResultOptional.get().getServiceDeploymentInfo();
        } else {
            throw new MicoServiceDeploymentInformationNotFoundException(applicationShortName, applicationVersion, serviceShortName);
        }
    }

    /**
     *
     * @param applicationShortName
     * @param applicationVersion
     * @param serviceShortName
     * @param serviceVersion
     * @return
     * @throws MicoServiceDeploymentInformationNotFoundException
     */
    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoServiceDeploymentInformationNotFoundException {
        Optional<MicoServiceDeploymentInfoQueryResult> micoServiceDeploymentInfoQueryResultOptional = serviceDeploymentInfoRepository.findByApplicationAndService(applicationShortName, applicationVersion, serviceShortName, serviceVersion);
        if (micoServiceDeploymentInfoQueryResultOptional.isPresent()) {
            return micoServiceDeploymentInfoQueryResultOptional.get().getServiceDeploymentInfo();
        } else {
            throw new MicoServiceDeploymentInformationNotFoundException(applicationShortName, applicationVersion, serviceShortName);
        }
    }

    /**
     *
     * @param applicationShortName
     * @param applicationVersion
     * @param serviceShortName
     * @return
     * @throws MicoServiceDeploymentInformationNotFoundException
     */
    public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion, serviceShortName);
        MicoApplication micoApplication = micoServiceDeploymentInfo.getApplication();
        micoApplication.getServiceDeploymentInfos().remove(micoServiceDeploymentInfo);

        // TODO: Update Kubernetes deployment
        return applicationRepository.save(micoApplication);
    }

    /**
     *
     * @param applicationShortName
     * @param applicationVersion
     * @param serviceShortName
     * @param serviceVersion
     * @return
     * @throws MicoServiceDeploymentInformationNotFoundException
     */
    public MicoApplication removeMicoServiceFromMicoApplicationByShortNameAndVersion(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion) throws MicoServiceDeploymentInformationNotFoundException {
        MicoServiceDeploymentInfo micoServiceDeploymentInfo = getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion, serviceShortName, serviceVersion);
        MicoApplication micoApplication = micoServiceDeploymentInfo.getApplication();
        micoApplication.getServiceDeploymentInfos().remove(micoServiceDeploymentInfo);

        // TODO: Update Kubernetes deployment
        return applicationRepository.save(micoApplication);
    }

    /**
     *
     * @param applicationShortName
     * @param applicationVersion
     * @param serviceShortName
     * @param serviceVersion
     * @param micoServiceDeploymentInfo
     * @return
     * @throws MicoServiceDeploymentInformationNotFoundException
     */
    public MicoApplication updateMicoServiceDeploymentInformationOfMicoApplication(String applicationShortName, String applicationVersion, String serviceShortName, String serviceVersion, MicoServiceDeploymentInfo micoServiceDeploymentInfo) throws MicoServiceDeploymentInformationNotFoundException {
        MicoServiceDeploymentInfo existingMicoServiceDeploymentInfo = getMicoServiceDeploymentInformationOfMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion, serviceShortName, serviceVersion);
        MicoApplication micoApplication = existingMicoServiceDeploymentInfo.getApplication();

        micoServiceDeploymentInfo.setId(existingMicoServiceDeploymentInfo.getId());
        micoApplication.getServiceDeploymentInfos().remove(existingMicoServiceDeploymentInfo);
        micoApplication.getServiceDeploymentInfos().add(micoServiceDeploymentInfo);

        // TODO: Update actual Kubernetes deployment (see issue mico#416).
        return applicationRepository.save(micoApplication);
    }

    /**
     *
     * @param shortName
     * @param version
     * @return
     * @throws MicoApplicationNotFoundException
     */
    //TODO: Change return value to MicoApplicationStatus
    public MicoApplicationStatusResponseDTO getMicoApplicationStatusOfMicoApplication(String shortName, String version) throws MicoApplicationNotFoundException {
        MicoApplication micoApplication = getMicoApplicationByShortNameAndVersion(shortName, version);
        return micoStatusService.getApplicationStatus(micoApplication);
    }
}
