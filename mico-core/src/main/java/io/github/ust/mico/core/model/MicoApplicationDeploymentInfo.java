package io.github.ust.mico.core.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Map;

/**
 * Represents the information necessary for deploying
 * a {@link MicoApplication}.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoApplicationDeploymentInfo {

    /**
     * The id of this application deployment info.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The service deployment info for each service this
     * application is composed of (service id -> service deployment info).
     */
    @ApiModelProperty(required = true)
    @Singular
    private final Map<Long, MicoServiceDeploymentInfo> serviceDeploymentInfos;

}
