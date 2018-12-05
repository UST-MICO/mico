package io.github.ust.mico.core;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepository extends Neo4jRepository<Service, Long> {

    List<Service> findByName(@Param("name") String name);

    List<Service> findByShortName(@Param("shortName") String shortName);

    List<Service> findByName(@Param("name") String name, @Depth int depth);

    List<Service> findByShortName(@Param("shortName") String shortName, @Depth int depth);

    Service findByNameAndVersion(@Param("name") String name, @Param("version") String version);

    Service findByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);

    Service findByNameAndVersion(@Param("name") String name, @Param("version") String version, @Depth int depth);

    Service findByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version, @Depth int depth);
}
