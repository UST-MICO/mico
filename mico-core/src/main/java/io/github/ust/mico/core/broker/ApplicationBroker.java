package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.persistence.MicoApplicationRepository;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationBroker {

    public static final String PATH_APPLICATIONS = "applications";
    public static final String PATH_SERVICES = "services";
    public static final String PATH_DEPLOYMENT_INFORMATION = "deploymentInformation";
    public static final String PATH_PROMOTE = "promote";
    private static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    private static final String PATH_VARIABLE_VERSION = "version";
    private static final String PATH_VARIABLE_SERVICE_SHORT_NAME = "serviceShortName";
    private static final String PATH_VARIABLE_SERVICE_VERSION = "serviceVersion";

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

    public MicoApplication getMicoApplicationByShortNameAndVersion() {
        //TODO: Implementation
    }

    public List<MicoApplication> getMicoApplicationsByShortName() {
        //TODO: Implementation
    }

    public List<MicoApplication> getMicoApplications() {
        //TODO: Implementation
    }

    public MicoApplication getMicoApplicationById() {
        //TODO: Implementation
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
}
