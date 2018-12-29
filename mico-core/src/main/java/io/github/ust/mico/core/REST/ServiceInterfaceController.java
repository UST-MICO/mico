package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.Service;
import io.github.ust.mico.core.ServiceInterface;
import io.github.ust.mico.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.REST.ServiceController.PATH_VARIABLE_SHORT_NAME;
import static io.github.ust.mico.core.REST.ServiceController.PATH_VARIABLE_VERSION;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceInterfaceController {

    private static final String PATH_VARIABLE_SERVICE_INTERFACE_NAME = "serviceInterfaceName";
    private static final String PATH_PART_INTERFACES = "/interfaces";
    public static final String SERVICE_INTERFACE_PATH = "/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES + "/";

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resources<Resource<ServiceInterface>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<ServiceInterface> serviceInterfaces = serviceRepository.findInterfacesOfService(shortName, version);
        List<Resource<ServiceInterface>> serviceInterfaceResources = serviceInterfaces.stream().map(
            serviceInterface -> new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))
        ).collect(Collectors.toList());
        return ResponseEntity.ok(new Resources<>(serviceInterfaceResources, linkTo(methodOn(ServiceInterfaceController.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping(SERVICE_INTERFACE_PATH + "{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<ServiceInterface>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                         @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<ServiceInterface> serviceInterfaceOptional = serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
        return serviceInterfaceOptional.map(serviceInterface ->
            new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<Resource<ServiceInterface>> createServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                             @RequestBody ServiceInterface serviceInterface) {
        Optional<Service> serviceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (serviceOptional.isPresent()) {
            Service service = serviceOptional.get();
            if (!serviceInterfaceExists(serviceInterface, service)) {
                List<ServiceInterface> serviceInterfaceList = service.getServiceInterfaces();
                serviceInterfaceList.add(serviceInterface);
                serviceRepository.save(service);
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

    private boolean serviceInterfaceExists(@RequestBody ServiceInterface serviceInterface, Service service) {
        return service.getServiceInterfaces().stream().anyMatch(existingServiceInterface -> existingServiceInterface.getServiceInterfaceName().equals(serviceInterface.getServiceInterfaceName()));
    }

    private Iterable<Link> getServiceInterfaceLinks(ServiceInterface serviceInterface, String shortName, String version) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceInterfaceController.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceInterfaceController.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(shortName, version)).withRel("service"));
        return links;
    }
}
