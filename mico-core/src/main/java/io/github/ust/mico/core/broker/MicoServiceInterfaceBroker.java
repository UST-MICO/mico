package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.exception.MicoServiceInterfaceAlreadyExistsException;
import io.github.ust.mico.core.exception.MicoServiceInterfaceNotFoundException;
import io.github.ust.mico.core.exception.MicoServiceIsDeployedException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MicoServiceInterfaceBroker {

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoServiceBroker micoServiceBroker;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    public List<MicoServiceInterface> getInterfacesOfService(String shortName, String version) {
        return serviceInterfaceRepository.findByService(shortName, version);
    }

    public MicoServiceInterface getInterfaceOfServiceByName(String shortName, String version, String interfaceName) throws MicoServiceInterfaceNotFoundException {
        Optional<MicoServiceInterface> micoServiceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(shortName, version, interfaceName);

        if (!micoServiceInterfaceOptional.isPresent()) {
            throw new MicoServiceInterfaceNotFoundException(shortName, version, interfaceName);
        }

        return micoServiceInterfaceOptional.get();
    }

    public void deleteMicoServiceInterface(MicoService micoService, String serviceInterfaceName) throws MicoServiceIsDeployedException {
        // Check whether service is currently deployed, i.e., it's not allowed to delete an interface
        if (micoKubernetesClient.isMicoServiceDeployed(micoService)) {
            throw new MicoServiceIsDeployedException(micoService.getShortName(), micoService.getVersion());
        }
        serviceInterfaceRepository.deleteByServiceAndName(micoService.getShortName(), micoService.getVersion(), serviceInterfaceName);
    }

    public MicoServiceInterface persistMicoServiceInterface(MicoService micoService, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceAlreadyExistsException, MicoServiceIsDeployedException {

        Optional<MicoServiceInterface> micoServiceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), micoServiceInterface.getServiceInterfaceName());
        if (micoServiceInterfaceOptional.isPresent()) {
            throw new MicoServiceInterfaceAlreadyExistsException(micoService.getShortName(), micoService.getVersion(), micoServiceInterface.getServiceInterfaceName());
        }
        // Check whether service is currently deployed, i.e., it's not allowed to add or update an interface
        if (micoKubernetesClient.isMicoServiceDeployed(micoService)) {
            throw new MicoServiceIsDeployedException(micoService.getShortName(), micoService.getVersion());
        }

        micoService.getServiceInterfaces().add(micoServiceInterface);
        micoServiceBroker.updateExistingService(micoService);

        return micoServiceInterface;
    }

    public MicoServiceInterface updateMicoServiceInterface(MicoService micoService, String serviceInterfaceName, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceNotFoundException, MicoServiceIsDeployedException {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(
            micoService.getShortName(), micoService.getVersion(), serviceInterfaceName);

        if (!serviceInterfaceOptional.isPresent()) {
            throw new MicoServiceInterfaceNotFoundException(micoService.getShortName(), micoService.getVersion(), serviceInterfaceName);
        }
        // Check whether service is currently deployed, i.e., it's not allowed to add or update an interface
        if (micoKubernetesClient.isMicoServiceDeployed(micoService)) {
            throw new MicoServiceIsDeployedException(micoService.getShortName(), micoService.getVersion());
        }

        MicoServiceInterface serviceInterface = serviceInterfaceOptional.get();
        MicoServiceInterface updatedServiceInterface = micoServiceInterface.setId(serviceInterface.getId());

        return serviceInterfaceRepository.save(updatedServiceInterface);
    }

}
