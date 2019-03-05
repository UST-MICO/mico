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

import javax.validation.constraints.NotEmpty;

import io.github.ust.mico.core.configuration.extension.CustomOpenApiExtentionsPlugin;
import io.github.ust.mico.core.service.GitHubCrawler;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * DTO for the information needed by a Crawler (e.g., {@link GitHubCrawler})
 * for crawling a service from a remote repository.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CrawlingInfoDTO {

    /**
     * The url to the remote repository to crawl from.
     * Must not be {@code null} nor empty.
     */
    @ApiModelProperty(required = true, extensions = {
        @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
            @ExtensionProperty(name = "title", value = "URL"),
            @ExtensionProperty(name = "x-order", value = "10"),
            @ExtensionProperty(name = "minLength", value = "1"),
            @ExtensionProperty(name = "description", value = "The url to the remote repository to crawl from.")})})
    @NotEmpty
    private String url;

    /**
     * The remote release tag. Defaults to 'latest'.
     * Must not be {@code null} nor empty, but can be omitted,
     * in which the default value will be used.
     */
    @ApiModelProperty(extensions = {
        @Extension(name = CustomOpenApiExtentionsPlugin.X_MICO_CUSTOM_EXTENSION, properties = {
            @ExtensionProperty(name = "title", value = "Version"),
            @ExtensionProperty(name = "default", value = "latest"),
            @ExtensionProperty(name = "minLength", value = "1"),
            @ExtensionProperty(name = "x-order", value = "20"),
            @ExtensionProperty(name = "description", value = "The remote release tag. Defaults to 'latest'.")})})
    @NotEmpty
    private String version = "latest";
}
