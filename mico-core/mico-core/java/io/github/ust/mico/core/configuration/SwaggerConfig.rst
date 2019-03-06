.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: springfox.documentation.builders ApiInfoBuilder

.. java:import:: springfox.documentation.builders PathSelectors

.. java:import:: springfox.documentation.builders RequestHandlerSelectors

.. java:import:: springfox.documentation.service ApiInfo

.. java:import:: springfox.documentation.spi DocumentationType

.. java:import:: springfox.documentation.spring.web.plugins Docket

.. java:import:: springfox.documentation.swagger2.annotations EnableSwagger2

SwaggerConfig
=============

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: @Configuration @EnableSwagger2 public class SwaggerConfig

Methods
-------
api
^^^

.. java:method:: @Bean public Docket api()
   :outertype: SwaggerConfig

