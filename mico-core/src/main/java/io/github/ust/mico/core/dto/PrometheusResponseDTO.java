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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.ust.mico.core.util.PrometheusValueDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for a response from Prometheus. It contains a status field and the value field for the CPU load / memory usage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PrometheusResponseDTO {

    /**
     * Indicates the status of the response: true if the response is successful and false if an error occurred.
     */
    private boolean success = false;

    /**
     * The data field and all nested fields in the response JSON are deserialized with {@link
     * PrometheusValueDeserializer} to retrieve the value for the memory usage / CPU load.
     */
    @JsonProperty("data")
    @JsonDeserialize(using = PrometheusValueDeserializer.class)
    private int value;

    /**
     * Status of the response: can be "success" or "error".
     */
    @JsonProperty("status")
    private void setResponseStatus(String status) {
        if (status.equals("success")) {
            this.success = true;
        }
    }
}
