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
