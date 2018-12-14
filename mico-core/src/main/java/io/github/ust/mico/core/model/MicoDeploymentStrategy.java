package io.github.ust.mico.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

/**
 * The deployment strategy to use to replace
 * existing {@link MicoService}s with new ones.
 */
@Value
@AllArgsConstructor
@Builder
public class MicoDeploymentStrategy {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    // The type of this deployment strategy, can
    // RECREATE or ROLLING_UPDATE.
    // Defaults to Type#ALWAYS.
    private final Type type;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    // The maximum number of instances that can be scheduled above the desired number of
    // instances during the update. Value can be an absolute number or a percentage of
    // desired instances. This can not be 0 if maxUnavailable is 0. Absolute number is
    // calculated from percentage by rounding up.
    // If both fields are specified, the percentage will be used.
    // Defaults to 25%.
    @Default
    private final double maxInstancesOnTopPercent = 0.25;
    private final double maxInstancesOnTopAbsolute;

    // The maximum number of instances that can be unavailable during the update.
    // Value can be an absolute number or a percentage of desired pods. Absolute number is
    // calculated from percentage by rounding down. This can not be 0 if MaxSurge is 0.
    // If both fields are specified, the percentage will be used.
    // Defaults to 25%.
    @Default
    private final double maxInstancesBelowPercent = 0.25;
    private final double maxInstancesBelow;


    /**
     * Enumeration for the supported types of deployment strategies.
     */
    public enum Type {

        /**
         * Delete all running instances and then create new ones.
         */
        RECREATE,
        /**
         * Update one after the other.
         */
        ROLLING_UPDATE;

        /**
         * Default deployment strategy type is {@link Type#ROLLING_UPDATE}.
         */
        public static Type DEFAULT = Type.ROLLING_UPDATE;

    }

}