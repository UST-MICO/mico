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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

public interface MicoApplicationRepository extends Neo4jRepository<MicoApplication, Long> {

    @Override
    List<MicoApplication> findAll();

    @Override
    List<MicoApplication> findAll(@Depth int depth);

    @Depth(3)
    List<MicoApplication> findByShortName(String shortName);

    @Depth(3)
    Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version);

    /**
     * Find all applications that are using the given service.
     *
     * @param shortName the shortName of the {@link MicoService}
     * @param version the version of the {@link MicoService}
     * @return a list of {@link MicoApplication}
     */
    @Query("MATCH (a:MicoApplication)-[i:INCLUDES]-(s:MicoService) "
    	+ "WHERE s.shortName = {shortName} AND s.version = {version} "
    	+ "RETURN COLLECT(a) AS applications")
    List<MicoApplication> findAllByUsedService(
    	@Param("shortName") String shortName,
    	@Param("version") String version);
    
}
