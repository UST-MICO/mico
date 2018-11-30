package io.github.ust.mico.core;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "dependsOns", path = "dependsOns")
public interface DependsOnRepository extends Neo4jRepository<DependsOn, Long> {
}
