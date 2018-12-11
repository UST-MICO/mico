package io.github.ust.mico.core.REST;

import io.github.ust.mico.core.Service;
import io.github.ust.mico.core.ServiceInterface;
import io.github.ust.mico.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceController {

    public static final String PATH_VARIABLE_SHORT_NAME = "shortName";
    public static final String PATH_VARIABLE_VERSION = "version";
    public static final String PATH_VARIABLE_ID = "id";
    private static final String PATH_VARIABLE_SERVICE_INTERFACE_NAME = "serviceInterfaceName";
    private static final String PATH_PART_INTERFACES = "/interfaces";

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping()
    public ResponseEntity<Resources<Resource<Service>>> getServiceList() {
        List<Service> services = serviceRepository.findAll();
        List<Resource<Service>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceController.class).getServiceList()).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}")
    //TODO Add validation to path variables
    public ResponseEntity<Resource<Service>> getServiceByShortNameAndVersion(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version) {
        Optional<Service> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        return serviceOpt.map(service -> new Resource<>(service, getServiceLinks(service)))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/")
    public ResponseEntity<Resources<Resource<Service>>> getVersionsOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName) {
        List<Service> services = serviceRepository.findByShortName(shortName);
        List<Resource<Service>> serviceResources = getServiceResourcesList(services);
        return ResponseEntity.ok(
                new Resources<>(serviceResources,
                        linkTo(methodOn(ServiceController.class).getVersionsOfService(shortName)).withSelfRel()));
    }


    //GET| /services/{shortName}/{version}/interface
    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES)
    public ResponseEntity<Resources<Resource<ServiceInterface>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<ServiceInterface> serviceInterfaces = serviceRepository.findInterfacesOfService(shortName, version);
        List<Resource<ServiceInterface>> serviceInterfaceResources = serviceInterfaces.stream().map(
                serviceInterface -> new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))
        ).collect(Collectors.toList());
        return ResponseEntity.ok(new Resources<>(serviceInterfaceResources, linkTo(methodOn(ServiceController.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}" + PATH_PART_INTERFACES + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<ServiceInterface>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                         @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                         @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<ServiceInterface> serviceInterfaceOptional = serviceRepository.findInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
        return serviceInterfaceOptional.map(serviceInterface ->
                new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    //TODO: Ambiguous endpoint with /services/shortName
    //@GetMapping("/{" + PATH_VARIABLE_ID + "}/")
    public ResponseEntity<Resource<Service>> getServiceById(@PathVariable(PATH_VARIABLE_ID) Long id) {
        Optional<Service> serviceOpt = serviceRepository.findById(id);

        return serviceOpt.map(service -> new Resource<>(service, getServiceLinks(service)))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    private List<Resource<Service>> getServiceResourcesList(List<Service> services) {
        return services.stream().map(service -> new Resource<>(service, getServiceLinks(service)))
                .collect(Collectors.toList());
    }

    private Iterable<Link> getServiceInterfaceLinks(ServiceInterface serviceInterface, String shortName, String version) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        return links;
    }

    private Iterable<Link> getServiceLinks(Service service) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceController.class).getServiceByShortNameAndVersion(service.getShortName(), service.getVersion())).withSelfRel());
        links.add(linkTo(methodOn(ServiceController.class).getServiceList()).withRel("services"));
        return links;
    }


}
