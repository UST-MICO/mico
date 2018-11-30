package io.github.ust.mico.core;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends Neo4jRepository<Service, Long> {

    Service findByName(@Param("name") String name);

    Service findByShortName(@Param("shortName") String shortName);

    Service findByName(String name, @Depth int depth);

    Service findByShortName(String shortName, @Depth int depth);
}
