package io.github.ust.mico.core.model;

import java.util.HashMap;
import java.util.Map;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents the information necessary for deploying
 * a {@link MicoApplication}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Service Deployment Info"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The service deployment info for each service this " +
                "application is composed of.")
        }
    )})
    private Map<Long, MicoServiceDeploymentInfo> serviceDeploymentInfos = new HashMap<>();

}
