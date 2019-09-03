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

import io.github.ust.mico.core.exception.MicoApplicationNotFoundException;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KafkaFaasConnectorDeploymentInfoBroker {

    @Autowired
    private MicoApplicationBroker applicationBroker;

    /**
     * Fetches a list of {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos} of all KafkaFaasConnector instances
     * associated with the specified {@link MicoApplication}.
     *
     * @param micoApplicationShortName the shortName of the micoApplication
     * @param micoApplicationVersion   the version of the micoApplication
     * @return the list of {@link MicoServiceDeploymentInfo MicoServiceDeploymentInfos}
     * @throws MicoApplicationNotFoundException if there is no such micoApplication
     */
    public List<MicoServiceDeploymentInfo> getKafkaFaasConnectorDeploymentInformations(
        String micoApplicationShortName, String micoApplicationVersion) throws MicoApplicationNotFoundException {

        MicoApplication micoApplication = applicationBroker.getMicoApplicationByShortNameAndVersion(micoApplicationShortName, micoApplicationVersion);
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos = micoApplication.getKafkaFaasConnectorDeploymentInfos();
        log.debug("There are {} KafkaFaasConnector deployment information for MicoApplication '{}' in version '{}'.",
            micoServiceDeploymentInfos.size(), micoApplicationShortName, micoApplicationVersion);
        return micoApplication.getKafkaFaasConnectorDeploymentInfos();
    }

    /**
     * Filters the list of {@link MicoServiceDeploymentInfo} from {@link KafkaFaasConnectorDeploymentInfoBroker#getKafkaFaasConnectorDeploymentInformations(String, String)}
     * for a specific instanceId.
     *
     * @param micoApplicationShortName
     * @param micoApplicationVersion
     * @param instanceId
     * @return a single {@link MicoServiceDeploymentInfo} with an instanceId equal to the give one.
     * @throws MicoApplicationNotFoundException
     */
    public Optional<MicoServiceDeploymentInfo> getKafkaFaasConnectorDeploymentInformationInstance(
        String micoApplicationShortName, String micoApplicationVersion, String instanceId) throws MicoApplicationNotFoundException {
        List<MicoServiceDeploymentInfo> micoServiceDeploymentInfos = getKafkaFaasConnectorDeploymentInformations(micoApplicationShortName, micoApplicationVersion);
        Optional<MicoServiceDeploymentInfo> micoServiceDeploymentInfoOptional = micoServiceDeploymentInfos.stream().filter(micoServiceDeploymentInfo -> micoServiceDeploymentInfo.getInstanceId().equals(instanceId))
            .reduce((a, b) -> {
                    throw new IllegalStateException("There are multiple kafkaFaasConnectors with the same instance: " + a + ", " + b);
                }
            );
        if (micoServiceDeploymentInfoOptional.isPresent()) {
            log.debug("There is a micoServiceDeploymentInfo for MicoApplication '{}' in version '{}' with the instanceId '{}'.", micoApplicationShortName, micoApplicationVersion, instanceId);
        } else {
            log.debug("There is no micoServiceDeploymentInfo for MicoApplication '{}' in version '{}' with the instanceId '{}'.", micoApplicationShortName, micoApplicationVersion, instanceId);
        }
        return micoServiceDeploymentInfoOptional;
    }

}
