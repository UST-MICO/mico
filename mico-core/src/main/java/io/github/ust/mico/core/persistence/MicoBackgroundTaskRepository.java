package io.github.ust.mico.core.persistence;

import io.github.ust.mico.core.model.MicoBackgroundTask;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MicoBackgroundTaskRepository extends Neo4jRepository<MicoBackgroundTask,Long> {
}
