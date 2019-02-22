/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.model;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * The deployment strategy to use to replace
 * existing {@link MicoService}s with new ones.
 */
@Value
@AllArgsConstructor
@Accessors(chain = true)
public class MicoDeploymentStrategy {

    // ----------------------
    // -> Required fields ---
    // ----------------------

    /**
     * The type of this deployment strategy, can
     * RECREATE or ROLLING_UPDATE.
     * Defaults to Type#ALWAYS.
     */
    @ApiModelProperty(required = true, extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Deployment Strategy Type"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "description", value = "The type of this deployment strategy, can RECREATE or " +
                "ROLLING_UPDATE. Defaults to ALWAYS.")
        }
    )})
    private final Type type;


    // ----------------------
    // -> Optional fields ---
    // ----------------------

    /**
     * The maximum percentage of instances that can be scheduled above the desired number of
     * instances during the update. This can not be 0 if maxUnavailable is 0.
     * If the absolute number is also specified, this field will be used prior to the absolute number.
     * Defaults to 25%.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Max. Additional Instances (Percentage)"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The maximum percentage of instances that can be " +
                "scheduled above the desired number of instances during the update. This can not be 0 if " +
                "maxUnavailable is 0. If the absolute number is also specified, this field will be used prior to the " +
                "absolute number. Defaults to 25%.")
        }
    )})
    private final double maxInstancesOnTopPercent = 0.25;
    /**
     * The maximum (absolute) number of instances that can be scheduled above the desired number of
     * instances during the update. This can not be 0 if maxUnavailable is 0.
     * If the percentage is also specified, it will be used prior to this absolute number.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Max. Additional Instances (Absolute)"),
            @ExtensionProperty(name = "x-order", value = "30"),
            @ExtensionProperty(name = "description", value = "The maximum (absolute) number of instances that can be " +
                "scheduled above the desired number of instances during the update. This can not be 0 if " +
                "maxUnavailable is 0. If the percentage is also specified, it will be used prior to this absolute " +
                "number.")
        }
    )})
    private final double maxInstancesOnTopAbsolute;

    /**
     * The maximum percentage of instances that can be unavailable during the update.
     * This can not be 0 if maxSurge is 0.
     * If the absolute number is also specified, this field will be used prior to the absolute number.
     * Defaults to 25%.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Max. Unavailable Instances During Updates (Percentage)"),
            @ExtensionProperty(name = "x-order", value = "40"),
            @ExtensionProperty(name = "description", value = "The maximum percentage of instances that can be " +
                "unavailable during the update. This can not be 0 if maxSurge is 0. If the absolute number is also " +
                "specified, this field will be used prior to the absolute number. Defaults to 25%.")
        }
    )})
    private final double maxInstancesBelowPercent = 0.25;

    /**
     * The maximum (absolute) number of instances that can be unavailable during the update.
     * This can not be 0 if maxSurge is 0.
     * If the percentage is also specified, it will be used prior to this absolute number.
     */
    @ApiModelProperty(extensions = {@Extension(
        name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION,
        properties = {
            @ExtensionProperty(name = "title", value = "Max. Unavailable Instances During Updates (Absolute)"),
            @ExtensionProperty(name = "x-order", value = "50"),
            @ExtensionProperty(name = "description", value = "The maximum (absolute) number of instances that can be " +
                "unavailable during the update. This can not be 0 if maxSurge is 0. If the percentage is also " +
                "specified, it will be used prior to this absolute number.")
        }
    )})
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
