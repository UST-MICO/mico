package io.github.ust.mico.core;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ServiceInterfaceRepository extends Neo4jRepository<ServiceInterface, Long> {
}
