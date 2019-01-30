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

    List<MicoService> findByShortName(@Param("shortName") String shortName);

    List<MicoService> findByShortName(@Param("shortName") String shortName, @Depth int depth);

    Optional<MicoService> findByShortNameAndVersion(String shortName, String version);

    Optional<MicoService> findByShortNameAndVersion(String shortName, String version, @Depth int depth);

    @Query("MATCH (service:MicoService)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface)-[:PROVIDES_PORTS]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} return COLLECT(port) AS ports")
    List<MicoServiceInterface> findInterfacesOfService(@Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface)-[:PROVIDES_PORTS]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} return COLLECT(port) AS ports")
    Optional<MicoServiceInterface> findInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface)-[:PROVIDES_PORTS]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} DETACH DELETE interface, port")
    void deleteInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService)-[:PROVIDES_INTERFACES]->(interface:MicoServiceInterface)-[:PROVIDES_PORTS]->(port:MicoServicePort) WHERE service.shortName = {shortName} AND service.version = {version} DETACH DELETE service, interface, port")
    void deleteServiceByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);

}
