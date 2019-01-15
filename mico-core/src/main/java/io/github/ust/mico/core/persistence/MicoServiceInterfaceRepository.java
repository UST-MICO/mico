package io.github.ust.mico.core.persistence;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.github.ust.mico.core.model.MicoServiceInterface;

public interface MicoServiceInterfaceRepository extends Neo4jRepository<MicoServiceInterface, Long> {
    
}
