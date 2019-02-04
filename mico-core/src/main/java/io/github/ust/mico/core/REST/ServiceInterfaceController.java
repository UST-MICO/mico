package io.github.ust.mico.core.REST;

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.LoadBalancerStatus;
import io.fabric8.kubernetes.api.model.Service;
import io.github.ust.mico.core.ClusterAwarenessFabric8;
import io.github.ust.mico.core.MicoKubernetesConfig;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.REST.ServiceController.PATH_VARIABLE_SHORT_NAME;
import static io.github.ust.mico.core.REST.ServiceController.PATH_VARIABLE_VERSION;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceInterfaceController {

    private static final String PATH_VARIABLE_SERVICE_INTERFACE_NAME = "serviceInterfaceName";
    private static final String PATH_PART_INTERFACES = "interfaces";
    private static final String PATH_PART_PUBLIC_IP = "publicIP";
    private static final String SERVICE_INTERFACE_PATH = "/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PART_INTERFACES;
    private static final String SERVICE_INTERFACE_PUBLIC_IP_PATH = SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}/" + PATH_PART_PUBLIC_IP;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private ClusterAwarenessFabric8 cluster;

    @Autowired
    private MicoKubernetesConfig kubernetesConfig;

    @GetMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resources<Resource<MicoServiceInterface>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                            @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<List<Resource<MicoServiceInterface>>> interfacesOpt = serviceRepository.findByShortNameAndVersion(shortName, version).map(service -> {
            // Use service to get the fully mapped interface objects from the ogm
            return service.getServiceInterfaces();
        }).map(interfaces -> {
            return interfaces.stream().map(serviceInterface -> {
                return new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version));
            }).collect(Collectors.toList());
        });
        if (!interfacesOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ok(new Resources<>(interfacesOpt.get(), linkTo(methodOn(ServiceInterfaceController.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<MicoServiceInterface>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                             @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceRepository.findByShortNameAndVersion(shortName, version).flatMap(service -> {
            // Use service to get the fully mapped interface objects from the ogm
            if (service.getServiceInterfaces() == null) {
                return Optional.of(null);
            }
            return service.getServiceInterfaces().stream().filter(serviceInterface ->
                serviceInterface.getServiceInterfaceName().equals(serviceInterfaceName
            )).findFirst();
        });
        return serviceInterfaceOptional.map(serviceInterface ->
            new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(SERVICE_INTERFACE_PUBLIC_IP_PATH)
    public ResponseEntity<List<String>> getInterfacePublicIpByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                   @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                   @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
        if (!serviceInterfaceOptional.isPresent()) {
            log.debug("Service interface with name '{}' of MicoService '{}' in version '{}' not found",
                serviceInterfaceName, shortName, version);
            return ResponseEntity.notFound().build();
        }

        // TODO Change naming of Kubernetes service
        Service service = cluster.getService(serviceInterfaceName, kubernetesConfig.getNamespaceMicoWorkspace());
        if (service == null) {
            log.debug("Kubernetes service with name '{}' in namespace '{}' not found",
                serviceInterfaceName, kubernetesConfig.getNamespaceMicoWorkspace());
            return ResponseEntity.notFound().build();
        }

        List<String> publicIps = new ArrayList<>();
        LoadBalancerStatus loadBalancer = service.getStatus().getLoadBalancer();
        if(loadBalancer != null) {
            List<LoadBalancerIngress> ingressList = loadBalancer.getIngress();
            if(ingressList != null && !ingressList.isEmpty()) {
                log.debug("There is/are {} ingress(es) defined for MicoServiceInterface '{}'.",
                ingressList.size(), serviceInterfaceName);
                for(LoadBalancerIngress ingress : ingressList) {
                    publicIps.add(ingress.getIp());
                }
                log.info("Service interface with name '{}' of MicoService '{}' in version '{}' has external IPs: {}",
                    serviceInterfaceName, shortName, version, publicIps);
            }
        }

        return ok().body(new ArrayList<>(publicIps));
    }

    @DeleteMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public void deleteServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                       @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        serviceRepository.deleteInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
    }


    /**
     * This is not transactional. At the moment we have only one user. If this changes transactional support
     * is a must. FIXME Add transactional support
     *
     * @param shortName
     * @param version
     * @param serviceInterface
     * @return
     */
    @PostMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resource<MicoServiceInterface>> createServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                 @RequestBody MicoServiceInterface serviceInterface) {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (serviceOptional.isPresent()) {
            MicoService service = serviceOptional.get();
            if (!serviceInterfaceExists(serviceInterface, service)) {
                MicoService serviceWithInterface = service.toBuilder().serviceInterface(serviceInterface).build();
                serviceRepository.save(serviceWithInterface);
                return ResponseEntity.created(
                    linkTo(methodOn(ServiceInterfaceController.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).toUri()).body(new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version)));
            }
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An interface with this name is already associated with this service.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean serviceInterfaceExists(@RequestBody MicoServiceInterface serviceInterface, MicoService service) {
        if (service.getServiceInterfaces() == null) {
            return false;
        }
        return service.getServiceInterfaces().stream().anyMatch(existingServiceInterface -> existingServiceInterface.getServiceInterfaceName().equals(serviceInterface.getServiceInterfaceName()));
    }

    private Iterable<Link> getServiceInterfaceLinks(MicoServiceInterface serviceInterface, String shortName, String version) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceInterfaceController.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceInterfaceController.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(shortName, version)).withRel("service"));
        return links;
    }

}
