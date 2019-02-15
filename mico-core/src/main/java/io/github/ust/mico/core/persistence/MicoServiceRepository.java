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
import io.github.ust.mico.core.model.MicoServiceInterface;

public interface MicoServiceRepository extends Neo4jRepository<MicoService, Long> {

    @Override
    List<MicoService> findAll();

    @Override
    List<MicoService> findAll(@Depth int depth);

    @Depth(2)
    List<MicoService> findByShortName(@Param("shortName") String shortName);

    @Depth(2)
    Optional<MicoService> findByShortNameAndVersion(String shortName, String version);

    /**
     * Find a specific service interface.
     *
     * The returned interface will NOT have ports mapped by the ogm.
     * If you want to have a interface with mapped ports use the serviceInterface
     * list in the corresponding MicoService object returned by findByShortNameAndVersion!
     *
     * @param serviceInterfaceName
     * @param shortName
     * @param version
     * @return
     */
    @Query("MATCH (service:MicoService)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface)-[:PROVIDES_PORTS]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} return COLLECT(port) AS ports")
    Optional<MicoServiceInterface> findInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface)-[:PROVIDES_PORTS]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} DETACH DELETE interface, port")
    void deleteInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService) WHERE service.shortName = {shortName} AND service.version = {version} WITH service OPTIONAL MATCH (service)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface) WITH service, interface OPTIONAL MATCH (interface)-[:PROVIDES_PORTS]->(port:MicoServicePort) DETACH DELETE service, interface, port")
    void deleteServiceByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);

}
