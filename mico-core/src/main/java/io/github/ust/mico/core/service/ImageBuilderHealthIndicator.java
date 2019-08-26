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

package io.github.ust.mico.core.service;

import io.github.ust.mico.core.service.imagebuilder.ImageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ImageBuilderHealthIndicator implements HealthIndicator {

    private final ImageBuilder imageBuilder;

    @Autowired
    public ImageBuilderHealthIndicator(ImageBuilder imageBuilder) {
        this.imageBuilder = imageBuilder;
    }

    @Override
    public Health health() {
        // The Image Builder is considered healthy if it is initialized successfully
        return imageBuilder.isInitialized() ? Health.up().build() : Health.down().build();
    }
}
