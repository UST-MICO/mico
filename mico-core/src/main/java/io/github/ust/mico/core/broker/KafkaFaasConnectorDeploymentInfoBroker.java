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

package io.github.ust.mico.core.broker;

import io.github.ust.mico.core.dto.request.KFConnectorDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KafkaFaasConnectorDeploymentInfoBroker {

    @Autowired
    private MicoApplicationBroker applicationBroker;

    @Autowired
    private MicoServiceDeploymentInfoRepository deploymentInfoRepository;

    /**
     * Fetches a list of {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos} of all KafkaFaasConnector instances
     * associated with the specified {@link MicoApplication}.
     *
     * @param micoApplicationShortName the shortName of the micoApplication
     * @param micoApplicationVersion   the version of the micoApplication
     * @return the list of {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos}
     * @throws MicoApplicationNotFoundException if there is no such micoApplication
     */
    public List<MicoServiceDeploymentInfo> getKafkaFaasConnectorDeploymentInformation(
        String micoApplicationShortName, String micoApplicationVersion) throws MicoApplicationNotFoundException {

        MicoApplication micoApplication = applicationBroker.getMicoApplicationByShortNameAndVersion(micoApplicationShortName, micoApplicationVersion);
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos = micoApplication.getKafkaFaasConnectorDeploymentInfos();
        log.debug("There are {} KafkaFaasConnector deployment information for MicoApplication '{}' in version '{}'.",
                micoServiceDeploymentInfos.size(), micoApplicationShortName, micoApplicationVersion);
        return micoApplication.getKafkaFaasConnectorDeploymentInfos();
    }


    /**
     * Updates an existing {@link MicoServiceDeploymentInfo} in the database
     * based on the values of a {@link MicoServiceDeploymentInfoRequestDTO} object.
     *
     * @param instanceID               the instance id of the deployment {@link MicoApplication}
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoRequestDTO}
     * @return the new {@link MicoServiceDeploymentInfo} stored in the database
     * @throws MicoApplicationNotFoundException                  if there is no {@code MicoApplication} with the specified short name and version
     * @throws MicoApplicationDoesNotIncludeMicoServiceException if there is no service included in the specified {@code MicoApplication} with the particular short name
     * @throws MicoServiceDeploymentInformationNotFoundException if there is no {@code MicoServiceDeploymentInfo} stored in the database
     * @throws KubernetesResourceException                       if there are problems with retrieving Kubernetes resource information
     * @throws MicoTopicRoleUsedMultipleTimesException           if a {@link MicoTopicRole} is used multiple times
     */
    public MicoServiceDeploymentInfo updateKafkaFaasConnectorDeploymentInformation(String instanceId, KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO)
            throws MicoServiceDeploymentInformationNotFoundException {

        Optional<MicoServiceDeploymentInfo> storedServiceDeploymentInfoOptional = deploymentInfoRepository.findByInstanceId(instanceId);

        if(!storedServiceDeploymentInfoOptional.isPresent()) {
            throw new MicoServiceDeploymentInformationNotFoundException(instanceId);
        }
        MicoServiceDeploymentInfo storedServiceDeploymentInfo = storedServiceDeploymentInfoOptional.get();
        // TODO Deployed services should also be updated.
        return saveValuesToDatabase(kfConnectorDeploymentInfoRequestDTO, storedServiceDeploymentInfo);
    }

    private MicoServiceDeploymentInfo saveValuesToDatabase(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO,
                                                           MicoServiceDeploymentInfo storedServiceDeploymentInfo) {
        // Eventually set new topic names
        List<MicoTopicRole> topics = storedServiceDeploymentInfo.getTopics();
        for (MicoTopicRole role: topics) {
            MicoTopic topic = role.getTopic();
            if(role.getRole() == MicoTopicRole.Role.INPUT) {
                topic.setName(kfConnectorDeploymentInfoRequestDTO.getInputTopicName());
            }
            else if(role.getRole() == MicoTopicRole.Role.OUTPUT) {
                topic.setName((kfConnectorDeploymentInfoRequestDTO.getOutputTopicName()));
            }
        }
        // Eventually set the OpenFaaS function name
        OpenFaaSFunction openFaaSFunction = storedServiceDeploymentInfo.getOpenFaaSFunction();
        if(openFaaSFunction == null) {
            storedServiceDeploymentInfo.setOpenFaaSFunction(
                    new OpenFaaSFunction().setName(kfConnectorDeploymentInfoRequestDTO.getOpenFaaSFunctionName()));
        }
        else {
            openFaaSFunction.setName(kfConnectorDeploymentInfoRequestDTO.getOpenFaaSFunctionName());
        }
        deploymentInfoRepository.save(storedServiceDeploymentInfo);
        return storedServiceDeploymentInfo;
    }

}
