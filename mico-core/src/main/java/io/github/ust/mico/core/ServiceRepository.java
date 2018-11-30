package io.github.ust.mico.core;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(collectionResourceRel = "services", path = "services")
public interface ServiceRepository extends Neo4jRepository<Service, Long> {

    Service findByName(@Param("name") String name);
    Service findByShortName(@Param("shortName") String shortName);

    @RestResource(exported = false)
    Service findByName(String name, @Depth int depth);

    @RestResource(exported = false)
    Service findByShortName(String shortName, @Depth int depth);
}
