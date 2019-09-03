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

package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.dto.response.internal.PrometheusResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
@ActiveProfiles("unit-testing")
public class PrometheusValueDeserializerTest {

    @Test
    public void testDeserialize() throws IOException {
        String testJsonForMemoryUsageRequest = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"data\": {\n" +
            "        \"resultType\": \"vector\",\n" +
            "        \"result\": [\n" +
            "            {\n" +
            "                \"metric\": {},\n" +
            "                \"value\": [\n" +
            "                    1552041266.607,\n" +
            "                    \"310083584\"\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

        String testJsonForCpuLoadRequest = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"data\": {\n" +
            "        \"resultType\": \"vector\",\n" +
            "        \"result\": [\n" +
            "            {\n" +
            "                \"metric\": {},\n" +
            "                \"value\": [\n" +
            "                    1552042589.238,\n" +
            "                    \"1\"\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

        String testJsonForRequestWithNoValue = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"data\": {\n" +
            "        \"resultType\": \"vector\",\n" +
            "        \"result\": [\n" +
            "            {\n" +
            "                \"metric\": {},\n" +
            "                \"value\": [\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

        String testJsonEmptyResult = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"data\": {\n" +
            "        \"resultType\": \"vector\",\n" +
            "        \"result\": []\n" +
            "    }\n" +
            "}";

        ObjectMapper objectMapper = new ObjectMapper();

        PrometheusResponseDTO responseCpuLoad = objectMapper.readValue(testJsonForCpuLoadRequest, PrometheusResponseDTO.class);
        assertTrue(responseCpuLoad.isSuccess());
        assertEquals(1, responseCpuLoad.getValue());

        PrometheusResponseDTO responseMemoryUsage = objectMapper.readValue(testJsonForMemoryUsageRequest, PrometheusResponseDTO.class);
        assertTrue(responseMemoryUsage.isSuccess());
        assertEquals(310083584, responseMemoryUsage.getValue());

        PrometheusResponseDTO responseDtoWithoutValue = objectMapper.readValue(testJsonForRequestWithNoValue, PrometheusResponseDTO.class);
        assertTrue(responseDtoWithoutValue.isSuccess());
        assertEquals(0, responseDtoWithoutValue.getValue());

        PrometheusResponseDTO responseDTOWithoutValue2 = objectMapper.readValue(testJsonEmptyResult, PrometheusResponseDTO.class);
        assertTrue(responseDTOWithoutValue2.isSuccess());
        assertEquals(0, responseDTOWithoutValue2.getValue());
    }
}
