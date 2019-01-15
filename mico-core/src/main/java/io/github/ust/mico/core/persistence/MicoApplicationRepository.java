package io.github.ust.mico.core.persistence;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.github.ust.mico.core.model.MicoApplication;

import java.util.List;
import java.util.Optional;

public interface MicoApplicationRepository extends Neo4jRepository<MicoApplication, Long> {
    @Override
    List<MicoApplication> findAll();

    // TODO: Adapt to new domain model
    @Deprecated
    Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version, @Depth int depth);

    // TODO: Adapt to new domain model
    @Deprecated
    Optional<MicoApplication> findByShortNameAndVersion(String shortName, String version);
}
