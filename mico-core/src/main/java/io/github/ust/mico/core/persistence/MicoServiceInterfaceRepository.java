package io.github.ust.mico.core.persistence;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import io.github.ust.mico.core.model.MicoServiceInterface;
import io.github.ust.mico.core.model.MicoServicePort;

public interface MicoServiceInterfaceRepository extends Neo4jRepository<MicoServiceInterface, Long> {

    @Override
    List<MicoServiceInterface> findAll();
    
    @Query("MATCH (interface:MicoServiceInterface)-[:PORTS]->(port:MicoServicePort) WHERE interface.serviceInterfaceName = {serviceInterfaceName} return port")
    List<MicoServicePort> findPortsOfServiceInterface(@Param("serviceInterfaceName") String serviceInterfaceName);
    
}
