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

package io.github.ust.mico.core.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;

public interface MicoServiceDeploymentInfoRepository extends Neo4jRepository<MicoServiceDeploymentInfo, Long> {

    Optional<MicoServiceDeploymentInfo> findByInstanceId(String instanceId);

    /**
     * Retrieves all service deployment information of a particular application.
     * Also includes these which are used for the deployments of KafkaFaasConnector instances.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @return a {@link List} of {@link MicoServiceDeploymentInfo} instances.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-[:FOR|:HAS*0..1]->(), (s)-[:PROVIDES*2]->()")
    List<MicoServiceDeploymentInfo> findAllByApplication(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion);

    /**
     * Retrieves all service deployment information that are used for normal MicoServices of a particular application.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @return a {@link List} of {@link MicoServiceDeploymentInfo} instances.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-[:FOR|:HAS*0..1]->(), (s)-[:PROVIDES*2]->()")
    List<MicoServiceDeploymentInfo> findMicoServiceSDIsByApplication(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion);

    /**
     * Retrieves all service deployment information that are used for KafkaFaasConnectors of a particular application.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @return a {@link List} of {@link MicoServiceDeploymentInfo} instances.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-[:FOR|:HAS*0..1]->(), (s)-[:PROVIDES*2]->()")
    List<MicoServiceDeploymentInfo> findKFConnectorSDIsByApplication(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion);

    /**
     * Retrieves all service deployment information of a service.
     * Note that one service can be used by (included in) multiple applications.
     * Also works with a KafkaFaasConnector instance.
     *
     * @param serviceShortName the short name of the {@link MicoService}.
     * @param serviceVersion   the version of the {@link MicoService}.
     * @return a {@link List} of {@link MicoServiceDeploymentInfo} instances.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE s.shortName = {serviceShortName} AND s.version = {serviceVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-[:FOR|:HAS*0..1]->(), (s)-[:PROVIDES*2]->()")
    List<MicoServiceDeploymentInfo> findAllByService(
        @Param("serviceShortName") String serviceShortName,
        @Param("serviceVersion") String serviceVersion);

    /**
     * Retrieves the deployment information for a particular application and service.
     * Also works with a KafkaFaasConnector instance.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @param serviceShortName     the short name of the {@link MicoService}.
     * @return an {@link Optional} of {@link MicoServiceDeploymentInfo}.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "AND s.shortName = {serviceShortName} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-[:FOR|:HAS*0..1]->(), (s)-[:PROVIDES*2]->()")
    Optional<MicoServiceDeploymentInfo> findByApplicationAndService(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName);

    /**
     * Retrieves the deployment information for a particular application and service.
     * Also works with a KafkaFaasConnector instance.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @param serviceShortName     the short name of the {@link MicoService}.
     * @param serviceVersion       the version of the {@link MicoService}.
     * @return an {@link Optional} of {@link MicoServiceDeploymentInfo}.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "AND s.shortName = {serviceShortName} AND s.version = {serviceVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-[:FOR|:HAS*0..1]->(), (s)-[:PROVIDES*2]->()")
    Optional<MicoServiceDeploymentInfo> findByApplicationAndService(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName,
        @Param("serviceVersion") String serviceVersion);

    /**
     * Deletes all deployment information for all versions of an application including the ones for the KafkaFaasConnectors.
     * All additional properties of a {@link MicoServiceDeploymentInfo} that
     * are stored as a separate node entity and connected to it via a {@code [:HAS]}
     * relationship will be deleted, too.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} "
        + "WITH a, sdi OPTIONAL MATCH (sdi)-[:HAS]->(additionalProperty) "
        + "WHERE a.shortName = {applicationShortName} "
        + "DETACH DELETE sdi, additionalProperty")
    void deleteAllByApplication(@Param("applicationShortName") String applicationShortName);

    /**
     * Deletes all deployment information for a particular application including the ones for the KafkaFaasConnectors.
     * All additional properties of a {@link MicoServiceDeploymentInfo} that
     * are stored as a separate node entity and connected to it via a {@code [:HAS]}
     * relationship will be deleted, too.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "WITH a, sdi OPTIONAL MATCH (sdi)-[:HAS]->(additionalProperty) "
        + "DETACH DELETE sdi, additionalProperty")
    void deleteAllByApplication(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion);

    /**
     * Deletes the deployment information for a particular application and service.
     * All additional properties of a {@link MicoServiceDeploymentInfo} that
     * are stored as a separate node entity and connected to it via a {@code [:HAS]}
     * relationship will be deleted, too.
     * Also works with a KafkaFaasConnector instance.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @param serviceShortName     the short name of the {@link MicoService}.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "AND s.shortName = {serviceShortName} "
        + "WITH a, sdi, s OPTIONAL MATCH (sdi)-[:HAS]->(additionalProperty) "
        + "DETACH DELETE sdi, additionalProperty")
    void deleteByApplicationAndService(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName);

    /**
     * Deletes the deployment information for a particular application and service.
     * All additional properties of a {@link MicoServiceDeploymentInfo} that
     * are stored as a separate node entity and connected to it via a {@code [:HAS]}
     * relationship will be deleted, too.
     * Also works with a KafkaFaasConnector instance.
     *
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion   the version of the {@link MicoApplication}.
     * @param serviceShortName     the short name of the {@link MicoService}.
     * @param serviceVersion       the version of the {@link MicoService}.
     */
    @Query("MATCH (a:MicoApplication)-[:PROVIDES|:PROVIDES_KF_CONNECTOR]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(s:MicoService) "
        + "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
        + "AND s.shortName = {serviceShortName} AND s.version = {serviceVersion} "
        + "WITH a, sdi, s OPTIONAL MATCH (sdi)-[:HAS]->(additionalProperty) "
        + "DETACH DELETE sdi, additionalProperty")
    void deleteByApplicationAndService(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName,
        @Param("serviceVersion") String serviceVersion);

}
