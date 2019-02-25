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

import io.github.ust.mico.core.model.MicoServiceDeploymentInfo;
import io.github.ust.mico.core.model.MicoServiceDeploymentInfoQueryResult;

public interface MicoServiceDeploymentInfoRepository extends Neo4jRepository<MicoServiceDeploymentInfo, Long> {
    
    @Query("MATCH (application:MicoApplication)-[serviceDeploymentInfo:INCLUDES_SERVICE]-(service:MicoService) "
            + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
            + "RETURN application, serviceDeploymentInfo, service")
    List<MicoServiceDeploymentInfoQueryResult> findAllByApplication(
            @Param("applicationShortName") String applicationShortName,
            @Param("applicationVersion") String applicationVersion);
    
    @Query("MATCH (application:MicoApplication)-[serviceDeploymentInfo:INCLUDES_SERVICE]-(service:MicoService) "
            + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
            + "AND service.shortName = {serviceShortName} "
            + "RETURN application, serviceDeploymentInfo, service")
    Optional<MicoServiceDeploymentInfoQueryResult> findByApplicationAndService(
            @Param("applicationShortName") String applicationShortName,
            @Param("applicationVersion") String applicationVersion,
            @Param("serviceShortName") String serviceShortName);
    
    @Query("MATCH (application:MicoApplication)-[serviceDeploymentInfo:INCLUDES_SERVICE]-(service:MicoService) "
            + "WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} "
            + "AND service.shortName = {serviceShortName} AND service.version = {serviceVersion} "
            + "RETURN application, serviceDeploymentInfo, service")
    Optional<MicoServiceDeploymentInfoQueryResult> findByApplicationAndService(
            @Param("applicationShortName") String applicationShortName,
            @Param("applicationVersion") String applicationVersion,
            @Param("serviceShortName") String serviceShortName,
            @Param("serviceVersion") String serviceVersion);
    
}
