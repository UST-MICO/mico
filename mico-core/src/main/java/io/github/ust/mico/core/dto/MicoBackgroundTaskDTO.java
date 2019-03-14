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

 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
 import io.github.ust.mico.core.model.MicoBackgroundTask;
 import io.github.ust.mico.core.model.MicoService;
 import io.swagger.annotations.ApiModelProperty;
 import io.swagger.annotations.Extension;
 import io.swagger.annotations.ExtensionProperty;
 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.NoArgsConstructor;
 import lombok.experimental.Accessors;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
 @Accessors(chain = true)
 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class MicoBackgroundTaskDTO {

     /**
      * The generated job id.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "JobId"),
             @ExtensionProperty(name = "x-order", value = "10"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The generated job id")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     String id;

     /**
      * The ShortName to the corresponding {@link MicoService}.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "ShortName of MicoService"),
             @ExtensionProperty(name = "x-order", value = "20"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The Name to the corresponding MicoService")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     String micoServiceShortName;

     /**
      * The Version to the corresponding {@link MicoService}.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "Version of MicoService"),
             @ExtensionProperty(name = "x-order", value = "30"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The Version to the corresponding MicoService")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     String micoServiceVersion;

     /**
      * The current Status of the job.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "Job Status"),
             @ExtensionProperty(name = "x-order", value = "40"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The current Status of the job.")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     MicoBackgroundTask.Status status;

     /**
      * An ErrorMessage if job failed
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "ErrorMessage"),
             @ExtensionProperty(name = "x-order", value = "50"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "An ErrorMessage if job failed")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     String errorMessage;

     /**
      * The type of job.
      */
     @ApiModelProperty(extensions = {
         @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
             @ExtensionProperty(name = "title", value = "Job Type"),
             @ExtensionProperty(name = "x-order", value = "60"),
             @ExtensionProperty(name = "readOnly", value = "true"),
             @ExtensionProperty(name = "description", value = "The type of job.")})})
     @JsonProperty(access = JsonProperty.Access.READ_ONLY)
     MicoBackgroundTask.Type type;

     /**
      * Creates a {@code MicoBackgroundTaskDTO} based on a
      * {@link MicoBackgroundTask}.
      *
      * @param task the {@link MicoBackgroundTask}.
      * @return a {@link MicoBackgroundTaskDTO} with all the values
      * of the given {@code MicoApplication}.
      */
     public static MicoBackgroundTaskDTO valueOf(MicoBackgroundTask task) {
         return new MicoBackgroundTaskDTO()
             .setId(task.getId())
             .setMicoServiceShortName(task.getMicoServiceShortName())
             .setMicoServiceVersion(task.getMicoServiceVersion())
             .setStatus(task.getStatus())
             .setErrorMessage(task.getErrorMessage())
             .setType(task.getType());
     }
 }


