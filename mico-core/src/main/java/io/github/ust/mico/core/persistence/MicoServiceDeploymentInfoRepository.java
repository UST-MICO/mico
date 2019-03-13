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
	
	// TODO: Include 'KubernetesDeploymentInfo' in all find queries via OPTIONAL MATCH.
	// TODO: Include 'KubernetesDeploymentInfo' in all delete queries via DETACH DELETE.
	
    /**
     * Retrieves all service deployment information of a particular application.
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @return a {@link List} of {@link MicoServiceDeploymentInfo} instances.
     */
    @Query("MATCH (application:MicoApplication)-[:PROVIDES]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(service:MicoService) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(label:MicoLabel) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(env:MicoEnvironmentVariable) "
        + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-->()")
    public List<MicoServiceDeploymentInfo> findAllByApplication(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion);
    
    /**
     * Retrieves the deployment information for a particular application and service. 
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @param serviceShortName the short name of the {@link MicoService}.
     * @return an {@link Optional} of {@link MicoServiceDeploymentInfo}.
     */
    @Query("MATCH (application:MicoApplication)-[:PROVIDES]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(service:MicoService) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(label:MicoLabel) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(env:MicoEnvironmentVariable) "
        + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
        + "AND service.shortName = {serviceShortName} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-->()")
    public Optional<MicoServiceDeploymentInfo> findByApplicationAndService(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName);
    
    /**
     * Retrieves the deployment information for a particular application and service.
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @param serviceShortName the short name of the {@link MicoService}.
     * @param serviceVersion the version of the {@link MicoService}.
     * @return an {@link Optional} of {@link MicoServiceDeploymentInfo}.
     */
    @Query("MATCH (application:MicoApplication)-[:PROVIDES]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(service:MicoService) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(label:MicoLabel) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(env:MicoEnvironmentVariable) "
        + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
        + "AND service.shortName = {serviceShortName} AND service.version = {serviceVersion} "
        + "RETURN (sdi:MicoServiceDeploymentInfo)-->()")
    public Optional<MicoServiceDeploymentInfo> findByApplicationAndService(
        @Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName,
        @Param("serviceVersion") String serviceVersion);
    
    /**
     * Delete the deployment for a particular application and service.
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @param serviceShortName the short name of the {@link MicoService}.
     */
    @Query("MATCH (application:MicoApplication)-[:PROVIDES]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(service:MicoService) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(label:MicoLabel) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(env:MicoEnvironmentVariable) "
    	 + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
         + "AND service.shortName = {serviceShortName} "
         + "DETACH DELETE sdi, label, env")
    public void deleteByApplicationAndService(
    	@Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName);
    
    /**
     * Delete the deployment for a particular application and service.
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @param serviceShortName the short name of the {@link MicoService}.
     * @param serviceVersion the version of the {@link MicoService}.
     */
    @Query("MATCH (application:MicoApplication)-[:PROVIDES]->(sdi:MicoServiceDeploymentInfo)-[:FOR]->(service:MicoService) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(label:MicoLabel) OPTIONAL MATCH (sdi:MicoServiceDeploymentInfo)-[:HAS]->(env:MicoEnvironmentVariable) "
    	 + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
         + "AND service.shortName = {serviceShortName} AND service.version = {serviceVersion} "
         + "DETACH DELETE sdi, label, env")
    public void deleteByApplicationAndService(
    	@Param("applicationShortName") String applicationShortName,
        @Param("applicationVersion") String applicationVersion,
        @Param("serviceShortName") String serviceShortName,
        @Param("serviceVersion") String serviceVersion);
    
}
