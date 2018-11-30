package io.github.ust.mico.core;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "serviceInterfaces", path = "serviceInterfaces")
public interface ServiceInterfaceRepository extends Neo4jRepository<ServiceInterface, Long> {
}
