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

import io.github.ust.mico.core.broker.KafkaFaasConnectorDeploymentInfoBroker;
import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.response.KFConnectorDeploymentInfoResponseDTO;
import io.github.ust.mico.core.exception.KafkaFaasConnectorInstanceNotFoundException;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static io.github.ust.mico.core.resource.ApplicationResource.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class KafkaFaasConnectorDeploymentInfoResource {

    @Autowired
    private KafkaFaasConnectorDeploymentInfoBroker kafkaFaasConnectorDeploymentInfoBroker;

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR)
    public ResponseEntity<Resources<Resource<KFConnectorDeploymentInfoResponseDTO>>> getKafkaFaasConnectorDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        log.debug("Get the KafkaFaasConnector deployment information of the MicoApplication '{}' '{}'.", shortName, version);
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos;
        try {
            micoServiceDeploymentInfos = kafkaFaasConnectorDeploymentInfoBroker.getKafkaFaasConnectorDeploymentInformation(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        List<Resource<KFConnectorDeploymentInfoResponseDTO>> micoServiceDeploymentInfoResources = getKfConnectorDeploymentInfoResponseDTOResources(shortName, version, micoServiceDeploymentInfos);
        return ResponseEntity.ok(new Resources<>(micoServiceDeploymentInfoResources, linkTo(methodOn(KafkaFaasConnectorDeploymentInfoResource.class).getKafkaFaasConnectorDeploymentInformation(shortName, version)).withSelfRel()));
    }

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR + "/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID + "}")
    public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> getKafkaFaasConnectorDeploymentInformationInstance(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                                             @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                                                             @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID) String instanceId) {
        log.debug("Get the KafkaFaasConnector deployment information of the MicoApplication '{}' '{}' with the instance id '{}'.", shortName, version, instanceId);
        Optional<MicoServiceDeploymentInfo> micoServiceDeploymentInfoOptional;
        try {
            micoServiceDeploymentInfoOptional = kafkaFaasConnectorDeploymentInfoBroker.getKafkaFaasConnectorDeploymentInformation(shortName, version, instanceId);
            micoServiceDeploymentInfoOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "There is no KafkaFaasConnector with the instance id '" + instanceId + "'!"));
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        Resource<KFConnectorDeploymentInfoResponseDTO> kfConnectorDeploymentInfoResponseDTOResource = getKfConnectorDeploymentInfoResponseDTOResource(shortName, version, micoServiceDeploymentInfoOptional.get());
        return ResponseEntity.ok(kfConnectorDeploymentInfoResponseDTOResource);
    }

    @PutMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECTOR + "/{" + PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID + "}")
    public ResponseEntity<Resource<KFConnectorDeploymentInfoResponseDTO>> updateKafkaFaasConnectorDeploymentInfo(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                                 @PathVariable(PATH_VARIABLE_VERSION) String version,
                                                                                                                 @PathVariable(PATH_VARIABLE_KAFKA_FAAS_CONNECTOR_INSTANCE_ID) String instanceId,
                                                                                                                 @Valid @RequestBody KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO) {
        if (!kfConnectorDeploymentInfoRequestDTO.getInstanceId().equals(instanceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "InstanceId in the request body does not match the request parameter");
        }

        MicoServiceDeploymentInfo updatedServiceDeploymentInfo;
        try {
            updatedServiceDeploymentInfo = kafkaFaasConnectorDeploymentInfoBroker.updateKafkaFaasConnectorDeploymentInformation(
                instanceId, kfConnectorDeploymentInfoRequestDTO);
        } catch (KafkaFaasConnectorInstanceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        // Convert to service deployment info DTO and return it
        Resource<KFConnectorDeploymentInfoResponseDTO> responseDTOResource = getKfConnectorDeploymentInfoResponseDTOResource(shortName, version, updatedServiceDeploymentInfo);
        return ResponseEntity.ok(responseDTOResource);
    }


    /**
     * Wraps a list of {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos} into a list of {@link KFConnectorDeploymentInfoResponseDTO} resources.
     *
     * @param applicationShortName       the short name of the {@link MicoApplication}
     * @param applicationVersion         the version of the {@link MicoApplication}
     * @param micoServiceDeploymentInfos the {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos}
     * @return A list of resources containing the {@link KFConnectorDeploymentInfoResponseDTO KFConnectorDeploymentInfoResponseDTOs}.
     */
    private List<Resource<KFConnectorDeploymentInfoResponseDTO>> getKfConnectorDeploymentInfoResponseDTOResources(
        String applicationShortName, String applicationVersion, List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos) {

        LinkedList<Resource<KFConnectorDeploymentInfoResponseDTO>> micoServiceDeploymentInfoResources = new LinkedList<>();
        for (MicoServiceDeploymentInfo micoServiceDeploymentInfo : micoServiceDeploymentInfos) {
            Resource<KFConnectorDeploymentInfoResponseDTO> micoServiceDeploymentInfoResource = getKfConnectorDeploymentInfoResponseDTOResource(applicationShortName, applicationVersion, micoServiceDeploymentInfo);
            micoServiceDeploymentInfoResources.add(micoServiceDeploymentInfoResource);
        }
        return micoServiceDeploymentInfoResources;
    }

    /**
     * Wraps a {@link KFConnectorDeploymentInfoResponseDTO} into a HATEOAS resource with a link to the application and a self-link.
     *
     * @param applicationShortName      the short name of the {@link MicoApplication}
     * @param applicationVersion        the version of the {@link MicoApplication}
     * @param micoServiceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return The resource containing the {@link KFConnectorDeploymentInfoResponseDTO}.
     */
    protected static Resource<KFConnectorDeploymentInfoResponseDTO> getKfConnectorDeploymentInfoResponseDTOResource(
        String applicationShortName, String applicationVersion, MicoServiceDeploymentInfo micoServiceDeploymentInfo) {

        KFConnectorDeploymentInfoResponseDTO kfConnectorDeploymentInfoResponseDTO = new KFConnectorDeploymentInfoResponseDTO(micoServiceDeploymentInfo);
        MicoService micoService = micoServiceDeploymentInfo.getService();
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationResource.class).getApplicationByShortNameAndVersion(applicationShortName, applicationVersion)).withRel("application"));
        links.add(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(micoService.getShortName(), micoService.getVersion())).withRel("kafkaFaasConnector"));
        links.add(linkTo(methodOn(KafkaFaasConnectorDeploymentInfoResource.class).getKafkaFaasConnectorDeploymentInformationInstance(applicationShortName, applicationVersion, micoServiceDeploymentInfo.getInstanceId())).withSelfRel());
        return new Resource<>(kfConnectorDeploymentInfoResponseDTO, links);
    }

}
