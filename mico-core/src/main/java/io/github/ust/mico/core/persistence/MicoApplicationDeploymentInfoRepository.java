package io.github.ust.mico.core.persistence;

import io.github.ust.mico.core.model.MicoApplicationDeploymentInfo;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MicoApplicationDeploymentInfoRepository extends Neo4jRepository<MicoApplicationDeploymentInfo, Long> {

}
