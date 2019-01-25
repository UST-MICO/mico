.. java:import:: java.util Arrays

.. java:import:: java.util LinkedList

.. java:import:: java.util List

.. java:import:: org.slf4j Logger

.. java:import:: org.slf4j LoggerFactory

.. java:import:: org.springframework.core.annotation Order

.. java:import:: org.springframework.stereotype Component

.. java:import:: com.google.common.base Optional

.. java:import:: io.swagger.annotations ApiModelProperty

.. java:import:: io.swagger.annotations Extension

.. java:import:: io.swagger.annotations ExtensionProperty

.. java:import:: springfox.documentation.service StringVendorExtension

.. java:import:: springfox.documentation.service VendorExtension

.. java:import:: springfox.documentation.spi DocumentationType

.. java:import:: springfox.documentation.spi.schema ModelPropertyBuilderPlugin

.. java:import:: springfox.documentation.spi.schema.contexts ModelPropertyContext

.. java:import:: springfox.documentation.swagger.common SwaggerPluginSupport

CustomOpenApiExtentionsPlugin
=============================

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @Component @Order public class CustomOpenApiExtentionsPlugin implements ModelPropertyBuilderPlugin

Fields
------
X_MICO_CUSTOM_EXTENSION
^^^^^^^^^^^^^^^^^^^^^^^

.. java:field:: public static final String X_MICO_CUSTOM_EXTENSION
   :outertype: CustomOpenApiExtentionsPlugin

Methods
-------
apply
^^^^^

.. java:method:: @Override public void apply(ModelPropertyContext context)
   :outertype: CustomOpenApiExtentionsPlugin

supports
^^^^^^^^

.. java:method:: @Override public boolean supports(DocumentationType delimiter)
   :outertype: CustomOpenApiExtentionsPlugin

