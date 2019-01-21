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

    List<MicoService> findByShortName(@Param("shortName") String shortName);

    List<MicoService> findByShortName(@Param("shortName") String shortName, @Depth int depth);

    Optional<MicoService> findByShortNameAndVersion(String shortName, String version);

    Optional<MicoService> findByShortNameAndVersion(String shortName, String version, @Depth int depth);

    // TODO: probably move to MicoServiceInterfaceRepository
    @Query("MATCH (service:MicoService)-[:MICO_SERVICE_INTERFACES]->(interface:MicoServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} return interface")
    List<MicoServiceInterface> findInterfacesOfService(@Param("shortName") String shortName, @Param("version") String version);

    // TODO: probably move to MicoServiceInterfaceRepository
    @Query("MATCH (service:MicoService)-[:MICO_SERVICE_INTERFACES]->(interface:MicoServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} return interface")
    Optional<MicoServiceInterface> findInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    // TODO: probably move to MicoServiceInterfaceRepository
    @Query("MATCH (service:MicoService)-[:MICO_SERVICE_INTERFACES]->(interface:MicoServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} detach delete interface")
    void deleteInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:MicoService) where service.shortName = {shortName} and service.version = {version} delete service")
    void deleteServiceByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);

}
