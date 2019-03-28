package io.github.ust.mico.core.broker;

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.LoadBalancerStatus;
import io.github.ust.mico.core.exception.MicoServiceInterfaceAlreadyExistsException;
import io.github.ust.mico.core.exception.MicoServiceInterfaceNotFoundException;
import io.github.ust.mico.core.exception.MicoServiceNotFoundException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MicoServiceInterfaceBroker {

    @Autowired
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoServiceBroker micoServiceBroker;

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

    public List<String> getPublicIpsOfInterfaceByInterfaceName(String shortName, String version, String serviceInterfaceName, io.fabric8.kubernetes.api.model.Service kubernetesService) {
        List<String> publicIps = new ArrayList<>();
        LoadBalancerStatus loadBalancer = kubernetesService.getStatus().getLoadBalancer();

        if (loadBalancer != null) {
            List<LoadBalancerIngress> ingressList = loadBalancer.getIngress();
            if (ingressList != null && !ingressList.isEmpty()) {
                log.debug("There is/are {} ingress(es) defined for Kubernetes service '{}' (MicoServiceInterface '{}').",
                        ingressList.size(), kubernetesService.getMetadata().getName(), serviceInterfaceName);
                for (LoadBalancerIngress ingress : ingressList) {
                    publicIps.add(ingress.getIp());
                }
                log.info("Service interface with name '{}' of MicoService '{}' in version '{}' has external IPs: {}",
                        serviceInterfaceName, shortName, version, publicIps);
            }
        }

        return publicIps;
    }

    public void deleteMicoServiceInterface(String shortName, String version, String serviceInterfaceName) {
        serviceInterfaceRepository.deleteByServiceAndName(shortName, version, serviceInterfaceName);
    }

    public MicoServiceInterface persistMicoServiceInterface(MicoService micoService, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceAlreadyExistsException, MicoServiceNotFoundException {
        Optional<MicoServiceInterface> micoServiceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(micoService.getShortName(), micoService.getVersion(), micoServiceInterface.getServiceInterfaceName());

        if (micoServiceInterfaceOptional.isPresent()) {
            throw new MicoServiceInterfaceAlreadyExistsException(micoService.getShortName(), micoService.getVersion(), micoServiceInterface.getServiceInterfaceName());
        }

        micoService.getServiceInterfaces().add(micoServiceInterface);
        micoServiceBroker.updateExistingService(micoService);

        return micoServiceInterface;
    }

    public MicoServiceInterface updateMicoServiceInterface(String shortName, String version, String serviceInterfaceName, MicoServiceInterface micoServiceInterface) throws MicoServiceInterfaceNotFoundException {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(shortName, version, serviceInterfaceName);

        if (!serviceInterfaceOptional.isPresent()) {
            throw new MicoServiceInterfaceNotFoundException(shortName, version, serviceInterfaceName);
        }

        MicoServiceInterface serviceInterface = serviceInterfaceOptional.get();
        MicoServiceInterface updatedServiceInterface = micoServiceInterface.setId(serviceInterface.getId());
        MicoServiceInterface persistedServiceInterface = serviceInterfaceRepository.save(updatedServiceInterface);

        return persistedServiceInterface;
    }

}
