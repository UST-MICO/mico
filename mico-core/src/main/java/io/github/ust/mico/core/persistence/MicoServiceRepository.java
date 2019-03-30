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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;

public interface MicoServiceRepository extends Neo4jRepository<MicoService, Long> {

    @Override
    List<MicoService> findAll();

    @Override
    List<MicoService> findAll(@Depth int depth);
    
    /**
     * Finds all services that are included by a given application.
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @return a list of {@link MicoService MicoServices}.
     */
    @Query("MATCH (a:MicoApplication)-[:INCLUDES]-(s:MicoService) "
    	+ "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
    	+ "RETURN COLLECT(s) AS services")
    List<MicoService> findAllByApplication(
    	@Param("applicationShortName") String applicationShortName,
    	@Param("applicationVersion") String applicationVersion);
    
    /**
     * Finds the service that is included in a given application
     * for a given service short name.
     * 
     * @param applicationShortName the short name of the {@link MicoApplication}.
     * @param applicationVersion the version of the {@link MicoApplication}.
     * @param serviceShortName the short name of the {@link MicoService}.
     * @return a list of {@link MicoService MicoServices}.
     */
    @Query("MATCH (a:MicoApplication)-[:INCLUDES]-(s:MicoService) "
    	+ "WHERE a.shortName = {applicationShortName} AND a.version = {applicationVersion} "
    	+ "AND s.shortName = {serviceShortName} "
    	+ "RETURN COLLECT(s) AS services")
    Optional<MicoService> findAllByApplicationAndServiceShortName(
    	@Param("applicationShortName") String applicationShortName,
    	@Param("applicationVersion") String applicationVersion,
    	@Param("serviceShortName") String serviceShortName);

    @Depth(2)
    List<MicoService> findByShortName(@Param("shortName") String shortName);

    @Depth(2)
    Optional<MicoService> findByShortNameAndVersion(String shortName, String version);
    
    /**
     * Finds all services (dependees) the given service (depender) depends on
     * as well as the service (depender) itself.
     * 
     * @param shortName the short name of the {@link MicoService} (depender). 
     * @param version the version of the {@link MicoService} (depender).
     * @return a list of {@link MicoService MicoServices} including all dependees
     * 		   as well as the depender.
     */
    @Query("MATCH (s:MicoService)-[:DEPENDS_ON*0..]->(d:MicoService) "
    	+ "WHERE s.shortName = {shortName} AND s.version = {version} "
    	+ "RETURN COLLECT(DISTINCT d)")
    List<MicoService> findDependeesIncludeDepender(
    	@Param("shortName") String shortName,
    	@Param("version") String version);

    /**
     * Finds all services (dependees) the given service (depender) depends on.
     * 
     * @param shortName the short name of the {@link MicoService} (depender). 
     * @param version the version of the {@link MicoService} (depender).
     * @return a list of {@link MicoService MicoServices}.
     */
    @Query("MATCH (s:MicoService)-[:DEPENDS_ON*1..]->(dependency:MicoService) "
    	+ "WHERE s.shortName = {shortName} AND s.version = {version} "
    	+ "RETURN COLLECT(DISTINCT dependency)")
    List<MicoService> findDependees(
    	@Param("shortName") String shortName,
    	@Param("version") String version);
    
    /**
     * Finds all services (dependers) that depend on the given service (dependee).
     * 
     * @param shortName the short name of the {@link MicoService} (dependee). 
     * @param version the version of the {@link MicoService} (dependee).
     * @return a list of {@link MicoService MicoServices}.
     */
    @Query("MATCH (s:MicoService)-[:DEPENDS_ON]->(dependency:MicoService) "
    	+ "WHERE dependency.shortName = {shortName} AND dependency.version = {version} "
    	+ "RETURN COLLECT(s)")
    List<MicoService> findDependers(
    	@Param("shortName") String shortName,
    	@Param("version") String version);

    @Query("MATCH (s:MicoService) WHERE s.shortName = {shortName} AND s.version = {version} "
    	+ "WITH s OPTIONAL MATCH (s)-[:PROVIDES]->(i:MicoServiceInterface) "
    	+ "WITH s, i OPTIONAL MATCH (i)-[:PROVIDES]->(p:MicoServicePort) "
    	+ "DETACH DELETE s, i, p")
    void deleteServiceByShortNameAndVersion(
    	@Param("shortName") String shortName,
    	@Param("version") String version);

}
