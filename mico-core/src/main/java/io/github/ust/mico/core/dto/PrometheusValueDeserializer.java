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

package io.github.ust.mico.core.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for JSON objects, which are received from Prometheus.
 */
public class PrometheusValueDeserializer extends StdDeserializer<Integer> {

    public PrometheusValueDeserializer() {
        this(null);
    }

    private PrometheusValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode dataJson = p.getCodec().readTree(p);
        JsonNode resultJsonArray = dataJson.get("result");
        JsonNode resultArrayFirstElement = resultJsonArray.get(0);
        JsonNode valueNode = resultArrayFirstElement.get("value");
        // TODO Is the metric name or any additional information necessary?
//        JsonNode metricNode = resultArrayFirstElement.get("metric");
//        String valueName;
//        if (metricNode.get("__name__").asText().contains("cpu")) {
//            valueName = "cpu_load";
//        } else {
//            valueName = "memory_usage";
//        }
        return valueNode.get(1).asInt();
    }
}
