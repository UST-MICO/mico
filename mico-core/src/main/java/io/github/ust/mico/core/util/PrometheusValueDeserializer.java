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

package io.github.ust.mico.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Custom deserializer for a response, which is received from Prometheus for CPU load / memory usage requests.
 */
@Slf4j
public class PrometheusValueDeserializer extends StdDeserializer<Integer> {

    private static final long serialVersionUID = 8170187864990259257L;

    public PrometheusValueDeserializer() {
        this(null);
    }

    private PrometheusValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        try {
            JsonNode dataJson = parser.getCodec().readTree(parser);
            JsonNode resultJsonArray = dataJson.get("result");
            if (resultJsonArray.size() > 0) {
                JsonNode resultJsonArrayFirstElement = resultJsonArray.get(0);
                JsonNode valueNode = resultJsonArrayFirstElement.get("value");
                if (valueNode.size() > 1) {
                    return valueNode.get(1).asInt();
                } else {
                    log.warn("Prometheus returned no values: {}", dataJson);
                }
            } else {
                log.warn("Prometheus returned no result: {}", dataJson);
            }
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }
}
