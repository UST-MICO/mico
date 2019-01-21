package io.github.ust.mico.core.persistence;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.github.ust.mico.core.model.MicoServiceDependency;

public interface MicoServiceDependencyRepository extends Neo4jRepository<MicoServiceDependency, Long> {
    
}
