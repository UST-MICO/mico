package io.github.ust.mico.core.model;

import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Represents the information necessary for deploying
 * a {@link MicoApplication}.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoApplicationDeploymentInfo {

    @Id
    @GeneratedValue
    private final long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    // The service deployment info for each service this
    // application is composed of (service id -> service deployment info).
    @ApiModelProperty(required = true)
    @Relationship // TODO: @Jan -> check please.
    @Singular
    private final Map<Long, MicoServiceDeploymentInfo> serviceDeploymentInfos;

}
