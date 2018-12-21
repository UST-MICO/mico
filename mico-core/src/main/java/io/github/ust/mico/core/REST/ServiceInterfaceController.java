package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.ServiceInterface;
import io.github.ust.mico.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.REST.ServiceController.PATH_VARIABLE_SHORT_NAME;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ServiceInterfaceController {

    private static final String PATH_VARIABLE_SERVICE_INTERFACE_NAME = "serviceInterfaceName";
    private static final String PATH_PART_INTERFACES = "/interfaces";

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + ServiceController.PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES)
    public ResponseEntity<Resources<Resource<ServiceInterface>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                        @PathVariable(ServiceController.PATH_VARIABLE_VERSION) String version) {
        List<ServiceInterface> serviceInterfaces = serviceRepository.findInterfacesOfService(shortName, version);
        List<Resource<ServiceInterface>> serviceInterfaceResources = serviceInterfaces.stream().map(
            serviceInterface -> new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))
        ).collect(Collectors.toList());
        return ResponseEntity.ok(new Resources<>(serviceInterfaceResources, linkTo(methodOn(ServiceInterfaceController.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + ServiceController.PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<ServiceInterface>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(ServiceController.PATH_VARIABLE_VERSION) String version,
                                                                         @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<ServiceInterface> serviceInterfaceOptional = serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
        return serviceInterfaceOptional.map(serviceInterface ->
            new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    private Iterable<Link> getServiceInterfaceLinks(ServiceInterface serviceInterface, String shortName, String version) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceInterfaceController.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceInterfaceController.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        return links;
    }
}
