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

import io.github.ust.mico.core.configuration.KafkaConfig;
import io.github.ust.mico.core.configuration.OpenFaaSConfig;
import io.github.ust.mico.core.dto.request.MicoServiceDeploymentInfoRequestDTO;
import io.github.ust.mico.core.dto.request.MicoTopicRequestDTO;
import io.github.ust.mico.core.exception.*;
import io.github.ust.mico.core.model.*;
import io.github.ust.mico.core.persistence.*;
import io.github.ust.mico.core.service.MicoKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MicoServiceDeploymentInfoBroker {

    @Autowired
    private MicoApplicationBroker applicationBroker;

    @Autowired
    private MicoServiceDeploymentInfoRepository serviceDeploymentInfoRepository;

    @Autowired
    private MicoKubernetesClient micoKubernetesClient;

    @Autowired
    private MicoLabelRepository micoLabelRepository;

    @Autowired
    private MicoTopicRepository micoTopicRepository;

    @Autowired
    private MicoEnvironmentVariableRepository micoEnvironmentVariableRepository;

    @Autowired
    private KubernetesDeploymentInfoRepository kubernetesDeploymentInfoRepository;

    @Autowired
    private MicoInterfaceConnectionRepository micoInterfaceConnectionRepository;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private OpenFaaSConfig openFaaSConfig;

    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException, MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        applicationBroker.checkForMicoServiceInMicoApplication(applicationShortName, applicationVersion, serviceShortName);

        Optional<MicoServiceDeploymentInfo> micoServiceDeploymentInfoOptional = serviceDeploymentInfoRepository.findByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
        if (micoServiceDeploymentInfoOptional.isPresent()) {
            return micoServiceDeploymentInfoOptional.get();
        } else {
            throw new MicoServiceDeploymentInformationNotFoundException(applicationShortName, applicationVersion, serviceShortName);
        }
    }

    public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion,
                                                                            String serviceShortName, MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws
        MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException,
        MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException, MicoTopicRoleUsedMultipleTimesException {

        validateTopics(serviceDeploymentInfoDTO);

        MicoApplication micoApplication = applicationBroker.getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        MicoServiceDeploymentInfo storedServiceDeploymentInfo = getMicoServiceDeploymentInformation(applicationShortName, applicationVersion, serviceShortName);

        int oldReplicas = storedServiceDeploymentInfo.getReplicas();

        MicoServiceDeploymentInfo sdiWithAppliedValues = storedServiceDeploymentInfo.applyValuesFrom(serviceDeploymentInfoDTO);
        // At first save the service deployment information with the new values without topics.
        // This is a workaround to save them later with new relationships. Otherwise Neo4j sometimes deletes the relationships!?
        sdiWithAppliedValues.setTopics(new ArrayList<>());
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(sdiWithAppliedValues);

        // If there are topics, apply them and save the service deployment information again.
        if (!serviceDeploymentInfoDTO.getTopics().isEmpty()) {
            MicoServiceDeploymentInfo sdiWithTopics = updatedServiceDeploymentInfo.setTopics(
                serviceDeploymentInfoDTO.getTopics().stream().map(topicDto -> MicoTopicRole.valueOf(topicDto, storedServiceDeploymentInfo)).collect(Collectors.toList()));
            // Check if topics with the same names already exist, if so reuse them.
            MicoServiceDeploymentInfo sdiWithReusedTopics = createOrReuseTopics(sdiWithTopics);
            updatedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(sdiWithReusedTopics);
        }

        // In case addition properties (stored as separate node entity) such as labels, environment variables
        // have been removed from this service deployment information,
        // the standard save() function of the service deployment information repository will not delete those
        // "tangling" (without relationships) labels (nodes), hence the manual clean up.
        micoLabelRepository.cleanUp();
        micoTopicRepository.cleanUp();
        micoEnvironmentVariableRepository.cleanUp();
        kubernetesDeploymentInfoRepository.cleanUp();
        micoInterfaceConnectionRepository.cleanUp();

        // FIXME: Currently we only supported scale in / scale out.
        // 		  If the MICO service is already deployed, we only update the replicas.
        // 	      The other properties are ignored!
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            MicoService micoService = updatedServiceDeploymentInfo.getService();
            log.info("MicoApplication '{}' {}' is already deployed. Update the deployment of the included MicoService '{} '{}'.",
                micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName(), micoService.getVersion());

            // MICO service is already deployed. Update the replicas.
            int replicasDiff = serviceDeploymentInfoDTO.getReplicas() - oldReplicas;
            if (replicasDiff > 0) {
                log.debug("Increase replicas of MicoService '{}' '{}' by {}.", micoService.getShortName(), micoService.getVersion(), replicasDiff);
                micoKubernetesClient.scaleOut(updatedServiceDeploymentInfo, replicasDiff);
            } else if (replicasDiff < 0) {
                log.debug("Decrease replicas of MicoService '{}' '{}' by {}.", micoService.getShortName(), micoService.getVersion(), replicasDiff);
                micoKubernetesClient.scaleIn(updatedServiceDeploymentInfo, Math.abs(replicasDiff));
            } else {
                // TODO: If no scale operation is required, maybe some other
                // 		 information still needs to be updated.
            }
        }

        return updatedServiceDeploymentInfo;
    }

    /**
     * Validates the topics.
     * Throws an error if there are multiple topics with the same role.
     *
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoRequestDTO}
     * @throws MicoTopicRoleUsedMultipleTimesException if an {@code MicoTopicRole.Role} is not unique
     */
    private void validateTopics(MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws MicoTopicRoleUsedMultipleTimesException {
        List<MicoTopicRequestDTO> newTopics = serviceDeploymentInfoDTO.getTopics();
        Set<MicoTopicRole.Role> usedRoles = new HashSet<>();
        for (MicoTopicRequestDTO requestDTO : newTopics) {
            if (!usedRoles.add(requestDTO.getRole())) {
                // Role is used twice, however a role should be used only once
                throw new MicoTopicRoleUsedMultipleTimesException(requestDTO.getRole());
            }
        }
    }

    /**
     * Checks if topics with the same name already exists.
     * If so reuse them by setting the id of the existing Neo4j node and save them.
     * If not create them in the database.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo} containing topics
     */
    private MicoServiceDeploymentInfo createOrReuseTopics(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        List<MicoTopicRole> topicRoles = serviceDeploymentInfo.getTopics();

        for (MicoTopicRole topicRole : topicRoles) {
            String topicName = topicRole.getTopic().getName();
            Optional<MicoTopic> existingTopic = micoTopicRepository.findByName(topicName);
            if (existingTopic.isPresent()) {
                topicRole.setTopic(existingTopic.get());
            } else {
                MicoTopic savedTopic = micoTopicRepository.save(topicRole.getTopic());
                topicRole.setTopic(savedTopic);
            }
        }
        return serviceDeploymentInfo;
    }


    /**
     * Sets the default environment variables for Kafka-enabled MicoServices. See {@link MicoEnvironmentVariable.DefaultNames}
     * for a complete list.
     *
     * @param micoServiceDeploymentInfo The {@link MicoServiceDeploymentInfo} with an corresponding MicoService
     */
    public void setDefaultDeploymentInformationForKafkaEnabledService(MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
        MicoService micoService = micoServiceDeploymentInfo.getService();
        if (micoService == null) {
            throw new IllegalArgumentException("The MicoServiceDeploymentInfo needs a valid MicoService set to check if the service is Kafka enabled");
        }
        if (!micoService.isKafkaEnabled()) {
            log.debug("MicoService '{}' '{}' is not Kafka-enabled. Not necessary to adding specific env variables.",
                micoService.getShortName(), micoService.getVersion());
            return;
        }
        log.debug("Adding default environment variables and topics to the Kafka-enabled MicoService '{}' '{}'.",
            micoService.getShortName(), micoService.getVersion());
        List<MicoEnvironmentVariable> micoEnvironmentVariables = micoServiceDeploymentInfo.getEnvironmentVariables();
        micoEnvironmentVariables.addAll(kafkaConfig.getDefaultEnvironmentVariablesForKafka());
        micoEnvironmentVariables.addAll(openFaaSConfig.getDefaultEnvironmentVariablesForOpenFaaS());
        List<MicoTopicRole> topics = micoServiceDeploymentInfo.getTopics();
        topics.addAll(kafkaConfig.getDefaultTopics(micoServiceDeploymentInfo));
    }

}
