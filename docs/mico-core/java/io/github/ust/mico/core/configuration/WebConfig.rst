.. java:import:: java.util List

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.boot.web.servlet FilterRegistrationBean

.. java:import:: org.springframework.context.annotation Bean

.. java:import:: org.springframework.context.annotation Configuration

.. java:import:: org.springframework.http HttpMethod

.. java:import:: org.springframework.web.cors CorsConfiguration

.. java:import:: org.springframework.web.cors UrlBasedCorsConfigurationSource

.. java:import:: org.springframework.web.filter CorsFilter

.. java:import:: org.springframework.web.servlet.config.annotation WebMvcConfigurerAdapter

WebConfig
=========

.. java:package:: io.github.ust.mico.core.configuration
   :noindex:

.. java:type:: @Configuration @Slf4j public class WebConfig extends WebMvcConfigurerAdapter

Fields
------
corsUserConfig
^^^^^^^^^^^^^^

.. java:field:: @Autowired  CorsConfig corsUserConfig
   :outertype: WebConfig

Methods
-------
corsFilter
^^^^^^^^^^

.. java:method:: @Bean public FilterRegistrationBean<CorsFilter> corsFilter()
   :outertype: WebConfig

   Based on https://github.com/springfox/springfox/issues/2215#issuecomment-446178059

