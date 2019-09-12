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
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static io.github.ust.mico.core.model.MicoTopicRole.Role.*;


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
     * based on the values of a {@link KFConnectorDeploymentInfoRequestDTO} object.
     *
     * @param instanceId                          the instance id of the deployment {@link MicoApplication}
     * @param kfConnectorDeploymentInfoRequestDTO the {@link KFConnectorDeploymentInfoRequestDTO}
     * @return the new {@link MicoServiceDeploymentInfo} stored in the database
     * @throws MicoServiceDeploymentInformationNotFoundException if there is no {@code MicoServiceDeploymentInfo} stored in the database
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

        eventuallyUpdateTopics(kfConnectorDeploymentInfoRequestDTO, storedServiceDeploymentInfo);
        eventuallyUpdateOpenFaaSFunction(kfConnectorDeploymentInfoRequestDTO, storedServiceDeploymentInfo);
        deploymentInfoRepository.save(storedServiceDeploymentInfo);
        return storedServiceDeploymentInfo;
    }

    private void eventuallyUpdateTopics(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO,
                                                   MicoServiceDeploymentInfo storedServiceDeploymentInfo) {
        // update existing topics
        List<MicoTopicRole> topics = storedServiceDeploymentInfo.getTopics();
        for (MicoTopicRole role: topics) {
            MicoTopic topic = role.getTopic();
            switch (role.getRole()) {
                case INPUT: topic.setName(kfConnectorDeploymentInfoRequestDTO.getInputTopicName()); break;
                case OUTPUT: topic.setName(kfConnectorDeploymentInfoRequestDTO.getOutputTopicName()); break;
                    case DEAD_LETTER: //TODO
                case INVALID_MESSAGE: //TODO
                case TEST_MESSAGE_OUTPUT: //TODO
            }
        }
        // add topics, if they do not exist, yet
        List<MicoTopicRole.Role> storedTopicRoles = storedServiceDeploymentInfo.getTopics().stream().map(
                topic -> topic.getRole()).collect(Collectors.toList());

        for(MicoTopicRole.Role role: MicoTopicRole.Role.values()) {
            // TODO, currently the the KFConnectorDeploymentInfoRequestDTO only provides INPUT and OUTPUT.
            //  As soon as it also provides DEAD_LETTER, INVALID_MESSAGE and TEST_MESSAGE_OUTPUT this should be
            //  handled here as well

            if(!storedTopicRoles.contains(role) && (role == INPUT | role == OUTPUT)) {
                storedServiceDeploymentInfo.getTopics().add(
                        new MicoTopicRole()
                                .setRole(role)
                                .setServiceDeploymentInfo(storedServiceDeploymentInfo)
                                .setTopic(new MicoTopic().setName(
                                        (role == INPUT) ? kfConnectorDeploymentInfoRequestDTO.getInputTopicName()
                                                        : kfConnectorDeploymentInfoRequestDTO.getOutputTopicName()
                                )));
            }
        }
    }

    private void eventuallyUpdateOpenFaaSFunction(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO,
                                                  MicoServiceDeploymentInfo storedServiceDeploymentInfo) {
        OpenFaaSFunction openFaaSFunction = storedServiceDeploymentInfo.getOpenFaaSFunction();
        if(openFaaSFunction == null) {
            storedServiceDeploymentInfo.setOpenFaaSFunction(
                    new OpenFaaSFunction().setName(kfConnectorDeploymentInfoRequestDTO.getOpenFaaSFunctionName()));
        }
        else {
            openFaaSFunction.setName(kfConnectorDeploymentInfoRequestDTO.getOpenFaaSFunctionName());
        }
    }
}
