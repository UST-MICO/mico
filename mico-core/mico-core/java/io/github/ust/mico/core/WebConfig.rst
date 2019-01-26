.. java:import:: java.util Arrays

.. java:import:: org.springframework.beans.factory.annotation Value

.. java:import:: org.springframework.boot.web.servlet FilterRegistrationBean

.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: org.springframework.web.cors CorsConfiguration

.. java:import:: org.springframework.web.cors UrlBasedCorsConfigurationSource

.. java:import:: org.springframework.web.filter CorsFilter

.. java:import:: org.springframework.web.servlet.config.annotation WebMvcConfigurerAdapter

WebConfig
=========

.. java:package:: io.github.ust.mico.core
   :noindex:

.. java:type:: @Configuration public class WebConfig extends WebMvcConfigurerAdapter

Fields
------
allowedOrigins
^^^^^^^^^^^^^^

.. java:field:: @Value  String[] allowedOrigins
   :outertype: WebConfig

Methods
-------
corsFilter
^^^^^^^^^^

.. java:method:: @Bean public FilterRegistrationBean corsFilter()
   :outertype: WebConfig

   Based on https://github.com/springfox/springfox/issues/2215#issuecomment-446178059

