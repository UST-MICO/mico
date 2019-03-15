/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.resource;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.validation.Valid;

import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import io.github.ust.mico.core.service.MicoStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static io.github.ust.mico.core.resource.ServiceResource.PATH_VARIABLE_SHORT_NAME;
import static io.github.ust.mico.core.resource.ServiceResource.PATH_VARIABLE_VERSION;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = "/services", produces = MediaTypes.HAL_JSON_VALUE)
public class ServiceInterfaceResource {

    private static final String PATH_VARIABLE_SERVICE_INTERFACE_NAME = "serviceInterfaceName";
    private static final String PATH_PART_INTERFACES = "interfaces";
    private static final String PATH_PART_PUBLIC_IP = "publicIP";
    private static final String SERVICE_INTERFACE_PATH = "/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_PART_INTERFACES;
    private static final String SERVICE_INTERFACE_PUBLIC_IP_PATH = SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}/" + PATH_PART_PUBLIC_IP;

    @Autowired
    private MicoServiceRepository serviceRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @GetMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resources<Resource<MicoServiceInterface>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                            @PathVariable(PATH_VARIABLE_VERSION) String version) {
        // Use service to get the fully mapped interface objects from the ogm
        Optional<List<Resource<MicoServiceInterface>>> interfacesOpt =
            serviceRepository.findByShortNameAndVersion(shortName, version)
                .map(MicoService::getServiceInterfaces)
                .map(interfaces -> interfaces.stream()
                    .map(serviceInterface -> new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version)))
                    .collect(Collectors.toList()));
        if (!interfacesOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service interface '" + shortName + "' '" + version + "' was not found!");
        }
        return ResponseEntity.ok(new Resources<>(interfacesOpt.get(), linkTo(methodOn(ServiceInterfaceResource.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<MicoServiceInterface>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                             @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceRepository.findByShortNameAndVersion(shortName, version).flatMap(service -> {
            // Use service to get the fully mapped interface objects from the ogm
            if (service.getServiceInterfaces() == null) {
                return Optional.empty();
            }
            return service.getServiceInterfaces().stream().filter(serviceInterface ->
                serviceInterface.getServiceInterfaceName().equals(serviceInterfaceName
                )).findFirst();
        });
        return serviceInterfaceOptional.map(serviceInterface ->
            new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version))).map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service interface '" + shortName + "' '" + version + "' links not found!"));
    }

    // TODO extract logic to MicoStatusService
    @GetMapping(SERVICE_INTERFACE_PUBLIC_IP_PATH)
    public ResponseEntity<String> getInterfacePublicIpByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                             @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        MicoService micoService = getServiceFromDatabase(shortName, version);
        String publicIp = micoStatusService.getPublicIpOfKubernetesService(micoService, serviceInterfaceName);
        // TODO error handling ResourceException

        return ResponseEntity.ok().body(publicIp);
    }

    @DeleteMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Void> deleteServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                       @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        serviceRepository.deleteInterfaceOfServiceByName(serviceInterfaceName, shortName, version);
        return ResponseEntity.noContent().build();
    }

    /**
     * This is not transactional. At the moment we have only one user. If this changes transactional support is a must.
     * FIXME Add transactional support
     *
     * @param shortName        the name of the MICO service
     * @param version          the version of the MICO service
     * @param serviceInterface the name of the MICO service interface
     * @return the created MICO service interface
     */
    @PostMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resource<MicoServiceInterface>> createServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                 @Valid @RequestBody MicoServiceInterface serviceInterface) {

        MicoService service = getServiceFromDatabase(shortName, version);
        if (serviceInterfaceExists(serviceInterface, service)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An interface with the name '" + serviceInterface.getServiceInterfaceName() +
                "' is already associated with the service '" + shortName + "' '" + version + "'.");
        }

        service.getServiceInterfaces().add(serviceInterface);
        serviceRepository.save(service);
        return ResponseEntity.created(
            linkTo(methodOn(ServiceInterfaceResource.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).toUri()).body(new Resource<>(serviceInterface, getServiceInterfaceLinks(serviceInterface, shortName, version)));
    }

    /**
     * Updates an existing micoServiceInterface
     */
    @PutMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<MicoServiceInterface>> updateMicoServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                     @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                     @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName,
                                                                                     @Valid @RequestBody MicoServiceInterface modifiedMicoServiceInterface) {

        if (!modifiedMicoServiceInterface.getServiceInterfaceName().equals(serviceInterfaceName)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The variable '" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "' must be equal to the name specified in the request body");
        }

        MicoService service = getServiceFromDatabase(shortName, version);
        Optional<MicoServiceInterface> micoServiceInterfaceOptional = service.getServiceInterfaces().stream().filter(getMicoServiceInterfaceNameMatchingPredicate(serviceInterfaceName)).findFirst();
        if (!micoServiceInterfaceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MicoServiceInterface was not found!");
        }

        log.debug("Remove old micoServiceInterface");
        service.getServiceInterfaces().removeIf(getMicoServiceInterfaceNameMatchingPredicate(serviceInterfaceName));
        MicoServiceInterface micoServiceInterface = micoServiceInterfaceOptional.get();
        modifiedMicoServiceInterface.setId(micoServiceInterface.getId());
        log.debug("Add new version of micoServiceInterface");
        service.getServiceInterfaces().add(modifiedMicoServiceInterface);
        serviceRepository.save(service);
        return ResponseEntity.ok(new Resource<>(modifiedMicoServiceInterface, getServiceInterfaceLinks(modifiedMicoServiceInterface, shortName, version)));
    }

    /**
     * Returns the existing {@link MicoService} object from the database for the given shortName and version.
     *
     * @param shortName the short name of a {@link MicoService}
     * @param version   the version of a {@link MicoService}
     * @return the existing {@link MicoService} from the database
     * @throws ResponseStatusException if a {@link MicoService} for the given shortName and version does not exist
     */
    private MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> serviceOpt = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' was not found!");
        }
        return serviceOpt.get();
    }

    /**
     * Checks if a micoServiceInterface exists for a given micoService. The matching is based on the interface name.
     */
    private boolean serviceInterfaceExists(MicoServiceInterface serviceInterface, MicoService service) {
        if (service.getServiceInterfaces() == null) {
            return false;
        }
        return service.getServiceInterfaces().stream().anyMatch(getMicoServiceInterfaceNameMatchingPredicate(serviceInterface.getServiceInterfaceName()));
    }

    /**
     * Generates a predicate which matches the given micoServiceInterfaceName.
     */
    private Predicate<MicoServiceInterface> getMicoServiceInterfaceNameMatchingPredicate(String micoServiceInterfaceName) {
        return existingServiceInterface -> existingServiceInterface.getServiceInterfaceName().equals(micoServiceInterfaceName);
    }

    private Iterable<Link> getServiceInterfaceLinks(MicoServiceInterface serviceInterface, String shortName, String version) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceInterfaceResource.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceInterfaceResource.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        links.add(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(shortName, version)).withRel("service"));
        return links;
    }
}
