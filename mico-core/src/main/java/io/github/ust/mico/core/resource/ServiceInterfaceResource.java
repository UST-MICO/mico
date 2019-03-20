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

import javax.validation.Valid;

import io.github.ust.mico.core.dto.response.MicoServiceInterfaceResponseDTO;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.persistence.MicoServiceInterfaceRepository;
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
    private MicoServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoStatusService micoStatusService;

    @GetMapping(SERVICE_INTERFACE_PATH)
    public ResponseEntity<Resources<Resource<MicoServiceInterfaceResponseDTO>>> getInterfacesOfService(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                       @PathVariable(PATH_VARIABLE_VERSION) String version) {
    	List<MicoServiceInterface> serviceInterfaces = serviceInterfaceRepository.findByService(shortName, version);
		List<Resource<MicoServiceInterfaceResponseDTO>> serviceInterfaceResources =
			getServiceInterfaceResponseDTOResourcesList(shortName, version, serviceInterfaces);
		return ResponseEntity.ok(new Resources<Resource<MicoServiceInterfaceResponseDTO>>(serviceInterfaceResources,
		    linkTo(methodOn(ServiceInterfaceResource.class).getInterfacesOfService(shortName, version)).withSelfRel()));
    }

    @GetMapping(SERVICE_INTERFACE_PATH + "/{" + PATH_VARIABLE_SERVICE_INTERFACE_NAME + "}")
    public ResponseEntity<Resource<MicoServiceInterfaceResponseDTO>> getInterfaceByName(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                             @PathVariable(PATH_VARIABLE_SERVICE_INTERFACE_NAME) String serviceInterfaceName) {
    	MicoServiceInterface serviceInterface = getServiceInterfaceFromDatabase(shortName, version, serviceInterfaceName);
    	return ResponseEntity.ok(getServiceInterfaceResponseDTOResource(shortName, version, serviceInterface));
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
        serviceInterfaceRepository.deleteByServiceAndName(shortName, version, serviceInterfaceName);
        return ResponseEntity.noContent().build();
    }

    /**
     * This is not transactional. At the moment we have only one user. If this changes transactional support is a must.
     * FIXME Add transactional support
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
        MicoService service = getServiceFromDatabase(shortName, version);
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
        getServiceFromDatabase(shortName, version);

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
     * @param version the version of the {@link MicoService}.
     * @return the {@link MicoService} if it exists.
     * @throws ResponseStatusException if the {@code MicoService} does not exist in the database.
     */
    private MicoService getServiceFromDatabase(String shortName, String version) throws ResponseStatusException {
        Optional<MicoService> serviceOptional = serviceRepository.findByShortNameAndVersion(shortName, version);
        if (!serviceOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service '" + shortName + "' '" + version + "' could not be found!");
        }
        return serviceOptional.get();
    }

    /**
     * Retrieves a {@code MicoServiceInterface} from the database for a
     * given service short name and version and service interface name.
     *
     * @param shortName the short name of the {@link MicoService}.
     * @param version the version of the {@link MicoService}.
     * @param serviceInterfaceName the name of the {@link MicoServiceInterface}.
     * @return the {@link MicoServiceInterface} if it exists.
     * @throws ResponseStatusException if the {@code MicoServiceInterface} does not exist in the database.
     */
    private MicoServiceInterface getServiceInterfaceFromDatabase(String shortName, String version, String serviceInterfaceName) throws ResponseStatusException {
        Optional<MicoServiceInterface> serviceInterfaceOptional = serviceInterfaceRepository.findByServiceAndName(shortName, version, serviceInterfaceName);
        if (!serviceInterfaceOptional.isPresent()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service  interface '" + serviceInterfaceName
			    + "' of service '" + shortName + "' '" + version + "' could not be found!");
        }
        return serviceInterfaceOptional.get();
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
