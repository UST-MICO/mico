package io.github.ust.mico.core;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Optional;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ObjectVendorExtension;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static springfox.documentation.schema.Annotations.findPropertyAnnotation;
import static springfox.documentation.swagger.schema.ApiModelProperties.*;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class CustomOpenApiExtentionsPlugin implements ModelPropertyBuilderPlugin {

    private Logger logger = LoggerFactory.getLogger(CustomOpenApiExtentionsPlugin.class);
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
                List<VendorExtension> vendorExtensions = new LinkedList<>();
                extensions.forEach(e -> {
                    //Only process mico extensions because we do not know about the semantics of other extensions.
                    if (X_MICO_CUSTOM_EXTENSION.equals(e.name())) {
                        List<ExtensionProperty> extensionProperties = Arrays.asList(e.properties());
                        extensionProperties.forEach(extensionProperty -> {
                            vendorExtensions.add(new StringVendorExtension(extensionProperty.name(), extensionProperty.value()));
                            logger.debug("Processed mico extension for:" + name + " and added " + extensionProperty.name());
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
