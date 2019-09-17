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
import io.github.ust.mico.core.exception.KafkaFaasConnectorInstanceNotFoundException;
import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.MicoServiceDeploymentInfoRepository;
import io.github.ust.mico.core.persistence.MicoTopicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.ust.mico.core.model.MicoTopicRole.Role.INPUT;
import static io.github.ust.mico.core.model.MicoTopicRole.Role.OUTPUT;


@Slf4j
@Service
public class KafkaFaasConnectorDeploymentInfoBroker {

    @Autowired
    private MicoApplicationBroker applicationBroker;

    @Autowired
    private MicoServiceDeploymentInfoRepository deploymentInfoRepository;

    @Autowired
    private MicoServiceDeploymentInfoBroker serviceDeploymentInfoBroker;

    @Autowired
    private MicoTopicRepository topicRepository;

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
     * Filters the list of {@link MicoServiceDeploymentInfo} from
     * {@link KafkaFaasConnectorDeploymentInfoBroker#getKafkaFaasConnectorDeploymentInformation(String, String)}
     * for a specific {@code instanceId}.
     *
     * @param micoApplicationShortName the short name of the {@link MicoApplication}
     * @param micoApplicationVersion   the version of the {@link MicoApplication}
     * @param instanceId               the instance ID of the {@link MicoServiceDeploymentInfo}
     * @return a single {@link MicoServiceDeploymentInfo} with an instance ID equal to the give one.
     * @throws MicoApplicationNotFoundException if the {@link MicoApplication} does not exist.
     */
    public Optional<MicoServiceDeploymentInfo> getKafkaFaasConnectorDeploymentInformation(
        String micoApplicationShortName, String micoApplicationVersion, String instanceId) throws MicoApplicationNotFoundException {
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos = getKafkaFaasConnectorDeploymentInformation(micoApplicationShortName, micoApplicationVersion);
        Optional<MicoServiceDeploymentInfo> micoServiceDeploymentInfoOptional = micoServiceDeploymentInfos.stream()
            .filter(sdi -> sdi.getInstanceId().equals(instanceId))
            .reduce((a, b) -> {
                    throw new IllegalStateException("There are multiple KafkaFaasConnectors with the same instance id: " + a + ", " + b);
                }
            );
        if (micoServiceDeploymentInfoOptional.isPresent()) {
            log.debug("There is a micoServiceDeploymentInfo for MicoApplication '{}' in version '{}' with the instanceId '{}'.", micoApplicationShortName, micoApplicationVersion, instanceId);
        } else {
            log.debug("There is no micoServiceDeploymentInfo for MicoApplication '{}' in version '{}' with the instanceId '{}'.", micoApplicationShortName, micoApplicationVersion, instanceId);
        }
        return micoServiceDeploymentInfoOptional;
    }


    /**
     * Updates an existing {@link MicoServiceDeploymentInfo} in the database
     * based on the values of a {@link KFConnectorDeploymentInfoRequestDTO} object.
     *
     * @param instanceId                          the instance ID of the {@link MicoServiceDeploymentInfo}
     * @param kfConnectorDeploymentInfoRequestDTO the {@link KFConnectorDeploymentInfoRequestDTO}
     * @return the new {@link MicoServiceDeploymentInfo} stored in the database
     * @throws KafkaFaasConnectorInstanceNotFoundException if there is no {@code MicoServiceDeploymentInfo} for the requested {@code instanceId} stored in the database
     */
    public MicoServiceDeploymentInfo updateKafkaFaasConnectorDeploymentInformation(String instanceId, KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO)
        throws KafkaFaasConnectorInstanceNotFoundException {

        Optional<MicoServiceDeploymentInfo> storedServiceDeploymentInfoOptional = deploymentInfoRepository.findByInstanceId(instanceId);

        if (!storedServiceDeploymentInfoOptional.isPresent()) {
            throw new KafkaFaasConnectorInstanceNotFoundException(instanceId);
        }
        MicoServiceDeploymentInfo storedServiceDeploymentInfo = storedServiceDeploymentInfoOptional.get();
        // Updates the deployment information in the database.
        // The new values will be applied to the deployed services as soon as a new deployment is triggered.
        return saveValuesToDatabase(kfConnectorDeploymentInfoRequestDTO, storedServiceDeploymentInfo);
    }

    /**
     * Saves the values of a {@link KFConnectorDeploymentInfoRequestDTO} to the database.
     *
     * @param kfConnectorDeploymentInfoRequestDTO the {@link KFConnectorDeploymentInfoRequestDTO} that includes the new values
     * @param storedServiceDeploymentInfo         the {@link MicoServiceDeploymentInfo} that is already stored in the database
     * @return the new {@link MicoServiceDeploymentInfo} stored in the database
     */
    private MicoServiceDeploymentInfo saveValuesToDatabase(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO,
                                                           MicoServiceDeploymentInfo storedServiceDeploymentInfo) {

        eventuallyUpdateTopics(kfConnectorDeploymentInfoRequestDTO, storedServiceDeploymentInfo);
        eventuallyUpdateOpenFaaSFunction(kfConnectorDeploymentInfoRequestDTO, storedServiceDeploymentInfo);
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo = deploymentInfoRepository.save(storedServiceDeploymentInfo);
        topicRepository.cleanUp();

        return updatedServiceDeploymentInfo;
    }

    /**
     * Stores or updates the input and output topics in the database
     * based on the values of a {@link KFConnectorDeploymentInfoRequestDTO}.
     *
     * @param kfConnectorDeploymentInfoRequestDTO the {@link KFConnectorDeploymentInfoRequestDTO} that includes the topics
     * @param storedServiceDeploymentInfo         the {@link MicoServiceDeploymentInfo} that is already stored in the database
     */
    private void eventuallyUpdateTopics(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO,
                                        MicoServiceDeploymentInfo storedServiceDeploymentInfo) {
        // update existing topics
        List<MicoTopicRole> topics = storedServiceDeploymentInfo.getTopics();
        for (MicoTopicRole role : topics) {
            MicoTopic topic = role.getTopic();
            // Set the name depending on the role.
            // The Neo4j ID will be reset, so that the existing topic that could be already in use by other deployments
            // is not affected by this change.
            switch (role.getRole()) {
                case INPUT:
                    topic.setName(kfConnectorDeploymentInfoRequestDTO.getInputTopicName());
                    topic.setId(null);
                    break;
                case OUTPUT:
                    topic.setName(kfConnectorDeploymentInfoRequestDTO.getOutputTopicName());
                    topic.setId(null);
                    break;
                case DEAD_LETTER:
                case INVALID_MESSAGE:
                case TEST_MESSAGE_OUTPUT:
            }
        }

        // add topics, if they do not exist, yet
        List<MicoTopicRole.Role> storedTopicRoles = storedServiceDeploymentInfo.getTopics().stream().map(
            MicoTopicRole::getRole).collect(Collectors.toList());

        for (MicoTopicRole.Role role : MicoTopicRole.Role.values()) {
            // TODO, currently the the KFConnectorDeploymentInfoRequestDTO only provides INPUT and OUTPUT.
            //  As soon as it also provides DEAD_LETTER, INVALID_MESSAGE and TEST_MESSAGE_OUTPUT this should be
            //  handled here as well

            if (!storedTopicRoles.contains(role) && (role == INPUT || role == OUTPUT)) {
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
        serviceDeploymentInfoBroker.createOrReuseTopicsInDatabase(storedServiceDeploymentInfo);
    }

    /**
     * Stores or updates the OpenFaaS function name in the database
     * based on the value of a {@link KFConnectorDeploymentInfoRequestDTO}.
     *
     * @param kfConnectorDeploymentInfoRequestDTO the {@link KFConnectorDeploymentInfoRequestDTO} that includes the topics
     * @param storedServiceDeploymentInfo         the {@link MicoServiceDeploymentInfo} that is already stored in the database
     */
    private void eventuallyUpdateOpenFaaSFunction(KFConnectorDeploymentInfoRequestDTO kfConnectorDeploymentInfoRequestDTO,
                                                  MicoServiceDeploymentInfo storedServiceDeploymentInfo) {
        OpenFaaSFunction openFaaSFunction = storedServiceDeploymentInfo.getOpenFaaSFunction();
        if (openFaaSFunction == null) {
            storedServiceDeploymentInfo.setOpenFaaSFunction(
                new OpenFaaSFunction().setName(kfConnectorDeploymentInfoRequestDTO.getOpenFaaSFunctionName()));
        } else {
            openFaaSFunction.setName(kfConnectorDeploymentInfoRequestDTO.getOpenFaaSFunctionName());
            openFaaSFunction.setId(null);
        }

        serviceDeploymentInfoBroker.createOrReuseOpenFaaSFunctionsInDatabase(storedServiceDeploymentInfo);

    }
}
