package io.github.ust.mico.core.persistence;
import io.github.ust.mico.core.model.OpenFaaSFunction;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.Optional;

public interface OpenFaaSFunctionRepository extends Neo4jRepository<OpenFaaSFunction, Long> {
    /**
     * Deletes all OpenFaaS functions that do <b>not</b> have any relationship to another node.
     */
    @Query("MATCH (openFaaSFunction:OpenFaaSFunction) WHERE size((openFaaSFunction)--()) = 0 DELETE openFaaSFunction")
    void cleanUp();

    Optional<OpenFaaSFunction> findByName(String name);
}
