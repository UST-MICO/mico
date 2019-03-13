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

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import io.github.ust.mico.core.model.MicoService;

public interface MicoServiceRepository extends Neo4jRepository<MicoService, Long> {

    @Override
    List<MicoService> findAll();

    @Override
    List<MicoService> findAll(@Depth int depth);

    @Depth(2)
    List<MicoService> findByShortName(@Param("shortName") String shortName);

    @Depth(2)
    Optional<MicoService> findByShortNameAndVersion(String shortName, String version);
    
    @Query("MATCH (application:MicoApplication)-[:INCLUDES_SERVICE]-(service:MicoService) WHERE application.shortName = {applicationShortName} AND application.version = {applicationVersion} RETURN COLLECT(service) AS services")
    List<MicoService> findAllByApplication(@Param("applicationShortName") String applicationShortName, @Param("applicationVersion") String applicationVersion);

    // Doesn't this belong in the MicoServiceInterfaceRepository?
    @Query("MATCH (service:MicoService)-[:PROVIDES]->(interface:MicoServiceInterface)-[:PROVIDES]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} DETACH DELETE interface, port")
    void deleteInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService) WHERE service.shortName = {shortName} AND service.version = {version} WITH service OPTIONAL MATCH (service)-[:PROVIDES]->(interface:MicoServiceInterface) WITH service, interface OPTIONAL MATCH (interface)-[:PROVIDES]->(port:MicoServicePort) DETACH DELETE service, interface, port")
    void deleteServiceByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);
    
    /**
     * Finds all services (dependees) the given service (depender) depends on
     * as well as the service (depender) itself.
     * 
     * @param shortName the short name of the {@link MicoService} (depender). 
     * @param version the version of the {@link MicoService} (depender).
     * @return a list of {@link MicoService MicoServices} including all dependees
     * 		   as well as the depender..
     */
    @Query("MATCH (service:MicoService)-[:DEPENDS_ON*0..]->(dependency:MicoService) WHERE service.shortName = {shortName} AND service.version = {version} RETURN COLLECT(DISTINCT dependency)")
    List<MicoService> findDependeesIncludeDepender(@Param("shortName") String shortName, @Param("version") String version);

    /**
     * Finds all services (dependees) the given service (depender) depends on.
     * 
     * @param shortName the short name of the {@link MicoService} (depender). 
     * @param version the version of the {@link MicoService} (depender).
     * @return a list of {@link MicoService MicoServices}.
     */
    @Query("MATCH (service:MicoService)-[:DEPENDS_ON*1..]->(dependency:MicoService) WHERE service.shortName = {shortName} AND service.version = {version} RETURN COLLECT(DISTINCT dependency)")
    List<MicoService> findDependees(@Param("shortName") String shortName, @Param("version") String version);
    
    /**
     * Finds all services (dependers) that depend on the given service (dependee).
     * 
     * @param shortName the short name of the {@link MicoService} (dependee). 
     * @param version the version of the {@link MicoService} (dependee).
     * @return a list of {@link MicoService MicoServices}.
     */
    @Query("MATCH (service:MicoService)-[:DEPENDS_ON]->(dependency:MicoService) WHERE dependency.shortName = {shortName} AND dependency.version = {version} RETURN COLLECT(service)")
    List<MicoService> findDependers(@Param("shortName") String shortName, @Param("version") String version);

}
