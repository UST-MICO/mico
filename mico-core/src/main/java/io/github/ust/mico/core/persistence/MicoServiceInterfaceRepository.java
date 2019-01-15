package io.github.ust.mico.core.persistence;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MicoServiceInterfaceRepository extends Neo4jRepository<ServiceInterface, Long> {
}
