package io.github.ust.mico.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import lombok.Singular;
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
public class MicoServiceDeploymentInfo {

    /**
     * The id of this service deployment info.
     */
    @Id
    @GeneratedValue
    private Long id;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The list of containers to run within this service.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Containers"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The list of containers to run within this service")
        }
    )})
    @Singular
    private List<MicoImageContainer> containers = new ArrayList<>();


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * Number of desired instances. Defaults to 1.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Replicas"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "Number of desired instances. Defaults to 1")
        }
    )})
    private int replicas = 1;

    /**
     * Minimum number of seconds for which this service should be ready
     * without any of its containers crashing, for it to be considered available.
     * Defaults to 0 (considered available as soon as it is ready).
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Time To Verify Ready State"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "Minimum number of seconds for which this service should be ready " +
                "without any of its containers crashing, for it to be considered available. " +
                "Defaults to 0 (considered available as soon as it is ready).")
        }
    )})
    private int minReadySecondsBeforeMarkedAvailable = 0;

    /**
     * Those labels are key-value pairs that are attached to the deployment
     * of this service. Intended to be used to specify identifying attributes
     * that are meaningful and relevant to users, but do not directly imply
     * semantics to the core system. Labels can be used to organize and to select
     * subsets of objects. Labels can be attached to objects at creation time and
     * subsequently added and modified at any time.
     * Each key must be unique for a given object.
     * Defaults to [ {"app" -> "Service#shortName"} ].
     */
    @Singular
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Labels"),
            @ExtensionProperty(name = "x-order", value = "60"),
            @ExtensionProperty(name = "description", value = "Those labels are key-value pairs that are attached to the deployment" +
                " of this service. Intended to be used to specify identifying attributes" +
                " that are meaningful and relevant to users, but do not directly imply" +
                " semantics to the core system. Labels can be used to organize and to select" +
                " subsets of objects. Labels can be attached to objects at creation time and" +
                " subsequently added and modified at any time.\n" +
                " Each key must be unique for a given object.")
        }
    )})
    private Map<String, String> labels = new HashMap<>();

    /**
     * Indicates whether and when to pull the image.
     * Defaults to ImagePullPolicy#DEFAULT.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Image Pull Policy"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "Indicates whether and when to pull the image.\n" +
                "Defaults to DEFAULT.")
        }
    )})
    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.DEFAULT;

    /**
     * Restart policy for all containers.
     * Defaults to RestartPolicy#ALWAYS.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Restart Policy"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "Restart policy for all containers.\n" +
                " Defaults to ALWAYS.")
        }
    )})
    private RestartPolicy restartPolicy = RestartPolicy.DEFAULT;


    /**
     * Enumeration for the different policies specifying
     * when to pull an image.
     */
    public enum ImagePullPolicy {

        ALWAYS,
        NEVER,
        IF_NOT_PRESENT;

        /**
         * Default image pull policy is {@link ImagePullPolicy#ALWAYS}.
         */
        public static ImagePullPolicy DEFAULT = ImagePullPolicy.ALWAYS;

    }


    /**
     * Enumeration for all supported restart policies.
     */
    public enum RestartPolicy {

        ALWAYS,
        ON_FAILURE,
        NEVER;

        /**
         * Default restart policy is {@link RestartPolicy#ALWAYS}.
         */
        public static RestartPolicy DEFAULT = RestartPolicy.ALWAYS;

    }

}
