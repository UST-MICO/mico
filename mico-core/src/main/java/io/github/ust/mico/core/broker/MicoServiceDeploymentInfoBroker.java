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
    private OpenFaaSFunctionRepository openFaaSFunctionRepository;

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

    /**
     * Returns the {@link MicoServiceDeploymentInfo} stored in the database.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}
     * @param applicationVersion   the version of the {@link MicoApplication}
     * @param serviceShortName     the short name of the {@link MicoService}
     * @return the {@link MicoServiceDeploymentInfo} stored in the database
     * @throws MicoServiceDeploymentInformationNotFoundException if there is no {@code MicoServiceDeploymentInfo} stored in the database
     * @throws MicoApplicationNotFoundException                  if there is no {@code MicoApplication} with the specified short name and version
     * @throws MicoApplicationDoesNotIncludeMicoServiceException if there is no service included in the specified {@code MicoApplication} with the particular short name
     */
    public MicoServiceDeploymentInfo getMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion, String serviceShortName) throws MicoServiceDeploymentInformationNotFoundException, MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException {
        applicationBroker.getMicoApplicationForMicoService(applicationShortName, applicationVersion, serviceShortName);

        List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfoRepository.findByApplicationAndService(applicationShortName, applicationVersion, serviceShortName);
        if (serviceDeploymentInfos.isEmpty()) {
            throw new MicoServiceDeploymentInformationNotFoundException(applicationShortName, applicationVersion, serviceShortName);
        }
        return serviceDeploymentInfos.get(0);
    }

    /**
     * Retrieves the {@link MicoServiceDeploymentInfo} that is used for the deployment
     * of the requested {@link MicoService} as part of a {@link MicoApplication}.
     * There must not be zero or more than one service deployment information stored.
     * If that's the case, an {@link IllegalStateException} will be thrown.
     *
     * @param micoApplication the {@link MicoApplication}
     * @param micoService     the {@link MicoService}
     * @return the one and only existing {@link MicoServiceDeploymentInfo}
     * @throws IllegalStateException if there is no or more than one service deployment information stored
     */
    public MicoServiceDeploymentInfo getExistingServiceDeploymentInfo(MicoApplication micoApplication, MicoService micoService) throws IllegalStateException {
        List<MicoServiceDeploymentInfo> serviceDeploymentInfos = serviceDeploymentInfoRepository
            .findByApplicationAndService(micoApplication.getShortName(), micoApplication.getVersion(),
                micoService.getShortName(), micoService.getVersion());
        if (serviceDeploymentInfos.isEmpty()) {
            String errorMessage = "Previously stored service deployment information for service '" + micoService.getShortName() + "' '" +
                micoService.getVersion() + "' used by application '" + micoApplication.getShortName() + "' '" + micoApplication.getVersion() + "' not found.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        if (serviceDeploymentInfos.size() > 1) {
            List<String> instanceIds = serviceDeploymentInfos.stream().map(MicoServiceDeploymentInfo::getInstanceId).collect(Collectors.toList());
            String errorMessage = "There are " + serviceDeploymentInfos.size() + " service deployment information stored for service '" +
                micoService.getShortName() + "' '" + micoService.getVersion() + "' used by application '" + micoApplication.getShortName() + "' '" +
                micoApplication.getVersion() + "': '" + instanceIds + "'. However, there must be only one.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        return serviceDeploymentInfos.get(0);
    }

    /**
     * Updates an existing {@link MicoServiceDeploymentInfo} in the database
     * based on the values of a {@link MicoServiceDeploymentInfoRequestDTO} object.
     *
     * @param applicationShortName     the short name of the {@link MicoApplication}
     * @param applicationVersion       the version of the {@link MicoApplication}
     * @param serviceShortName         the short name of the {@link MicoService}
     * @param serviceDeploymentInfoDTO the {@link MicoServiceDeploymentInfoRequestDTO}
     * @return the new {@link MicoServiceDeploymentInfo} stored in the database
     * @throws MicoApplicationNotFoundException                  if there is no {@code MicoApplication} with the specified short name and version
     * @throws MicoApplicationDoesNotIncludeMicoServiceException if there is no service included in the specified {@code MicoApplication} with the particular short name
     * @throws MicoServiceDeploymentInformationNotFoundException if there is no {@code MicoServiceDeploymentInfo} stored in the database
     * @throws KubernetesResourceException                       if there are problems with retrieving Kubernetes resource information
     * @throws MicoTopicRoleUsedMultipleTimesException           if a {@link MicoTopicRole} is used multiple times
     */
    public MicoServiceDeploymentInfo updateMicoServiceDeploymentInformation(String applicationShortName, String applicationVersion,
                                                                            String serviceShortName, MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO) throws
        MicoApplicationNotFoundException, MicoApplicationDoesNotIncludeMicoServiceException,
        MicoServiceDeploymentInformationNotFoundException, KubernetesResourceException, MicoTopicRoleUsedMultipleTimesException {

        validateTopics(serviceDeploymentInfoDTO);

        MicoApplication micoApplication = applicationBroker.getMicoApplicationByShortNameAndVersion(applicationShortName, applicationVersion);
        MicoServiceDeploymentInfo storedServiceDeploymentInfo = getMicoServiceDeploymentInformation(applicationShortName, applicationVersion, serviceShortName);

        int oldReplicas = storedServiceDeploymentInfo.getReplicas();
        MicoServiceDeploymentInfo updatedServiceDeploymentInfo = saveValuesToDatabase(serviceDeploymentInfoDTO, storedServiceDeploymentInfo);

        // FIXME: Currently we only supported scale in / scale out.
        // 		  If the MICO service is already deployed, we only update the replicas.
        // 	      The other properties are ignored!
        if (micoKubernetesClient.isApplicationDeployed(micoApplication)) {
            MicoService micoService = updatedServiceDeploymentInfo.getService();
            log.info("MicoApplication '{}' {}' is already deployed. Update the deployment of the included MicoService '{} '{}'.",
                micoApplication.getShortName(), micoApplication.getVersion(), micoService.getShortName(), micoService.getVersion());
            // MICO service is already deployed. Update the replicas.
            int requestedReplicas = updatedServiceDeploymentInfo.getReplicas();
            scaleDeployment(requestedReplicas, oldReplicas, updatedServiceDeploymentInfo);
        }

        return updatedServiceDeploymentInfo;
    }

    /**
     * Scales a deployment of a {@link MicoService} by calculating the required replicas
     * based on the requested and the old replicas.
     *
     * @param requestedReplicas     the requested replicas
     * @param oldReplicas           the old replicas
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @throws KubernetesResourceException if there is an exception during undeploying a Kubernetes service
     */
    private void scaleDeployment(int requestedReplicas, int oldReplicas, MicoServiceDeploymentInfo serviceDeploymentInfo) throws KubernetesResourceException {
        MicoService micoService = serviceDeploymentInfo.getService();
        int replicasDiff = requestedReplicas - oldReplicas;
        if (replicasDiff > 0) {
            log.debug("Increase replicas of MicoService '{}' '{}' by {}.", micoService.getShortName(), micoService.getVersion(), replicasDiff);
            micoKubernetesClient.scaleOut(serviceDeploymentInfo, replicasDiff);
        } else if (replicasDiff < 0) {
            log.debug("Decrease replicas of MicoService '{}' '{}' by {}.", micoService.getShortName(), micoService.getVersion(), replicasDiff);
            micoKubernetesClient.scaleIn(serviceDeploymentInfo, Math.abs(replicasDiff));
        } else {
            // TODO: If no scale operation is required, maybe some other
            // 		 information still needs to be updated.
        }
    }

    /**
     * Saves the values of a {@link MicoServiceDeploymentInfoRequestDTO} to the database.
     * A workaround is required to save the topics correctly:
     * Delete all relationships of this {@link MicoServiceDeploymentInfo} and recreate them.
     *
     * @param serviceDeploymentInfoDTO    the {@link MicoServiceDeploymentInfoRequestDTO} that includes the new values
     * @param storedServiceDeploymentInfo the {@link MicoServiceDeploymentInfo} that is already stored in the database
     * @return the new {@link MicoServiceDeploymentInfo} stored in the database
     */
    private MicoServiceDeploymentInfo saveValuesToDatabase(MicoServiceDeploymentInfoRequestDTO serviceDeploymentInfoDTO, MicoServiceDeploymentInfo storedServiceDeploymentInfo) {
        MicoServiceDeploymentInfo sdiWithAppliedValues = storedServiceDeploymentInfo.applyValuesFrom(serviceDeploymentInfoDTO);
        // At first save the service deployment information with the new values without topics.
        // This is a workaround to save them later with new relationships. Otherwise Neo4j sometimes deletes the relationships!?
        // FIXME https://community.neo4j.com/t/neo4jrepository-save-does-not-persist-updated-relation/5163/6

        sdiWithAppliedValues.setTopics(new ArrayList<>());
        final MicoServiceDeploymentInfo updatedServiceDeploymentInfoWithoutTopics = serviceDeploymentInfoRepository.save(sdiWithAppliedValues);

        final MicoServiceDeploymentInfo finalUpdatedServiceDeploymentInfo;

        if (serviceDeploymentInfoDTO.getTopics().isEmpty()) {
            finalUpdatedServiceDeploymentInfo = updatedServiceDeploymentInfoWithoutTopics;
        } else {
            // If there are topics, apply them and save the service deployment information again.
            MicoServiceDeploymentInfo sdiWithTopics = updatedServiceDeploymentInfoWithoutTopics.setTopics(
                serviceDeploymentInfoDTO.getTopics().stream().map(t -> MicoTopicRole.valueOf(t, updatedServiceDeploymentInfoWithoutTopics)).collect(Collectors.toList()));

            // Check if topics with the same names already exist, if so reuse them.
            MicoServiceDeploymentInfo sdiWithReusedTopics = createOrReuseTopicsInDatabase(sdiWithTopics);
            finalUpdatedServiceDeploymentInfo = serviceDeploymentInfoRepository.save(sdiWithReusedTopics);
        }
        cleanUpTanglingNodes();

        return finalUpdatedServiceDeploymentInfo;
    }

    /**
     * Cleans up tangling nodes related to a {@link MicoServiceDeploymentInfo} in the database.
     *
     * In case addition properties (stored as separate node entity) such as labels, environment variables
     * have been removed from a service deployment information,
     * the standard {@code save()} function of the service deployment information repository will not delete those
     * "tangling" (without relationships) labels (nodes), hence the manual clean up.
     */
    public void cleanUpTanglingNodes() {

        micoLabelRepository.cleanUp();
        micoTopicRepository.cleanUp();
        micoEnvironmentVariableRepository.cleanUp();
        kubernetesDeploymentInfoRepository.cleanUp();
        micoInterfaceConnectionRepository.cleanUp();
        openFaaSFunctionRepository.cleanUp();
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
     * Checks if topics with the same name already exists in the database.
     * If so reuse them by setting the id of the existing Neo4j node and save them.
     * If not create them in the database.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo} containing topics
     */
    MicoServiceDeploymentInfo createOrReuseTopicsInDatabase(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        List<MicoTopicRole> topicRoles = serviceDeploymentInfo.getTopics();

        for (MicoTopicRole topicRole : topicRoles) {
            MicoTopic topic = topicRole.getTopic();
            Optional<MicoTopic> existingTopicOptional = micoTopicRepository.findByName(topic.getName());
            if (existingTopicOptional.isPresent()) {
                // Topic node with same name already exists -> Reuse it
                topicRole.setTopic(existingTopicOptional.get());
            } else {
                // Topic node with requested name does not exist yet -> Create it
                topic.setId(null);
                MicoTopic savedTopic = micoTopicRepository.save(topic);
                topicRole.setTopic(savedTopic);
            }
        }
        return serviceDeploymentInfo;
    }

    /**
     * Checks if the given OpenFaaS function name is already present in the database.
     * If so it will be reused. Otherwise a new node will be created.
     *
     * @param serviceDeploymentInfo the {@link MicoServiceDeploymentInfo}
     * @return the updated {@link MicoServiceDeploymentInfo}
     */
    MicoServiceDeploymentInfo createOrReuseOpenFaaSFunctionsInDatabase(MicoServiceDeploymentInfo serviceDeploymentInfo) {
        OpenFaaSFunction openFaaSFunction = serviceDeploymentInfo.getOpenFaaSFunction();
        if (openFaaSFunction == null) {
            // There is no OpenFaaS function -> nothing to do.
            return serviceDeploymentInfo;
        }

        Optional<OpenFaaSFunction> existingOpenFaaSFunctionOptional = openFaaSFunctionRepository.findByName(openFaaSFunction.getName());
        if (existingOpenFaaSFunctionOptional.isPresent()) {
            // OpenFaasFunction node with same name already exists -> Reuse it
            serviceDeploymentInfo.setOpenFaaSFunction(existingOpenFaaSFunctionOptional.get());
        } else {
            // OpenFaasFunction node with requested name does not exist yet -> Create it
            openFaaSFunction.setId(null);
            OpenFaaSFunction savedOpenFaaSFunction = openFaaSFunctionRepository.save(openFaaSFunction);
            serviceDeploymentInfo.setOpenFaaSFunction(savedOpenFaaSFunction);
        }
        return serviceDeploymentInfo;
    }

    /**
     * Sets the default environment variables for Kafka-enabled MicoServices. See {@link MicoEnvironmentVariable.DefaultNames}
     * for a complete list.
     *
     * @param micoServiceDeploymentInfo The {@link MicoServiceDeploymentInfo} with an corresponding MicoService
     */
    void setDefaultDeploymentInformationForKafkaEnabledService(MicoServiceDeploymentInfo micoServiceDeploymentInfo) {
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
        // set a unique group id for each service instance
        micoEnvironmentVariables.add(new MicoEnvironmentVariable()
            .setName(MicoEnvironmentVariable.DefaultNames.KAFKA_GROUP_ID.name())
            .setValue(micoServiceDeploymentInfo.getInstanceId()));
        micoEnvironmentVariables.addAll(openFaaSConfig.getDefaultEnvironmentVariablesForOpenFaaS());
        List<MicoTopicRole> topics = micoServiceDeploymentInfo.getTopics();
        topics.addAll(kafkaConfig.getDefaultTopics(micoServiceDeploymentInfo));
    }

}
