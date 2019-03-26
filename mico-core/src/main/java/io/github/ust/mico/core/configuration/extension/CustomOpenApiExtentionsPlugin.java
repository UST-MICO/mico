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

package io.github.ust.mico.core.configuration.extension;

import static springfox.documentation.schema.Annotations.findPropertyAnnotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

@Slf4j
@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CustomOpenApiExtentionsPlugin implements ModelPropertyBuilderPlugin {

    public static final String X_MICO_CUSTOM_EXTENSION = "x-mico-custom";

    @Override
    public void apply(ModelPropertyContext context) {
        if (context.getBeanPropertyDefinition().isPresent()) {
            Optional<ApiModelProperty> annotation = findPropertyAnnotation(
                context.getBeanPropertyDefinition().get(),
                ApiModelProperty.class);
            if (annotation.isPresent()) {
                String name = context.getBeanPropertyDefinition().get().getName();
                List<Extension> extensions = Arrays.asList(annotation.get().extensions());
                @SuppressWarnings("rawtypes")
				List<VendorExtension> vendorExtensions = new ArrayList<>();
                extensions.forEach(e -> {
                    //Only process mico extensions because we do not know about the semantics of other extensions.
                    if (X_MICO_CUSTOM_EXTENSION.equals(e.name())) {
                        List<ExtensionProperty> extensionProperties = Arrays.asList(e.properties());
                        extensionProperties.forEach(extensionProperty -> {
                            vendorExtensions.add(new StringVendorExtension(extensionProperty.name(), extensionProperty.value()));
                            log.debug("Processed mico extension for:" + name + " and added " + extensionProperty.name());
                        });
                    }
                });
                if (!vendorExtensions.isEmpty()) {
                    context.getBuilder().extensions(vendorExtensions);
                }
            }
        }

    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
    }
}
