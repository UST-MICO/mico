package io.github.ust.mico.core;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ApplicationRepository extends Neo4jRepository<Application, Long> {
}
