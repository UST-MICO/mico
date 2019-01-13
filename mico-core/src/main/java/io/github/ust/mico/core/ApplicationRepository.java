package io.github.ust.mico.core;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends Neo4jRepository<Application, Long> {
    @Override
    List<Application> findAll();

    Optional<Application> findByShortNameAndVersion(String shortName, String version, @Depth int depth);

    Optional<Application> findByShortNameAndVersion(String shortName, String version);

    @Query("match (application:Application) where application.shortName = {shortName} and application.version = {version} delete application")
    void deleteApplicationByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);
}
