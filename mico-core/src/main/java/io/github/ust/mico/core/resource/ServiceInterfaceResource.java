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

import io.fabric8.kubernetes.api.model.LoadBalancerIngress;
import io.fabric8.kubernetes.api.model.LoadBalancerStatus;
import io.fabric8.kubernetes.api.model.Service;
import io.github.ust.mico.core.broker.MicoServiceBroker;
import io.github.ust.mico.core.broker.MicoServiceInterfaceBroker;
import io.github.ust.mico.core.dto.request.MicoServiceInterfaceRequestDTO;
import io.github.ust.mico.core.dto.response.MicoServiceInterfaceResponseDTO;
import io.github.ust.mico.core.exception.KubernetesResourceException;
import io.github.ust.mico.core.exception.MicoServiceInterfaceNotFoundException;
import io.github.ust.mico.core.exception.MicoServiceNotFoundException;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
import io.github.ust.mico.core.persistence.MicoServiceRepository;
import io.github.ust.mico.core.service.MicoKubernetesClient;
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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private MicoServiceInterfaceRepository serviceInterfaceRepository; //TODO: Remove

    @Autowired
    private MicoServiceInterfaceBroker micoServiceInterfaceBroker;

    @Autowired
    private MicoServiceBroker micoServiceBroker;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @GetMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resources<Resource<MicoServiceInterfaceResponseDTO>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
        List<MicoServiceInterface> serviceInterfaces = micoServiceInterfaceBroker.getInterfacesOfService(shortName, version);

        List<Resource<MicoServiceInterfaceResponseDTO>> serviceInterfaceResources =
                getServiceInterfaceResponseDTOResourcesList(shortName, version, serviceInterfaces);
        return ResponseEntity.ok(new Resources<>(serviceInterfaceResources,
                linkTo(methodOn(ServiceInterfaceResource.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                        @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                        @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        MicoServiceInterface serviceInterface = getServiceInterfaceFromServiceInterfaceBroker(shortName, version, serviceInterfaceName);

        return ResponseEntity.ok(getServiceInterfaceResponseDTOResource(shortName, version, serviceInterface));
    }

    @GetMapping(SERVICE_INTERFACE_PUBLIC_IP_PATH)
    public ResponseEntity<List<String>> getInterfacePublicIpByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                   @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                   @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        MicoService micoService = getServiceFromServiceBroker(shortName, version);

        MicoServiceInterface micoServiceInterface = getServiceInterfaceFromServiceInterfaceBroker(shortName, version, serviceInterfaceName);

        Optional<Service> kubernetesServiceOptional;
        try {
            kubernetesServiceOptional = micoKubernetesClient.getInterfaceByNameOfMicoService(micoService, micoServiceInterface.getServiceInterfaceName());
        } catch (KubernetesResourceException e) {
            log.error("Error occur while retrieving Kubernetes service of MicoServiceInterface '{}' of MicoService '{}' in version '{}'. Caused by: {}",
                    serviceInterfaceName, shortName, version, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occur while retrieving Kubernetes service of MicoServiceInterface '" + serviceInterfaceName +
                            "' of MicoService '" + shortName + "' '" + version + "'!");
        }
        if (!kubernetesServiceOptional.isPresent()) {
            log.warn("There is no MicoServiceInterface deployed with name '{}' of MicoService '{}' in version '{}'.",
                    serviceInterfaceName, shortName, version);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No deployed service interface '" + serviceInterfaceName + "' of MicoService '" + shortName + "' '" + version + "' was found!");
        }

        Service kubernetesService = kubernetesServiceOptional.get();

        List<String> publicIps = micoServiceInterfaceBroker.getPublicIpsOfInterfaceByInterfaceName(shortName, version, serviceInterfaceName, kubernetesService);

        return ResponseEntity.ok().body(new ArrayList<>(publicIps));
    }

    @DeleteMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Void> deleteServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                       @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                       @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
        serviceInterfaceRepository.deleteByServiceAndName(shortName, version, serviceInterfaceName);
        return ResponseEntity.noContent().build();
    }

    /**
     * This is not transactional. At the moment we have only one user. If this changes transactional support
     * is a must. FIXME Add transactional support
     *
     * @param shortName                  the name of the MICO service
     * @param version                    the version of the MICO service
     * @param serviceInterfaceRequestDto the {@link MicoServiceInterfaceRequestDTO}
     * @return the created MICO service interface
     */
    @PostMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> createServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                            @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                            @Valid @RequestBody MicoServiceInterfaceRequestDTO serviceInterfaceRequestDto) {
        MicoService service = getServiceFromServiceBroker(shortName, version);
        if (serviceInterfaceRepository.findByServiceAndName(shortName, version, serviceInterfaceRequestDto.getServiceInterfaceName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An interface with the name '" + serviceInterfaceRequestDto.getServiceInterfaceName() +
                    "' is already associated with the service '" + shortName + "' '" + version + "'.");
        }

        MicoServiceInterface serviceInterface = MicoServiceInterface.valueOf(serviceInterfaceRequestDto);
        service.getServiceInterfaces().add(serviceInterface);
        serviceRepository.save(service);
        return ResponseEntity
                .created(linkTo(methodOn(ServiceInterfaceResource.class).getInterfaceByName(shortName, version,
                        serviceInterface.getServiceInterfaceName())).toUri())
                .body(new Resource<>(new MicoServiceInterfaceResponseDTO(serviceInterface),
                        getServiceInterfaceLinks(shortName, version, serviceInterface)));
    }

    /**
     * Updates an existing MICO service interface.
     *
     * @param shortName                         the name of a {@link MicoService}
     * @param version                           the version a {@link MicoService}
     * @param serviceInterfaceName              the name of a {@link MicoServiceInterface}
     * @param updatedServiceInterfaceRequestDto the {@link MicoServiceInterfaceRequestDTO}
     * @return the updated {@link MicoServiceInterfaceResponseDTO}
     */
    @PutMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> updateServiceInterface(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                            @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                            @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName,
                                                                                            @Valid @RequestBody MicoServiceInterfaceRequestDTO updatedServiceInterfaceRequestDto) {
        if (!updatedServiceInterfaceRequestDto.getServiceInterfaceName().equals(serviceInterfaceName)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The variable '" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "' must be equal to the name specified in the request body");
        }

        // Used to check whether the corresponding service exists
        getServiceFromServiceBroker(shortName, version);

        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(shortName, version, serviceInterfaceName);
        if (!serviceInterfaceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MicoServiceInterface was not found!");
        }

        MicoServiceInterface serviceInterface = serviceInterfaceOptional.get();
        MicoServiceInterface updatedServiceInterface = MicoServiceInterface.valueOf(updatedServiceInterfaceRequestDto).setId(serviceInterface.getId());
        serviceInterfaceRepository.save(updatedServiceInterface);

        return ResponseEntity.ok(new Resource<>(new MicoServiceInterfaceResponseDTO(updatedServiceInterface),
                getServiceInterfaceLinks(shortName, version, updatedServiceInterface)));
    }

    /**
     * Retrieves a {@code MicoService} from the database for a given short name and version.
     *
     * @param shortName the short name of the {@link MicoService}.
     * @param version   the version of the {@link MicoService}.
     * @return the {@link MicoService} if it exists.
     * @throws ResponseStatusException if the {@code MicoService} does not exist in the database.
     */
    private MicoService getServiceFromServiceBroker(String shortName, String version) throws ResponseStatusException {
        MicoService micoService;
        try {
            micoService = micoServiceBroker.getServiceFromDatabase(shortName, version);
        } catch (MicoServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return micoService;
    }

    /**
     * Retrieves a {@code MicoServiceInterface} from the database for a
     * given service short name and version and service interface name.
     *
     * @param shortName            the short name of the {@link MicoService}.
     * @param version              the version of the {@link MicoService}.
     * @param serviceInterfaceName the name of the {@link MicoServiceInterface}.
     * @return the {@link MicoServiceInterface} if it exists.
     * @throws ResponseStatusException if the {@code MicoServiceInterface} does not exist in the database.
     */
    private MicoServiceInterface getServiceInterfaceFromServiceInterfaceBroker(String shortName, String version, String serviceInterfaceName) throws ResponseStatusException {
        MicoServiceInterface serviceInterface;
        try {
            serviceInterface = micoServiceInterfaceBroker.getInterfaceOfServiceByName(shortName, version, serviceInterfaceName);
        } catch (MicoServiceInterfaceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        return serviceInterface;
    }

    protected Resource<MicoServiceInterfaceResponseDTO> getServiceInterfaceResponseDTOResource(String serviceShortName, String serviceVersion, MicoServiceInterface serviceInterface) {
        return new Resource<>(new MicoServiceInterfaceResponseDTO(serviceInterface), getServiceInterfaceLinks(serviceShortName, serviceVersion, serviceInterface));
    }

    protected List<Resource<MicoServiceInterfaceResponseDTO>> getServiceInterfaceResponseDTOResourcesList(String serviceShortName, String serviceVersion, List<MicoServiceInterface> serviceInterfaces) {
        return serviceInterfaces.stream().map(serviceInterface -> getServiceInterfaceResponseDTOResource(serviceShortName, serviceVersion, serviceInterface)).collect(Collectors.toList());
    }

    private Iterable<Link> getServiceInterfaceLinks(String shortName, String version, MicoServiceInterface serviceInterface) {
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ServiceInterfaceResource.class).getInterfaceByName(shortName, version, serviceInterface.getServiceInterfaceName())).withSelfRel());
        links.add(linkTo(methodOn(ServiceInterfaceResource.class).getInterfacesOfService(shortName, version)).withRel("interfaces"));
        links.add(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(shortName, version)).withRel("service"));
        return links;
    }

}
