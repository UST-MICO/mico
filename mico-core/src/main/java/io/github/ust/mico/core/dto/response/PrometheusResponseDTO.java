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

package io.github.ust.mico.core.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
// TODO: Class comment.
public class PrometheusResponseDTO {
    
    // TODO: Add comments for fields.
    
    public static final String PROMETHEUS_SUCCESSFUL_RESPONSE = "success";

    private String status;
    private int value;

    public boolean wasSuccessful(){
        return status.equals(PROMETHEUS_SUCCESSFUL_RESPONSE);
    }

    // TODO: Optimize
    @JsonProperty("data")
    @SuppressWarnings("unchecked")
    private void unpackNested(Map<String, Object> data) {
        //TODO find a better mapping solution
        List<Object> resultList = (List<Object>) data.get("result");
        Map<String,Object> resultEntry = (Map<String,Object>) resultList.get(0);
        List<String> dataPoint = (List<String>) resultEntry.get("value");
        this.value = Integer.parseInt(dataPoint.get(1));
    }

}
