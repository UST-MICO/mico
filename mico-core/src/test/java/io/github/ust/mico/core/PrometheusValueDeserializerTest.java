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

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ust.mico.core.dto.PrometheusResponseDTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrometheusValueDeserializerTest {

    @Test
    public void testDeserialize() {
        String testJsonForMemoryUsageRequest = "{\n" +
            "    \"status\": \"success\",\n" +
            "    \"data\": {\n" +
            "        \"resultType\": \"vector\",\n" +
            "        \"result\": [\n" +
            "            {\n" +
            "                \"metric\": {\n" +
            "                    \"__name__\": \"container_memory_working_set_bytes\",\n" +
            "                    \"agentpool\": \"nodepool1\",\n" +
            "                    \"beta_kubernetes_io_arch\": \"amd64\",\n" +
            "                    \"beta_kubernetes_io_instance_type\": \"Standard_B2s\",\n" +
            "                    \"beta_kubernetes_io_os\": \"linux\",\n" +
            "                    \"container_name\": \"mico-core\",\n" +
            "                    \"failure_domain_beta_kubernetes_io_region\": \"westeurope\",\n" +
            "                    \"failure_domain_beta_kubernetes_io_zone\": \"1\",\n" +
            "                    \"id\": \"/kubepods/besteffort/podcf6a6787-3e64-11e9-9cb7-ceea768c1492/fe9743fa17bcc00c80066722ff71794d1c90c299504f9e59b517ce0e3b598d7a\",\n" +
            "                    \"image\": \"ustmico/mico-core@sha256:bcfa6ef8d45bfe7f3eb5f816fbd10476135f123843587311a34b454b7b736b18\",\n" +
            "                    \"instance\": \"aks-nodepool1-26424023-1\",\n" +
            "                    \"job\": \"kubernetes-cadvisor\",\n" +
            "                    \"kubernetes_azure_com_cluster\": \"MC_ust-mico-resourcegroup_ust-mico-cluster_westeurope\",\n" +
            "                    \"kubernetes_io_hostname\": \"aks-nodepool1-26424023-1\",\n" +
            "                    \"kubernetes_io_role\": \"agent\",\n" +
            "                    \"name\": \"k8s_mico-core_mico-core-5b9c49f5dc-qr9b8_mico-system_cf6a6787-3e64-11e9-9cb7-ceea768c1492_0\",\n" +
            "                    \"namespace\": \"mico-system\",\n" +
            "                    \"pod_name\": \"mico-core-5b9c49f5dc-qr9b8\",\n" +
            "                    \"storageprofile\": \"managed\",\n" +
            "                    \"storagetier\": \"Premium_LRS\"\n" +
            "                },\n" +
            "                \"value\": [\n" +
            "                    1551979840.464,\n" +
            "                    \"301961216\"\n" +
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
            "                \"metric\": {\n" +
            "                    \"__name__\": \"container_cpu_load_average_10s\",\n" +
            "                    \"agentpool\": \"nodepool1\",\n" +
            "                    \"beta_kubernetes_io_arch\": \"amd64\",\n" +
            "                    \"beta_kubernetes_io_instance_type\": \"Standard_B2s\",\n" +
            "                    \"beta_kubernetes_io_os\": \"linux\",\n" +
            "                    \"container_name\": \"mico-core\",\n" +
            "                    \"failure_domain_beta_kubernetes_io_region\": \"westeurope\",\n" +
            "                    \"failure_domain_beta_kubernetes_io_zone\": \"1\",\n" +
            "                    \"id\": \"/kubepods/besteffort/podcf6a6787-3e64-11e9-9cb7-ceea768c1492/fe9743fa17bcc00c80066722ff71794d1c90c299504f9e59b517ce0e3b598d7a\",\n" +
            "                    \"image\": \"ustmico/mico-core@sha256:bcfa6ef8d45bfe7f3eb5f816fbd10476135f123843587311a34b454b7b736b18\",\n" +
            "                    \"instance\": \"aks-nodepool1-26424023-1\",\n" +
            "                    \"job\": \"kubernetes-cadvisor\",\n" +
            "                    \"kubernetes_azure_com_cluster\": \"MC_ust-mico-resourcegroup_ust-mico-cluster_westeurope\",\n" +
            "                    \"kubernetes_io_hostname\": \"aks-nodepool1-26424023-1\",\n" +
            "                    \"kubernetes_io_role\": \"agent\",\n" +
            "                    \"name\": \"k8s_mico-core_mico-core-5b9c49f5dc-qr9b8_mico-system_cf6a6787-3e64-11e9-9cb7-ceea768c1492_0\",\n" +
            "                    \"namespace\": \"mico-system\",\n" +
            "                    \"pod_name\": \"mico-core-5b9c49f5dc-qr9b8\",\n" +
            "                    \"storageprofile\": \"managed\",\n" +
            "                    \"storagetier\": \"Premium_LRS\"\n" +
            "                },\n" +
            "                \"value\": [\n" +
            "                    1551980217.607,\n" +
            "                    \"0\"\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PrometheusResponseDTO responseCpuLoad = objectMapper.readValue(testJsonForCpuLoadRequest, PrometheusResponseDTO.class);
            assertEquals("success", responseCpuLoad.getStatus());
            assertEquals(0, responseCpuLoad.getValue());

            PrometheusResponseDTO responseMemoryUsage = objectMapper.readValue(testJsonForMemoryUsageRequest, PrometheusResponseDTO.class);
            assertEquals("success", responseMemoryUsage.getStatus());
            assertEquals(301961216, responseMemoryUsage.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
