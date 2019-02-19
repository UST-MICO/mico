package io.github.ust.mico.core.persistence;

import io.github.ust.mico.core.model.MicoBackgroundTask;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MicoBackgroundTaskRepository extends CrudRepository<MicoBackgroundTask, String> {
}
