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
import io.github.ust.mico.core.dto.response.KFConnectorDeploymentInfoResponseDTO;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;

import static io.github.ust.mico.core.resource.ApplicationResource.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping(value = PATH_APPLICATIONS, produces = MediaTypes.HAL_JSON_VALUE)
public class KafkaFaasConnectorDeploymentInfoResource {

    public static final String PATH_KAFKA_FAAS_CONNECOTR_DEPLOYMENT_INFORMATION = "kafkaFaasConnectorDeploymentInformation";

    @Autowired
    private KafkaFaasConnectorDeploymentInfoBroker kafkaFaasConnectorDeploymentInfoBroker;

    @GetMapping("/{" + PATH_VARIABLE_SHORT_NAME + "}/{" + PATH_VARIABLE_VERSION + "}/" + PATH_KAFKA_FAAS_CONNECOTR_DEPLOYMENT_INFORMATION)
    public ResponseEntity<Resources<Resource<KFConnectorDeploymentInfoResponseDTO>>> getKafkaFaasConnectorDeploymentInformation(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName,
                                                                                                                                @PathVariable(PATH_VARIABLE_VERSION) String version) {
        log.debug("Request to getKafkaFaasConnectorDeploymentInformation with the micoApplicationShortname '{}' and version '{}'.", shortName, version);
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos;
        try {
            micoServiceDeploymentInfos = kafkaFaasConnectorDeploymentInfoBroker.getKafkaFaasConnectorDeploymentInformation(shortName, version);
        } catch (MicoApplicationNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        List<Resource<KFConnectorDeploymentInfoResponseDTO>> micoServiceDeploymentInfoResources = getKfConnectorDeploymentInfoResponseDTOResources(shortName, version, micoServiceDeploymentInfos);
        return ResponseEntity.ok(new Resources<>(micoServiceDeploymentInfoResources, linkTo(methodOn(KafkaFaasConnectorDeploymentInfoResource.class).getKafkaFaasConnectorDeploymentInformation(shortName, version)).withSelfRel()));
    }

    /**
     * Warps a list of {@link MicoServiceDeploymentInfo} into a list of {@link KFConnectorDeploymentInfoResponseDTO} resources.
     *
     * @param shortName
     * @param version
     * @param micoServiceDeploymentInfos
     * @return A list of resources. Each item in the list contains a {@link KFConnectorDeploymentInfoResponseDTO}.
     */
    private List<Resource<KFConnectorDeploymentInfoResponseDTO>> getKfConnectorDeploymentInfoResponseDTOResources(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName, @PathVariable(PATH_VARIABLE_VERSION) String version, List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos) {
        LinkedList<Resource<KFConnectorDeploymentInfoResponseDTO>> micoServiceDeploymentInfoResources = new LinkedList<>();
        for (MicoServiceDeploymentInfo micoServiceDeploymentInfo : micoServiceDeploymentInfos) {
            Resource<KFConnectorDeploymentInfoResponseDTO> micoServiceDeploymentInfoResource = getKfConnectorDeploymentInfoResponseDTOResource(shortName, version, micoServiceDeploymentInfo);
            micoServiceDeploymentInfoResources.add(micoServiceDeploymentInfoResource);
        }
        return micoServiceDeploymentInfoResources;
    }

    /**
     * Warps a {@link KFConnectorDeploymentInfoResponseDTO} into a HATOAS resource with a link to the application and a self-link.
     *
     * @param shortName
     * @param version
     * @param micoServiceDeploymentInfo
     * @return
     */
    private Resource<KFConnectorDeploymentInfoResponseDTO> getKfConnectorDeploymentInfoResponseDTOResource(@PathVariable(PATH_VARIABLE_SHORT_NAME) String shortName, @PathVariable(PATH_VARIABLE_VERSION) String version, MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
        KFConnectorDeploymentInfoResponseDTO kfConnectorDeploymentInfoResponseDTO = new KFConnectorDeploymentInfoResponseDTO(micoServiceDeploymentInfo);
        MicoService micoService = micoServiceDeploymentInfo.getService();
        LinkedList<Link> links = new LinkedList<>();
        links.add(linkTo(methodOn(ApplicationResource.class).getApplicationByShortNameAndVersion(shortName, version)).withRel("application"));
        links.add(linkTo(methodOn(ServiceResource.class).getServiceByShortNameAndVersion(micoService.getShortName(), micoService.getVersion())).withRel("kafkaFaasConnector"));
        return new Resource<>(kfConnectorDeploymentInfoResponseDTO, links);
    }


}
