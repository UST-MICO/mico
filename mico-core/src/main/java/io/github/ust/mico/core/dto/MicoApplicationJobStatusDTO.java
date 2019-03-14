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
 import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
 import io.github.ust.mico.core.model.MicoApplication;
 import io.github.ust.mico.core.model.MicoApplicationJobStatus;
 import io.github.ust.mico.core.model.MicoBackgroundTask;
 import io.swagger.annotations.ApiModelProperty;
 import io.swagger.annotations.Extension;
 import io.swagger.annotations.ExtensionProperty;
 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;
 import lombok.experimental.Accessors;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.stream.Collectors;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
 @Accessors(chain = true)
 public class MicoApplicationJobStatusDTO {

     /**
      * The aggregated status of jobs from the {@link MicoApplication}.
      * Is readonly.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "Status"),
             @ExtensionProperty(name = "x-order", value = "10"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The aggregated status of jobs from the MicoApplication")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     private MicoBackgroundTask.Status status;

     /**
      * The list of jobs for the {@link MicoApplication}.
      * Is readonly.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "List of Jobs"),
             @ExtensionProperty(name = "x-order", value = "20"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The list of jobs for the MicoApplication")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     private List<MicoBackgroundTaskDTO> jobs = new ArrayList<>();

     /**
      * Creates a {@code MicoApplicationJobStatusDTO} based on a
      * {@link MicoApplicationJobStatus}.
      *
      * @param applicationJobStatus the {@link MicoApplicationJobStatus}.
      * @return a {@link MicoApplicationJobStatusDTO} with all the values
      * of the given {@code MicoApplicationJobStatus}.
      */
     public static MicoApplicationJobStatusDTO valueOf(MicoApplicationJobStatus applicationJobStatus) {
         return new MicoApplicationJobStatusDTO()
             .setStatus(applicationJobStatus.getStatus())
             .setJobs(applicationJobStatus.getJobs().stream().map(MicoBackgroundTaskDTO::valueOf).collect(Collectors.toList()));
     }
 }
