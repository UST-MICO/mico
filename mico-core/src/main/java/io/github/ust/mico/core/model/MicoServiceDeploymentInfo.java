package io.github.ust.mico.core.model;

import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
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
public class MicoServiceDeploymentInfo {

    /**
     * The id of this service deployment info.
     */
    @Id
    @GeneratedValue
    private final long id;

    /**
     * The id of the parent service.
     */
    // TODO: @Jakob -> Do we want to link the DB object instead of the ID?
    // The id of the parent service.
    @ApiModelProperty(required = true)
    private final long serviceId;


    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The list of containers to run within this service.
     */
    @ApiModelProperty(required = true)
    @Singular
    private final List<MicoImageContainer> containers;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * Number of desired instances. Defaults to 1.
     */
    @Default
    private final int replicas = 1;

    /**
     * Minimum number of seconds for which this service should be ready
     * without any of its containers crashing, for it to be considered available.
     * Defaults to 0 (considered available as soon as it is ready).
     */
    @Default
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
    private Map<String, String> labels;

    /**
     * Indicates whether and when to pull the image.
     * Defaults to ImagePullPolicy#DEFAULT.
     */
    @Default
    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.DEFAULT;

    /**
     * Restart policy for all containers.
     * Defaults to RestartPolicy#ALWAYS.
     */
    @Default
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
