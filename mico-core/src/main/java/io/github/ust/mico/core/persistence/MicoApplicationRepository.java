package io.github.ust.mico.core.persistence;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.github.ust.mico.core.model.MicoApplication;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MicoApplicationRepository extends Neo4jRepository<MicoApplication, Long> {
    
    @Override
    List<MicoApplication> findAll();
    
    @Override
    List<MicoApplication> findAll(@Depth int depth);

    @Depth(3)
    List<MicoApplication> findByShortName(@Param("shortName") String shortName);

    @Depth(3)
    Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version);
    
}
